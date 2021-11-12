package jp.osaka.cherry.work.service.history;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;


import jp.osaka.cherry.work.data.History;

import static jp.osaka.cherry.work.Config.LOG_I;

/**
 * 履歴アクセサ
 */
class HistoryAccessor {

    /**
     * @serial 履歴最大値
     */
    private static final int MAX_HISTORY = 100;

    /**
     * @serial 目印
     */
    private static final String TAG = "HistoryAccessor";

    /**
     * データ取得
     *
     * @param context コンテキスト
     * @return データ
     */
    static List<History> getData(Context context) {
        List<History> result = new ArrayList<>();

        try (Cursor cursor = context.getContentResolver().query(HistoryColumns.CONTENT_URI, null, null, null, null)) {
            if (null != cursor) {
                while (cursor.moveToNext()) {
                    History history = new History();

                    int index = 1;
                    history.date = cursor.getLong(index++);
                    history.title = cursor.getString(index++);
                    history.message = cursor.getString(index);

                    result.add(history);
                }
            }
        } catch (IllegalArgumentException | UnsupportedOperationException | ClassCastException | IllegalStateException e) {
            e.printStackTrace();
        }
        // 終了時にはCursorをcloseする
        return result;
    }

    /**
     * 挿入
     *
     * @param context コンテキスト
     * @param history 履歴
     */
    static void insert(Context context, History history) {
        if (LOG_I) {
            Log.i(TAG, "insert#enter");
        }

        int recordCnt;
        try {
            ContentValues insertValue = new ContentValues();

            insertValue.put(HistoryColumns.DATE, history.date);
            insertValue.put(HistoryColumns.TITLE, history.title);
            insertValue.put(HistoryColumns.MESSAGE, history.message);

            context.getContentResolver().insert(HistoryColumns.CONTENT_URI, insertValue);

            Cursor cursor = context.getContentResolver().query(HistoryColumns.CONTENT_URI, null, null, null, null);
            if (cursor == null) {
                return;
            }
            try {
                recordCnt = cursor.getCount();
                if (recordCnt == MAX_HISTORY) {
                    cursor.close();
                    return;
                } else if (recordCnt > MAX_HISTORY) {
                    int count = recordCnt - MAX_HISTORY;

                    for (int i = 0; i < count; i++) {
                        cursor.moveToFirst();
                        int delIndex = cursor.getInt(0);

                        context.getContentResolver().delete(HistoryColumns.CONTENT_URI, HistoryColumns.ID + " = ?", new String[]{String.valueOf(delIndex)});
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                cursor.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (LOG_I) {
            Log.i(TAG, "insert#leave");
        }
    }

    /**
     * 削除
     *
     * @param context コンテキスト
     */
    static public void delete(Context context) {
        if (LOG_I) {
            Log.i(TAG, "delete#enter");
        }

        Cursor cursor = context.getContentResolver().query(HistoryColumns.CONTENT_URI, null, null, null, null);
        if (cursor == null) {
            return;
        }
        try {
            cursor.moveToFirst();
            do {
                int delIndex = cursor.getInt(0);
                context.getContentResolver().delete(HistoryColumns.CONTENT_URI, HistoryColumns.ID + " = ?", new String[]{String.valueOf(delIndex)});
            } while(cursor.moveToNext());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cursor.close();
        }

        if (LOG_I) {
            Log.i(TAG, "delete#leave");
        }
    }

}
