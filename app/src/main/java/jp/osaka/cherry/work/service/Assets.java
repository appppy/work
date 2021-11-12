
package jp.osaka.cherry.work.service;

import java.util.Collection;

import jp.osaka.cherry.work.data.Asset;
import jp.osaka.cherry.work.util.controller.status.BaseStatus;

/**
 * 資産
 */
public class Assets extends BaseStatus {

    /**
     * @serial 一覧
     */
    public Collection<Asset> collection;

    /**
     * コンストラクタ
     *
     * @param collection 一覧
     */
    Assets(Collection<Asset> collection) {
        this.collection = collection;
    }

}
