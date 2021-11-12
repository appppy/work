package jp.osaka.cherry.work.tasks.binding;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import jp.osaka.cherry.work.R;
import jp.osaka.cherry.work.data.Asset;
import jp.osaka.cherry.work.databinding.TaskItemBinding;
import jp.osaka.cherry.work.tasks.TaskItemUserActionsListener;
import jp.osaka.cherry.work.util.view.recyclerview.adapter.RecyclerArrayAdapter;

import static jp.osaka.cherry.work.util.helper.AssetHelper.isMultiSelected;
import static jp.osaka.cherry.work.util.helper.AssetHelper.isSelected;

/**
 * アダプタ
 */
public class TasksAdapter extends RecyclerArrayAdapter<Asset, TasksAdapter.ViewHolder> {

    /**
     * @serial ロック
     */
    private final Object lock = new Object();

    /**
     * @serial コンテキスト
     */
    private final Context mContext;

    /**
     * @serial リスナ
     */
    private final TaskItemUserActionsListener mListener;

    /**
     * @serial タッチヘルパ
     */
    private ItemTouchHelper mItemTouchHelper;

    /**
     * @serial タッチ描画保持
     */
    private final Collection<View> touchViewHolder = new ArrayList<>();

    /**
     * @serial 選択
     */
    private boolean isSelection = true;

    /**
     * コンテキスト
     *
     * @param context コンテキスト
     * @param tasks 一覧
     * @param listener リスナ
     */
    TasksAdapter(Context context, List<Asset> tasks, TaskItemUserActionsListener listener) {
        super(tasks);
        mContext = context;
        mListener = listener;
    }

    /**
     * 項目タッチヘルパ
     *
     * @param helper ヘルパ
     */
    void setItemTouchHelper(ItemTouchHelper helper) {
        mItemTouchHelper = helper;
    }

    /**
     * 選択有効
     */
    void enableSelection() {
        isSelection = true;
    }

