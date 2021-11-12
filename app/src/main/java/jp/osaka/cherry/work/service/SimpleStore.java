/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.osaka.cherry.work.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import java.util.Objects;

import jp.osaka.cherry.work.constants.CONTENT;
import jp.osaka.cherry.work.constants.PRIORITY;
import jp.osaka.cherry.work.constants.PROGRESS;
import jp.osaka.cherry.work.data.Asset;

import static jp.osaka.cherry.work.Config.LOG_I;
import static jp.osaka.cherry.work.constants.EXTRA.EXTRA_CONTENT;
import static jp.osaka.cherry.work.constants.EXTRA.EXTRA_CREATION_DATE;
import static jp.osaka.cherry.work.constants.EXTRA.EXTRA_DISPLAY_NAME;
import static jp.osaka.cherry.work.constants.EXTRA.EXTRA_END_DATE;
import static jp.osaka.cherry.work.constants.EXTRA.EXTRA_KEY;
import static jp.osaka.cherry.work.constants.EXTRA.EXTRA_MODIFIED_DATE;
import static jp.osaka.cherry.work.constants.EXTRA.EXTRA_NOTE;
import static jp.osaka.cherry.work.constants.EXTRA.EXTRA_PIRIORITY;
import static jp.osaka.cherry.work.constants.EXTRA.EXTRA_PROGRESS_STATE;
import static jp.osaka.cherry.work.constants.EXTRA.EXTRA_RATE;
import static jp.osaka.cherry.work.constants.EXTRA.EXTRA_START_DATE;
import static jp.osaka.cherry.work.constants.EXTRA.EXTRA_TIMESTAMP;
import static jp.osaka.cherry.work.constants.EXTRA.EXTRA_UUID;
import static jp.osaka.cherry.work.constants.INVALID.INVALID_INT_VALUE;
import static jp.osaka.cherry.work.constants.INVALID.INVALID_LONG_VALUE;
import static jp.osaka.cherry.work.constants.INVALID.INVALID_STRING_VALUE;

/**
 *　保存
 */
public class SimpleStore {

    /**
     * @serial 目印
     */
    private static final String TAG = "SimpleStore";

    /**
     * @serial プリファレンス
     */
    private final SharedPreferences mPrefs;

    /**
     * @serial 名前
     */
    static final String SHARED_PREFERENCE_NAME =
                    "SimpleStore";

    /**
     * コンストラクタ
     *
     * @param context コンテキスト
     */
    SimpleStore(Context context) {
        if (LOG_I) {Log.i(TAG, "SimpleStore#enter");}

        // プリファレンスの取得
        mPrefs =
                context.getSharedPreferences(
                        SHARED_PREFERENCE_NAME,
                        Context.MODE_PRIVATE);

        if (LOG_I) {Log.i(TAG, "SimpleStore#leave");}
    }

    /**
     * 取得
     *
     * @param id 識別子
     * @return データ
     */
    public Asset get(String id) {
        // UUID
        String uuid = mPrefs.getString(
                getFieldKey(id, EXTRA_UUID),
                INVALID_STRING_VALUE);

        // 作成日
        long creationDate = mPrefs.getLong(
                getFieldKey(id,EXTRA_CREATION_DATE),
                INVALID_LONG_VALUE);

        // 変更日
        long modifiedDate = mPrefs.getLong(
                getFieldKey(id, EXTRA_MODIFIED_DATE),
                INVALID_LONG_VALUE);

        // 表示名
        String displayName = mPrefs.getString(
                getFieldKey(id, EXTRA_DISPLAY_NAME),
                INVALID_STRING_VALUE);

        // 開始日
        long startDate = mPrefs.getLong(
                getFieldKey(id, EXTRA_START_DATE),
                INVALID_LONG_VALUE);

        // 期限
        long endDate = mPrefs.getLong(
                getFieldKey(id, EXTRA_END_DATE),
                INVALID_LONG_VALUE);

        // 進捗状況
        PROGRESS progressState = PROGRESS.valueOf(mPrefs.getString(
                getFieldKey(id, EXTRA_PROGRESS_STATE),
                PROGRESS.NOT_START.name()));

        // 優先度
        PRIORITY priority = PRIORITY.valueOf(mPrefs.getString(
                getFieldKey(id, EXTRA_PIRIORITY),
                PRIORITY.MIDDLE.name()));

        // レート
        int rate = mPrefs.getInt(
                getFieldKey(id, EXTRA_RATE),
                INVALID_INT_VALUE);

        // ノート
        String note = mPrefs.getString(
                getFieldKey(id, EXTRA_NOTE),
                INVALID_STRING_VALUE);

        // インボックスの有無
        CONTENT content = CONTENT.valueOf(mPrefs.getString(
                getFieldKey(id, EXTRA_CONTENT),
                CONTENT.INBOX.name()));

        // タイムスタンプ
        long timestamp = mPrefs.getLong(
                getFieldKey(id, EXTRA_TIMESTAMP),
                INVALID_LONG_VALUE);


        // 緯度、経度、半径、期限、型、遅延時間、通知応答の確認
        assert uuid != null;
        if (
            !uuid.equals(INVALID_STRING_VALUE) &&
            creationDate != INVALID_LONG_VALUE &&
            modifiedDate != INVALID_LONG_VALUE &&
            !Objects.requireNonNull(displayName).equals(INVALID_STRING_VALUE)
                ) {

            // ジオフェンスの生成

            // if (LOG_I) {Log.i(TAG, leave(person));}
            return new Asset(
                    id,
                    uuid,
                    creationDate,
                    modifiedDate,
                    displayName,
                    startDate,
                    endDate,
                    progressState,
                    priority,
                    rate,
                    note,
                    content,
                    timestamp
            );
        } else {

        //    if (LOG_I) {Log.i(TAG, leave(null));}
            return null;
        }
    }

