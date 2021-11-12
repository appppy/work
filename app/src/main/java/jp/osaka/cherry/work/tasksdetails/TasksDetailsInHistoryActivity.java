package jp.osaka.cherry.work.tasksdetails;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

import jp.osaka.cherry.work.R;
import jp.osaka.cherry.work.data.Asset;
import jp.osaka.cherry.work.databinding.TasksDetailsInActivityBinding;

import static jp.osaka.cherry.work.Config.LOG_I;
import static jp.osaka.cherry.work.constants.EXTRA.EXTRA_ASSETS;
import static jp.osaka.cherry.work.constants.EXTRA.EXTRA_NAME;
import static jp.osaka.cherry.work.util.helper.ActivityHelper.startTaskDetailsInHistoryActivity;

/**
 * 履歴内のタスク詳細画面
 */
public class TasksDetailsInHistoryActivity extends AppCompatActivity implements
        TaskGroupsUserActionsListener {

    /**
     * @serial データセット
     */
    private ArrayList<Asset> mDataSet;

    /**
     * インテント生成
     *
     * @param context コンテキスト
     * @param assets アセット
     * @param name 名前
     * @return インテント
     */
    public static Intent createIntent(Context context, ArrayList<Asset> assets, String name) {
        Intent intent = new Intent(context, TasksDetailsInHistoryActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(EXTRA_ASSETS, assets);
        bundle.putString(EXTRA_NAME, name);
        intent.putExtras(bundle);
        return intent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String TAG = "TasksDetailsInHisAct";
        if (LOG_I) {
            Log.i(TAG, "onCreate#enter");
        }

        setTheme(R.style.AppTheme_BlueGrey);

        // インテントの取得
        Intent intent = getIntent();
        mDataSet = intent.getParcelableArrayListExtra(EXTRA_ASSETS);
        String name = intent.getStringExtra(EXTRA_NAME);

        // レイアウトの設定
        TasksDetailsInActivityBinding mBinding = DataBindingUtil.setContentView(this, R.layout.tasks_details_in_activity);

        // ツールバーの設定
        setSupportActionBar(mBinding.toolbar);
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setTitle(name);
            bar.setDisplayHomeAsUpEnabled(true);
        }

        // 表示の設定
        setView();

        if (LOG_I) {
            Log.i(TAG, "onCreate#leave");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // メニュー設定
        getMenuInflater().inflate(R.menu.history_details, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            setResult();
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 表示設定
     */
    private void setView() {
        // 一覧表示の更新
        updateCollectionView(mDataSet);
    }

    /**
     * 一覧更新
     *
     * @param collection 一覧
     */
    private void updateCollectionView(ArrayList<Asset> collection) {
        // 一覧表示の取得
        Fragment fragment = TaskGroupsFragment.newInstance(collection);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.contentFrame, fragment)
                .commit();
    }

    /**
     * 結果の設定
     */
    private void setResult() {
        Intent intent = getIntent();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(EXTRA_ASSETS, mDataSet);
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBackPressed() {

        //結果の設定
        setResult();

        super.onBackPressed();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onTaskClicked(View view, Asset asset) {
        startTaskDetailsInHistoryActivity(this, this, asset);
    }
}
