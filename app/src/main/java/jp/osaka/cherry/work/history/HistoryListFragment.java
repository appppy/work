package jp.osaka.cherry.work.history;


import android.content.Context;
import android.os.Bundle;
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
import java.util.Collections;
import java.util.Date;
import java.util.List;

import jp.osaka.cherry.work.R;
import jp.osaka.cherry.work.data.History;
import jp.osaka.cherry.work.databinding.FragmentBinding;
import jp.osaka.cherry.work.util.view.DividerItemDecoration;

import static com.google.common.base.Preconditions.checkNotNull;
import static jp.osaka.cherry.work.constants.EXTRA.EXTRA_HISTORY;

/**
 * 履歴一覧表示
 */
public class HistoryListFragment extends Fragment {

    /**
     * 履歴項目ユーザーアクションリスナ
     */
    private HistoryItemUserActionsListener mListener;

    /**
     * 履歴一覧表示
     *
     * @param history 履歴一覧
     * @return 履歴一覧表示
     */
    public static HistoryListFragment newInstance(ArrayList<History> history) {
        // フラグメントの生成
        HistoryListFragment fragment = new HistoryListFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(EXTRA_HISTORY, history);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 再生成を抑止
        setRetainInstance(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (HistoryItemUserActionsListener) getActivity();
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
        requireView();
        FragmentBinding binding = DataBindingUtil.bind(requireView());

        ArrayList<History> historylist = requireArguments().getParcelableArrayList(EXTRA_HISTORY);

        // アダプタの設定
        checkNotNull(historylist);
        HistoryListAdapter mAdapter = new HistoryListAdapter(mListener, toList(historylist));

        // レイアウトの設定
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        checkNotNull(binding);
        binding.collection.addItemDecoration(new DividerItemDecoration(requireActivity()));
        binding.collection.setLayoutManager(layoutManager);
        binding.collection.setAdapter(mAdapter);
        binding.collection.setItemAnimator(new DefaultItemAnimator());
        binding.collection.setVerticalScrollBarEnabled(false);
    }

    /**
     * 一覧変換
     *
     * @param histories 履歴一覧
     * @return 履歴一覧
     */
    private List<History> toList(List<History> histories) {
        for (History history : histories) {
            history.name = DateFormat.getDateInstance().format(new Date(history.date)) + " " + history.title + " items";
            history.description = DateFormat.getDateTimeInstance().format(new Date(history.date));
        }
        Collections.reverse(histories);
        return histories;
    }
}