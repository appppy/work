
package jp.osaka.cherry.work.service.history;

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
import jp.osaka.cherry.work.data.History;

import static jp.osaka.cherry.work.Config.LOG_I;

/**
 * 履歴クライアント
 */
public class HistoryClient {

    /**
     * @serial 目印
     */
    private static final String TAG = "HistoryClient";

    /**
     * @serial コンテキスト
     */
    private final Context mContext;

    /**
     * @serial コールバック
     */
    private static Callbacks mCallbacks;

    /**
     * @serial バインダ
     */
    private IHistoryService mBinder;

    /**
     * @serial 自身
     */
    private final HistoryClient mSelf;

    /**
     * @serial サービス接続
     */
    private final ServiceConnection mConnection = new ServiceConnection() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBinder = IHistoryService.Stub.asInterface(service);
            try {
                mBinder.registerCallback(mCallback);
                mBinder.getHistoryList();
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
     * 履歴サービスコールバック
     */
    private final IHistoryServiceCallback mCallback = new IHistoryServiceCallback.Stub() {
        /**
         * 更新
         *
         * @param historyList 履歴一覧
         */
        @Override
        public void update(List<History> historyList) {
            if (mCallbacks != null) {
                mCallbacks.onUpdatedHistoryList(mSelf, historyList);
            }
        }
    };

    /**
     * 履歴クライアント
     *
     * @param context コンテキスト
     * @param callbacks コールバック
     */
    public HistoryClient(Context context, Callbacks callbacks) {
        mSelf = this;
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
        if (mBinder == null) {
            try {
                Intent intent = new Intent(INTERFACE.IHistoryService);
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
            try {
                mBinder.unregisterCallback(mCallback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
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
     * 削除
     */
    public void clear() {
        if (LOG_I) {
            Log.i(TAG, "clear#enter");
        }
        if (mBinder != null) {
            try {
                mBinder.delete();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        if (LOG_I) {
            Log.i(TAG, "clear#leave");
        }
    }

    /**
     * コールバック
     */
    public interface Callbacks {
        void onUpdatedHistoryList(Object object, List<History> historyList);
    }
}
