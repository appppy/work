
package jp.osaka.cherry.work.service.history;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import jp.osaka.cherry.work.constants.INTERFACE;
import jp.osaka.cherry.work.constants.PACKAGE;
import jp.osaka.cherry.work.data.History;

import static jp.osaka.cherry.work.Config.LOG_I;

/**
 * 履歴代理
 */
public class HistoryProxy {

    /**
     * @serial 目印
     */
    private static final String TAG = "HistoryProxy";

    /**
     * @serial コンテキスト
     */
    private final Context mContext;

    /**
     * @serial バインダ
     */
    private IHistoryProxy mBinder;

    /**
     * コンストラクタ
     *
     * @param context コンテキスト
     */
    public HistoryProxy(Context context) {
        mContext = context;
    }

    /**
     * @serial サービス接続
     */
    private final ServiceConnection mConnection = new ServiceConnection() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBinder = IHistoryProxy.Stub.asInterface(service);
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
     * 接続
     */
    public void connect() {
        if (LOG_I) {
            Log.i(TAG, "connect#enter");
        }
        if (mBinder == null) {
            try {
                Intent intent = new Intent(INTERFACE.IHistoryProxy);
                intent.setPackage(PACKAGE.PACKAGE);
                mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
        if (LOG_I) {
            Log.i(TAG, "connect#leave");
        }

    }

    /**
     * 非接続
     */
    public void disconnect() {
        if (LOG_I) {
            Log.i(TAG, "disconnect#enter");
        }
        if (mBinder != null) {
            mBinder = null;
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
     * 履歴設定
     *
     * @param history 履歴
     */
    public void setHistory(History history) {
        if (LOG_I) {
            Log.i(TAG, "setHistory#enter");
        }
        if (mBinder != null) {
            try {
                mBinder.setHistory(history);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        if (LOG_I) {
            Log.i(TAG, "setHistory#leave");
        }
    }
}
