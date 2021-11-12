// IPeopleService.aidl
package jp.osaka.cherry.work.service;

import jp.osaka.cherry.work.service.ISimpleServiceCallback;
import jp.osaka.cherry.work.data.Asset;

interface ISimpleService {
    /**
     * コールバック登録
     */
    void registerCallback(ISimpleServiceCallback callback);

    /**
     * コールバック解除
     */
    void unregisterCallback(ISimpleServiceCallback callback);

    /**
     * 設定
     */
    void setList(in List<Asset> list);

    /**
     * 取得
     */
    void getList();
}
