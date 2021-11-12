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
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;

/**
 * 一覧
 *
 * @param <T> 値
 */
class CollectionStore<T> {

    /**
     * @serial プリファレンス名
     */
    private static final String SHARED_PREFERENCE_NAME =
            "CollectionStore";

    /**
     * @serial 共有プリファレンス
     */
    private final SharedPreferences mPrefs;

    /**
     * コンストラクタ
     *
     * @param context コンテキスト
     */
    CollectionStore(Context context) {
        // プリファレンスの取得
        mPrefs =
                context.getSharedPreferences(
                        SHARED_PREFERENCE_NAME,
                        Context.MODE_PRIVATE);
    }

    /**
     * 一覧取得
     *
     * @param key キー
     * @return 一覧
     */
    public Collection<JSONObject> get(String key) {
        Collection<JSONObject> collection = new ArrayList<>();
        String json = mPrefs.getString(key, "");
        if(!TextUtils.isEmpty(json)) {
            // JSON -> List<Object>に変換
            try {
                JSONArray arr = new JSONArray(json);
                for (int i = 0; i < arr.length(); i++) {
                    collection.add((JSONObject) arr.get(i));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return collection;
    }

    /**
     * 設定
     *
     * @param key キー
     * @param collection 一覧
     */
    public void set(String key, Collection<T> collection) {
        String json = new JSONArray(collection).toString();
        mPrefs.edit().putString(key, json).apply();
    }
}
