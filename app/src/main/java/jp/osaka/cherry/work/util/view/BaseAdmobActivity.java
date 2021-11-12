
package jp.osaka.cherry.work.util.view;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

//import com.google.android.gms.appindexing.AppIndex;
//import com.google.android.gms.common.api.GoogleApiClient;

import androidx.appcompat.app.AppCompatActivity;

import jp.osaka.cherry.work.R;

/**
 * ADMOB画面
 */
public abstract class BaseAdmobActivity extends AppCompatActivity {

    /**
     * @serial タイムアウト時間
     */
    static private final int TIMEOUT_ADMOB;

    static {
        TIMEOUT_ADMOB = 30 * 1000;
    }

    /**
     * @serial ハンドラ
     */
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    /**
     * @serial 実行
     */
    private final Runnable mTask = () -> {
        View AdMob = findViewById(R.id.AdMob);
        if (AdMob != null) {
            AdMob.setVisibility(View.INVISIBLE);
        }
    };

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onResume() {
        super.onResume();

        // モバイル広告の表示
        View AdMob = findViewById(R.id.AdMob);
        if (AdMob != null) {
            AdMob.setVisibility(View.VISIBLE);
        }

        // 実行
        mHandler.postDelayed(mTask, TIMEOUT_ADMOB);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onPause() {
        // モバイル広告の非表示
        View AdMob = findViewById(R.id.AdMob);
        if (AdMob != null) {
            AdMob.setVisibility(View.INVISIBLE);
        }
        super.onPause();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStart() {
        super.onStart();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStop() {
        super.onStop();
    }
}
