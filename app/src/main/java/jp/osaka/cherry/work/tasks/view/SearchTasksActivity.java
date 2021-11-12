package jp.osaka.cherry.work.tasks.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import jp.osaka.cherry.work.R;
import jp.osaka.cherry.work.ViewModelFactory;
import jp.osaka.cherry.work.constants.ActivityTransition;
import jp.osaka.cherry.work.constants.INVALID;
import jp.osaka.cherry.work.data.Asset;
import jp.osaka.cherry.work.databinding.SearchTasksActivityBinding;
import jp.osaka.cherry.work.tasks.TasksModel;
import jp.osaka.cherry.work.tasks.TasksNavigator;
import jp.osaka.cherry.work.tasks.TasksViewModel;
import jp.osaka.cherry.work.tasks.binding.TasksFragment;
import jp.osaka.cherry.work.util.ActivityUtils;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;
import static jp.osaka.cherry.work.Config.LOG_I;
import static jp.osaka.cherry.work.constants.EXTRA.EXTRA_ASSET;
import static jp.osaka.cherry.work.util.controller.command.Action.ACTION.MODIFY;
import static jp.osaka.cherry.work.util.helper.ActivityHelper.startTaskDetailsInHistoryActivity;

/**
 * 検索画面
 */
public class SearchTasksActivity extends AppCompatActivity implements
        TasksNavigator {

    /**
     * @serial 目印
     */
    private final String TAG = "SearchTasksActivity";

    /**
     * @serial 表示モデル
     */
    private TasksViewModel mViewModel;

    /**
     * @serial モデル
     */
    private TasksModel mModel;

    /**
     * インテント生成
     *
     * @param context コンテキスト
     * @return インテント
     */
    public static Intent createIntent(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, SearchTasksActivity.class);
        return intent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (LOG_I) {
            Log.i(TAG, "onCreate#enter");
        }

        SearchTasksActivityBinding mBinding = DataBindingUtil.setContentView(this, R.layout.search_tasks_activity);

        setSupportActionBar(mBinding.toolbar);
        initActionBar();

        setupViewFragment();

        mModel = new TasksModel(this);

        mViewModel = obtainViewModel(this);

        mViewModel.bind(mModel);
        mViewModel.setNavigater(this);
        mViewModel.setLayout(mBinding.coordinatorLayout);
        mViewModel.setPreferences(getDefaultSharedPreferences(this));
        mViewModel.setSearching(true);

        // Subscribe to "change title" event
        mViewModel.getSetTitleEvent().observe(this, name -> {
            if (name != null) {
                onTitleChanged(name);
            }
        });

        // Subscribe to "open task" event
        mViewModel.getOpenTaskEvent().observe(this, asset -> {
            if (asset != null) {
                openTaskDetails(asset);
            }
        });

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
                mViewModel.update(toListOf(mModel.getTasks(), charSequence.toString()));
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
        mModel.enable();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onPause() {
        super.onPause();
        mModel.disable();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (LOG_I) {
            Log.i(TAG, "onActivityResult#enter");
        }

        // 結果確認
        ActivityTransition type = ActivityTransition.get(requestCode);
        switch (Objects.requireNonNull(type)) {
            case REQUEST_DETAIL_TASK:
            case REQUEST_EDIT_TASK: {
                if (resultCode == RESULT_OK) {
                    // データの取得
                    Bundle bundle = data.getExtras();
                    assert bundle != null;
                    Asset asset = bundle.getParcelable(EXTRA_ASSET);
                    // レジューム後に動作させる
                    mViewModel.setRedo(MODIFY, 0, asset);
                }
                break;
            }
            default: {
                break;
            }
        }
        if (LOG_I) {
            Log.i(TAG, "onActivityResult#leave");
        }
    }

    /**
     * 表示モデル生成
     *
     * @param activity 画面
     * @return 表示モデル
     */
    public TasksViewModel obtainViewModel(FragmentActivity activity) {
        // Use a Factory to inject dependencies into the ViewModel
        ViewModelFactory factory = ViewModelFactory.getInstance(activity.getApplication());

        return ViewModelProviders.of(activity, factory).get(TasksViewModel.class);
    }

    /**
     * 表示フラグメント設定
     */
    private void setupViewFragment() {
        ActivityUtils.replaceFragmentInActivity(
                getSupportFragmentManager(),
                TasksFragment.newInstance(new ArrayList<>(), true),
                R.id.contentFrame);
    }

    /**
     * 表示フラグメント更新
     *
     * @param tasks 一覧
     */
    private void updateViewFragment(List<Asset> tasks) {
        ActivityUtils.replaceFragmentInActivity(
                getSupportFragmentManager(),
                TasksFragment.newInstance((ArrayList<Asset>) tasks, false),
                R.id.contentFrame);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finishActivity();
                finish();
                return true;
            case R.id.menu_edit: {
                mViewModel.editDetails(this);
                return true;
            }
            case R.id.menu_info: {
                mViewModel.openDetails();
                return true;
            }
            case R.id.menu_archive: {
                mViewModel.archive();
                return true;
            }
            case R.id.menu_trash: {
                mViewModel.trash();
                return true;
            }
            case R.id.menu_copy: {
                mViewModel.copy();
                return true;
            }
            case R.id.menu_share: {
                mViewModel.share();
                return true;
            }
            case R.id.menu_priority_high:
            case R.id.menu_priority_middle:
            case R.id.menu_priority_low: {
                mViewModel.changePriority(item.getItemId());
                return true;
            }
            case R.id.menu_progress_not_start:
            case R.id.menu_progress_completed:
            case R.id.menu_progress_inprogress:
            case R.id.menu_progress_waiting:
            case R.id.menu_progress_postponement: {
                mViewModel.changeProgress(item.getItemId());
                return true;
            }
            case R.id.menu_schedule_this_week:
            case R.id.menu_schedule_weekend:
            case R.id.menu_schedule_next_week: {
                mViewModel.changeSchedule(item.getItemId());
                return true;
            }
            default: {
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onTitleChanged(String name) {
        // 処理なし
   }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSelectMode(int number) {
        // 処理なし
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onNormalMode() {
        // 処理し
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onTasksChanged(List<Asset> tasks) {
        updateViewFragment(tasks);
    }

    /**
     * 画面終了
     */
    private void finishActivity() {
        Intent intent = getIntent();
        intent.setClass(this, TasksActivity.class);
        startActivity(intent);
        overridePendingTransition(R.animator.fade_out, R.animator.fade_in);
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
     * 一覧変換
     *
     * @param collection 一覧
     * @return 一覧
     */
    private ArrayList<Asset> toList(Collection<Asset> collection) {
        ArrayList<Asset> result = new ArrayList<>();
        for (Asset asset : collection) {
            switch (asset.content) {
                case ARCHIVE:
                case TRASH: {
                    break;
                }
                default: {
                    result.add(asset);
                    break;
                }
            }
        }
        return result;
    }

    /**
     * 一覧変換
     *
     * @param assets 一覧
     * @param src 文字
     * @return 一覧
     */
    private ArrayList<Asset> toListOf(ArrayList<Asset> assets, String src) {
        ArrayList<Asset> result = new ArrayList<>();
        if (src.isEmpty()) {
            result.addAll(assets);
            return result;
        }
        for (Asset asset : toList(assets)) {
            if (asset.displayName != null
                    && !asset.displayName.equals(INVALID.INVALID_STRING_VALUE)
                    && (asset.displayName.contains(src))) {
                result.add(asset);
            } else if (asset.description != null
                    && !asset.description.equals(INVALID.INVALID_STRING_VALUE)
                    && asset.description.contains(src)) {
                result.add(asset);
            } else if (asset.note != null
                    && !asset.note.equals(INVALID.INVALID_STRING_VALUE)
                    && asset.note.contains(src)) {
                result.add(asset);
            }
        }
        return result;
    }

    /**
     * 詳細表示
     *
     * @param asset 項目
     */
    public void openTaskDetails(Asset asset) {
        startTaskDetailsInHistoryActivity(this, this, mModel.getTask(asset.uuid));
    }
}
