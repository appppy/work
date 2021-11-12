package jp.osaka.cherry.work.file;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jp.osaka.cherry.work.R;
import jp.osaka.cherry.work.data.File;
import jp.osaka.cherry.work.databinding.FragmentBinding;
import jp.osaka.cherry.work.util.view.DividerItemDecoration;

import static com.google.common.base.Preconditions.checkNotNull;
import static jp.osaka.cherry.work.Config.LOG_I;
import static jp.osaka.cherry.work.constants.EXTRA.EXTRA_FILE;

/**
 * ファイルフラグメント
 */
public class FilesFragment extends Fragment {

    /**
     * @serial 目印
     */
    private final String TAG = "FilesFragment";

    /**
     * @serial リスナ
     */
    private FileItemUserActionsListener mListener;

    /**
     * @serial アダプタ
     */
    private FilesAdapter mAdapter;

    /**
     * インスタンス生成
     */
    public static FilesFragment newInstance(ArrayList<File> collection) {
        // フラグメントの生成
        FilesFragment fragment = new FilesFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(EXTRA_FILE, collection);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (LOG_I) {
            Log.i(TAG, "onCreate#enter");
        }

        // 再生成を抑止
        setRetainInstance(true);

        if (LOG_I) {
            Log.i(TAG, "onCreate#leave");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        mListener = (FileItemUserActionsListener) getActivity();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment, container, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (LOG_I) {
            Log.i(TAG, "onActivityCreated#enter");
        }

        requireView();
        FragmentBinding binding = DataBindingUtil.bind(requireView());

        ArrayList<File> files = requireArguments().getParcelableArrayList(EXTRA_FILE);

        // アダプタの設定
        checkNotNull(files);
        mAdapter = new FilesAdapter(mListener, toList(files));

        // レイアウトの設定
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        checkNotNull(binding);
        binding.collection.addItemDecoration(new DividerItemDecoration(requireActivity()));
        binding.collection.setLayoutManager(layoutManager);
        binding.collection.setAdapter(mAdapter);
        binding.collection.setItemAnimator(new DefaultItemAnimator());
        binding.collection.setVerticalScrollBarEnabled(false);

        if (LOG_I) {
            Log.i(TAG, "onActivityCreated#leave");
        }
    }

    /**
     * 削除
     *
     * @param item 項目
     */
    public void remove(@NonNull File item) {
        int location;
        if (mAdapter != null) {
            location = mAdapter.getList().indexOf(item);
            if (location >= 0) {
                mAdapter.remove(location);
            }
        }
    }

    /**
     * ファイル一覧変換
     *
     * @param files ファイル一覧
     * @return ファイル一覧
     */
    private List<File> toList(List<File> files) {
        for (File file : files) {
            file.title = file.name;
            file.description = DateFormat.getDateTimeInstance().format(new Date(file.date));
        }
        return files;
    }
}