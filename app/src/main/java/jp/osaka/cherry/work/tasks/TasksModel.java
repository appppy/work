/*
 * Copyright 2016, The Android Open Source Project
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

package jp.osaka.cherry.work.tasks;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import jp.osaka.cherry.work.data.Asset;
import jp.osaka.cherry.work.data.History;
import jp.osaka.cherry.work.service.SimpleClient;
import jp.osaka.cherry.work.service.history.HistoryProxy;

import static jp.osaka.cherry.work.Config.LOG_I;
import static jp.osaka.cherry.work.constants.CONTENT.INBOX;
import static jp.osaka.cherry.work.util.helper.AssetHelper.copyTasks;

/**
 * モデル
 */
public class TasksModel implements
        SimpleClient.Callbacks {

    /**
     * @serial 目印
     */
    private final String TAG = "TasksModel";

    /**
     * @serial クライアント
     */
    private final SimpleClient mClient;

    /**
     * @serial 代理
     */
    private final HistoryProxy mProxy;

    /**
     * @serial コールバック
     */
    private Callbacks mCallbacks;

    /**
     * @serial 一覧
     */
    private final ArrayList<Asset> mTasks = new ArrayList<>();

    /**
     * @serial バックアップ
     */
    private final ArrayList<Asset> mBackupTasks = new ArrayList<>();

    /**
     * コンストラクタ
     *
     * @param context コンテンツ
     */
    public TasksModel(Context context) {
        // To avoid leaks, this must be an Application Context.
        Context mContext = context.getApplicationContext();
        mClient = new SimpleClient(mContext, this);
        mProxy = new HistoryProxy(mContext);
    }

    /**
     * コールバック設定
     *
     * @param callbacks コールバック
     */
    public void setCallbacks(Callbacks callbacks) {
        mCallbacks = callbacks;
    }

    /**
     * 有効
     */
    public void enable() {
        if (LOG_I) {
            Log.i(TAG, "enable#enter");
        }
        mClient.connect();
        mProxy.connect();
        if (LOG_I) {
            Log.i(TAG, "enable#leave");
        }
    }

    /**
     * 無効
     */
    public void disable() {
        if (LOG_I) {
            Log.i(TAG, "disable#enter");
        }
        mProxy.disconnect();
        mClient.disconnect();
        if (LOG_I) {
            Log.i(TAG, "disable#leave");
        }
    }

    /**
     * 履歴設定
     *
     * @param history 履歴
     */
    public void setHistory(History history) {
        if (LOG_I) {
            Log.i(TAG, "setHistory#enter");
        }
        mProxy.setHistory(history);
        if (LOG_I) {
            Log.i(TAG, "setHistory#leave");
        }
    }

    /**
     * タスク取得
     *
     * @param uuid UUID
     * @return タスク
     */
    public Asset getTask(String uuid) {
        if (LOG_I) {
            Log.i(TAG, "getTask#enter");
        }
        Asset result = null;
        for (Asset asset : mTasks) {
            if (asset.uuid.equals(uuid)) {
                result = asset;
            }
        }
        if (LOG_I) {
            Log.i(TAG, "getTask#leave");
        }
        return result;
    }

    /**
     * バックアップ取得
     *
     * @param uuid UUID
     * @return バックアップ
     */
    Asset getBackupTask(String uuid) {
        if (LOG_I) {
            Log.i(TAG, "getBackupTask#enter");
        }
        Asset result = null;
        for (Asset asset : mBackupTasks) {
            if (asset.uuid.equals(uuid)) {
                result = asset;
            }
        }
        if (LOG_I) {
            Log.i(TAG, "getBackupTask#leave");
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onUpdated(Object object, final List<Asset> tasks) {
        if (LOG_I) {
            Log.i(TAG, "onUpdated#enter");
        }
        boolean result;
        result = mTasks.isEmpty() || mTasks.size() != tasks.size() || !mTasks.equals(tasks);
        if (result) {
            // データ設定
            mTasks.clear();
            mTasks.addAll(tasks);
            if (mCallbacks != null) {
                mCallbacks.onUpdated(this, tasks);
            }
        }
        if (LOG_I) {
            Log.i(TAG, "onUpdated#leave");
        }
    }

    /**
     * 一覧取得
     *
     * @return 一覧
     */
    public ArrayList<Asset> getTasks() {
        return mTasks;
    }

    /**
     * 空状態取得
     *
     * @return 空状態
     */
    public boolean isEmpty() {
        return mTasks.isEmpty();
    }

    /**
     * ゴミ箱削除
     *
     * @param tasks 一覧
     */
    void unTrashTasks(List<Asset> tasks) {
        if (LOG_I) {
            Log.i(TAG, "unTrashTasks#enter");
        }
        for (Asset src : tasks) {
            for (Asset dst : mTasks) {
                // 一致
                if (dst.uuid.equals(src.uuid)) {
                    dst.content = INBOX;
                }
            }
        }
        if (LOG_I) {
            Log.i(TAG, "unTrashTask#leave");
        }
    }

    /**
     * 更新
     *
     * @param task 一覧
     */
    public void update(Asset task) {
        if (LOG_I) {
            Log.i(TAG, "update#enter");
        }
        for (Asset dst : mTasks) {
            // 一致
            if (dst.uuid.equals(task.uuid)) {
                dst.setParams(task);
            }
        }
        update();
        if (LOG_I) {
            Log.i(TAG, "update#leave");
        }
    }

    /**
     * 更新
     *
     * @param tasks 一覧
     */
    public void update(List<Asset> tasks) {
        if (LOG_I) {
            Log.i(TAG, "update#enter");
        }
        deselect(tasks);
        mClient.setList(tasks);
        if (LOG_I) {
            Log.i(TAG, "update#leave");
        }
    }

    /**
     * 更新
     */
    public void update() {
        if (LOG_I) {
            Log.i(TAG, "update#enter");
        }
        update(mTasks);
        if (LOG_I) {
            Log.i(TAG, "update#leave");
        }
    }

    /**
     * バックアップ
     */
    public void backup() {
        if (LOG_I) {
            Log.i(TAG, "backup#enter");
        }
        mBackupTasks.clear();
        copyTasks(mBackupTasks, mTasks);
        if (LOG_I) {
            Log.i(TAG, "backup#leave");
        }
    }

    /**
     * リストア
     */
    public void restore() {
        if (LOG_I) {
            Log.i(TAG, "restore#enter");
        }
        mTasks.clear();
        copyTasks(mTasks, mBackupTasks);
        update();
        if (LOG_I) {
            Log.i(TAG, "restore#leave");
        }
    }

    /**
     * バックアップ一覧取得
     *
     * @return 一覧
     */
    ArrayList<Asset> getBackupTasks() {
        return mBackupTasks;
    }

    /**
     * 移動変更
     *
     * @param tasks 一覧
     */
    void moveChange(List<Asset> tasks) {
        if (LOG_I) {
            Log.i(TAG, "moveChange#enter");
        }
        ArrayList<Asset> assets = new ArrayList<>(tasks);
        for (Asset src1 : mTasks) {
            boolean isChecked = true;
            for (Asset src2 : assets) {
                if (src1.equal(src2)) {
                    isChecked = false;
                }
            }
            if (isChecked) {
                assets.add(src1);
            }
        }
        mTasks.clear();
        deselect(assets);
        copyTasks(mTasks, assets);
        update();
        if (LOG_I) {
            Log.i(TAG, "moveChange#leave");
        }
    }

    /**
     * 未選択
     *
     * @param tasks 一覧
     */
    private void deselect(List<Asset> tasks) {
        for (Asset task : tasks) {
            task.selected = false;
        }
    }

    /**
     * コールバック
     */
    public interface Callbacks {
        /**
         * コマンド更新通知
         *
         * @param object オブジェクト
         * @param tasks  タスク
         */
        void onUpdated(Object object, List<Asset> tasks);
    }
}
