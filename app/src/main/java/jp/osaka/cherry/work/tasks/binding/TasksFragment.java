package jp.osaka.cherry.work.tasks.binding;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import jp.osaka.cherry.work.R;
import jp.osaka.cherry.work.ViewModelFactory;
import jp.osaka.cherry.work.data.Asset;
import jp.osaka.cherry.work.databinding.TaskItemBinding;
import jp.osaka.cherry.work.databinding.TasksFragmentBinding;
import jp.osaka.cherry.work.tasks.TaskNavigator;
import jp.osaka.cherry.work.tasks.TasksViewModel;
import jp.osaka.cherry.work.util.timer.SimpleTimer;
import jp.osaka.cherry.work.util.timer.TimerListener;
import jp.osaka.cherry.work.util.view.DividerItemDecoration;

import static jp.osaka.cherry.work.Config.LOG_I;
import static jp.osaka.cherry.work.constants.EXTRA.EXTRA_ASSETS;
import static jp.osaka.cherry.work.constants.EXTRA.EXTRA_DATA_LOADING;
import static jp.osaka.cherry.work.constants.TIMEOUT.FLOATING_ACTION_BUTTON_HIDE;
import static jp.osaka.cherry.work.tasks.binding.TasksFragment.MODE_FAB.MODE_FAB_NEW_TASK;
import static jp.osaka.cherry.work.tasks.binding.TasksFragment.MODE_FAB.MODE_FAB_NONE;
import static jp.osaka.cherry.work.util.helper.AssetHelper.isMultiSelected;
import static jp.osaka.cherry.work.util.helper.AssetHelper.isSelected;
import static jp.osaka.cherry.work.util.timer.TimerHelper.createTimer;
import static jp.osaka.cherry.work.util.timer.TimerHelper.startTimer;

/**
 * フラグメント
 */
