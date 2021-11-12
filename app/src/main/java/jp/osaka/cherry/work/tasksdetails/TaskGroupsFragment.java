package jp.osaka.cherry.work.tasksdetails;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jp.osaka.cherry.work.R;
import jp.osaka.cherry.work.constants.CONTENT;
import jp.osaka.cherry.work.data.Asset;
import jp.osaka.cherry.work.databinding.TaskGroupsFragmentBinding;
import jp.osaka.cherry.work.util.view.DividerItemDecoration;

import static jp.osaka.cherry.work.constants.EXTRA.EXTRA_ASSETS;

/**
 * タスクグループフラグメント
 */
public class TaskGroupsFragment extends Fragment {

    /**
     * @serial バインディング
     */
    private TaskGroupsFragmentBinding mBinding;

    /**
     * @serial リスナ
     */
    private TaskGroupsUserActionsListener mListener;

    /**
     * コンストラクタ
     */
    @SuppressLint("ValidFragment")
    private TaskGroupsFragment() {
        // Requires empty public constructor
    }

    /**
     * インスタンス生成
     *
     * @param assets 一覧
     * @return 表示
     */
    public static TaskGroupsFragment newInstance(ArrayList<Asset> assets) {
        TaskGroupsFragment fragment = new TaskGroupsFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(EXTRA_ASSETS, assets);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        mListener = (TaskGroupsUserActionsListener) getActivity();
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = TaskGroupsFragmentBinding.inflate(inflater, container, false);

        setHasOptionsMenu(true);

        return mBinding.getRoot();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setupListAdapter();
    }

    /**
     * 一覧アダプタ設定
     */
    private void setupListAdapter() {
        RecyclerView recyclerView;
        recyclerView = mBinding.tasksList;
        recyclerView.addItemDecoration(new DividerItemDecoration(requireActivity()));
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setVerticalScrollBarEnabled(false);
        ArrayList<Asset> src = requireArguments().getParcelableArrayList(EXTRA_ASSETS);
        List<Asset> dst = new ArrayList<>();
        List<Asset> tasks = toTasksList(Objects.requireNonNull(src));
        if(!tasks.isEmpty()) {
            Asset header = Asset.createInstance();
            header.displayName = getString(R.string.task);
            header.mCategory = 1;
            dst.add(header);
            dst.addAll(tasks);
        }
        List<Asset> archives = toArchiveList(src);
        if(!archives.isEmpty()) {
            Asset header = Asset.createInstance();
            header.displayName = getString(R.string.archive);
            header.mCategory = 1;
            dst.add(header);
            dst.addAll(archives);
        }
        List<Asset> trashs = toTrashList(src);
        if(!trashs.isEmpty()) {
            Asset header = Asset.createInstance();
            header.displayName = getString(R.string.trash);
            header.mCategory = 1;
            dst.add(header);
            dst.addAll(trashs);
        }
        TaskGroupsAdapter mAdapter = new TaskGroupsAdapter(dst, mListener);
        recyclerView.setAdapter(mAdapter);
    }

    /**
     * 一覧変換
     *
     * @param collection 一覧
     * @return 一覧
     */
    private List<Asset> toTasksList(List<Asset> collection) {
        List<Asset> result = new ArrayList<>();
        for (Asset item : collection) {
            switch (item.content) {
                case ARCHIVE:
                case TRASH: {
                    /* 含めない */
                    break;
                }
                default: {
                    result.add(item);
                    break;
                }
            }
        }
        return result;
    }

    /**
     * ゴミ箱一覧取得
     *
     * @param collection 一覧
     * @return ゴミ箱一覧
     */
    private List<Asset> toTrashList(List<Asset> collection) {
        List<Asset> result = new ArrayList<>();
        for (Asset item : collection) {
            if (item.content == CONTENT.TRASH) {
                result.add(item);
            }
        }
        return result;
    }

    /**
     * アーカイブ一覧取得
     *
     * @param collection 一覧
     * @return アーカイブ一覧
     */
    private List<Asset> toArchiveList(List<Asset> collection) {
        List<Asset> result = new ArrayList<>();
        for (Asset item : collection) {
            if (item.content == CONTENT.ARCHIVE) {
                result.add(item);
            }
        }
        return result;
    }
}
