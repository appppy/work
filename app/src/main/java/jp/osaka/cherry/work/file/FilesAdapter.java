package jp.osaka.cherry.work.file;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import jp.osaka.cherry.work.R;
import jp.osaka.cherry.work.data.File;
import jp.osaka.cherry.work.databinding.FileItemBinding;
import jp.osaka.cherry.work.util.view.recyclerview.adapter.RecyclerArrayAdapter;

/**
 * ファイルアダプタ
 */
class FilesAdapter extends RecyclerArrayAdapter<File, FilesAdapter.ViewHolder> {

    /**
     * @serial リスナ
     */
    private final FileItemUserActionsListener mListener;

    /**
     * コンストラクタ
     *
     * @param listener リスナ
     * @param list 一覧
     */
    FilesAdapter(FileItemUserActionsListener listener, List<File> list) {
        super(list);
        mListener = listener;
    }

    /**
     * 表示保持生成
     *
     * @param parent 親
     * @param viewType 表示種類
     * @return 表示
     */
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        FileItemBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()), R.layout.file_item, parent, false);
        return new ViewHolder(binding);
    }

    /**
     *　表示保持接続
     *
     * @param holder 表示保持
     * @param position 位置
     */
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        FileItemBinding binding = holder.getBinding();

        final File file = getList().get(position);

        binding.setFile(file);

        binding.setListener(mListener);

        binding.executePendingBindings();

        // 短押しの設定
        holder.getBinding().cardView.setOnClickListener(new View.OnClickListener() {
            /**
             * {@inheritDoc}
             */
            @Override
            public void onClick(View view) {
                mListener.onFileClicked(view, file);
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
     * 表示保持
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        private final FileItemBinding mBinding;

        ViewHolder(FileItemBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }

        public FileItemBinding getBinding() {
            return mBinding;
        }
    }

}