public class TasksFragment extends Fragment implements
        TimerListener,
        TaskNavigator {

    /**
     * @serial 目印
     */
    private final String TAG = "TasksFragment";

    /**
     * @serial 表示モデル
     */
    private TasksViewModel mViewModel;

    /**
     * @serial バインディング
     */
    private TasksFragmentBinding mBinding;

    /**
     * @serial アダプタ
     */
    private TasksAdapter mAdapter;

    /**
     * @serial タイマ
     */
    private SimpleTimer mTimer;

    /**
     * @serial ハンドラ
     */
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    /**
     * FAB
     */
    public enum MODE_FAB {
        MODE_FAB_NONE,
        MODE_FAB_NEW_TASK,
        MODE_FAB_EDIT_TASK
    }

    /**
     * @serial モード
     */
    private MODE_FAB mMode = MODE_FAB_NONE;

    /**
     * コンストラクタ
     */
    @SuppressLint("ValidFragment")
    private TasksFragment() {
        // Requires empty public constructor
    }

    /**
     * インスタンス生成
     *
     * @param assets 一覧
     * @param dataloading データバインディング
     * @return 表示
     */
    public static TasksFragment newInstance(ArrayList<Asset> assets, boolean dataloading) {
        TasksFragment fragment = new TasksFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(EXTRA_ASSETS, assets);
        args.putBoolean(EXTRA_DATA_LOADING, dataloading);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTimer = createTimer(mTimer, FLOATING_ACTION_BUTTON_HIDE, this);
        setRetainInstance(true);
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (LOG_I) {
            Log.i(TAG, "onCreateView#enter");
        }

        mBinding = TasksFragmentBinding.inflate(inflater, container, false);

        mViewModel = obtainViewModel(requireActivity());

        mBinding.setViewmodel(mViewModel);

        mViewModel.setListNavigater(this);

        mViewModel.setBinding(mBinding);

        setHasOptionsMenu(true);

        if (LOG_I) {
            Log.i(TAG, "onCreateView#leave");
        }
        return mBinding.getRoot();
    }

    /**
     * 表示モデル
     *
     * @param activity 画面
     * @return 表示モデル
     */
    private TasksViewModel obtainViewModel(FragmentActivity activity) {
        // Use a Factory to inject dependencies into the ViewModel
        ViewModelFactory factory = ViewModelFactory.getInstance(activity.getApplication());
        return ViewModelProviders.of(activity, factory).get(TasksViewModel.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (LOG_I) {
            Log.i(TAG, "onActivityCreated#enter");
        }

        boolean dataloading = requireArguments().getBoolean(EXTRA_DATA_LOADING);
        if (!dataloading) {
            setupFab(MODE_FAB_NEW_TASK);
        }

        setupListAdapter();

        setupImages();

        if (LOG_I) {
            Log.i(TAG, "onActivityCreated#leave");
        }
    }

    /**
     * FAB設定
     *
     * @param mode モード
     */
    public void setupFab(MODE_FAB mode) {
        mMode = mode;
        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        if (fab != null) {
            switch (mode) {
                case MODE_FAB_NEW_TASK: {
                    fab.setImageResource(R.drawable.ic_add_white_36dp);
                    fab.setOnClickListener(view -> mViewModel.editNewDetails(view, getActivity()));
                    fab.hide();
                    fab.show();
                    break;
                }
                case MODE_FAB_EDIT_TASK: {
                    fab.setImageResource(R.drawable.ic_create_white_36dp);
                    fab.setOnClickListener(view -> mViewModel.editDetails(view, getActivity()));
                    fab.hide();
                    fab.show();
                    break;
                }
                default: {
                    fab.hide();
                    break;
                }
            }
        }
    }

    /**
     * 画像設定
     */
    private void setupImages() {
        ImageView image = mBinding.emptyView;
        switch (mViewModel.getId()) {
            case R.id.archive: {
                image.setImageDrawable(ContextCompat.getDrawable(requireActivity(),R.drawable.ic_archive_black_100dp));
                break;
            }
            case R.id.trash: {
                image.setImageDrawable(ContextCompat.getDrawable(requireActivity(),R.drawable.ic_delete_black_100dp));
                break;
            }
            case R.id.next_week: {
                image.setImageDrawable(ContextCompat.getDrawable(requireActivity(),R.drawable.ic_next_week_black_100dp));
                break;
            }
            case R.id.this_week: {
                image.setImageDrawable(ContextCompat.getDrawable(requireActivity(),R.drawable.ic_work_black_100dp));
                break;
            }
            case R.id.weekend: {
                image.setImageDrawable(ContextCompat.getDrawable(requireActivity(),R.drawable.ic_weekend_black_100dp));
                break;
            }
            case R.id.not_start: {
                image.setImageDrawable(ContextCompat.getDrawable(requireActivity(),R.drawable.ic_do_not_disturb_on_black_100dp));
                break;
            }
            case R.id.inprogress: {
                image.setImageDrawable(ContextCompat.getDrawable(requireActivity(),R.drawable.ic_play_circle_filled_black_100dp));
                break;
            }
            case R.id.completed: {
                image.setImageDrawable(ContextCompat.getDrawable(requireActivity(),R.drawable.ic_lens_completed_100dp));
                break;
            }
            case R.id.recent: {
                image.setImageDrawable(ContextCompat.getDrawable(requireActivity(),R.drawable.ic_access_time_black_100dp));
                break;
            }
            default: {
                image.setImageDrawable(ContextCompat.getDrawable(requireActivity(),R.drawable.ic_circle_work_black_100dp));
                break;
            }
        }
    }

    /**
     * アダプタ設定
     */
    private void setupListAdapter() {
        RecyclerView recyclerView;
        recyclerView = mBinding.tasksList;
        recyclerView.addItemDecoration(new DividerItemDecoration(requireActivity()));
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setVerticalScrollBarEnabled(false);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            /**
             * {@inheritDoc}
             */
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                // タイマ開始
                startTimer(mTimer);
                FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
                if (fab != null) {
                    fab.hide();
                }
            }
        });
        ItemTouchHelper.Callback callback1 = new ItemTouchHelper.Callback() {
            /**
             * {@inheritDoc}
             */
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                // 複数選択の場合、ドラッグ無効、スワイプ無効
                if (isMultiSelected(mAdapter.getList())) {
                    return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG, 0) |
                            makeFlag(ItemTouchHelper.ACTION_STATE_SWIPE, 0);
                }
                // 単数選択の場合、
                if (isSelected(mAdapter.getList())) {
                    return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG, ItemTouchHelper.DOWN | ItemTouchHelper.UP | ItemTouchHelper.START | ItemTouchHelper.END) |
                            makeFlag(ItemTouchHelper.ACTION_STATE_SWIPE, 0);
                }
                return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG, ItemTouchHelper.DOWN | ItemTouchHelper.UP) |
                        makeFlag(ItemTouchHelper.ACTION_STATE_SWIPE, ItemTouchHelper.START | ItemTouchHelper.END);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                try {
                    int from = viewHolder.getAdapterPosition();
                    int to = target.getAdapterPosition();
                    if (from >= 0 && to >= 0) {
                        // 選択状態を解除
                        Asset asset = mAdapter.getList().get(from);
                        asset.selected = false;
                        mAdapter.move(from, to);
                        mViewModel.onMoveChanged(mAdapter.getList());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
                try {
                    switch (actionState) {
                        case ItemTouchHelper.ACTION_STATE_IDLE:
                        case ItemTouchHelper.ACTION_STATE_SWIPE: {
                            break;
                        }
                        default: {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                float elevation = 8 * getResources().getDisplayMetrics().density;
                                viewHolder.itemView.setElevation(elevation);
                            }

                            TasksAdapter.ViewHolder holder = (TasksAdapter.ViewHolder) viewHolder;
                            TaskItemBinding binding = holder.getBinding();

                            // 背景の設定
                            mAdapter.setBackground(holder.getBinding().cardView, true);

                            // アイコンの設定
                            binding.icon.setImageResource(R.drawable.ic_check_circle_black_24dp);

                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                super.onSelectedChanged(viewHolder, actionState);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        viewHolder.itemView.setElevation(0);
                    }

                    TasksAdapter.ViewHolder holder = (TasksAdapter.ViewHolder) viewHolder;

                    // 項目の選択状態の取得
                    TaskItemBinding binding = holder.getBinding();
                    int position = viewHolder.getAdapterPosition();
                    if (position >= 0) {
                        Asset task = mAdapter.getList().get(position);
                        // 項目が選択状態でなければ、選択表示を解除
                        if (!task.selected) {

                            // 背景の設定
                            mAdapter.setBackground(binding.cardView, false);

                            // アイコンの設定
                            switch (task.content) {
                                case ARCHIVE: {
                                    binding.icon.setImageResource(R.drawable.ic_pause_circle_filled_black_24dp);
                                    break;
                                }
                                case TRASH: {
                                    binding.icon.setImageResource(R.drawable.ic_do_not_disturb_on_black_24dp);
                                    break;
                                }
                                case INBOX:
                                default: {
                                    switch (task.progressState) {
                                        case NOT_START: {
                                            binding.icon.setImageResource(R.drawable.ic_do_not_disturb_on_black_24dp);
                                            break;
                                        }
                                        case INPROGRESS: {
                                            binding.icon.setImageResource(R.drawable.ic_play_circle_filled_black_24dp);
                                            break;
                                        }
                                        case COMPLETED: {
                                            binding.icon.setImageResource(R.drawable.ic_lens_completed_24dp);
                                            break;
                                        }
                                        case WAITING: {
                                            binding.icon.setImageResource(R.drawable.ic_pause_circle_filled_black_24dp);
                                            break;
                                        }
                                        case POSTPONEMENT: {
                                            binding.icon.setImageResource(R.drawable.ic_cancel_black_24dp);
                                            break;
                                        }
                                        default: {
                                            binding.icon.setImageResource(R.drawable.ic_lens_black_24dp);
                                            break;
                                        }
                                    }
                                    break;
                                }
                            }
                            binding.icon.setVisibility(View.VISIBLE);
                            mViewModel.onTaskLongClicked(recyclerView, task);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean isLongPressDragEnabled() {
                return false;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                try {
                    int position = viewHolder.getAdapterPosition();
                    if (position >= 0) {
                        mViewModel.onSwiped(position);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean isItemViewSwipeEnabled() {
                return true;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
                return 0.6f;
            }

        };

        ItemTouchHelper.Callback callback2 = new ItemTouchHelper.Callback() {

            /**
             * {@inheritDoc}
             */
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG, 0) |
                        makeFlag(ItemTouchHelper.ACTION_STATE_SWIPE, ItemTouchHelper.START | ItemTouchHelper.END);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                try {
                    int position = viewHolder.getAdapterPosition();
                    if (position >= 0) {
                        mViewModel.onSwiped(position);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean isItemViewSwipeEnabled() {
                return true;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
                return 0.6f;
            }
        };

        ArrayList<Asset> assets = requireArguments().getParcelableArrayList(EXTRA_ASSETS);
        mAdapter = new TasksAdapter(getContext(), assets, mViewModel);
        if (mViewModel.getId() == R.id.recent) {
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback2);
            itemTouchHelper.attachToRecyclerView(recyclerView);
            mAdapter.setItemTouchHelper(itemTouchHelper);
            mAdapter.disableSelection();
        } else {
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback1);
            itemTouchHelper.attachToRecyclerView(recyclerView);
            mAdapter.setItemTouchHelper(itemTouchHelper);
            mAdapter.enableSelection();
        }
        recyclerView.setAdapter(mAdapter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onTimer(final Object timer, final int count, final boolean inProgress) {
        mHandler.post(() -> {
            try {
                if (mMode != MODE_FAB_NONE) {
                    FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
                    if (fab != null) {
                        fab.show();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 選択
     *
     * @param view 表示
     * @param asset 項目
     */
    public void onSelectedMore(final View view, final Asset asset) {
        // We need to post a Runnable to show the file_more to make sure that the PopupMenu is
        // correctly positioned. The reason being that the view may change position before the
        // PopupMenu is shown.
        view.post(() -> {
            switch (asset.content) {
                case ARCHIVE: {
                    showPopupMenu(R.menu.archive_selected_all_one, view, asset);
                    break;
                }
                case TRASH: {
                    showPopupMenu(R.menu.trash_selected_all_one, view, asset);
                    break;
                }
                default: {
                    showPopupMenu(R.menu.tasks_selected_all_one, view, asset);
                    break;
                }
            }

        });
    }


    /**
     * 項目のポップアップメニュー選択 優先度
     *
     * @param view           項目表示
     * @param asset          項目
     */
    public void onSelectedPriority(final View view, final Asset asset) {
        // We need to post a Runnable to show the file_more to make sure that the PopupMenu is
        // correctly positioned. The reason being that the view may change position before the
        // PopupMenu is shown.
        view.post(() -> showPopupMenu(R.menu.piriority, view, asset));
    }

    /**
     * 進捗選択
     *
     * @param view 表示
     * @param asset 項目
     */
    public void onSelectedProgress(final View view, final Asset asset) {
        // We need to post a Runnable to show the file_more to make sure that the PopupMenu is
        // correctly positioned. The reason being that the view may change position before the
        // PopupMenu is shown.
        view.post(() -> showPopupMenu(R.menu.progress, view, asset));
    }

    /**
     * ポップアップ表示
     *
     * @param id 識別子
     * @param view 表示
     * @param asset 項目
     */
    // BEGIN_INCLUDE(show_popup)
    private void showPopupMenu(int id, final View view, final Asset asset) {
        // Create a PopupMenu, giving it the clicked view for an anchor
        PopupMenu popup = new PopupMenu(requireContext(), view);

        // Inflate our menu resource into the PopupMenu's Menu
        popup.getMenuInflater().inflate(id, popup.getMenu());


        // Set a listener so we are notified if a menu item is clicked
        popup.setOnMenuItemClickListener(menuItem -> {
            mViewModel.addSelectedItem(asset);
            requireActivity().onOptionsItemSelected(menuItem);
            return false;
        });

        // Finally show the PopupMenu
        popup.show();
    }
    // END_INCLUDE(show_popup)

}
