package com.shuangnian;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * 心搏形状（取自 LuckyEcgDemo 的示例心电数据）。
 *
 * 资源文件 lucky_ecg_beats.txt 里是 53 个从 LuckyEcgDemo 的 ecgData.json 切出来的真实心搏。
 * 这里把它们以 R 峰对齐后取平均，得到一个干净的 P-QRS-T 形状，并归一化：
 * R 峰 = 1.0，低于基线的 S 波按比例压低，方便按心率数值定高度。
 *
 * 不依赖 Android，可直接在 JVM 上测试。
 */
final class LuckyEcgEngine {

    private static final int PRE = 50;   // R 峰之前取 0.20 秒（含 P 波）
    private static final int POST = 130; // R 峰之后取 0.52 秒（含 T 波）
    private static final float NEGATIVE_SCALE = 0.22f;

    private LuckyEcgEngine() { }

    static float[] loadShape(InputStream in) {
        try {
            ArrayList<float[]> beats = new ArrayList<>();
            ArrayList<Integer> rs = new ArrayList<>();
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                int bar = line.indexOf('|');
                if (bar <= 0) continue;
                int r = Integer.parseInt(line.substring(0, bar));
                String[] parts = line.substring(bar + 1).split(",");
                if (r < PRE || r + POST > parts.length) continue;
                float[] v = new float[parts.length];
                for (int i = 0; i < parts.length; i++) v[i] = Float.parseFloat(parts[i]);
                beats.add(v);
                rs.add(r);
            }
            br.close();
            if (beats.isEmpty()) return syntheticShape();

            int len = PRE + POST;
            float[] mean = new float[len];
            for (int b = 0; b < beats.size(); b++) {
                float[] v = beats.get(b);
                int r = rs.get(b);
                for (int i = 0; i < len; i++) mean[i] += v[r - PRE + i];
            }
            for (int i = 0; i < len; i++) mean[i] /= beats.size();
            return normalize(mean);
        } catch (Exception e) {
            return syntheticShape();
        }
    }

    /** 资源缺失时的备用形状：由高斯函数合成。 */
    static float[] syntheticShape() {
        int len = PRE + POST;
        float[] v = new float[len];
        int r = PRE;
        for (int i = 0; i < len; i++) {
            v[i] = g(i, r - 40, 7, 0.08f) + g(i, r - 5, 2.2f, -0.12f) + g(i, r, 2.6f, 1.0f)
                    + g(i, r + 5, 2.6f, -0.28f) + g(i, r + 55, 13, 0.22f);
        }
        return normalize(v);
    }

    private static float g(float x, float mu, float sigma, float amp) {
        float d = (x - mu) / sigma;
        return amp * (float) Math.exp(-0.5f * d * d);
    }

    /** 首尾拉回基线 0，R 峰归一到 1，负向部分按比例压低。 */
    private static float[] normalize(float[] v) {
        int n = v.length;
        float b0 = (v[0] + v[1] + v[2]) / 3f;
        float b1 = (v[n - 1] + v[n - 2] + v[n - 3]) / 3f;
        float peak = 0f;
        float[] out = new float[n];
        for (int i = 0; i < n; i++) {
            out[i] = v[i] - (b0 + (b1 - b0) * i / (n - 1f));
            peak = Math.max(peak, out[i]);
        }
        if (peak <= 0f) peak = 1f;
        for (int i = 0; i < n; i++) {
            float x = out[i] / peak;
            out[i] = x < 0f ? x * NEGATIVE_SCALE : x;
        }
        return out;
    }
}
