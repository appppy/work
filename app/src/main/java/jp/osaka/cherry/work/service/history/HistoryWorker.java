package jp.osaka.cherry.work.service.history;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.Objects;

import jp.osaka.cherry.work.data.History;
import jp.osaka.cherry.work.util.controller.BaseCommand;
import jp.osaka.cherry.work.util.controller.IWorker;
import jp.osaka.cherry.work.util.controller.command.Backup;
import jp.osaka.cherry.work.util.controller.command.Delete;
import jp.osaka.cherry.work.util.controller.command.Restore;

import static jp.osaka.cherry.work.Config.LOG_I;
import static jp.osaka.cherry.work.constants.EXTRA.EXTRA_HISTORY;

/**
 * 履歴ワーカー
 */
class HistoryWorker implements IWorker {

    /**
     * @serial 目印
     */
    private static final String TAG = "HistoryWorker";

    /**
     * @serial メッセージの定義
     */
    private enum MESSAGE {
        RESTORE,
        BACKUP,
        DELETE;
        /**
         * 値に合致する enum 定数を返す。
         *
         * @param index インデックス
         * @return メッセージ
         */
        public static MESSAGE get(int index) {
            // 値から enum 定数を特定して返す処理
            for (MESSAGE msg : MESSAGE.values()) {
                if (msg.ordinal() == index) {
                    return msg;
                }
            }
            return null; // 特定できない場合
        }
    }

    /**
     * @serial コールバック
     */
    private final Callbacks mCallbacks;

    /**
     * @serial 自身
     */
    private final HistoryWorker mSelf;

    /**
     * @serial コンテキスト
     */
    private final Context mContext;

    /**
     * @serial スレッド
     */
    private final HandlerThread mThread;

    /**
     * @serial ハンドラ
     */
    private final Handler mHandler;

    /**
     * コンストラクタ
     *
     * @param context コンテキスト
     * @param callbacks コールバック
     */
    HistoryWorker(Context context, Callbacks callbacks) {

        mContext = context;

        mCallbacks = callbacks;

        mSelf = this;

        mThread = new HandlerThread("History");
        mThread.start();

        mHandler =  new Handler(mThread.getLooper()) {

            /**
             * {@inheritDoc}
             */
            @Override
            public void handleMessage(@NonNull Message msg) {
                // 処理開始を通知
                if (mCallbacks != null) {
                    mCallbacks.onStarted(mSelf, (BaseCommand) msg.obj);
                }

                MESSAGE type = MESSAGE.get(msg.what);
                switch(Objects.requireNonNull(type)) {
                    case RESTORE: {
                        Restore command = (Restore) msg.obj;
                        HistoryList status = new HistoryList(HistoryAccessor.getData(mContext));
                        // 処理結果を通知
                        if (mCallbacks != null) {
                            mCallbacks.onUpdated(mSelf, command, status);
                        }
                        if (mCallbacks != null) {
                            mCallbacks.onSuccessed(mSelf, (BaseCommand) msg.obj);
                        }
                        break;
                    }
                    case BACKUP: {
                        Backup command = (Backup) msg.obj;
                        History item = command.args.getParcelable(EXTRA_HISTORY);
                        HistoryAccessor.insert(mContext, item);
                        if (mCallbacks != null) {
                            mCallbacks.onSuccessed(mSelf, (BaseCommand) msg.obj);
                        }
                        break;
                    }
                    case DELETE: {
                        HistoryAccessor.delete(mContext);
                        if (mCallbacks != null) {
                            mCallbacks.onSuccessed(mSelf, (BaseCommand) msg.obj);
                        }
                        break;
                    }

                    default: {
                        break;
                    }
                }
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(BaseCommand command) {
        if (LOG_I) {
            Log.i(TAG, "start#enter");
        }

        if (command instanceof Restore) {
            if (mHandler != null) {
                mHandler.removeMessages(MESSAGE.RESTORE.ordinal());
                Message msg = mHandler.obtainMessage(MESSAGE.RESTORE.ordinal(), command);
                mHandler.sendMessage(msg);
            }
        }
        if (command instanceof Backup) {
            if (mHandler != null) {
                mHandler.removeMessages(MESSAGE.BACKUP.ordinal());
                Message msg = mHandler.obtainMessage(MESSAGE.BACKUP.ordinal(), command);
                mHandler.sendMessage(msg);
            }
        }
        if (command instanceof Delete) {
            if (mHandler != null) {
                mHandler.removeMessages(MESSAGE.DELETE.ordinal());
                Message msg = mHandler.obtainMessage(MESSAGE.DELETE.ordinal(), command);
                mHandler.sendMessage(msg);
            }
        }
        if (LOG_I) {
            Log.i(TAG, "start#leave");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() {
        if (LOG_I) {
            Log.i(TAG, "stop#enter");
        }

        if (mHandler != null) {
            mHandler.removeMessages(MESSAGE.RESTORE.ordinal());
            mHandler.removeMessages(MESSAGE.BACKUP.ordinal());
            mHandler.removeMessages(MESSAGE.DELETE.ordinal());
        }

        if (mThread != null && mThread.isAlive()) {
            mThread.quit();
        }

        if (LOG_I) {
            Log.i(TAG, "stop#leave");
        }
    }
}
