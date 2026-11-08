package com.shuangnian;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.SystemClock;
import android.view.View;

import java.io.InputStream;
import java.util.ArrayList;

/**
 * 心电图控件（按 LuckyEcgDemo 的实时心电图改写）。
 *
 * 和 Lucky 一样：
 *  - 数据按每秒 250 点走时间轴，屏幕宽度 = 4 秒，波形用真实心电数据里的 P-QRS-T 形状
 *  - 两种实时模式：刷新模式（扫描线从左往右画，画到头从左边擦除重画）
 *           平移模式（波形整体从右往左移动）
 *
 * 和 Lucky 不同的地方：
 *  - 背景换成和「心率趋势」一样的横线与刻度，颜色全部来自应用主题
 *  - 设备每上报一次心率，只画一个心跳，高度对应这次的心率（50 跳到 50 那条线）；
 *    没有新数据时就是一条平直基线，不会自己连续播放
 *
 * 这是由心率数值生成的示意波形，不是实测心电，不能用于诊断。
 */
public class LuckyEcgView extends View {

    private static final String ASSET_NAME = "lucky_ecg_beats.txt";

    private static final int RATE = 250;                 // 每秒点数（与 Lucky 一致）
    private static final int POINTS = 4 * RATE;          // 屏幕宽度 = 4 秒
    private static final float SPEED = 0.6f;             // 走纸速度（越小越慢）
    private static final float PPMS = RATE * SPEED / 1000f; // 每毫秒前进的点数
    private static final float AXIS_TOP_BPM = 150f;      // 基线到顶部对应的 BPM
    private static final long LIVE_MS = 6000L;           // 超过这个时间没有新心率，就不再补心跳
    private static final int MAX_QUEUE = 3;
    private static final long FRAME_MS = 16L;

    private static float[] sharedShape;

    private final float[] shape;
    private final ArrayList<Integer> queue = new ArrayList<>();

    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint minorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path wavePath = new Path();

    /** 环形缓冲，数值单位是「BPM 刻度」：形状 × 心率。 */
    private final float[] ring = new float[POINTS];
    private int head;

    private boolean eraseMode = true;   // 默认刷新模式（从左往右）
    private boolean running;
    private long lastFrame;
    private float acc;
    private int flatRun;

    private int curBpm;
    private int lastBpm;               // 最近一次设备上报的心率
    private long lastReport;
    private float sinceBeat;           // 距上一个心跳开始已经走了多少点
    private int beatPos = -1;           // -1 = 当前没有在画心跳

    public LuckyEcgView(Context context) {
        super(context);
        shape = loadShape(context);
        float density = getResources().getDisplayMetrics().density;

        gridPaint.setStrokeWidth(density * 1f);
        minorPaint.setStrokeWidth(Math.max(1f, density * 0.6f));
        textPaint.setTextSize(density * 10f);

        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(density * 2.2f);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeJoin(Paint.Join.ROUND);

        glowPaint.setStyle(Paint.Style.STROKE);
        glowPaint.setStrokeWidth(density * 4.5f);
        glowPaint.setStrokeCap(Paint.Cap.ROUND);
        glowPaint.setStrokeJoin(Paint.Join.ROUND);

        dotPaint.setStyle(Paint.Style.FILL);
        setBackgroundColor(Color.TRANSPARENT);
    }

    private static synchronized float[] loadShape(Context context) {
        if (sharedShape == null) {
            try {
                InputStream in = context.getAssets().open(ASSET_NAME);
                sharedShape = LuckyEcgEngine.loadShape(in);
                in.close();
            } catch (Exception e) {
                sharedShape = LuckyEcgEngine.syntheticShape();
            }
        }
        return sharedShape;
    }

    /**
     * @param line  波形颜色（应用主题强调色）
     * @param grid  横线颜色（应用主题分隔线色）
     * @param label 刻度文字颜色（应用主题次要文字色）
     */
    public void setColors(int line, int grid, int label) {
        linePaint.setColor(line);
        glowPaint.setColor(Color.argb(65, Color.red(line), Color.green(line), Color.blue(line)));
        dotPaint.setColor(line);
        gridPaint.setColor(grid);
        minorPaint.setColor(Color.argb(Math.round(Color.alpha(grid) * 0.5f), Color.red(grid), Color.green(grid), Color.blue(grid)));
        textPaint.setColor(label);
        invalidate();
    }

    /** true = 刷新模式（从左往右扫描），false = 平移模式（整体从右往左移动）。 */
    public void setEraseMode(boolean erase) {
        if (eraseMode == erase) return;
        eraseMode = erase;
        clearSamples();
    }

    public boolean isEraseMode() {
        return eraseMode;
    }

    /** 清空画面（断开连接、切换模式时），只留横线。 */
    public void clearSamples() {
        java.util.Arrays.fill(ring, 0f);
        queue.clear();
        head = 0;
        beatPos = -1;
        flatRun = 0;
        lastBpm = 0;
        running = false;
        invalidate();
    }

