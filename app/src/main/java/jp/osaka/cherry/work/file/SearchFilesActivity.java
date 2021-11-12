package jp.osaka.cherry.work.file;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.app.ShareCompat;
import androidx.databinding.DataBindingUtil;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

import jp.osaka.cherry.work.R;
import jp.osaka.cherry.work.data.Asset;
import jp.osaka.cherry.work.data.File;
import jp.osaka.cherry.work.databinding.SearchFilesActivityBinding;
import jp.osaka.cherry.work.tasksdetails.TasksDetailsInFileActivity;
import jp.osaka.cherry.work.util.helper.FileHelper;
import jp.osaka.cherry.work.util.view.BaseAdmobActivity;

import static jp.osaka.cherry.work.Config.LOG_I;
import static jp.osaka.cherry.work.constants.ActivityTransition.REQUEST_FILE_DETAIL_LIST;
import static jp.osaka.cherry.work.constants.EXTRA.EXTRA_FILE_NAME;
import static jp.osaka.cherry.work.util.helper.FileHelper.toCSV;

/**
 * 検索ファイル画面
 */
public class SearchFilesActivity extends BaseAdmobActivity implements
        FileItemUserActionsListener {

    /**
     * @serial データセット
     */
    private final ArrayList<File> mDataSet = new ArrayList<>();

    /**
     * @serial ハンドラ
     */
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    /**
     * @serial バインディング
     */
    private SearchFilesActivityBinding mBinding;

    /**
     * @serial フラグメント
     */
    private FilesFragment mFrragment;

    /**
     * @serial 自身
     */
    private SearchFilesActivity mSelf;

    /**
     * インテント生成
     *
     * @param context コンテキスト
     * @return インテント
     */
    public static Intent createIntent(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, SearchFilesActivity.class);
        return intent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String TAG = "SearchFilesActivity";
        if (LOG_I) {
            Log.i(TAG, "onCreate#enter");
        }

        mSelf = this;

        // レイアウト設定
        mBinding = DataBindingUtil.setContentView(this, R.layout.search_files_activity);

        setSupportActionBar(mBinding.toolbar);
        initActionBar();

        mBinding.editSearch.setInputType(InputType.TYPE_CLASS_TEXT);
        mBinding.editSearch.addTextChangedListener(new TextWatcher() {
            /**
             * {@inheritDoc}
             */
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // 処理なし
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // 一覧表示の更新
                updateView(FileHelper.toListOf(mDataSet, charSequence.toString()));
                //updateEmptyView(FileHelper.toListOf(mDataSet, charSequence.toString()));
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void afterTextChanged(Editable editable) {
                // 処理なし
            }
        });

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
            // 一覧表示の更新
            updateView(FileHelper.toListOf(mDataSet, mBinding.editSearch.getText().toString()));
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        // 識別子ごとの処理
        if (id == android.R.id.home) {
            finishActivity();
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 画面終了
     */
    private void finishActivity() {
        Intent intent = getIntent();
        intent.setClass(this, FilesActivity.class);
        startActivity(intent);
    }

    /**
     * 表示更新
     *
     * @param collection 一覧
     */
    private void updateView(ArrayList<File> collection) {
        // 空表示の更新
        //updateEmptyView(collection);
        // 一覧表示の更新
        updateFragment(collection);
    }

    /**
     * フラグメント更新
     *
     * @param files ファイル一覧
     */
    private void updateFragment(ArrayList<File> files) {
        mFrragment = FilesFragment.newInstance(files);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.contentFrame, mFrragment)
                .commit();
    }

    /**
     * ポップアップメニュー表示
     *
     * @param view 表示
     * @param file ファイル
     */
    private void showPopupMenu(View view, final File file) {
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
                    intent.putExtra(EXTRA_FILE_NAME, file.name);
                    setIntent(intent);
                    setResult(RESULT_OK, intent);
                    finish();
                    return true;
                }
                case R.id.menu_delete: {
                    FileHelper.deleteFile(mSelf, file.name);
                    if (mFrragment != null) {
                        mFrragment.remove(file);
                    }
                    mHandler.post(() -> {
                        String[] fileList = mSelf.fileList();
                        ArrayList<File> list = new ArrayList<>();
                        for (String filename : fileList) {
                            if (filename.endsWith(".csv")) {
                                String name = filename.replace(".csv", "");
                                java.io.File out = mSelf.getFileStreamPath(name);
                                File file1 = new File();
                                file1.name = name;
                                file1.date = out.lastModified();
                                list.add(file1);
                            }
                        }
                        mDataSet.clear();
                        mDataSet.addAll(list);
                        // 空表示の更新
                        //updateEmptyView(mDataSet);
                        // 一覧表示の更新
                        //updateFragment(mDataSet);
                        // メッセージ表示
                        Snackbar.make(mBinding.coordinatorLayout, getText(R.string.succed_delete_file), Snackbar.LENGTH_SHORT)
                                .show();
                    });
                    return true;
                }
                case R.id.menu_share: {
                    // IntentBuilder をインスタンス化
                    ShareCompat.IntentBuilder builder = ShareCompat.IntentBuilder.from(mSelf);
                    // データをセットする
                    ArrayList<Asset> list = FileHelper.loadFile(mSelf, file.name);
                    builder.setSubject(file + ".csv");
                    builder.setText(toCSV(list));
                    builder.setType("text/plain");
                    // Intent を起動する
                    builder.startChooser();
                    return true;
                }
            }
            return false;
        });

        // Finally show the PopupMenu
        popup.show();
    }

    /**
     * アクションバー初期化
     */
    private void initActionBar() {
        // ToolBarの場合はもっとスマートなやり方があるかもしれません。
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            //bar.setBackgroundDrawable(ContextCompat.getDrawable(this, search_frame));
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setDisplayShowHomeEnabled(true);
            bar.setDisplayShowTitleEnabled(true);
            bar.setHomeButtonEnabled(true);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBackPressed() {
        finishActivity();
        super.onBackPressed();
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
