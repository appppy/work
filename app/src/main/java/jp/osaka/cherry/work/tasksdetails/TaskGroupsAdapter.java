package jp.osaka.cherry.work.tasksdetails;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import jp.osaka.cherry.work.R;
import jp.osaka.cherry.work.data.Asset;
import jp.osaka.cherry.work.databinding.HeaderItemBinding;
import jp.osaka.cherry.work.databinding.TaskItemBinding;
import jp.osaka.cherry.work.util.view.recyclerview.adapter.RecyclerArrayAdapter;

/**
 * タスクグループアダプタ
 */
public class TaskGroupsAdapter extends RecyclerArrayAdapter<Asset, TaskGroupsAdapter.ViewHolder> {

    /**
     * リスナ
     */
    private final TaskGroupsUserActionsListener mListener;

    /**
     * コンストラクタ
     *
     * @param tasks 一覧
     * @param listener リスナ
     */
    TaskGroupsAdapter(List<Asset> tasks, TaskGroupsUserActionsListener listener) {
        super(tasks);
        mListener = listener;
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case 1: {
                HeaderItemBinding binding = DataBindingUtil
                        .inflate(LayoutInflater.from(parent.getContext()), R.layout.header_item, parent, false);
                return new HeaderViewHolder(binding);
            }
            case 2: {
                TaskItemBinding binding = DataBindingUtil
                        .inflate(LayoutInflater.from(parent.getContext()), R.layout.task_item, parent, false);
                return new ViewHolder(binding);
            }
        }
        return new ViewHolder(parent);
    }

    /**
     * 表示種類取得
     *
     * @param position 位置
     * @return 表示種類
     */
    public int getItemViewType(int position) {
        return getTasks().get(position).getCategory();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case 1:
                onBindHeaderItemViewHolder(holder, position);
                break;
            case 2:
                onBindTaskItemViewHolder(holder, position);
                break;
        }
    }

    /**
     * ヘッダ表示保持接続
     *
     * @param holder 保持
     * @param position 位置
     */
    private void onBindHeaderItemViewHolder(final ViewHolder holder, int position) {
        HeaderItemBinding binding = holder.getHeaderItemBinding();

        final Asset task = getList().get(position);

        binding.setAsset(task);

        binding.executePendingBindings();
    }

    /**
     * 項目表示保持接続
     *
     * @param holder 保持
     * @param position 位置
     */
    private void onBindTaskItemViewHolder(final ViewHolder holder, int position) {
        TaskItemBinding binding = holder.getBinding();

        final Asset task = getList().get(position);

        binding.setAsset(task);

        binding.executePendingBindings();

        try {
            holder.getBinding().cardView.setOnClickListener(v -> mListener.onTaskClicked(v, task));
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

            binding.buttonPopup.setVisibility(View.GONE);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getItemCount() {
        return getList().size();
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
         * コンストラクタ
         *
         * @param view 表示
         */
        ViewHolder(View view) {
            super(view);
            mBinding = null;
        }

        /**
         * コンストラクタ
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

        /**
         * ヘッダバインディング取得
         *
         * @return ヘッダバインディング
         */
        public HeaderItemBinding getHeaderItemBinding() {
            return null;
        }
    }

    /**
     * ヘッダ表示保持
     */
    static class HeaderViewHolder extends ViewHolder {
        /**
         * @serial バインディング
         */
        private final HeaderItemBinding mBinding;

        /**
         * コンストラクタ
         *
         * @param binding バインディング
         */
        HeaderViewHolder(HeaderItemBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }

        /**
         * バインディング取得
         *
         * @return バインディング
         */
        public TaskItemBinding getBinding() {
            return null;
        }

        /**
         * ヘッダバインディング取得
         *
         * @return ヘッダバインディング
         */
        public HeaderItemBinding getHeaderItemBinding() {
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
}

