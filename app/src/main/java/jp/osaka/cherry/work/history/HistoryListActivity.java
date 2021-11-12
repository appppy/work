package jp.osaka.cherry.work.history;


import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.databinding.DataBindingUtil;

import com.google.android.material.snackbar.Snackbar;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import jp.osaka.cherry.work.R;
import jp.osaka.cherry.work.data.Asset;
import jp.osaka.cherry.work.data.History;
import jp.osaka.cherry.work.databinding.HistoryListActivityBinding;
import jp.osaka.cherry.work.service.history.HistoryClient;
import jp.osaka.cherry.work.tasksdetails.TasksDetailsInHistoryActivity;
import jp.osaka.cherry.work.util.helper.AssetHelper;
import jp.osaka.cherry.work.util.view.BaseAdmobActivity;

import static jp.osaka.cherry.work.Config.LOG_I;
import static jp.osaka.cherry.work.constants.ActivityTransition.REQUEST_DETAIL_TASK;
import static jp.osaka.cherry.work.constants.EXTRA.EXTRA_HISTORY;

/**
 * 履歴一覧画面
 */
public class HistoryListActivity extends BaseAdmobActivity implements
        HistoryItemUserActionsListener,
        HistoryClient.Callbacks {

    /**
     * @serial バインディング
     */
    private HistoryListActivityBinding mBinding;

    /**
     * @serial データセット
     */
    private final ArrayList<History> mDataSet = new ArrayList<>();

    /**
     * @serial ハンドラ
     */
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    /**
     * @serial 履歴クライアント
     */
    private final HistoryClient mClient = new HistoryClient(this, this);

    /**
     * インテント生成
     *
     * @param context コンテキスト
     * @return インテント
     */
    public static Intent createIntent(Context context) {
        return new Intent(context, HistoryListActivity.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String TAG = "HistoryListActivity";
        if (LOG_I) {
            Log.i(TAG, "onCreatre#enter");
        }

        // テーマの設
        setTheme(R.style.AppTheme_BlueGrey);

        // レイアウト設定
        mBinding = DataBindingUtil.setContentView(this, R.layout.history_list_activity);

        // ツールバーの設定
        setSupportActionBar(mBinding.toolbar);
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
        }

        if (LOG_I) {
            Log.i(TAG, "onCreate#leave");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onResume() {
        super.onResume();

        // サービスの接続
        mClient.connect();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onPause() {
        // サービスの非接続
        mClient.disconnect();

        super.onPause();
    }

    /**
     * 表示更新
     *
     * @param collection 一覧
     */
    private void updateView(ArrayList<History> collection) {
        // プログレスバーの更新
        updateProgressBar();
        // 空表示の更新
        updateEmptyView(collection);
        // 一覧表示の更新
        updateCollectionView(collection);
        // メニューの更新
        updateMenu();
        // タイトルの更新
        updateTitle(collection);
    }

    /**
     * メニュー更新
     */
    private void updateMenu() {
        invalidateOptionsMenu();
    }

    /**
     * プログレスバー更新
     */
    private void updateProgressBar() {
        ProgressBar bar = mBinding.productImageLoading;
        bar.setVisibility(View.INVISIBLE);
    }

    /**
     * 空表示更新
     *
     * @param collection 一覧
     */
    private void updateEmptyView(List<History> collection) {
        // 空表示の更新
        ImageView view = mBinding.emptyView;
        boolean isEmpty = collection.isEmpty();
        if (isEmpty) {
            view.setVisibility(View.VISIBLE);
        } else {

            view.setVisibility(View.GONE);
        }
    }

    /**
     * 一覧更新
     *
     * @param collection 一覧
     */
    private void updateCollectionView(ArrayList<History> collection) {
        // 表示の取得
        HistoryListFragment collectionView = getFragment(collection);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.contentFrame, collectionView)
                .commit();
    }

    /**
     * タイトル更新
     *
     * @param collection 一覧
     */
    private void updateTitle(ArrayList<History> collection) {
        StringBuilder sb = new StringBuilder();
        if (collection.isEmpty()) {
            sb.append(this.getString(R.string.history));
        } else {
            sb.append(this.getString(R.string.history)).append("  ").append(collection.size());
        }
        mBinding.toolbar.setTitle(sb.toString());
        sb.delete(0, sb.length());
    }

    /**
     * 履歴一覧表示取得
     *
     * @param collection 履歴一覧
     * @return 履歴一覧表示
     */
    private HistoryListFragment getFragment(ArrayList<History> collection) {
        return HistoryListFragment.newInstance(collection);
    }

    /**
     * ポップアップメニュー表示
     *
     * @param view 表示
     * @param item 項目
     */
    // BEGIN_INCLUDE(show_popup)
    private void showPopupMenu(View view, final History item) {
        // Create a PopupMenu, giving it the clicked view for an anchor
        final PopupMenu popup = new PopupMenu(this, view);

        // Inflate our menu resource into the PopupMenu's Menu
        popup.getMenuInflater().inflate(R.menu.history_more, popup.getMenu());

        // Set a listener so we are notified if a menu item is clicked
        popup.setOnMenuItemClickListener(menuItem -> {
            if (menuItem.getItemId() == R.id.menu_open) {// 結果通知
                Intent intent = getIntent();
                intent.putExtra(EXTRA_HISTORY, item);
                setIntent(intent);
                setResult(RESULT_OK, intent);
                finish();
                return true;
            }
            return false;
        });


        // Finally show the PopupMenu
        popup.show();
    }
    // END_INCLUDE(show_popup)

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mDataSet.isEmpty()) {
            getMenuInflater().inflate(R.menu.history_empty, menu);
        } else if (mDataSet.size() == 1) {
            getMenuInflater().inflate(R.menu.history_one, menu);
        } else {
            getMenuInflater().inflate(R.menu.history_some, menu);
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        // 識別子ごとの処理
        switch (id) {
            case android.R.id.home: {
                finish();
                return true;
            }
            case R.id.menu_swap_vert: {
                Collections.reverse(mDataSet);
                updateView(mDataSet);
                // メッセージ表示
                Snackbar.make(mBinding.coordinatorLayout, getText(R.string.succed_swap_vert), Snackbar.LENGTH_SHORT)
                        .show();
                return true;
            }
            case R.id.menu_empty: {
                mClient.clear();
                mDataSet.clear();
                updateView(mDataSet);
                // メッセージ表示
                Snackbar.make(mBinding.coordinatorLayout, getText(R.string.empty_history_is_done), Snackbar.LENGTH_SHORT)
                        .show();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 日付順に並び替え
     *
     * @param collection 一覧
     */
    static void toSortByDateModifiedCollection(Collection<History> collection) {
        Collections.sort((List<History>) collection, (lhs, rhs) -> (int) (lhs.date - rhs.date));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onUpdatedHistoryList(Object object, final List<History> historyList) {
        mHandler.post(() -> {
            try {
                boolean result;
                result = mDataSet.isEmpty() || mDataSet.size() != historyList.size() || !mDataSet.equals(historyList);
                if (result) {
                    mDataSet.clear();
                    mDataSet.addAll(historyList);
                    toSortByDateModifiedCollection(mDataSet);
                    updateView(mDataSet);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onHistoryClicked(View view, History history) {
        // アセットを取得
        ArrayList<Asset> assets = AssetHelper.toAssets(history.message);
        String name = DateFormat.getDateInstance().format(new Date(history.date)) + " " + history.title + " assets";
        if (Build.VERSION.SDK_INT >= 21) {
            ActivityOptionsCompat opts = ActivityOptionsCompat.makeScaleUpAnimation(
                    view, 0, 0, view.getWidth(), view.getHeight());
            Intent intent = TasksDetailsInHistoryActivity.createIntent(this, assets, name);
            ActivityCompat.startActivityForResult(this, intent, REQUEST_DETAIL_TASK.ordinal(), opts.toBundle());
        } else {
            Intent intent = TasksDetailsInHistoryActivity.createIntent(this, assets, name);
            startActivityForResult(intent, REQUEST_DETAIL_TASK.ordinal());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onHistoryMoreClicked(final View view, final History history) {
        // We need to post a Runnable to show the file_more to make sure that the PopupMenu is
        // correctly positioned. The reason being that the view may change position before the
        // PopupMenu is shown.
        view.post(() -> showPopupMenu(view, history));
    }
}
