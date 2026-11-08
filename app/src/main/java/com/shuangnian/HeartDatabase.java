package com.shuangnian;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class HeartDatabase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME =
            "heart_monitor.db";

    private static final int DATABASE_VERSION = 1;

    private static final String TABLE =
            "heart_records";

    public HeartDatabase(Context context) {

        super(
                context,
                DATABASE_NAME,
                null,
                DATABASE_VERSION
        );
    }

    @Override
    public void onCreate(
            SQLiteDatabase db
    ) {

        db.execSQL(
                "CREATE TABLE " +
                        TABLE +
                        " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "heart_rate INTEGER NOT NULL," +
                        "timestamp INTEGER NOT NULL" +
                        ")"
        );

        db.execSQL(
                "CREATE INDEX idx_heart_timestamp " +
                        "ON " +
                        TABLE +
                        "(timestamp)"
        );
    }

    @Override
    public void onUpgrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion
    ) {
    }

    public void insertHeartRate(
            int heartRate,
            long timestamp
    ) {

        SQLiteDatabase db =
                getWritableDatabase();

        ContentValues values =
                new ContentValues();

        values.put(
                "heart_rate",
                heartRate
        );

        values.put(
                "timestamp",
                timestamp
        );

        db.insert(
                TABLE,
                null,
                values
        );
    }

    public int getTodayCount() {

        long start =
                getStartOfToday();

        SQLiteDatabase db =
                getReadableDatabase();

        Cursor cursor =
                db.rawQuery(
                        "SELECT COUNT(*) FROM " +
                                TABLE +
                                " WHERE timestamp >= ?",
                        new String[]{
                                String.valueOf(start)
                        }
                );

        int result = 0;

        if (cursor.moveToFirst()) {
            result = cursor.getInt(0);
        }

        cursor.close();

        return result;
    }

    public int getTodayMin() {

        return getTodayAggregate(
                "MIN"
        );
    }

    public int getTodayMax() {

        return getTodayAggregate(
                "MAX"
        );
    }

    public int getTodayAverage() {

        return getTodayAggregate(
                "AVG"
        );
    }

    private int getTodayAggregate(
            String function
    ) {

        SQLiteDatabase db =
                getReadableDatabase();

        Cursor cursor =
                db.rawQuery(
                        "SELECT " +
                                function +
                                "(heart_rate) FROM " +
                                TABLE +
                                " WHERE timestamp >= ?",
                        new String[]{
                                String.valueOf(
                                        getStartOfToday()
                                )
                        }
                );

        int result = 0;

        if (cursor.moveToFirst() &&
                !cursor.isNull(0)) {

            result =
                    cursor.getInt(0);
        }

        cursor.close();

        return result;
    }

    public long getLastTimestamp() {

        SQLiteDatabase db =
                getReadableDatabase();

        Cursor cursor =
                db.rawQuery(
                        "SELECT timestamp FROM " +
                                TABLE +
                                " ORDER BY timestamp DESC LIMIT 1",
                        null
                );

        long result = 0;

        if (cursor.moveToFirst()) {
            result = cursor.getLong(0);
        }

        cursor.close();

        return result;
    }

    public ArrayList<Integer> getRecentValues(
            int limit
    ) {

        ArrayList<Integer> values =
                new ArrayList<>();

        SQLiteDatabase db =
                getReadableDatabase();

        Cursor cursor =
                db.rawQuery(
                        "SELECT heart_rate FROM " +
                                TABLE +
                                " ORDER BY timestamp DESC LIMIT ?",
                        new String[]{
                                String.valueOf(limit)
                        }
                );

        while (cursor.moveToNext()) {

            values.add(
                    cursor.getInt(0)
            );
        }

        cursor.close();

        ArrayList<Integer> reversed =
                new ArrayList<>();

        for (int i = values.size() - 1;
             i >= 0;
             i--) {

            reversed.add(
                    values.get(i)
            );
        }

        return reversed;
    }

    public ArrayList<Record> getRecentRecords(
            int limit
    ) {

        ArrayList<Record> records =
                new ArrayList<>();

        SQLiteDatabase db =
                getReadableDatabase();

        Cursor cursor =
                db.rawQuery(
                        "SELECT heart_rate,timestamp FROM " +
                                TABLE +
                                " ORDER BY timestamp DESC LIMIT ?",
                        new String[]{
                                String.valueOf(limit)
                        }
                );

        while (cursor.moveToNext()) {

            records.add(
                    new Record(
                            cursor.getInt(0),
                            cursor.getLong(1)
                    )
            );
        }

        cursor.close();

        return records;
    }

    public String exportCsv() {

        StringBuilder builder =
                new StringBuilder();

        builder.append("\uFEFF时间,心率\n");

        SQLiteDatabase db =
                getReadableDatabase();

        Cursor cursor =
                db.rawQuery(
                        "SELECT heart_rate,timestamp FROM " +
                                TABLE +
                                " ORDER BY timestamp ASC",
                        null
                );

        SimpleDateFormat format =
                new SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss",
                        Locale.getDefault()
                );

        while (cursor.moveToNext()) {

            int heartRate =
                    cursor.getInt(0);

            long timestamp =
                    cursor.getLong(1);

            builder.append(
                    format.format(
                            new Date(timestamp)
                    )
            );

            builder.append(",");

            builder.append(
                    heartRate
            );

            builder.append("\n");
        }

        cursor.close();

        return builder.toString();
    }

    /** 返回指定时间区间的统计摘要；结束时间不包含在统计范围内。 */
    public Summary getSummary(long startInclusive, long endExclusive) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*), MIN(heart_rate), ROUND(AVG(heart_rate)), MAX(heart_rate) FROM " + TABLE + " WHERE timestamp >= ? AND timestamp < ?",
                new String[]{String.valueOf(startInclusive), String.valueOf(endExclusive)}
        );
        Summary result = new Summary();
        if (cursor.moveToFirst()) {
            result.count = cursor.getInt(0);
            result.min = cursor.isNull(1) ? 0 : cursor.getInt(1);
            result.average = cursor.isNull(2) ? 0 : cursor.getInt(2);
            result.max = cursor.isNull(3) ? 0 : cursor.getInt(3);
        }
        cursor.close();
        return result;
    }

    /** 读取指定区间内的心率点，供趋势图展示，返回值按时间正序排列。 */
    public ArrayList<Integer> getValuesBetween(long startInclusive, long endExclusive, int limit) {
        ArrayList<Integer> newestFirst = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT heart_rate FROM " + TABLE + " WHERE timestamp >= ? AND timestamp < ? ORDER BY timestamp DESC LIMIT ?",
                new String[]{String.valueOf(startInclusive), String.valueOf(endExclusive), String.valueOf(Math.max(1, limit))}
        );
        while (cursor.moveToNext()) newestFirst.add(cursor.getInt(0));
        cursor.close();
        ArrayList<Integer> ordered = new ArrayList<>();
        for (int i = newestFirst.size() - 1; i >= 0; i--) ordered.add(newestFirst.get(i));
        return ordered;
    }

    public static class Summary {
        public int count;
        public int min;
        public int average;
        public int max;
    }

    /** Inserts a batch of imported records atomically to avoid one disk transaction per CSV row. */
    public int insertRecords(ArrayList<Record> records) {
        if (records == null || records.isEmpty()) return 0;
        SQLiteDatabase db = getWritableDatabase();
        int inserted = 0;
        db.beginTransaction();
        try {
            for (Record record : records) {
                if (record == null || record.heartRate < 25 || record.heartRate > 240 || record.timestamp <= 0) continue;
                Cursor duplicate = db.rawQuery(
                        "SELECT 1 FROM " + TABLE + " WHERE heart_rate = ? AND timestamp = ? LIMIT 1",
                        new String[]{String.valueOf(record.heartRate), String.valueOf(record.timestamp)}
                );
                boolean exists = duplicate.moveToFirst();
                duplicate.close();
                if (exists) continue;
                ContentValues values = new ContentValues();
                values.put("heart_rate", record.heartRate);
                values.put("timestamp", record.timestamp);
                if (db.insert(TABLE, null, values) != -1) inserted++;
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return inserted;
    }

    public void clearAll() {

        SQLiteDatabase db =
                getWritableDatabase();

        db.delete(
                TABLE,
                null,
                null
        );
    }

    private long getStartOfToday() {

        String today =
                new SimpleDateFormat(
                        "yyyy-MM-dd",
                        Locale.getDefault()
                ).format(
                        new Date()
                );

        try {

            return new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss",
                    Locale.getDefault()
            ).parse(
                    today +
                            " 00:00:00"
            ).getTime();

        } catch (Exception e) {

            return System.currentTimeMillis();
        }
    }

    public static class Record {

        public final int heartRate;

        public final long timestamp;

        public Record(
                int heartRate,
                long timestamp
        ) {

            this.heartRate =
                    heartRate;

            this.timestamp =
                    timestamp;
        }
    }
}