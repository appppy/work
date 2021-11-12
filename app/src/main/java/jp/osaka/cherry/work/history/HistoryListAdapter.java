package jp.osaka.cherry.work.history;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import jp.osaka.cherry.work.R;
import jp.osaka.cherry.work.data.History;
import jp.osaka.cherry.work.databinding.HistoryItemBinding;
import jp.osaka.cherry.work.util.view.recyclerview.adapter.RecyclerArrayAdapter;

/**
 * 履歴一覧アダプタ
 */
class HistoryListAdapter extends RecyclerArrayAdapter<History, HistoryListAdapter.ViewHolder> {

    /**
     * @serial 履歴項目ユーザーアクションリスナ
     */
    private final HistoryItemUserActionsListener mListener;

    /**
     * コンストラクタ
     *
     * @param listener リスナ
     * @param collection 一覧
     */
    HistoryListAdapter(HistoryItemUserActionsListener listener, List<History> collection) {
        super(collection);
        mListener = listener;
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        HistoryItemBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()), R.layout.history_item, parent, false);
        return new ViewHolder(binding);
    }

    /**
     * 表示保持接続
     *
     * @param holder 表示保持
     * @param position 位置
     */
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        HistoryItemBinding binding = holder.getBinding();

        final History history = getList().get(position);

        binding.setHistory(history);

        binding.setListener(mListener);

        binding.executePendingBindings();

        // 短押しの設定
        holder.getBinding().cardView.setOnClickListener(new View.OnClickListener() {
            /**
             * {@inheritDoc}
             */
            @Override
            public void onClick(View view) {
                mListener.onHistoryClicked(view, history);
            }
        });
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
     * 表示維持
     */
    static class ViewHolder extends RecyclerView.ViewHolder {

        /**
         * @serial バインディング
         */
        private final HistoryItemBinding mBinding;

        /**
         * コンストラクタ
         *
         * @param binding バインディング
         */
        ViewHolder(HistoryItemBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }

        /**
         * バインディング取得
         *
         * @return バインディング
         */
        public HistoryItemBinding getBinding() {
            return mBinding;
        }
    }

}

