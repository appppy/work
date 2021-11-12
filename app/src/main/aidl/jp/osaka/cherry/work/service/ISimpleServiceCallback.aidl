// IPeopleServiceCallback.aidl
package jp.osaka.cherry.work.service;

import jp.osaka.cherry.work.data.Asset;

interface ISimpleServiceCallback {
    /**
     * 更新
     */
    void update(in List<Asset> list);
}
