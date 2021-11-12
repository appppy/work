// IHistoryService.aidl
package jp.osaka.cherry.work.service.history;

import jp.osaka.cherry.work.data.History;

interface IHistoryProxy {
    /**
     * 設定
     */
    void setHistory(in History history);
}
