package jp.osaka.cherry.work.util.controller;


import jp.osaka.cherry.work.util.controller.status.BaseStatus;

/**
 * ワーカー
 */
public interface IWorker {

    /**
     * 開始
     *
     * @param command コマンド
     */
    void start(BaseCommand command);

    /**
     * 停止
     */
    void stop();

    /**
     * コールバック
     */
    interface Callbacks {

        /**
         * 開始
         *
         * @param worker 自身
         * @param command 命令
         */
        void onStarted(IWorker worker, BaseCommand command);

        /**
         * 更新
         *
         * @param worker 自身
         * @param command 命令
         * @param status 状態
         */
        void onUpdated(IWorker worker, BaseCommand command, BaseStatus status);

        /**
         * 成功
         *
         * @param worker 自身
         * @param command 命令
         */
        void onSuccessed(IWorker worker, BaseCommand command);

    }

}
