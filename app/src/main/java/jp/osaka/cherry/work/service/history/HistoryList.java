package jp.osaka.cherry.work.service.history;

import java.util.Collection;

import jp.osaka.cherry.work.data.History;
import jp.osaka.cherry.work.util.controller.status.BaseStatus;

/**
 * 履歴一覧
 */
class HistoryList extends BaseStatus {

    /**
     * @serial 履歴一覧
     */
    public Collection<History> collection;

    /**
     * コンストラクタ
     *
     * @param collection 履歴一覧
     */
    HistoryList(Collection<History> collection) {
        this.collection = collection;
    }

}
