// IHistoryServiceCallback.aidl
package jp.osaka.cherry.work.service.history;

import jp.osaka.cherry.work.data.History;

interface IHistoryServiceCallback {
    /**
     * 更新
     */
    void update(in List<History> history);
}