    /**
     * 設定
     *
     * @param id 識別子
     * @param simpleTask 値
     */
    public void set(String id, Asset simpleTask) {
        /*
         * Get a SharedPreferences editor instance. Among other
         * things, SharedPreferences ensures that updates are atomic
         * and non-concurrent
         */
        Editor editor = mPrefs.edit();

        // Write the Geofence values to SharedPreferences
        editor.putString(
                getFieldKey(id, EXTRA_UUID),
                simpleTask.uuid);

        editor.putLong(
                getFieldKey(id, EXTRA_CREATION_DATE),
                simpleTask.creationDate);

        editor.putLong(
                getFieldKey(id, EXTRA_MODIFIED_DATE),
                simpleTask.modifiedDate);

        editor.putString(
                getFieldKey(id, EXTRA_DISPLAY_NAME),
                simpleTask.displayName);

        editor.putLong(
                getFieldKey(id, EXTRA_START_DATE),
                simpleTask.startDate);

        editor.putLong(
                getFieldKey(id, EXTRA_END_DATE),
                simpleTask.endDate);

        editor.putString(
                getFieldKey(id, EXTRA_PROGRESS_STATE),
                simpleTask.progressState.toString());

        editor.putString(
                getFieldKey(id, EXTRA_PIRIORITY),
                simpleTask.priority.toString());

        editor.putInt(
                getFieldKey(id, EXTRA_RATE),
                simpleTask.rate);

        editor.putString(
                getFieldKey(id, EXTRA_NOTE),
                simpleTask.note);

        editor.putString(
                getFieldKey(id, EXTRA_CONTENT),
                simpleTask.content.name());

        editor.putLong(
                getFieldKey(id, EXTRA_TIMESTAMP),
                simpleTask.timestamp);

        // Commit the changes
        editor.apply();
    }

    /**
     * 削除
     *
     * @param id 識別子
     */
    public void clear(String id) {
        // Remove a flattened geofence object from storage by removing all of its keys
        Editor editor = mPrefs.edit();
        editor.remove(getFieldKey(id, EXTRA_UUID));
        editor.remove(getFieldKey(id, EXTRA_CREATION_DATE));
        editor.remove(getFieldKey(id, EXTRA_MODIFIED_DATE));
        editor.remove(getFieldKey(id, EXTRA_DISPLAY_NAME));
        editor.remove(getFieldKey(id, EXTRA_START_DATE));
        editor.remove(getFieldKey(id, EXTRA_END_DATE));
        editor.remove(getFieldKey(id, EXTRA_PROGRESS_STATE));
        editor.remove(getFieldKey(id, EXTRA_PIRIORITY));
        editor.remove(getFieldKey(id, EXTRA_RATE));
        editor.remove(getFieldKey(id, EXTRA_NOTE));
        editor.remove(getFieldKey(id, EXTRA_CONTENT));
        editor.remove(getFieldKey(id, EXTRA_TIMESTAMP));
        editor.apply();
    }

    /**
     * キー取得
     *
     * @param id 識別子
     * @param fieldName 名
     * @return キー名
     */
    private String getFieldKey(String id, String fieldName) {
        return EXTRA_KEY +
                id +
                "_" +
                fieldName;
    }
}
