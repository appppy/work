package jp.osaka.cherry.work.file;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jp.osaka.cherry.work.R;
import jp.osaka.cherry.work.data.Asset;
import jp.osaka.cherry.work.data.File;
import jp.osaka.cherry.work.databinding.FilesActivityBinding;
import jp.osaka.cherry.work.tasksdetails.TasksDetailsInFileActivity;
import jp.osaka.cherry.work.util.helper.FileHelper;
import jp.osaka.cherry.work.util.view.BaseAdmobActivity;

import static jp.osaka.cherry.work.Config.LOG_I;
import static jp.osaka.cherry.work.constants.ActivityTransition.REQUEST_FILE_DETAIL_LIST;
import static jp.osaka.cherry.work.constants.EXTRA.EXTRA_FILE_NAME;
import static jp.osaka.cherry.work.util.helper.AssetHelper.toSortByNameFileCollection;
import static jp.osaka.cherry.work.util.helper.FileHelper.toSortByDateModifiedCollection;

/**
 * ファイル
 */
public class FilesActivity extends BaseAdmobActivity implements
        FileItemUserActionsListener {

    /**
     * @serial 自身
     */
    private FilesActivity mSelf;

    /**
     * @serial バインディング
     */
    private FilesActivityBinding mBinding;

    /**
     * @serial データセット
     */
    private final ArrayList<File> mDataSet = new ArrayList<>();

    /**
     * @serial フラグラメント
     */
    private FilesFragment fragment;

    /**
     * @serial ハンドラ
     */
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    /**
     * インテント作成
     *
     * @param context コンテキスト
     * @return インテント
     */
    public static Intent createIntent(Context context) {
        return new Intent(context, FilesActivity.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String TAG = "FilesActivity";
        if (LOG_I) {
            Log.i(TAG, "onCreate#enter");
        }

        // 自身の取得
        mSelf = this;

        // テーマの設定
        //setTheme(R.style.AppTheme_BlueGrey);

        // レイアウト設定
        mBinding = DataBindingUtil.setContentView(this, R.layout.files_activity);

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

        mHandler.post(() -> {
            try {
                String[] fileList = mSelf.fileList();
                ArrayList<File> list = new ArrayList<>();
                for (String filename : fileList) {
                    if (filename.endsWith(".csv")) {
                        String name = filename.replace(".csv", "");
                        java.io.File out = mSelf.getFileStreamPath(name);
                        File file = new File();
                        file.name = name;
                        file.date = out.lastModified();
                        list.add(file);
                    }
                }
                mDataSet.clear();
                mDataSet.addAll(list);
                updateView(mDataSet);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }

    /**
     * 表示の更新
     *
     * @param collection 一覧
     */
    private void updateView(ArrayList<File> collection) {
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
     * 空表示の更新
     *
     * @param collection 一覧
     */
    private void updateEmptyView(List<File> collection) {
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
     * 一覧表示の更新
     *
     * @param collection 一覧
     */
    private void updateCollectionView(ArrayList<File> collection) {
        // 一覧表示の取得
        fragment = getFragment(collection);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.contentFrame, fragment)
                .commit();
    }

    /**
     * タイトル更新
     *
     * @param collection 一覧
     */
    private void updateTitle(ArrayList<File> collection) {
        StringBuilder sb = new StringBuilder();
        if (collection.isEmpty()) {
            sb.append(this.getString(R.string.file));
        } else {
            sb.append(this.getString(R.string.file)).append("  ").append(collection.size());
        }
        mBinding.toolbar.setTitle(sb.toString());
        sb.delete(0, sb.length());
    }

    /**
     * フラグメントを得る
     *
     * @param collection 一覧
     * @return フラグメント
     */
    private FilesFragment getFragment(ArrayList<File> collection) {
        return FilesFragment.newInstance(collection);
    }

    /**
     * ポップメニュー表示
     *
     * @param view 表示
     * @param item 項目
     */
    // BEGIN_INCLUDE(show_popup)
    private void showPopupMenu(View view, final File item) {

        // Create a PopupMenu, giving it the clicked view for an anchor
        final PopupMenu popup = new PopupMenu(this, view);

        // Inflate our menu resource into the PopupMenu's Menu
        popup.getMenuInflater().inflate(R.menu.file_more, popup.getMenu());

        // Set a listener so we are notified if a menu item is clicked
        popup.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.menu_open: {
                    // 結果通知
                    Intent intent = getIntent();
                    intent.putExtra(EXTRA_FILE_NAME, item.name);
                    setIntent(intent);
                    setResult(RESULT_OK, intent);
                    finish();
                    return true;
                }
                case R.id.menu_delete: {
                    FileHelper.deleteFile(mSelf, item.name);
                    if (fragment != null) {
                        fragment.remove(item);
                    }
                    mHandler.post(() -> {
                        String[] fileList = mSelf.fileList();
                        ArrayList<File> list = new ArrayList<>();
                        for (String filename : fileList) {
                            if (filename.endsWith(".csv")) {
                                String name = filename.replace(".csv", "");
                                java.io.File out = mSelf.getFileStreamPath(name);
                                File file = new File();
                                file.name = name;
                                file.date = out.lastModified();
                                list.add(file);
                            }
                        }
                        mDataSet.clear();
                        mDataSet.addAll(list);
                        // 空表示の更新
                        updateEmptyView(mDataSet);
                        // 一覧表示の更新
                        //updateCollectionView(mDataSet);
                        // メニューの更新
                        updateMenu();
                        // タイトル更新
                        updateTitle(mDataSet);
                        // メッセージ表示
                        Snackbar.make(mBinding.coordinatorLayout, getText(R.string.succed_delete_file), Snackbar.LENGTH_SHORT)
                                .show();
                    });
                    return true;
                }
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
            getMenuInflater().inflate(R.menu.file_empty, menu);
        } else if(mDataSet.size() == 1) {
            getMenuInflater().inflate(R.menu.file_one, menu);
        } else {
            getMenuInflater().inflate(R.menu.file_some, menu);
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
            case R.id.menu_by_name: {
                ArrayList<File> collection = (ArrayList<File>) toSortByNameFileCollection(mDataSet);
                updateView(collection);
                Snackbar.make(mBinding.coordinatorLayout, getText(R.string.succed_by_name), Snackbar.LENGTH_SHORT)
                        .show();
                return true;
            }
            case R.id.menu_by_date_modified: {
                ArrayList<File> collection = (ArrayList<File>) toSortByDateModifiedCollection(mDataSet);
                Collections.reverse(collection);
                updateView(collection);
                Snackbar.make(mBinding.coordinatorLayout, getText(R.string.succed_by_date_modified), Snackbar.LENGTH_SHORT)
                        .show();
                return true;
            }
            case R.id.menu_swap_vert: {
                Collections.reverse(mDataSet);
                updateView(mDataSet);
                Snackbar.make(mBinding.coordinatorLayout, getText(R.string.succed_swap_vert), Snackbar.LENGTH_SHORT)
                        .show();
                return true;
            }
            case R.id.menu_search: {
                Intent intent = SearchFilesActivity.createIntent(getApplicationContext());
                startActivity(intent);
                //overridePendingTransition(R.animator.fade_out, R.animator.fade_in);
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onFileClicked(View view, File file) {
        // アセットを取得
        ArrayList<Asset> assets = FileHelper.loadFile(mSelf, file.name + ".csv");
        if (Build.VERSION.SDK_INT >= 21) {
            ActivityOptionsCompat opts = ActivityOptionsCompat.makeScaleUpAnimation(
                    view, 0, 0, view.getWidth(), view.getHeight());
            Intent intent = TasksDetailsInFileActivity.createIntent(this, assets, file.name);
            ActivityCompat.startActivityForResult(this, intent, REQUEST_FILE_DETAIL_LIST.ordinal(), opts.toBundle());
        } else {
            Intent intent = TasksDetailsInFileActivity.createIntent(this, assets, file.name);
            startActivityForResult(intent, REQUEST_FILE_DETAIL_LIST.ordinal());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onFileMoreClicked(final View view, final File file) {
        // We need to post a Runnable to show the file_more to make sure that the PopupMenu is
        // correctly positioned. The reason being that the view may change position before the
        // PopupMenu is shown.
        view.post(() -> showPopupMenu(view, file));
    }
}
