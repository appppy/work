
package jp.osaka.cherry.work.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jp.osaka.cherry.work.constants.INTERFACE;
import jp.osaka.cherry.work.data.Asset;
import jp.osaka.cherry.work.util.controller.BaseCommand;
import jp.osaka.cherry.work.util.controller.Controller;
import jp.osaka.cherry.work.util.controller.IWorker;
import jp.osaka.cherry.work.util.controller.command.Backup;
import jp.osaka.cherry.work.util.controller.command.Restore;
import jp.osaka.cherry.work.util.controller.status.BaseStatus;

import static jp.osaka.cherry.work.Config.LOG_I;
import static jp.osaka.cherry.work.constants.EXTRA.EXTRA_ASSETS;

/**
 * サービス
 */
public class SimpleService extends Service implements
        IWorker.Callbacks {

    /**
     * @serial 目印
     */
    private static final String TAG = "SimpleService";

    /**
     * @serial コールバック一覧
     */
    private final RemoteCallbackList<ISimpleServiceCallback> mCallbacks =
            new RemoteCallbackList<>();

    /**
     * @serial 制御
     */
    private final Controller mController = new Controller();

    /**
     * @serial インタフェース
     */
    private final ISimpleService.Stub mBinder = new ISimpleService.Stub() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void registerCallback(ISimpleServiceCallback callback) {
            mCallbacks.register(callback);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void unregisterCallback(ISimpleServiceCallback callback) {
             mCallbacks.unregister(callback);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setList(List<Asset> assets) {
            // バックアップ
            BaseCommand command = new Backup();
            command.args.putParcelableArrayList(EXTRA_ASSETS, new ArrayList<>(assets));
            mController.start(command);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void getList() {
            // リストア
            mController.start(new Restore());
        }
    };

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate() {
        super.onCreate();
        if (LOG_I) {
            Log.i(TAG, "onCreate#enter");
        }

        // 登録
        mController.register(new SimpleAccessor(this, this));


        if (LOG_I) {
            Log.i(TAG, "onCreate#leave");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy() {
        // コールバックの削除
        mCallbacks.kill();

        // 停止
        mController.stop();

        // 解除
        mController.unregisterAll();

        super.onDestroy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IBinder onBind(Intent intent) {
        IBinder result = null;
        if (INTERFACE.ISimpleService.equals(intent.getAction())) {
            result = mBinder;
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStarted(IWorker worker, BaseCommand command) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onUpdated(IWorker worker, BaseCommand command, BaseStatus status) {
        try {
            // データベースコントローラー
            if (worker instanceof SimpleAccessor) {
                // コマンド
                if (command instanceof Restore) {
                    // リストア
                    if (status instanceof Assets) {
                        Assets s = (Assets) status;
                        // ブロードキャスト
                        broadcast(s.collection);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSuccessed(IWorker worker, BaseCommand command) {
    }

    public void broadcast(Collection<Asset> collection) {
        int n = mCallbacks.beginBroadcast();
        for (int i = 0; i < n; i++) {
            try {
                mCallbacks.getBroadcastItem(i).update(new ArrayList<>(collection));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        mCallbacks.finishBroadcast();
    }
}
