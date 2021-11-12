
package jp.osaka.cherry.work.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.util.List;

import jp.osaka.cherry.work.constants.INTERFACE;
import jp.osaka.cherry.work.constants.PACKAGE;
import jp.osaka.cherry.work.data.Asset;

import static jp.osaka.cherry.work.Config.LOG_I;

/**
 * クライアント
 */
public class SimpleClient {

    /**
     * @serial 目印
     */
    private static final String TAG = "SimpleClient";

    /**
     * @serial コンテキスト
     */
    private final Context mContext;

    /**
     * @serial コールバック
     */
    private static Callbacks mCallbacks;

    /**
     * @serial インタフェース
     */
    private ISimpleService mBinder;

    /**
     * サービス接続
     */
    private final ServiceConnection mConnection = new ServiceConnection() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBinder = ISimpleService.Stub.asInterface(service);
            try {
                mBinder.registerCallback(mCallback);
                mBinder.getList();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBinder = null;
            mContext.unbindService(mConnection);
        }
    };

    /**
     * @serial コールバック
     */
    private final ISimpleServiceCallback mCallback = new ISimpleServiceCallback.Stub() {
        /**
         * 更新
         *
         * @param tasks タスク
         */
        @Override
        public void update(List<Asset> tasks) {
            if (mCallbacks != null) {
                mCallbacks.onUpdated(this, tasks);
            }
        }
    };

    /**
     * コンストラクタ
     *
     * @param context コンテキスト
     * @param callbacks コールバック
     */
    public SimpleClient(Context context, Callbacks callbacks) {
        mContext = context;
        mCallbacks = callbacks;
    }

    /**
     * 接続
     */
    public void connect() {
        if (LOG_I) {
            Log.i(TAG, "connect#enter");
        }
        try {
            Intent intent = new Intent(INTERFACE.ISimpleService);
            intent.setPackage(PACKAGE.PACKAGE);
            mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }  catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        if (LOG_I) {
            Log.i(TAG, "connect#leave");
        }
    }

    /**
     * 解除
     */
    public void disconnect() {
        if (LOG_I) {
            Log.i(TAG, "disconnect#enter");
        }
        if (mBinder != null) {
            try {
                mBinder.unregisterCallback(mCallback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        if (mContext != null) {
            try {
                mContext.unbindService(mConnection);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
        if (LOG_I) {
            Log.i(TAG, "disconnect#leave");
        }
    }

    /**
     * 一覧の設定
     *
     * @param assets 一覧
     */
    public void setList(List<Asset> assets) {
        if (LOG_I) {
            Log.i(TAG, "setList#enter");
        }
        if (mBinder != null) {
            try {
                mBinder.setList(assets);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        if (LOG_I) {
            Log.i(TAG, "setList#leave");
        }
    }

    /**
     * コールバックインタフェース
     */
    public interface Callbacks {
        /**
         * コマンド更新通知
         *
         * @param object オブジェクト
         * @param assets アセット一覧
         */
        void onUpdated(Object object, List<Asset> assets);
    }
}