    /**
     * 選択無効
     */
    void disableSelection() {
        isSelection = false;
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TaskItemBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()), R.layout.task_item, parent, false);
        return new ViewHolder(binding);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        TaskItemBinding binding = holder.getBinding();

        final Asset task = getList().get(position);

        binding.setAsset(task);

        binding.setListener(mListener);

        binding.executePendingBindings();

        try {
            // タッチの設定
            holder.getBinding().cardView.setOnTouchListener((view, motionEvent) -> {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    touchViewHolder.clear();
                    touchViewHolder.add(view);
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if (isSelected(getList())) {
                        for (View holdview : touchViewHolder) {
                            if (holdview.equals(view)) {
                                select(holder, task);
                            }
                        }
                    } else {
                        for (View holdview : touchViewHolder) {
                            if (holdview.equals(view)) {
                                mListener.onTaskClicked(view, task);
                            }
                        }
                    }
                    touchViewHolder.clear();
                }
                return false;
            });
            // 長押しの設定
            binding.cardView.setOnLongClickListener(new View.OnLongClickListener() {
                /**
                 * {@inheritDoc}
                 */
                @Override
                public boolean onLongClick(View v) {
                    select(holder, task);
                    touchViewHolder.clear();
                    return true;
                }
            });

            // 背景の設定
            setBackground(binding.cardView, task.selected);

            // 項目の選択状態の取得
            if (task.selected) {

                // アイコンの設定
                binding.icon.setImageResource(R.drawable.ic_check_circle_black_24dp);

            } else {

                // アイコンの設定
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
                        holder.getBinding().icon.setImageResource(R.drawable.ic_lens_completed_24dp);
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
            }

            // Trendingの設定
            switch (task.priority) {
                case HIGH: {
                    binding.buttonTrending.setImageResource(R.drawable.ic_trending_up_black_24dp);
                    break;
                }
                case LOW: {
                    binding.buttonTrending.setImageResource(R.drawable.ic_trending_down_black_24dp);
                    break;
                }
                default: {
                    binding.buttonTrending.setImageResource(R.drawable.ic_trending_flat_black_24dp);
                    break;
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
    public long getItemId(int position) {
        return position;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getItemCount() {
        return getList().size();
    }

    /**
     * 選択
     *
     * @param holder 保持
     * @param task 項目
     */
    private void select(ViewHolder holder, Asset task) {
        // 選択機能がない場合は処理をスキップ
        if(!isSelection) return;

        if (mItemTouchHelper == null) {
            return;
        }

        TaskItemBinding binding = holder.getBinding();

        // 選択状態を変更
        task.selected = !task.selected;

        // 複数選択の場合、
        if (isMultiSelected(getList())) {

            // 背景の設定
            setBackground(binding.cardView, task.selected);

            // 項目の選択状態の取得
            if (task.selected) {

                // アイコンの設定
                binding.icon.setImageResource(R.drawable.ic_check_circle_black_24dp);

            } else {

                // アイコンの設定
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
            }

        } else {
            if (task.selected) {

                // ドラッグを開始する
                mItemTouchHelper.startDrag(holder);

            } else {

                // 選択状態を非表示
                setBackground(binding.cardView, false);

                // アイコンの設定
                switch (task.progressState) {
                    case NOT_START: {
                        holder.getBinding().icon.setImageResource(R.drawable.ic_do_not_disturb_on_black_24dp);
                        break;
                    }
                    case INPROGRESS: {
                        holder.getBinding().icon.setImageResource(R.drawable.ic_play_circle_filled_black_24dp);
                        break;
                    }
                    case COMPLETED: {
                        holder.getBinding().icon.setImageResource(R.drawable.ic_lens_completed_24dp);
                        break;
                    }
                    case WAITING: {
                        holder.getBinding().icon.setImageResource(R.drawable.ic_pause_circle_filled_black_24dp);
                        break;
                    }
                    case POSTPONEMENT: {
                        holder.getBinding().icon.setImageResource(R.drawable.ic_cancel_black_24dp);
                        break;
                    }
                    default: {
                        holder.getBinding().icon.setImageResource(R.drawable.ic_lens_black_24dp);
                        break;
                    }
                }
            }
        }

        // 長押し
        mListener.onTaskLongClicked(holder.itemView, task);
    }

    /**
     * 移動
     *
     * @param from 移動元
     * @param to 移動先
     */
    public void move(int from, int to) {
        synchronized (lock) {
            if (from < to) {
                for (int i = from; i < to; i++) {
                    Collections.swap(getList(), i, i + 1);
                }
            } else {
                for (int i = from; i > to; i--) {
                    Collections.swap(getList(), i, i - 1);
                }
            }
        }
        notifyItemMoved(from, to);
    }

    /**
     * 表示保持
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        /**
         * @serial バインディング
         */
        private final TaskItemBinding mBinding;

        /**
         * コンテキスト
         *
         * @param binding バインディング
         */
        ViewHolder(TaskItemBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }

        /**
         * バインディング取得
         *
         * @return バインディング
         */
        public TaskItemBinding getBinding() {
            return mBinding;
        }
    }

    /**
     * 一覧取得
     *
     * @return 一覧
     */
    public List<Asset> getTasks() {
        return getList();
    }

    /**
     * 背景設定
     *
     * @param view 表示
     * @param selected 選択
     */
    void setBackground(View view, boolean selected) {
        if (selected) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                view.setBackground(getSelectedBackgroundDrawable(mContext));
            } else {
                view.setBackgroundColor(getSelectedBackgroundColor(mContext));
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                view.setBackground(getBackgroundDrawable(mContext));
            } else {
                view.setBackgroundColor(getBackgroundColor(mContext));
            }
        }
    }

    /**
     * 背景表示取得
     *
     * @param context コンテキスト
     * @return 背景表示
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private Drawable getBackgroundDrawable(Context context) {
        return context.getResources().getDrawable(R.drawable.ripple_background, context.getTheme());
    }

    /**
     * 背景表示取得
     *
     * @param context コンテキスト
     * @return 背景表示
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private Drawable getSelectedBackgroundDrawable(Context context) {
        return context.getResources().getDrawable(R.drawable.ripple_selected_background, context.getTheme());
    }

    /**
     * 選択背景色取得
     *
     * @param context コンテキスト
     * @return 色
     */
    private int getSelectedBackgroundColor(Context context) {
        return ContextCompat.getColor(context, R.color.grey_300);
    }

    /**
     * 背景色取得
     *
     * @param context コンテキスト
     * @return 色示
     */
    private int getBackgroundColor(Context context) {
        return ContextCompat.getColor(context, R.color.grey_100);
    }
}