    /** 设备上报了一条新心率：画一个心跳。 */
    public void pushBpm(int bpm) {
        if (bpm <= 0) return;
        lastBpm = Math.min(220, bpm);
        lastReport = SystemClock.uptimeMillis();
        queue.add(lastBpm);
        while (queue.size() > MAX_QUEUE) queue.remove(0);
        if (!running) {
            running = true;
            lastFrame = SystemClock.uptimeMillis();
            acc = 0f;
        }
        flatRun = 0;
        invalidate();
    }

    private boolean live() { return isLive(); }

    /** 设备最近 6 秒内上报过心率：这段时间内按该心率的节奏补上心跳，不让画面空着。 */
    private boolean isLive() {
        return lastBpm > 0 && SystemClock.uptimeMillis() - lastReport < LIVE_MS;
    }

    private void advance() {
        long now = SystemClock.uptimeMillis();
        long dt = Math.max(0L, Math.min(120L, now - lastFrame));
        lastFrame = now;
        acc += dt * PPMS;
        int n = (int) acc;
        acc -= n;
        boolean live = isLive();
        for (int i = 0; i < n; i++) {
            if (beatPos < 0 || beatPos >= shape.length) {
                beatPos = -1;
                if (!queue.isEmpty()) {
                    curBpm = queue.remove(0);       // 刚收到的新心率：马上跳一次
                    beatPos = 0;
                    sinceBeat = 0f;
                } else if (live && sinceBeat >= 60000f / lastBpm * PPMS) {
                    curBpm = lastBpm;                // 两次上报之间按心率节奏补心跳
                    beatPos = 0;
                    sinceBeat = 0f;
                }
            }
            float v = 0f;
            if (beatPos >= 0) {
                v = shape[beatPos++] * curBpm;
                flatRun = 0;
            } else if (queue.isEmpty() && !live) {
                flatRun++;
            }
            sinceBeat += 1f;
            ring[head] = v;
            head = (head + 1) % POINTS;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth(), h = getHeight();
        if (w <= 0 || h <= 0) return;
        float density = getResources().getDisplayMetrics().density;

        if (running) advance();

        float labelW = density * 30f;
        float left = density * 4f + labelW;
        float right = w - density * 6f;
        float centerY = h / 2f;
        float halfH = h / 2f - density * 8f;
        float plotW = right - left;
        if (plotW <= 0 || halfH <= 0) return;
        float perBpm = halfH / AXIS_TOP_BPM;

        // 一条一条的横线：每 10 BPM 一条（和「心率趋势」一样的线），每 50 的线稍重，每 20 标一个刻度
        canvas.drawLine(density * 4f, centerY, right, centerY, gridPaint);
        textPaint.setTextSize(density * 9f);
        for (int bpm = 10; bpm <= (int) AXIS_TOP_BPM; bpm += 10) {
            float dy = bpm * perBpm;
            Paint pl = bpm % 50 == 0 ? gridPaint : minorPaint;
            canvas.drawLine(density * 4f, centerY - dy, right, centerY - dy, pl);
            canvas.drawLine(density * 4f, centerY + dy, right, centerY + dy, pl);
            if (bpm % 20 == 0) {
                canvas.drawText(String.valueOf(bpm), density * 4f, centerY - dy + density * 3f, textPaint);
            }
        }

        float step = plotW / (POINTS - 1f);
        wavePath.reset();
        if (!eraseMode) {
            // 平移模式：最新的点在右边，整体向左移动
            for (int k = 0; k < POINTS; k++) {
                int idx = (head + k) % POINTS;
                float x = left + k * step;
                float y = centerY - Math.min(AXIS_TOP_BPM, ring[idx]) * perBpm;
                if (k == 0) wavePath.moveTo(x, y); else wavePath.lineTo(x, y);
            }
        } else {
            // 刷新模式：扫描线从左往右，前方留一小段空隙（擦除）
            int gap = POINTS / 25;
            boolean pen = false;
            for (int idx = 0; idx < POINTS; idx++) {
                int ahead = (idx - head + POINTS) % POINTS;
                if (ahead < gap) { pen = false; continue; }
                float x = left + idx * step;
                float y = centerY - Math.min(AXIS_TOP_BPM, ring[idx]) * perBpm;
                if (!pen) { wavePath.moveTo(x, y); pen = true; } else wavePath.lineTo(x, y);
            }
        }

        canvas.save();
        canvas.clipRect(left, 0f, right, h);
        canvas.drawPath(wavePath, glowPaint);
        canvas.drawPath(wavePath, linePaint);
        if (eraseMode && running) {
            int last = (head - 1 + POINTS) % POINTS;
            canvas.drawCircle(left + last * step, centerY - Math.min(AXIS_TOP_BPM, ring[last]) * perBpm,
                    density * 2.6f, dotPaint);
        }
        canvas.restore();

        // 还有心跳要画，或者旧波形还没被基线刷平，就继续；否则停下，不空转
        if (running) {
            if (beatPos >= 0 || !queue.isEmpty() || live() || flatRun < POINTS) {
                postInvalidateDelayed(FRAME_MS);
            } else {
                running = false;
            }
        }
    }
}
