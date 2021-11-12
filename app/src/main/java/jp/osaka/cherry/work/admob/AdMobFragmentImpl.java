
package jp.osaka.cherry.work.admob;

import com.google.android.gms.ads.AdSize;

/**
 * AdMobフラグメント実装
 */
public class AdMobFragmentImpl extends AdMobFragment {

    /**
     * {@inheritDoc}
     */
    @Override
    protected AdSize getAdSize() {
        return AdSize.BANNER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getUnitId() {
        return "キーを設定してください";
    }
}