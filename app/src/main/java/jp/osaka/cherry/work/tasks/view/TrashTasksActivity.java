package jp.osaka.cherry.work.tasks.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jp.osaka.cherry.work.R;
import jp.osaka.cherry.work.ViewModelFactory;
import jp.osaka.cherry.work.constants.ActivityTransition;
import jp.osaka.cherry.work.data.Asset;
import jp.osaka.cherry.work.databinding.TrashTasksActivityBinding;
import jp.osaka.cherry.work.tasks.TasksModel;
import jp.osaka.cherry.work.tasks.TasksNavigator;
import jp.osaka.cherry.work.tasks.TasksViewModel;
import jp.osaka.cherry.work.tasks.binding.TasksFragment;
import jp.osaka.cherry.work.util.ActivityUtils;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;
import static jp.osaka.cherry.work.Config.LOG_I;
import static jp.osaka.cherry.work.constants.EXTRA.EXTRA_ASSET;
import static jp.osaka.cherry.work.constants.SELECTION.UNSELECTED;
import static jp.osaka.cherry.work.util.controller.command.Action.ACTION.MODIFY;
import static jp.osaka.cherry.work.util.helper.ActivityHelper.getStartActivity;
import static jp.osaka.cherry.work.util.helper.ActivityHelper.startTaskDetailsActivity;

public class TrashTasksActivity extends AppCompatActivity implements
        TasksNavigator {

    /**
     * @serial 目印
     */
    private final String TAG = "TrashTasksActivity";

    /**
     * @serial レイアウト
     */
    private DrawerLayout mDrawerLayout;

    /**
     * @serial トグル
     */
    private ActionBarDrawerToggle mToggle;

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

        // レイアウト設定
        TrashTasksActivityBinding mBinding = DataBindingUtil.setContentView(this, R.layout.trash_tasks_activity);

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
     * 表示モデル生成
     *
     * @param activity 画面
     * @return 表示モデル
     */
    public static TasksViewModel obtainViewModel(FragmentActivity activity) {
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
            ab.setTitle(R.string.trash);
            ab.setHomeAsUpIndicator(R.drawable.ic_menu);
            ab.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * ナビゲーション設定
     */
    private void setupNavigationDrawer() {
        mDrawerLayout = findViewById(R.id.drawer_layout);
        Toolbar mToolbar = findViewById(R.id.toolbar);
        if (mToggle == null) {
            mToggle = new ActionBarDrawerToggle(
                    this,
                    mDrawerLayout,
                    mToolbar,
                    R.string.navigation_drawer_open,
                    R.string.navigation_drawer_close);
        }
        mToggle.setDrawerIndicatorEnabled(true);
        mToggle.setToolbarNavigationClickListener(v -> mViewModel.changeSelection(UNSELECTED));
        mToggle.syncState();
        mDrawerLayout.addDrawerListener(mToggle);
        mDrawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                int id = mViewModel.getId();
                if (id != R.id.trash && TrashTasksActivity.class != getStartActivity(id)) {
                    Intent intent = getIntent();
                    intent.setClass(getApplicationContext(), getStartActivity(id));
                    startActivity(intent);
                    overridePendingTransition(R.animator.fade_out, R.animator.fade_in);
                    finish();
                }
            }
        });
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        NavigationView navigationView = findViewById(R.id.nav_view);
        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }
    }

    /**
     * コンテンツ設定
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
     * ナビゲーション表示
     */
    private void enableNavigationDrawer() {
        setupNavigationDrawer();
    }

    /**
     * ナビゲーション非表示
     */
    private void disableNavigationDrawer() {
        mDrawerLayout = findViewById(R.id.drawer_layout);
        if (mToggle != null) {
            mToggle.setDrawerIndicatorEnabled(false);
            mToggle.syncState();
            mDrawerLayout.removeDrawerListener(mToggle);
        }
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 選択状態の確認
        switch (mViewModel.getCondition()) {
            case ONE_ITEM:
            case ITEMS: {
                getMenuInflater().inflate(R.menu.trash_some, menu);
                break;
            }
            case ONE_ITEM_ONE_SELECTD: {
                getMenuInflater().inflate(R.menu.trash_selected_all_one, menu);
                break;
            }
            case ITEMS_ONE_SELECTED: {
                getMenuInflater().inflate(R.menu.trash_selected_one, menu);
                break;
            }
            case ITEMS_SOME_SELECTED: {
                getMenuInflater().inflate(R.menu.trash_selected_some, menu);
                break;
            }
            case ITEMS_ALL_SELECTED: {
                getMenuInflater().inflate(R.menu.trash_selected_all_some, menu);
                break;
            }
            default: {
                getMenuInflater().inflate(R.menu.trash_empty, menu);
                break;
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // 識別子ごとの処理
        switch (item.getItemId()) {
            case android.R.id.home:
                // Open the navigation drawer when the home icon is selected from the toolbar.
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;

            case R.id.menu_info: {
                mViewModel.openDetails();
                break;
            }
            case R.id.menu_empty: {
                mViewModel.deleteTrashItems();
                break;
            }
            case R.id.menu_untrash: {
                mViewModel.untrashItems();
                break;
            }
            case R.id.menu_selected_all: {
                mViewModel.selectAll();
                break;
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
                    Asset asset;
                    if (bundle != null) {
                        asset = bundle.getParcelable(EXTRA_ASSET);
                        // レジューム後に動作させる
                        mViewModel.setRedo(MODIFY, 0, asset);
                    }
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
     * タイトル更新
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
        // ナビゲーションの設定
        disableNavigationDrawer();
        // アクションバーの設定
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);
            ab.setDisplayHomeAsUpEnabled(true);
        }
        // メニュー更新
        invalidateOptionsMenu();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onNormalMode() {
        // ナビゲーションの設定
        enableNavigationDrawer();
        // アクションバーの設定
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setHomeAsUpIndicator(R.drawable.ic_menu);
            ab.setDisplayHomeAsUpEnabled(true);
        }
        // メニューを設定
        invalidateOptionsMenu();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onTasksChanged(List<Asset> tasks) {
        updateViewFragment(tasks);
        // メニュー更新
        invalidateOptionsMenu();
    }

    /**
     * 詳細表示
     *
     * @param asset 項目
     */
    public void openTaskDetails(Asset asset) {
        startTaskDetailsActivity(this, this, mModel.getTask(asset.uuid));
    }
}
