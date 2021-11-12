package jp.osaka.cherry.work.tasks.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import jp.osaka.cherry.work.R;
import jp.osaka.cherry.work.ViewModelFactory;
import jp.osaka.cherry.work.constants.ActivityTransition;
import jp.osaka.cherry.work.data.Asset;
import jp.osaka.cherry.work.databinding.RecentTasksActivityBinding;
import jp.osaka.cherry.work.tasks.TasksModel;
import jp.osaka.cherry.work.tasks.TasksNavigator;
import jp.osaka.cherry.work.tasks.TasksViewModel;
import jp.osaka.cherry.work.tasks.binding.TasksFragment;
import jp.osaka.cherry.work.util.ActivityUtils;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;
import static jp.osaka.cherry.work.Config.LOG_I;
import static jp.osaka.cherry.work.constants.EXTRA.EXTRA_ASSET;
import static jp.osaka.cherry.work.util.controller.command.Action.ACTION.MODIFY;
import static jp.osaka.cherry.work.util.helper.ActivityHelper.getStartActivity;
import static jp.osaka.cherry.work.util.helper.ActivityHelper.startTaskDetailsActivity;

/**
 * 最近画面
 */
public class RecentTasksActivity extends AppCompatActivity implements
        TasksNavigator {

    /**
     * @serial 目印
     */
    private final String TAG = "RecentTasksActivity";

    /**
     * @serial ドローワーレイアウト
     */
    private DrawerLayout mDrawerLayout;

    /**
     * @serial 表示モデル
     */
    private TasksViewModel mViewModel;

    /**
     * @serial モデル
     */
    private TasksModel mModel;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (LOG_I) {
            Log.i(TAG, "onCreate#enter");
        }

        RecentTasksActivityBinding mBinding = DataBindingUtil.setContentView(this, R.layout.recent_tasks_activity);

        setupToolbar();

        setupNavigationDrawer();

        setupViewFragment();

        mModel = new TasksModel(this);

        mViewModel = obtainViewModel(this);

        mViewModel.bind(mModel);
        mViewModel.setNavigater(this);
        mViewModel.setLayout(mBinding.coordinatorLayout);
        mViewModel.setPreferences(getDefaultSharedPreferences(this));

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
                TasksFragment.newInstance(new ArrayList<>(tasks), false),
                R.id.contentFrame);
    }

    /**
     * ツールバー設定
     */
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setTitle(R.string.recent);
            ab.setHomeAsUpIndicator(R.drawable.ic_menu);
            ab.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * ナビゲーションドローワー設定
     */
    private void setupNavigationDrawer() {
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerLayout.setStatusBarBackground(R.color.colorPrimaryDark);
        mDrawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                int id = mViewModel.getId();
                if (id != R.id.recent && RecentTasksActivity.class != getStartActivity(id)) {
                    Intent intent = getIntent();
                    intent.setClass(getApplicationContext(), getStartActivity(id));
                    startActivity(intent);
                    overridePendingTransition(R.animator.fade_out, R.animator.fade_in);
                    finish();
                }
            }
        });
        NavigationView navigationView = findViewById(R.id.nav_view);
        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }
    }

    /**
     * ドローワーコンテンツ設定
     *
     * @param navigationView ナビゲーション表示
     */
    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                menuItem -> {
                    mViewModel.setId(menuItem.getItemId());
                    // Close the navigation drawer when an item is selected.
                    menuItem.setChecked(true);
                    mDrawerLayout.closeDrawers();
                    return true;
                });
    }

    /**
     * タイトル変更
     *
     * @param name 名前
     */
    public void onTitleChanged(String name) {
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setTitle(name);
            ab.setHomeAsUpIndicator(R.drawable.ic_menu);
            ab.setDisplayHomeAsUpEnabled(true);
        }
        invalidateOptionsMenu();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSelectMode(int number) {
        /* 処理なし */
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onNormalMode() {
        /* 処理なし */
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onTasksChanged(List<Asset> tasks) {
        updateViewFragment(tasks);
    }

    /**
     * 詳細表示
     *
     * @param asset 項目
     */
    public void openTaskDetails(Asset asset) {
        startTaskDetailsActivity(this, this, mModel.getTask(asset.uuid));
    }

    /**
     * 検索一覧変換
     *
     * @param collection 一覧
     * @return 検索一覧
     */
    public static Collection<Asset> toSortByCollection(Collection<Asset> collection) {
        Collections.sort((List<Asset>) collection, (lhs, rhs) -> (int) (lhs.timestamp - rhs.timestamp));
        return collection;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Open the navigation drawer when the home icon is selected from the toolbar.
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.menu_edit: {
                mViewModel.editDetails(this);
                return true;
            }
            case R.id.menu_info: {
                mViewModel.openDetails();
                return true;
            }
            case R.id.menu_share: {
                mViewModel.share();
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

}

