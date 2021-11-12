/*
 * Copyright 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.osaka.cherry.work.tasks;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.AndroidViewModel;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jp.osaka.cherry.work.R;
import jp.osaka.cherry.work.SingleLiveEvent;
import jp.osaka.cherry.work.SnackbarMessage;
import jp.osaka.cherry.work.addedittask.TaskEditDialog;
import jp.osaka.cherry.work.constants.CONDITION;
import jp.osaka.cherry.work.constants.CONVERTER;
import jp.osaka.cherry.work.constants.PRIORITY;
import jp.osaka.cherry.work.constants.PROGRESS;
import jp.osaka.cherry.work.constants.SCHEDULE;
import jp.osaka.cherry.work.constants.SELECTION;
import jp.osaka.cherry.work.data.Asset;
import jp.osaka.cherry.work.data.History;
import jp.osaka.cherry.work.databinding.TasksFragmentBinding;
import jp.osaka.cherry.work.tasks.binding.TasksAdapter;
import jp.osaka.cherry.work.util.controller.command.Action;
import jp.osaka.cherry.work.util.helper.ActivityHelper;
import jp.osaka.cherry.work.util.helper.AssetHelper;
import jp.osaka.cherry.work.util.helper.FileHelper;

import static androidx.core.app.ActivityCompat.startActivityForResult;
import static jp.osaka.cherry.work.Config.LOG_D;
import static jp.osaka.cherry.work.Config.LOG_I;
import static jp.osaka.cherry.work.constants.ActivityTransition.REQUEST_SYNC_FILE;
import static jp.osaka.cherry.work.constants.CONTENT.ARCHIVE;
import static jp.osaka.cherry.work.constants.CONTENT.INBOX;
import static jp.osaka.cherry.work.constants.CONTENT.TRASH;
import static jp.osaka.cherry.work.constants.EXTRA.EXTRA_CONTENT_ID;
import static jp.osaka.cherry.work.constants.EXTRA.EXTRA_FILE_NAME;
import static jp.osaka.cherry.work.constants.EXTRA.EXTRA_SORT;
import static jp.osaka.cherry.work.constants.INVALID.INVALID_LONG_VALUE;
import static jp.osaka.cherry.work.constants.INVALID.INVALID_STRING_VALUE;
import static jp.osaka.cherry.work.constants.SELECTION.MULTI_SELECTED;
import static jp.osaka.cherry.work.constants.SELECTION.SELECTED;
import static jp.osaka.cherry.work.constants.SELECTION.SELECTED_ALL;
import static jp.osaka.cherry.work.constants.SELECTION.SELECTING;
import static jp.osaka.cherry.work.constants.SELECTION.UNSELECTED;
import static jp.osaka.cherry.work.constants.SORT.BY_DATE_CREATED;
import static jp.osaka.cherry.work.constants.SORT.BY_DATE_MODIFIED;
import static jp.osaka.cherry.work.constants.SORT.BY_NAME;
import static jp.osaka.cherry.work.tasks.view.RecentTasksActivity.toSortByCollection;
import static jp.osaka.cherry.work.util.controller.command.Action.ACTION.CHANGE;
import static jp.osaka.cherry.work.util.controller.command.Action.ACTION.CREATE;
import static jp.osaka.cherry.work.util.controller.command.Action.ACTION.INSERT;
import static jp.osaka.cherry.work.util.controller.command.Action.ACTION.MODIFY;
import static jp.osaka.cherry.work.util.controller.command.Action.ACTION.REMOVE;
import static jp.osaka.cherry.work.util.helper.ActivityHelper.startFolderActivity;
import static jp.osaka.cherry.work.util.helper.ActivityHelper.startHistoryListActivity;
import static jp.osaka.cherry.work.util.helper.ActivityHelper.startSearchTasksActivity;
import static jp.osaka.cherry.work.util.helper.ActivityHelper.startTasksDetailsActivity;
import static jp.osaka.cherry.work.util.helper.AssetHelper.isModified;
import static jp.osaka.cherry.work.util.helper.AssetHelper.toSortByDateCreatedCollection;
import static jp.osaka.cherry.work.util.helper.AssetHelper.toSortByDateModifiedCollection;
import static jp.osaka.cherry.work.util.helper.AssetHelper.toSortByNameCollection;
import static jp.osaka.cherry.work.util.helper.FileHelper.loadFile;

/**
 * 表示モデル
 */
public class TasksViewModel extends AndroidViewModel implements
        TaskItemUserActionsListener,
        TasksModel.Callbacks {

    /**
     * @serial 目印
     */
    private final String TAG = "TasksViewModel";

    /**
     * @serial データ読み込み
     */
    public final ObservableBoolean dataLoading = new ObservableBoolean(true);

    /**
     * @serial 空状態
     */
    public final ObservableBoolean empty = new ObservableBoolean(false);

    /**
     * @serial 一覧表示状態
     */
    private final ObservableBoolean tasksAddViewVisible = new ObservableBoolean();

    /**
     * @serial スナックバー
     */
    private final SnackbarMessage mSnackbarText = new SnackbarMessage();

    /**
     * @serial 詳細イベント
     */
    private final SingleLiveEvent<Asset> mOpenTaskEvent = new SingleLiveEvent<>();

    /**
     * @serial タイトル設定イベント
     */
    private final SingleLiveEvent<String> mSetTitleEvent = new SingleLiveEvent<>();

    /**
     * @serial 新規作成イベント
     */
    private final SingleLiveEvent<Void> mNewTaskEvent = new SingleLiveEvent<>();

    /**
     * @serial モデル
     */
    private TasksModel mTasksModel = null;

    /**
     * @serial 選択一覧
     */
    private final ArrayList<Asset> mSelectedTasks = new ArrayList<>();

    /**
     * @serial UNDO一覧
     */
    private final List<Action<Asset>> mUndoList = new ArrayList<>();

    /**
     * @serial REDO一覧
     */
    private final List<Action<Asset>> mRedoList = new ArrayList<>();

    /**
     * @serial ハンドラ
     */
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    /**
     * @serial 一覧ナビゲーター
     */
    private TaskNavigator mListNavigater = null;

    /**
     * @serial 項目ナビゲーター
     */
    private TasksNavigator mNavigater = null;

    /**
     * @serial 識別子
     */
    private int mId;

    /**
     * @serial レイアウト
     */
    @SuppressLint("StaticFieldLeak")
    private CoordinatorLayout mLayout;

    /**
     * @serial プリファレンス
     */
    private SharedPreferences mPref;

    /**
     * @serial 選択
     */
    private SELECTION mSelection = UNSELECTED;

    /**
     * @serial URI
     */
    private Uri mUri;

    /**
     * @serial ファイル有無
     */
    private boolean isFile = false;

    /**
     * @serial バインディング
     */
    private TasksFragmentBinding mBinding;

    /**
     * @serial 履歴有無
     */
    private boolean isHistory = false;

    /**
     * @serial 履歴
     */
    private History mHistory;

    /**
     * @serial 検索有無
     */
    private boolean isSearching = false;

    /**
     * コンストラクタ
     *
     * @param context コンテキスト
     */
    public TasksViewModel(Application context) {
        super(context);
    }

    /**
     * イベント取得
     *
     * @return オープンタスクイベント
     */
    public SingleLiveEvent<Asset> getOpenTaskEvent() {
        return mOpenTaskEvent;
    }

    /**
     * イベント取得
     *
     * @return 新規作成イベント
     */
    public SingleLiveEvent<Void> getNewTaskEvent() {
        return mNewTaskEvent;
    }

    /**
     * イベント取得
     *
     * @return タイトル設定イベント
     */
    public SingleLiveEvent<String> getSetTitleEvent() {
        return mSetTitleEvent;
    }

    /**
     * バインド
     *
     * @param model モデル
     */
    public void bind(TasksModel model) {
        mTasksModel = model;
        mTasksModel.setCallbacks(this);
    }

    /**
     * 一覧取得
     *
     * @return 一覧
     */
    private List<Asset> getList() {
        TasksAdapter adapter = (TasksAdapter) mBinding.tasksList.getAdapter();
        return Objects.requireNonNull(adapter).getList();
    }

    /**
     * 一覧取得
     *
     * @return 一覧
     */
    private List<Asset> getTasks() {
        return toList(mTasksModel.getTasks());
    }

    /**
     * 数取得
     *
     * @return 数
     */
    public int getSelectedItemsSize() {
        return mSelectedTasks.size();
    }

    /**
     * 選択項目追加
     *
     * @param aaset 項目
     */
    public void addSelectedItem(Asset aaset) {
        mSelectedTasks.clear();
        mSelectedTasks.add(aaset);
    }

    /**
     * 一覧ナビゲーター設定
     *
     * @param navigater 一覧ナビゲーター
     */
    public void setListNavigater(TaskNavigator navigater) {
        mListNavigater = navigater;
    }

    /**
     * ナビゲーター設定
     *
     * @param navigater ナビゲーター
     */
    public void setNavigater(TasksNavigator navigater) {
        mNavigater = navigater;
    }

    /**
     * レイアウト設定
     *
     * @param layout レイアウト
     */
    public void setLayout(CoordinatorLayout layout) {
        mLayout = layout;
    }

    /**
     * プリファレンス設定
     *
     * @param pref プリファレンス
     */
    public void setPreferences(SharedPreferences pref) {
        mPref = pref;
        mId = mPref.getInt(EXTRA_CONTENT_ID, R.id.task);
    }

    /**
     * 検索設定
     *
     * @param enable 検索状態
     */
    public void setSearching(boolean enable) {
        isSearching = enable;
    }

    /**
     * 検索状態取得
     *
     * @return 検索状態
     */
    private boolean isSearching() {
        return isSearching;
    }

    /**
     * 識別子設定
     *
     * @param id 識別子
     */
    public void setId(int id) {
        mId = id;
        mPref.edit().putInt(EXTRA_CONTENT_ID, id).apply();
    }

    /**
     * 識別子取得
     *
     * @return 識別子
     */
    public int getId() {
        return mId;
    }

    /**
     * バインディング取得
     *
     * @param binding バインディング
     */
    public void setBinding(TasksFragmentBinding binding) {
        mBinding = binding;
    }

    /**
     * 選択変更
     *
     * @param selection 選択
     * @return 選択変更有無
     */
    public boolean changeSelection(SELECTION selection) {
        boolean result = false;
        // 状態の確認
        if (mSelection != selection) {
            // 状態の変更
            mSelection = selection;
            mHandler.post(() -> {
                try {
                    // 選択状態の取得
                    switch (mSelection) {
                        case UNSELECTED: {
                            if (mNavigater != null) {
                                // データ更新
                                for (Asset task : getList()) {
                                    task.selected = false;
                                }
                                // 表示モデルの更新
                                //onTasksChanged();
                                update();
                                mNavigater.onNormalMode();
                            }
                            break;
                        }
                        case SELECTED_ALL: {
                            if (mNavigater != null) {
                                // データ更新
                                for (Asset task : getList()) {
                                    task.selected = true;
                                }
                                makeSelectedTasksList(getList());
                                // 表示モデルの更新
                                update();
                                //onTasksChanged();
                                // テーマの設定
                                mNavigater.onSelectMode(getSelectedItemsSize());
                            }
                            break;
                        }
                        case MULTI_SELECTED:
                        case SELECTED: {
                            if (mNavigater != null) {
                                // ツールバーの設定
                                updateTitle(mId, mSelection, getList().size(), getSelectedItemsSize());
                                // テーマの設定
                                mNavigater.onSelectMode(getSelectedItemsSize());
                            }
                            break;
                        }
                        default: {
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            result = true;
        }
        return result;
    }

    /**
     * コンディション取得
     *
     * @return コンディション
     */
    public CONDITION getCondition() {
        CONDITION result;

        if (empty.get()) {
            result = CONDITION.EMPTY;
        } else {
            // 選択状態の確認
            switch (mSelection) {
                case SELECTED_ALL: {
                    if (getSelectedItemsSize() == 1) {
                        result = CONDITION.ONE_ITEM_ONE_SELECTD;
                    } else {
                        result = CONDITION.ITEMS_ALL_SELECTED;
                    }
                    break;
                }
                case MULTI_SELECTED:
                case SELECTING:
                case SELECTED: {
                    if (getSelectedItemsSize() == 1) {
                        result = CONDITION.ITEMS_ONE_SELECTED;
                    } else {
                        result = CONDITION.ITEMS_SOME_SELECTED;
                    }
                    break;
                }
                default: {
                    if (getTasks().size() == 1) {
                        result = CONDITION.ONE_ITEM;
                    } else {
                        result = CONDITION.ITEMS;
                    }
                    break;
                }
            }
        }
        return result;
    }

    /**
     * 選択設定
     * @param selection 選択
     * @return 選択設定有無
     */
    private boolean setSelection(SELECTION selection) {
        return changeSelection(selection);
    }

    /**
     * URI設定
     *
     * @param uri URI
     */
    public void setUri(Uri uri) {
        mUri = uri;
    }

    /**
     * ファイル設定
     */
    public void setFile() {
        isFile = true;
    }

    /**
     * 履歴設定
     *
     * @param history 履歴
     */
    public void setHistory(History history) {
        isHistory = true;
        mHistory = history;
    }

    /**
     * 戻る設定
     *
     * @param action 実行
     * @param position 位置
     * @param asset アセット
     */
    public void setRedo(Action.ACTION action, int position, Asset asset) {
        beginTransaction();
        mRedoList.add(new Action<>(action, position, asset));
        endTransaction();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onTaskClicked(View view, Asset task) {
        // タイムスタンプを保存
        task.timestamp = System.currentTimeMillis();
        mTasksModel.update(task);

        // 詳細画面の表示
        mOpenTaskEvent.setValue(task);
        mOpenTaskEvent.call();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onTaskLongClicked(View view, Asset task) {
        // 選択数の変更
        //mTasksModel.update(task);
        makeSelectedTasksList(getList());

        // 選択状態の確認
        int selectedItemSize = getSelectedItemsSize();
        if (getList().size() == selectedItemSize) {
            // 全選択
            if (!setSelection(SELECTED_ALL)) {
                // 遷移なしの場合
                // 選択数を変更
                updateTitle(mId, mSelection, getList().size(), getSelectedItemsSize());
            }
        } else if (selectedItemSize > 1) {
            // マルチ選択
            if (!setSelection(MULTI_SELECTED)) {
                // 遷移なしの場合
                // 選択数を変更
                updateTitle(mId, mSelection, getList().size(), getSelectedItemsSize());
            }
        } else if (selectedItemSize == 1) {
            // 選択
            if (!setSelection(SELECTED)) {
                // 遷移なしの場合
                // 選択数を変更
                updateTitle(mId, mSelection, getList().size(), getSelectedItemsSize());
            }
        } else {
            // 非選択
            setSelection(UNSELECTED);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onTaskProgressClicked(View view, Asset asset) {
        mListNavigater.onSelectedProgress(view, asset);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onTaskPriorityClicked(View view, Asset asset) {
        mListNavigater.onSelectedPriority(view, asset);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onTaskMoreClicked(View view, Asset asset) {
        switch (getCondition()) {
            case ONE_ITEM:
            case ITEMS:
                mListNavigater.onSelectedMore(view, asset);{
                break;
            }
            case EMPTY:
            case ONE_ITEM_ONE_SELECTD:
            case ITEMS_ONE_SELECTED:
            case ITEMS_SOME_SELECTED:
            case ITEMS_ALL_SELECTED:
            default: {
                /* skip */
                break;
            }
        }
    }

    /**
     * スワイプ
     *
     * @param position 位置
     */
    public void onSwiped(int position) {
        // バックアップ
        mTasksModel.backup();

        // データ設定
        Asset task = getItem(position);
        switch (getId()) {
            case R.id.archive:
            case R.id.trash: {
                task.content = INBOX;
                break;
            }
            case R.id.recent: {
                break;
            }
            case R.id.this_week: {
                if(isSearching()) {
                    task.content = ARCHIVE;
                    task.startDate = getNextWeek();
                    task.endDate = getNextWeek();
                }
                break;
            }
            case R.id.weekend: {
                if(isSearching()) {
                    task.content = ARCHIVE;
                } else {
                    task.startDate = getNextWeekEnd();
                    task.endDate = getNextWeekEnd();
                }
                break;
            }
            case R.id.next_week: {
                if(isSearching()) {
                    task.content = ARCHIVE;
                } else {
                    task.startDate = getThisWeek();
                    task.endDate = getThisWeek();
                }
                break;
            }
            case R.id.not_start: {
                if(isSearching()) {
                    task.content = ARCHIVE;
                } else {
                    task.progressState = PROGRESS.INPROGRESS;
                }
                break;
            }
            case R.id.inprogress: {
                if(isSearching()) {
                    task.content = ARCHIVE;
                } else {
                    task.progressState = PROGRESS.COMPLETED;
                }
                break;
            }
            case R.id.completed: {
                if(isSearching()) {
                    task.content = ARCHIVE;
                } else {
                    task.progressState = PROGRESS.NOT_START;
                }
                break;
            }
            default: {
                task.content = ARCHIVE;
                break;
            }
        }
        mTasksModel.update(task);

        // 表示モデルの更新
        insertTaskAddUndo(position, task);

        // スナックバーの生成
        String message;
        switch (getId()) {
            case R.id.archive: {
                message = getContext().getString(R.string.unarchived_item);
                break;
            }
            case R.id.trash: {
                message = getContext().getString(R.string.restored_item);
                break;
            }
            case R.id.this_week: {
                if(isSearching()) {
                    message = getContext().getString(R.string.moved_to_archive_item);
                } else {
                    message = getContext().getString(R.string.moved_to_weekend);
                }
                break;
            }
            case R.id.weekend: {
                if(isSearching()) {
                    message = getContext().getString(R.string.moved_to_archive_item);
                } else {
                    message = getContext().getString(R.string.moved_to_nextweek);
                }
                break;
            }
            case R.id.next_week: {
                if(isSearching()) {
                    message = getContext().getString(R.string.moved_to_archive_item);
                } else {
                    message = getContext().getString(R.string.moved_to_thisweek);
                }
                break;
            }
            case R.id.not_start: {
                if(isSearching()) {
                    message = getContext().getString(R.string.moved_to_archive_item);
                } else {
                    message = getContext().getString(R.string.moved_to_inprogress);
                }
                break;
            }
            case R.id.inprogress: {
                if(isSearching()) {
                    message = getContext().getString(R.string.moved_to_archive_item);
                } else {
                    message = getContext().getString(R.string.moved_to_completed);
                }
                break;
            }
            case R.id.completed: {
                if(isSearching()) {
                    message = getContext().getString(R.string.moved_to_archive_item);
                } else {
                    message = getContext().getString(R.string.moved_to_not_start);
                }
                break;
            }
            default: {
                message = getContext().getString(R.string.moved_to_archive_item);
                break;
            }
        }
        makeUndoSnackbar(mLayout, message);
    }

    /**
     * 移動変更
     *
     * @param tasks 一覧
     */
    public void onMoveChanged(List<Asset> tasks) {
        // モデル変更
        mTasksModel.moveChange(tasks);

        // 未選択
        setSelection(SELECTING);

        if (mNavigater != null) {
            updateTitle(mId, mSelection, getList().size(), getSelectedItemsSize());
            mNavigater.onSelectMode(getSelectedItemsSize());
        }
    }

    /**
     * 更新
     *
     * @param assets 一覧
     */
    public void update(ArrayList<Asset> assets) {
        dataLoading.set(false);
        mNavigater.onTasksChanged(assets);
        updateTitle(mId, mSelection, assets.size(), getSelectedItemsSize());
        empty.set(assets.isEmpty());
    }

    /**
     * 更新
     */
    private void update() {
        dataLoading.set(false);
        mNavigater.onTasksChanged(getTasks());
        // 一覧表示の更新
        TasksAdapter adapter = (TasksAdapter) mBinding.tasksList.getAdapter();
        Objects.requireNonNull(adapter).getTasks().clear();
        adapter.getTasks().addAll(getTasks());
        adapter.notifyDataSetChanged();
        // タイトルの更新
        updateTitle(mId, mSelection, getTasks().size(), getSelectedItemsSize());
        empty.set(getTasks().isEmpty());
    }

    /**
     * タイトル更新
     *
     * @param id 識別子
     * @param selection 選択
     * @param size サイズ
     * @param selectedSize 選択サイズ
     */
    private void updateTitle(int id, SELECTION selection, int size, int selectedSize) {
        switch (selection) {
            case SELECTED:
            case SELECTED_ALL:
            case SELECTING:
            case MULTI_SELECTED: {
                mSetTitleEvent.setValue(String.valueOf(selectedSize));
                mSetTitleEvent.call();
                break;
            }
            case UNSELECTED: {
                StringBuilder sb = new StringBuilder();
                switch (id) {
                    case R.id.recent: {
                        sb.append(getContext().getString(R.string.recent));
                        break;
                    }
                    case R.id.trash: {
                        sb.append(getContext().getString(R.string.trash));
                        break;
                    }
                    case R.id.archive: {
                        sb.append(getContext().getString(R.string.archive));
                        break;
                    }
                    case R.id.next_week: {
                        sb.append(getContext().getString(R.string.next_week));
                        break;
                    }
                    case R.id.this_week: {
                        sb.append(getContext().getString(R.string.this_week));
                        break;
                    }
                    case R.id.weekend: {
                        sb.append(getContext().getString(R.string.weekend));
                        break;
                    }
                    case R.id.not_start: {
                        sb.append(getContext().getString(R.string.progress_not_start));
                        break;
                    }
                    case R.id.inprogress: {
                        sb.append(getContext().getString(R.string.progress_inprogress));
                        break;
                    }
                    case R.id.completed: {
                        sb.append(getContext().getString(R.string.progress_completed));
                        break;
                    }
                    default: {
                        sb.append(getContext().getString(R.string.task));
                        break;
                    }
                }
                if (size != 0) {
                    sb.append("  ").append(size);
                }
                mSetTitleEvent.setValue(sb.toString());
                mSetTitleEvent.call();
                sb.delete(0, sb.length());
                break;
            }
        }
    }

    /**
     * スナックバー生成
     *
     * @param layout レイアウト
     * @param message メッセージ
     */
    private void makeUndoSnackbar(CoordinatorLayout layout, String message) {
        Snackbar.make(layout, message, Snackbar.LENGTH_LONG)
                .setAction(getContext().getString(R.string.undo), new View.OnClickListener() {
                    /**
                     * {@inheritDoc}
                     */
                    @Override
                    public void onClick(View v) {
                        try {

                            // モデル更新
                            mTasksModel.restore();

                            // 表示モデルの更新
                            undoTasks();

                            // メッセージ表示
                            makeSnackbar(R.string.succed_undo);


                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                })
                .show();
    }

    /**
     * スナックバー生成
     *
     * @param resid 識別子
     */
    private void makeSnackbar(int resid) {
        mSnackbarText.setValue(resid);
        mSnackbarText.call();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onUpdated(Object object, final List<Asset> assets) {
        mHandler.post(() -> {
            // 更新
            update();
            // FAB
            tasksAddViewVisible.set(mId == R.id.task);

            // 次の動作を指定
            for (Action<Asset> doAction : mRedoList) {
                if (doAction.action.equals(CREATE)) {
                    CreateTaskRunner runner = new CreateTaskRunner(doAction.object);
                    mHandler.post(runner);
                }
                if (doAction.action.equals(MODIFY)) {
                    List<Asset> assets1 = new ArrayList<>();
                    assets1.add(doAction.object);
                    switch (getId()) {
                        case R.id.archive: {
                            if (doAction.object.content != ARCHIVE) {
                                UnArchiveTasksRunner runner = new UnArchiveTasksRunner(assets1);
                                mHandler.post(runner);
                            } else {
                                Asset dest = mTasksModel.getTask(doAction.object.uuid);
                                if (!dest.equals(doAction.object)) {
                                    if (isModified(dest, doAction.object)) {
                                        if (toList(assets1).isEmpty()) {
                                            RemoveTaskRunner runner = new RemoveTaskRunner(doAction.object);
                                            mHandler.post(runner);
                                        } else {
                                            ChangeTaskRunner runner = new ChangeTaskRunner(doAction.object);
                                            mHandler.post(runner);
                                        }
                                    }
                                }
                            }
                            break;
                        }
                        case R.id.trash: {
                            if (doAction.object.content != TRASH) {
                                List<Asset> tasks = new ArrayList<>();
                                tasks.add(doAction.object);
                                UnTrashTasksRunner runner = new UnTrashTasksRunner(tasks);
                                mHandler.post(runner);
                            } else {
                                Asset dest = mTasksModel.getTask(doAction.object.uuid);
                                if (!dest.equals(doAction.object)) {
                                    if (isModified(dest, doAction.object)) {
                                        if (toList(assets1).isEmpty()) {
                                            RemoveTaskRunner runner = new RemoveTaskRunner(doAction.object);
                                            mHandler.post(runner);
                                        } else {
                                            ChangeTaskRunner runner = new ChangeTaskRunner(doAction.object);
                                            mHandler.post(runner);
                                        }
                                    }
                                }
                            }
                            break;
                        }
                        default: {
                            if (doAction.object.content == ARCHIVE) {
                                List<Asset> tasks = new ArrayList<>();
                                tasks.add(doAction.object);
                                ArchiveTasksRunner runner = new ArchiveTasksRunner(tasks);
                                mHandler.post(runner);
                            } else if (doAction.object.content == TRASH) {
                                List<Asset> tasks = new ArrayList<>();
                                tasks.add(doAction.object);
                                TrashTasksRunner runner = new TrashTasksRunner(tasks);
                                mHandler.post(runner);
                            } else {
                                Asset dest = mTasksModel.getTask(doAction.object.uuid);
                                if (!dest.equals(doAction.object)) {
                                    if (isModified(dest, doAction.object)) {
                                        if (toList(assets1).isEmpty()) {
                                            RemoveTaskRunner runner = new RemoveTaskRunner(doAction.object);
                                            mHandler.post(runner);
                                        } else {
                                            ChangeTaskRunner runner = new ChangeTaskRunner(doAction.object);
                                            mHandler.post(runner);
                                        }
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
            }
            finishTransaction();

            if (mUri != null) {
                SyncRunner runner = new SyncRunner(getContext(), mUri);
                mHandler.post(runner);
                mUri = null;
            }
            if (isFile) {
                String filename = mPref.getString(EXTRA_FILE_NAME, INVALID_STRING_VALUE);
                LoadFileRunner runner = new LoadFileRunner(filename);
                mHandler.post(runner);
                isFile = false;
            }
            if (isHistory) {
                LoadHistoryRunner runner = new LoadHistoryRunner(mHistory);
                mHandler.post(runner);
                isHistory = false;
            }

            setSelection(UNSELECTED);
        });
    }

    /**
     * 変換
     *
     * @param collection 一覧
     * @return 一覧
     */
    private static ArrayList<Asset> toEmptyList(Collection<Asset> collection) {
        ArrayList<Asset> result = new ArrayList<>();
        for (Asset asset : collection) {
            if (asset.content != TRASH) {
                result.add(asset);
            }
        }
        return result;
    }

    /**
     * ゴミ箱削除
     */
    public void deleteTrashItems() {
        setSelection(UNSELECTED);

        mTasksModel.backup();

        dataLoading.set(false);
        ArrayList<Asset> tasks = toEmptyList(mTasksModel.getTasks());
        mTasksModel.getTasks().clear();
        mTasksModel.getTasks().addAll(tasks);
        mTasksModel.update();
        mNavigater.onTasksChanged(getTasks());
        updateTitle(mId, mSelection, getTasks().size(), getSelectedItemsSize());
        empty.set(getTasks().isEmpty());

        // メッセージ保持
        String message = getContext().getString(R.string.empty_trash_is_done);
        makeUndoSnackbar(mLayout, message);
    }

    /**
     * ゴミ箱戻す
     */
    public void untrashItems() {
        // 選択した優先度の変更
        mHandler.post(new UnTrashTasksRunner(mSelectedTasks));
        // 選択状態の解除
        setSelection(UNSELECTED);
    }

    /**
     * 全選択
     */
    public void selectAll() {
        setSelection(SELECTED_ALL);
    }

    /**
     * 詳細表示
     */
    public void openDetails() {
        // 項目の取得
        Asset task = mTasksModel.getTask(mSelectedTasks.get(0).uuid);
        // 選択一覧の削除
        mSelectedTasks.clear();
        // タイムスタンプを保存
        task.timestamp = System.currentTimeMillis();
        // モデル更新
        mTasksModel.update();
        // 編集画面の表示
        mOpenTaskEvent.setValue(task);
        mOpenTaskEvent.call();
    }

    /**
     * 円表示
     * @param activity 画面
     */
    public void openPieChart(Activity activity) {
        startTasksDetailsActivity(activity, mTasksModel.getTasks());
    }

    /**
     * 新規作成表示
     *
     * @param view 表示
     * @param activity 画面
     */
    public void editNewDetails(View view, Activity activity) {
        ActivityHelper.startNewTaskActivity(activity, activity, view, Asset.createInstance());
    }

    /**
     * 編集表示
     *
     * @param view 表示
     * @param activity 画面
     */
    public void editDetails(View view, Activity activity) {
        // 項目の取得
        Asset task = mTasksModel.getTask(mSelectedTasks.get(0).uuid);
        // 選択一覧の削除
        mSelectedTasks.clear();
        // タイムスタンプを保存
        task.timestamp = System.currentTimeMillis();
        // モデル更新
        mTasksModel.update();
        // 編集画面の表示
        ActivityHelper.startEditActivity(activity, activity, view, task);
    }

    /**
     * 編集表示
     *
     * @param activity 画面
     */
    public void editDetails(Activity activity) {
        // 項目の取得
        Asset task = mTasksModel.getTask(mSelectedTasks.get(0).uuid);
        // 選択一覧の削除
        mSelectedTasks.clear();
        // タイムスタンプを保存
        task.timestamp = System.currentTimeMillis();
        // モデル更新
        mTasksModel.update();
        // 編集画面の表示
        ActivityHelper.startEditActivity(activity, activity, task);
    }

    /**
     * 並べ替え
     */
    public void sortByName() {
        mPref.edit().putString(EXTRA_SORT, BY_NAME.name()).apply();
        List<Asset> collection = (ArrayList<Asset>) toSortByNameCollection(mTasksModel.getTasks());
        mTasksModel.update(collection);
        update();
        makeSnackbar(R.string.succed_by_name);
    }

    /**
     * 並べ替え
     */
    public void sortByDateModified() {
        mPref.edit().putString(EXTRA_SORT, BY_DATE_MODIFIED.name()).apply();
        List<Asset> collection = (ArrayList<Asset>) toSortByDateModifiedCollection(mTasksModel.getTasks());
        Collections.reverse(collection);
        mTasksModel.update(collection);
        update();
        makeSnackbar(R.string.succed_by_date_modified);
    }

    /**
     * 並べ替え
     */
    public void sortByDateCreated() {
        mPref.edit().putString(EXTRA_SORT, BY_DATE_CREATED.name()).apply();
        List<Asset> collection = (ArrayList<Asset>) toSortByDateCreatedCollection(mTasksModel.getTasks());
        Collections.reverse(collection);
        mTasksModel.update(collection);
        update();
        makeSnackbar(R.string.succed_by_date_created);
    }

    /**
     * スワイプ
     */
    public void swapVert() {
        mHandler.post(() -> {
            Collections.reverse(mTasksModel.getTasks());
            update();
            makeSnackbar(R.string.succed_swap_vert);
        });
    }

    /**
     * ファイル同期
     *
     * @param activity 画面
     */
    public void syncFile(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("text/*");
        startActivityForResult(activity, intent, REQUEST_SYNC_FILE.ordinal(), null);
    }

    /**
     * 検索
     *
     * @param activity 画面
     */
    public void search(Activity activity) {
        startSearchTasksActivity(activity);
    }

    /**
     * アーカイブ
     */
    public void archive() {
        // 選択したアーカイブの解除
        mHandler.post(new ArchiveTasksRunner(mSelectedTasks));
        // 選択状態の解除
        setSelection(UNSELECTED);
    }

    /**
     * ゴミ箱へ移動
     */
    public void trash() {
        // ゴミ箱に移動
        mHandler.post(new TrashTasksRunner(mSelectedTasks));
        // 選択状態の解除
        setSelection(UNSELECTED);
    }

    /**
     * 複製
     */
    public void copy() {
        // コピー
        mHandler.post(new CopyTasksRunner(mSelectedTasks));
        // 選択状態の解除
        setSelection(UNSELECTED);
    }

    /**
     * シェア
     */
    public void share() {
        try {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_TITLE, mSelectedTasks.get(0).displayName);
            intent.putExtra(Intent.EXTRA_SUBJECT, mSelectedTasks.get(0).displayName);
            intent.putExtra(Intent.EXTRA_TEXT, mSelectedTasks.get(0).note);
            intent.setType("text/plain");
            getContext().startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ファイル開く
     */
    public void openFile(Activity activity) {
        startFolderActivity(activity);
    }

    /**
     * ファイル保存
     *
     * @param activity 画面
     */
    public void saveFile(AppCompatActivity activity) {
        // ファイル名を取得
        String filename = mPref.getString(EXTRA_FILE_NAME, INVALID_STRING_VALUE);

        // 編集ダイアログの表示
        TaskEditDialog fragment = TaskEditDialog.newInstance(filename);
        fragment.show(activity.getSupportFragmentManager(), TaskEditDialog.class.getSimpleName());
    }

    /**
     * ファイル保存
     *
     * @param name 名前
     */
    public void savedFile(String name) {
        // ファイル保存
        mHandler.post(new SaveFileRunner(name));
    }

    /**
     * 優先度変更
     *
     * @param id 識別子
     */
    public void changePriority(int id) {
        // 選択した優先度の変更
        mHandler.post(new ChangePriorityTasksRunner(mSelectedTasks, PRIORITY.valueOf(CONVERTER.getPriority(id))));
        // 選択状態の解除
        setSelection(UNSELECTED);
    }

    /**
     * 進捗変更
     *
     * @param id 識別子
     */
    public void changeProgress(int id) {
        // 選択した進捗の変更
        mHandler.post(new ChangeProgressTasksRunner(mSelectedTasks, PROGRESS.valueOf(CONVERTER.getProgress(id))));
        // 選択状態の解除
        setSelection(UNSELECTED);
    }

    /**
     * 日程変更
     *
     * @param id 識別子
     */
    public void changeSchedule(int id) {
        // 選択した進捗の変更
        mHandler.post(new ChangeScheduleTasksRunner(mSelectedTasks, SCHEDULE.valueOf(CONVERTER.getSchedule(id))));
        // 選択状態の解除
        setSelection(UNSELECTED);
    }

    /**
     * 履歴表示
     *
     * @param activity 画面
     */
    public void openHistory(Activity activity) {
        startHistoryListActivity(activity);
    }

    /**
     * 履歴とじる
     */
    public void leaveHistory() {
        mTasksModel.setHistory(
                new History(System.currentTimeMillis(),
                        String.valueOf(mTasksModel.getTasks().size()),
                        AssetHelper.toJSONString(mTasksModel.getTasks())));
        makeSnackbar(R.string.suceed_leave_history);
    }

    /**
     * アーカイブ解除
     */
    public void unarchive() {
        // 選択したアーカイブの解除
        mHandler.post(new UnArchiveTasksRunner(mSelectedTasks));
        // 選択状態の解除
        setSelection(UNSELECTED);
    }

    /**
     * 戻す
     * @param task 項目
     */
    private void deleteTaskAddUndo(Asset task) {
        beginTransaction();
        int position = deleteTask(task);
        mUndoList.add(new Action<>(INSERT, position, mTasksModel.getBackupTask(task.uuid)));
        endTransaction();
    }

    /**
     * 戻す
     * @param tasks 一覧
     */
    private void deleteTaskAddUndo(List<Asset> tasks) {
        beginTransaction();
        for (Asset task : tasks) {
            int position = deleteTask(task);
            mUndoList.add(new Action<>(INSERT, position, mTasksModel.getBackupTask(task.uuid)));
        }
        endTransaction();
    }

    /**
     * 挿入
     * @param position 位置
     * @param task 項目
     */
    private void insertTaskAddUndo(int position, Asset task) {
        beginTransaction();
        deleteTask(task);
        mUndoList.add(new Action<>(INSERT, position, mTasksModel.getBackupTask(task.uuid)));
        endTransaction();
    }

    /**
     * 変更
     * @param task 項目
     */
    private void changeTaskAddUndo(Asset task) {
        beginTransaction();
        int position = changeTask(task);
        mUndoList.add(new Action<>(CHANGE, position, mTasksModel.getBackupTask(task.uuid)));
        endTransaction();
    }

    /**
     * 変更
     * @param tasks 一覧
     */
    private void changeTasksAddUndo(List<Asset> tasks) {
        beginTransaction();
        for (Asset task : tasks) {
            int position = changeTask(task);
            // 戻す処理に追加
            mUndoList.add(new Action<>(CHANGE, position, mTasksModel.getBackupTask(task.uuid)));
        }
        endTransaction();
    }

    /**
     * 変更
     *
     * @param task 項目
     */
    private void addTaskAddUndo(Asset task) {
        beginTransaction();
        addTask(task);
        mUndoList.add(new Action<>(REMOVE, 0, task));
        endTransaction();
    }

    /**
     * 変更
     *
     * @param task 項目
     * @return 位置
     */
    private int changeTask(Asset task) {
        // プログレスバーの更新
        dataLoading.set(false);
        // 一覧表示の更新
        TasksAdapter adapter = (TasksAdapter) mBinding.tasksList.getAdapter();
        List<Asset> assets = Objects.requireNonNull(adapter).getList();
        int position = assets.indexOf(task);
        if (position == -1) {
            for (Asset asset : assets) {
                if (asset.uuid.equals(task.uuid)) {
                    position = assets.indexOf(asset);
                    break;
                }
            }
        }
        adapter.set(position, task);
        // 空表示の更新
        empty.set(false);
        // タイトル変更
        updateTitle(mId, mSelection, getTasks().size(), getSelectedItemsSize());
        return position;
    }

    /**
     * 削除
     *
     * @param task 項目
     * @return 位置
     */
    private int deleteTask(Asset task) {
        // プログレスバーの更新
        dataLoading.set(false);
        // 一覧表示の更新
        TasksAdapter adapter = (TasksAdapter) mBinding.tasksList.getAdapter();
        List<Asset> assets = Objects.requireNonNull(adapter).getList();
        int position = adapter.getList().indexOf(task);
        if (position == -1) {
            for (Asset asset : assets) {
                if (asset.uuid.equals(task.uuid)) {
                    position = assets.indexOf(asset);
                    break;
                }
            }
        }
        adapter.remove(position);
        // 一覧表示の更新
        adapter = (TasksAdapter) mBinding.tasksList.getAdapter();
        adapter.getTasks().clear();
        adapter.getTasks().addAll(getTasks());
        adapter.notifyDataSetChanged();
        // 空表示の更新
        empty.set(getTasks().isEmpty());
        // タイトル変更
        updateTitle(mId, mSelection, getTasks().size(), getSelectedItemsSize());
        return position;
    }

    /**
     * 追加
     *
     * @param task 項目
     */
    private void addTask(Asset task) {
        // プログレスバーの更新
        dataLoading.set(false);
        // 一覧表示の更新
        TasksAdapter adapter = (TasksAdapter) mBinding.tasksList.getAdapter();
        Objects.requireNonNull(adapter).insert(0, task);
        mBinding.tasksList.scrollToPosition(0);
        // 空表示の更新
        empty.set(false);
        // タイトル変更
        updateTitle(mId, mSelection, getTasks().size(), getSelectedItemsSize());
    }

    /**
     * 挿入
     *
     * @param index 位置
     * @param task 項目
     */
    private void insertTask(int index, Asset task) {
        if (LOG_I) {
            Log.i(TAG, "insertTask(" + index + "," + task.displayName + ")#enter");
        }
        // プログレスバーの更新
        dataLoading.set(false);
        // 一覧表示の更新
        TasksAdapter adapter = (TasksAdapter) mBinding.tasksList.getAdapter();
        Objects.requireNonNull(adapter).insert(index, task);
        if (index == 0) {
            mBinding.tasksList.scrollToPosition(index);
        }
        // 空表示の更新
        empty.set(false);
        // タイトル変更
        updateTitle(mId, mSelection, getTasks().size(), getSelectedItemsSize());
        if (LOG_I) {
            Log.i(TAG, "insertTask(" + index + "," + task.displayName + ")#leave");
        }
    }

    /**
     * 戻す
     */
    private void undoTasks() {
        if(LOG_I) {
            Log.i("TasksViewMode","undoTasks()#enter");
        }
        //  mNavigater.onTasksChanged(getTasks());
        // 一覧表示の更新
        for (Action<Asset> undo : mUndoList) {
            Asset asset = undo.object;
            switch (undo.action) {
                case INSERT: {
                    if(LOG_D) {
                        Log.d("TasksViewMode","INSERT");
                    }
                    insertTask(undo.arg, asset);
                    break;
                }
                case CHANGE: {
                    if(LOG_D) {
                        Log.d("TasksViewMode","CHANGE");
                    }
                    changeTask(asset);
                    break;
                }
                case REMOVE: {
                    if(LOG_D) {
                        Log.d("TasksViewMode","REMOVE");
                    }
                    deleteTask(asset);
                    break;
                }
                default: {
                    break;
                }
            }
        }
        finishTransaction();

        // プログレスバーの更新
        dataLoading.set(false);
        // 一覧表示の更新
        TasksAdapter adapter = (TasksAdapter) mBinding.tasksList.getAdapter();
        Objects.requireNonNull(adapter).getTasks().clear();
        adapter.getTasks().addAll(getTasks());
        adapter.notifyDataSetChanged();
        // 空表示の更新
        empty.set(getTasks().isEmpty());
        // タイトル変更
        updateTitle(mId, mSelection, getTasks().size(), getSelectedItemsSize());
        if(LOG_I) {
            Log.i("TasksViewMode","undoTasks()#leave");
        }
    }

    /**
     * 変更
     */
    private class ChangeTaskRunner implements Runnable {

        /**
         * 項目
         */
        Asset mTask;

        /**
         * コンストラクタ
         *
         * @param task 項目
         */
        ChangeTaskRunner(Asset task) {
            mTask = task;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            try {
                // モデル更新
                mTasksModel.backup();
                mTasksModel.update(mTask);

                // 表示モデルの更新
                changeTaskAddUndo(mTask);

                // スナックバーの生成
                String message = getContext().getString(R.string.modified_item);
                makeUndoSnackbar(mLayout, message);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 削除
     */
    private class RemoveTaskRunner implements Runnable {

        /**
         * 項目
         */
        Asset mTask;

        /**
         * コンストラクタ
         *
         * @param task 項目
         */
        RemoveTaskRunner(Asset task) {
            mTask = task;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            try {
                // モデル更新
                mTasksModel.backup();
                mTasksModel.update(mTask);

                // 表示モデルの更新
                deleteTaskAddUndo(mTask);

                // スナックバーの生成
                String message = getContext().getString(R.string.modified_item);
                makeUndoSnackbar(mLayout, message);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * ゴミ箱を解除
     */
    private class UnTrashTasksRunner implements Runnable {

        /**
         * データ
         */
        List<Asset> mTasks;

        /**
         * コンストラクタ
         *
         * @param assets データ
         */
        UnTrashTasksRunner(List<Asset> assets) {
            mTasks = assets;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            try {
                // モデル変更
                mTasksModel.backup();
                mTasksModel.unTrashTasks(mTasks);
                mTasksModel.update();

                // 表示モデルの更新
                deleteTaskAddUndo(mTasks);

                // スナックバーの生成
                String message;
                int size = mUndoList.size();
                if (size == 1) {
                    message = getContext().getString(R.string.restored_item);
                    makeUndoSnackbar(mLayout, message);
                } else if (size > 1) {
                    message = getContext().getString(R.string.restored_some_items, size);
                    makeUndoSnackbar(mLayout, message);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 作成
     */
    private class CreateTaskRunner implements Runnable {

        /***
         * @serial 項目
         */
        Asset mData;

        /**
         * コンストラクタ
         *
         * @param data 項目
         */
        CreateTaskRunner(Asset data) {
            mData = data;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            try {

                // バックアップ
                mTasksModel.backup();

                // モデル更新
                mTasksModel.getTasks().add(0, mData);
                mTasksModel.update();

                // 表示モデルの更新
                addTaskAddUndo(mData);

                // スナックバー生成
                String message = getContext().getString(R.string.created_item);
                makeUndoSnackbar(mLayout, message);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 同期
     */
    private class SyncRunner implements Runnable {

        /**
         * @serial URI
         */
        Uri mUri;

        /**
         * @serial コンテキスト
         */
        Context mContext;

        /**
         * コンストラクタ
         *
         * @param context コンテキスト
         * @param uri URI
         */
        SyncRunner(Context context, Uri uri) {
            mContext = context;
            mUri = uri;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            try {
                Cursor cursor = getContext().getContentResolver().query(mUri, null, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();
                    //int docIdIndex = cursor.getColumnIndex("document_id");
                    int index = cursor.getColumnIndex("mime_type");
                    String type = cursor.getString(index);
                    if (type != null) {
                        downloadPicker(mContext, mUri);
                    }
                    cursor.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * ダウンロード表示
     *
     * @param context コンテキスト
     * @param uri URI
     */
    private void downloadPicker(Context context, Uri uri) {
        new FileAsyncTask(context, uri).execute();
    }

    /**
     * ファイル同期
     */
    private class FileAsyncTask {

        /**
         * @serial 実行状態
         */
        boolean isActive = false;

        /**
         * @serial コンテキスト
         */
        private final Context context;

        /**
         * @serial URI
         */
        private final Uri uri;

        /**
         * コンストラクタ
         *
         * @param context コンテキスト
         * @param uri URI
         */
        public FileAsyncTask(Context context, Uri uri) {
            this.context = context;
            this.uri = uri;
        }

        /**
         * 実行
         */
        private void execute() {
            final Handler handler = new Handler(Looper.getMainLooper());
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                // do something in background
                File cacheFile = new File(context.getExternalCacheDir(), "file_cache");
                try {
                    //ここで取り出し
                    InputStream is = context.getContentResolver().openInputStream(uri);

                    int data;
                    OutputStream os = new FileOutputStream(cacheFile);
                    byte[] readBytes;
                    if (is != null) {
                        readBytes = new byte[is.available()];
                        while ((data = is.read(readBytes)) != -1) {
                            os.write(readBytes, 0, data);
                        }
                    }
                    // onPreExecute
                    //
                    isActive = true;
                    //
                    // update UI
                    handler.post(() -> {
                        ArrayList<Asset> collection = loadFile(cacheFile);

                        if (mTasksModel.isEmpty()) {
                            // 空の場合

                            // 更新
                            mTasksModel.update(collection);
                            update();

                            // スナックバーの生成
                            String message = getContext().getString(R.string.sync_item);
                            Snackbar.make(mLayout, message, Snackbar.LENGTH_LONG)
                                    .setAction(getContext().getString(R.string.undo), new View.OnClickListener() {
                                        /**
                                         * {@inheritDoc}
                                         */
                                        @Override
                                        public void onClick(View v) {
                                            // データ設定
                                            mTasksModel.getTasks().clear();
                                            // 表示モデルの更新
                                            update();
                                            //モデル更新
                                            mTasksModel.update();
                                        }
                                    })
                                    .show();

                        } else {

                            // バックアップ
                            mTasksModel.backup();

                            // 履歴更新
                            mTasksModel.setHistory(
                                    new History(System.currentTimeMillis(),
                                            String.valueOf(mTasksModel.getBackupTasks().size()),
                                            AssetHelper.toJSONString(mTasksModel.getBackupTasks())));

                            // 一覧表示の更新
                            beginTransaction();

                            boolean isFound;
                            ArrayList<Asset> stack = new ArrayList<>();
                            for (Asset src : collection) {
                                isFound = false;
                                for (Asset dest : mTasksModel.getTasks()) {
                                    // 表示名が同じデータの確認
                                    if (dest.displayName.equals(src.displayName)) {
                                        isFound = true;
                                        switch (dest.content) {
                                            case ARCHIVE:
                                            case TRASH: {
                                                break;
                                            }
                                            default: {
                                                boolean isMod = false;
                                                // noteを更新
                                                if (!src.note.equals(INVALID_STRING_VALUE)) {
                                                    dest.note = src.note;
                                                    isMod = true;
                                                }
                                                if (isMod) {
                                                    dest.modifiedDate = src.modifiedDate;
                                                    int position = changeTask(dest);
                                                    mUndoList.add(new Action<>(CHANGE, position, dest));
                                                }
                                                break;
                                            }
                                        }
                                    }
                                }
                                if (!isFound) {
                                    stack.add(src);
                                }
                            }
                            endTransaction();

                            // 更新
                            ArrayList<Asset> tasks = new ArrayList<>(mTasksModel.getTasks());
                            tasks.addAll(stack);
                            mTasksModel.update(tasks);
                            update();

                            // スナックバーの生成
                            String message = getContext().getString(R.string.sync_item);
                            makeUndoSnackbar(mLayout, message);
                        }
                        isActive = false;
                    });
                } catch (Exception ex) {
                    // something went wrong
                    ex.printStackTrace();
                }
            });
        }
    }

    /**
     * アーカイブ
     */
    private class ArchiveTasksRunner implements Runnable {

        /**
         * @serial 一覧
         */
        List<Asset> mList;

        /**
         * コンストラクタ
         *
         * @param list 一覧
         */
        ArchiveTasksRunner(List<Asset> list) {
            mList = list;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            try {
                // バックアップ
                mTasksModel.backup();

                // データの変更
                for (Asset dest : mTasksModel.getTasks()) {
                    for (Asset src : mList) {
                        // 一致
                        if (dest.equal(src)) {
                            dest.content = ARCHIVE;
                        }
                    }
                }
                // モデル更新
                mTasksModel.update();

                // 表示モデルの更新
                deleteTaskAddUndo(mList);

                // スナックバーの生成
                String message;
                int size = mUndoList.size();
                if (size == 1) {
                    message = getContext().getString(R.string.moved_to_archive_item);
                    makeUndoSnackbar(mLayout, message);
                } else if (size > 1) {
                    message = getContext().getString(R.string.moved_to_archive_some_items, size);
                    makeUndoSnackbar(mLayout, message);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * ゴミ箱移動
     */
    private class TrashTasksRunner implements Runnable {

        /**
         * @serial 一覧
         */
        List<Asset> mList;

        /**
         * コンストラクタ
         *
         * @param list 一覧
         */
        TrashTasksRunner(List<Asset> list) {
            mList = list;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            try {

                // バックアップ
                mTasksModel.backup();

                // データの変更
                for (Asset data : mTasksModel.getTasks()) {
                    for (Asset src : mList) {
                        // 一致
                        if (data.equal(src)) {
                            data.content = TRASH;
                        }
                    }
                }

                // モデル更新
                mTasksModel.update();

                // 表示モデルの更新
                deleteTaskAddUndo(mList);

                // スナックバーの生成
                String message;
                int size = mUndoList.size();
                if (size == 1) {
                    message = getContext().getString(R.string.moved_to_trash_item);
                    makeUndoSnackbar(mLayout, message);
                } else if (size > 1) {
                    message = getContext().getString(R.string.moved_to_trash_some_items, size);
                    makeUndoSnackbar(mLayout, message);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 複製
     */
    private class CopyTasksRunner implements Runnable {

        /**
         * @serial 一覧
         */
        List<Asset> mList;

        /**
         * コンストラクタ
         *
         * @param list 一覧
         */
        CopyTasksRunner(List<Asset> list) {
            mList = list;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            try {
                // バックアップ
                mTasksModel.backup();

                // データを変更
                Asset dst = Asset.createInstance();
                Asset src = mTasksModel.getTask(mList.get(0).uuid);
                dst.setParams(src);
                mTasksModel.getTasks().add(0, dst);

                // モデル更新
                mTasksModel.update();

                // 表示モデルの更新
                addTaskAddUndo(dst);

                // 選択一覧の削除
                mSelectedTasks.clear();

                // スナックバー生成
                String message = getContext().getString(R.string.created_item);
                makeUndoSnackbar(mLayout, message);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * ファイル読み込み
     */
    private class LoadFileRunner implements Runnable {

        /**
         * @serial ファイル名
         */
        String filename;

        /**
         * コンストラクタ
         *
         * @param filename ファイル名
         */
        LoadFileRunner(String filename) {
            this.filename = filename;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            try {
                // 履歴更新
                mTasksModel.setHistory(new History(System.currentTimeMillis(),
                        String.valueOf(mTasksModel.getTasks().size()),
                        AssetHelper.toJSONString(mTasksModel.getTasks())));

                // データ設定
                ArrayList<Asset> tasks = FileHelper.loadFile(getContext(), filename + ".csv");

                // 更新
                mTasksModel.update(tasks);

                update();

                //onTasksChanged();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * ファイル保存
     */
    private class SaveFileRunner implements Runnable {

        /**
         * @serial ファイル名
         */
        String filename;

        /**
         * コンストラクタ
         *
         * @param filename ファイル名
         */
        SaveFileRunner(String filename) {
            this.filename = filename;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            try {
                // ファイル保存
                FileHelper.saveFile(getContext(), filename + ".csv", mTasksModel.getTasks());
                makeSnackbar(R.string.suceed_save_file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 優先度変更
     */
    private class ChangePriorityTasksRunner implements Runnable {

        /**
         * @serial 一覧
         */
        List<Asset> mList;

        /**
         * @serial 優先度
         */
        PRIORITY mPRIORITY;

        /**
         * コンストラクタ
         *
         * @param list 一覧
         * @param priority 優先度
         */
        ChangePriorityTasksRunner(List<Asset> list, PRIORITY priority) {
            mList = list;
            mPRIORITY = priority;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            try {
                // バックアップ
                mTasksModel.backup();

                // データの更新
                for (Asset item : mList) {
                    item.priority = mPRIORITY;
                }
                for (Asset task : mTasksModel.getTasks()) {
                    for (Asset item : mList) {
                        // 一致
                        if (task.uuid.equals(item.uuid)) {
                            task.priority = mPRIORITY;
                            break;
                        }
                    }
                }

                // モデル更新
                mTasksModel.update();

                // 表示モデルの更新
                changeTasksAddUndo(mList);

                // スナックバーの生成
                String message;
                int size = mUndoList.size();
                if (size == 1) {
                    message = getContext().getString(R.string.modified_item);
                    makeUndoSnackbar(mLayout, message);
                } else if (size > 1) {
                    message = getContext().getString(R.string.modified_items, size);
                    makeUndoSnackbar(mLayout, message);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 進捗度変更
     */
    private class ChangeProgressTasksRunner implements Runnable {

        /**
         * @serial 一覧
         */
        List<Asset> mList;

        /**
         * @serial 進捗
         */
        PROGRESS mPROGRESS;

        /**
         * コンストラクタ
         *
         * @param list 一覧
         * @param progress 進捗
         */
        ChangeProgressTasksRunner(List<Asset> list, PROGRESS progress) {
            mList = list;
            mPROGRESS = progress;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            try {
                // バックアップ
                mTasksModel.backup();

                // データの更新
                for (Asset item : mList) {
                    item.progressState = mPROGRESS;
                }
                for (Asset task : mTasksModel.getTasks()) {
                    for (Asset item : mList) {
                        // 一致
                        if (task.uuid.equals(item.uuid)) {
                            task.progressState = mPROGRESS;
                            break;
                        }
                    }
                }

                // モデル設定
                mTasksModel.update();

                // 表示モデルの更新
                switch (getId()) {
                    case R.id.not_start: {
                        if (mPROGRESS != PROGRESS.NOT_START && !isSearching()) {
                            deleteTaskAddUndo(mList);
                        } else {
                            changeTasksAddUndo(mList);
                        }
                        break;
                    }
                    case R.id.inprogress: {
                        if (mPROGRESS != PROGRESS.INPROGRESS && !isSearching()) {
                            deleteTaskAddUndo(mList);
                        } else {
                            changeTasksAddUndo(mList);
                        }
                        break;
                    }
                    case R.id.completed: {
                        if (mPROGRESS != PROGRESS.COMPLETED && !isSearching()) {
                            deleteTaskAddUndo(mList);
                        } else {
                            changeTasksAddUndo(mList);
                        }
                        break;
                    }
                    default: {
                        changeTasksAddUndo(mList);
                        break;
                    }
                }

                // スナックバーの生成
                String message;
                int size = mUndoList.size();
                if (size == 1) {
                    message = getContext().getString(R.string.modified_item);
                    makeUndoSnackbar(mLayout, message);
                } else if (size > 1) {
                    message = getContext().getString(R.string.modified_items, size);
                    makeUndoSnackbar(mLayout, message);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 日程変更
     */
    private class ChangeScheduleTasksRunner implements Runnable {

        /**
         * @serial 一覧
         */
        List<Asset> mList;

        /**
         * @serial 日程
         */
        SCHEDULE mSCHEDULE;

        /**
         * コンストラクタ
         *
         * @param list 一覧
         * @param schedule 日程
         */
        ChangeScheduleTasksRunner(List<Asset> list, SCHEDULE schedule) {
            mList = list;
            mSCHEDULE = schedule;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            try {
                // バックアップ
                mTasksModel.backup();

                // データの更新
                for (Asset item : mList) {
                    switch (mSCHEDULE) {
                        case THIS_WEEK: {
                            item.startDate = getNextWeek();
                            item.endDate = getNextWeek();
                            break;
                        }
                        case WEEK_END: {
                            item.startDate = getNextWeekEnd();
                            item.endDate = getNextWeekEnd();
                            break;
                        }
                        case NEXT_WEEK: {
                            item.startDate = getThisWeek();
                            item.endDate = getThisWeek();
                            break;
                        }
                    }
                }
                for (Asset task : mTasksModel.getTasks()) {
                    for (Asset item : mList) {
                        // 一致
                        if (task.uuid.equals(item.uuid)) {
                            task.startDate = item.startDate;
                            task.endDate = item.endDate;
                        }
                    }
                }

                // モデル設定
                mTasksModel.update();

                // 表示モデルの更新
                switch (getId()) {
                    case R.id.this_week: {
                        if(mSCHEDULE != SCHEDULE.THIS_WEEK && !isSearching()) {
                            deleteTaskAddUndo(mList);
                        } else {
                            changeTasksAddUndo(mList);
                        }
                        break;
                    }
                    case R.id.weekend: {
                        if(mSCHEDULE != SCHEDULE.WEEK_END && !isSearching()) {
                            deleteTaskAddUndo(mList);
                        } else {
                            changeTasksAddUndo(mList);
                        }
                        break;
                    }
                    case R.id.next_week: {
                        if(mSCHEDULE != SCHEDULE.NEXT_WEEK && !isSearching()) {
                            deleteTaskAddUndo(mList);
                        } else {
                            changeTasksAddUndo(mList);
                        }
                        break;
                    }
                    default: {
                        changeTasksAddUndo(mList);
                        break;
                    }
                }

                // スナックバーの生成
                String message;
                int size = mUndoList.size();
                if (size == 1) {
                    message = getContext().getString(R.string.modified_item);
                    makeUndoSnackbar(mLayout, message);
                } else if (size > 1) {
                    message = getContext().getString(R.string.modified_items, size);
                    makeUndoSnackbar(mLayout, message);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 履歴よみこみ
     */
    private class LoadHistoryRunner implements Runnable {

        /**
         * @serial 履歴
         */
        History mHistory;

        /**
         * コンストラクタ
         *
         * @param history 一覧
         */
        LoadHistoryRunner(History history) {
            mHistory = history;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            try {
                // データ設定
                ArrayList<Asset> tasks = AssetHelper.toAssets(mHistory.message);

                // 更新
                mTasksModel.update(tasks);
                update();//onTasksChanged();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * アーカイブ解除
     */
    private class UnArchiveTasksRunner implements Runnable {

        /**
         * @serial 一覧
         */
        List<Asset> mList;

        /**
         * コンストラクタ
         *
         * @param list 一覧
         */
        UnArchiveTasksRunner(List<Asset> list) {
            mList = list;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            try {
                // バックアップ
                mTasksModel.backup();

                // データの更新
                for (Asset task : mTasksModel.getTasks()) {
                    for (Asset item : mList) {
                        // 一致
                        if (task.uuid.equals(item.uuid)) {
                            task.content = INBOX;
                            break;
                        }
                    }
                }

                // モデル更新
                mTasksModel.update();

                // 表示モデルの更新
                deleteTaskAddUndo(mList);

                // スナックバーの生成
                String message;
                int size = mUndoList.size();
                if (size == 1) {
                    message = getContext().getString(R.string.unarchived_item);
                    makeUndoSnackbar(mLayout, message);
                } else if (size > 1) {
                    message = getContext().getString(R.string.unarchived_some_items, size);
                    makeUndoSnackbar(mLayout, message);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 変換
     *
     * @param collection 一覧
     * @return 一覧
     */
    private List<Asset> toList(List<Asset> collection) {
        List<Asset> result;
        switch (getId()) {
            case R.id.archive: {
                result = toArchiveList(collection);
                break;
            }
            case R.id.trash: {
                result = toTrashList(collection);
                break;
            }
            case R.id.weekend: {
                result = toWeekEndList(collection);
                break;
            }
            case R.id.this_week: {
                result = toThisWeekList(collection);
                break;
            }
            case R.id.next_week: {
                result = toNextWeekList(collection);
                break;
            }
            case R.id.not_start: {
                result = toNotStartList(collection);
                break;
            }
            case R.id.inprogress: {
                result = toInprogressList(collection);
                break;
            }
            case R.id.completed: {
                result = toCompletedList(collection);
                break;
            }
            case R.id.recent: {
                result = toRecentList(collection);
                break;
            }
            default:
            case R.id.task: {
                result = toTasksList(collection);
                break;
            }

        }
        return result;
    }

    /**
     * 変換
     *
     * @param collection 一覧
     * @return 一覧
     */
    private List<Asset> toTrashList(List<Asset> collection) {
        List<Asset> result = new ArrayList<>();
        for (Asset item : collection) {
            if (item.content == TRASH) {
                result.add(item);
            }
        }
        return result;
    }

    /**
     * 変換
     *
     * @param collection 一覧
     * @return 一覧
     */
    private List<Asset> toRecentList(List<Asset> collection) {
        List<Asset> result = new ArrayList<>();
        for (Asset item : collection) {
            switch (item.content) {
                case ARCHIVE:
                case TRASH: {
                    break;
                }
                default: {
                    if (!(item.timestamp == INVALID_LONG_VALUE)) {
                        result.add(item);
                    }
                }
            }
        }
        toSortByCollection(result);
        Collections.reverse(result);
        return result;
    }

    /**
     * 週取得
     *
     * @return 週取得
     */
    private long getThisWeek() {
        Calendar calen = Calendar.getInstance();
        calen.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        if (LOG_D) {
            Log.d(TAG, "this week:" + DateFormat.getDateTimeInstance().format(new Date(calen.getTimeInMillis())));
        }
        return calen.getTimeInMillis();
    }

    /**
     * 週末取得
     *
     * @return 週末取得
     */
    private long getThisWeekEnd() {
        Calendar calen = Calendar.getInstance();
        calen.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        calen.add(Calendar.DATE, 5);
        if (LOG_D) {
            Log.d(TAG, "this weekend:" + DateFormat.getDateTimeInstance().format(new Date(calen.getTimeInMillis())));
        }
        return calen.getTimeInMillis();
    }

    /**
     * 次週取得
     *
     * @return 次週取得
     */
    private long getNextWeek() {
        Calendar calen = Calendar.getInstance();
        calen.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        calen.add(Calendar.DATE, 7);
        if (LOG_D) {
            Log.d(TAG, "next week:" + DateFormat.getDateTimeInstance().format(new Date(calen.getTimeInMillis())));
        }
        return calen.getTimeInMillis();
    }

    /**
     * 次週末取得
     *
     * @return 次週末取得
     */
    private long getNextWeekEnd() {
        Calendar calen = Calendar.getInstance();
        calen.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        calen.add(Calendar.DATE, 12);
        if (LOG_D) {
            Log.d(TAG, "next weekend:" + DateFormat.getDateTimeInstance().format(new Date(calen.getTimeInMillis())));
        }
        return calen.getTimeInMillis();
    }

    /**
     * 変換
     *
     * @param collection 一覧
     * @return 一覧
     */
    private List<Asset> toThisWeekList(List<Asset> collection) {
        List<Asset> result = new ArrayList<>();

        long thisweekend = getThisWeekEnd();

        for (Asset item : collection) {
            switch (item.content) {
                case ARCHIVE:
                case TRASH: {
                    break;
                }
                default: {
                    if (item.endDate >= item.startDate) {
                        if (item.startDate < thisweekend) {
                            result.add(item);
                        }
                    }
                    break;
                }
            }
        }
        return result;
    }

    /**
     * 変換
     *
     * @param collection 一覧
     * @return 一覧
     */
    private List<Asset> toWeekEndList(List<Asset> collection) {
        List<Asset> result = new ArrayList<>();

        long thisweekend = getThisWeekEnd();
        long nextweek = getNextWeek();

        for (Asset item : collection) {
            switch (item.content) {
                case ARCHIVE:
                case TRASH: {
                    break;
                }
                default: {
                    if (item.endDate >= item.startDate) {
                        if (item.startDate >= thisweekend && item.startDate < nextweek) {
                            result.add(item);
                        }
                    }
                    break;
                }

            }
        }
        return result;
    }

    /**
     * 変換
     *
     * @param collection 一覧
     * @return 一覧
     */
    private List<Asset> toNextWeekList(List<Asset> collection) {
        List<Asset> result = new ArrayList<>();

        long nextweek = getNextWeek();
        long nextweekend = getNextWeekEnd();

        for (Asset item : collection) {
            switch (item.content) {
                case ARCHIVE:
                case TRASH: {
                    break;
                }
                default: {
                    if (item.endDate >= item.startDate) {
                        if (item.startDate >= nextweek && item.startDate < nextweekend) {
                            result.add(item);
                        }
                    }
                    break;
                }
            }
        }
        return result;
    }

    /**
     * 変換
     *
     * @param collection 一覧
     * @return 一覧
     */
    private List<Asset> toArchiveList(List<Asset> collection) {
        List<Asset> result = new ArrayList<>();
        for (Asset item : collection) {
            if (item.content == ARCHIVE) {
                result.add(item);
            }
        }
        return result;
    }

    /**
     * 変換
     *
     * @param collection 一覧
     * @return 一覧
     */
    private List<Asset> toTasksList(List<Asset> collection) {
        List<Asset> result = new ArrayList<>();
        for (Asset person : collection) {
            switch (person.content) {
                case ARCHIVE:
                case TRASH: {
                    break;
                }
                default: {
                    result.add(person);
                    break;
                }
            }
        }
        return result;
    }

    /**
     * 変換
     *
     * @param assets 一覧
     * @return 一覧
     */
    private List<Asset> toNotStartList(List<Asset> assets) {
        List<Asset> result = new ArrayList<>();
        for (Asset asset : assets) {
            switch (asset.content) {
                case ARCHIVE:
                case TRASH: {
                    break;
                }
                default: {
                    if (asset.progressState.equals(PROGRESS.NOT_START)) {
                        result.add(asset);
                    }
                    break;
                }
            }
        }
        return result;
    }

    /**
     * 変換
     *
     * @param assets 一覧
     * @return 一覧
     */
    private List<Asset> toInprogressList(List<Asset> assets) {
        List<Asset> result = new ArrayList<>();
        for (Asset asset : assets) {
            switch (asset.content) {
                case ARCHIVE:
                case TRASH: {
                    break;
                }
                default: {
                    if (asset.progressState.equals(PROGRESS.INPROGRESS)) {
                        result.add(asset);
                    }
                    break;
                }
            }
        }
        return result;
    }

    /**
     * 変換
     *
     * @param assets 一覧
     * @return 一覧
     */
    private List<Asset> toCompletedList(List<Asset> assets) {
        List<Asset> result = new ArrayList<>();
        for (Asset asset : assets) {
            switch (asset.content) {
                case ARCHIVE:
                case TRASH: {
                    break;
                }
                default: {
                    if (asset.progressState.equals(PROGRESS.COMPLETED)) {
                        result.add(asset);
                    }
                    break;
                }
            }
        }
        return result;
    }

    /**
     * 項目取得
     *
     * @param position 位置
     * @return 項目
     */
    private Asset getItem(int position) {
        TasksAdapter adapter = (TasksAdapter) mBinding.tasksList.getAdapter();
        return Objects.requireNonNull(adapter).getTasks().get(position);
    }

    /**
     * コンテンツ取得
     *
     * @return コンテンツ
     */
    private Context getContext() {
        return getApplication().getApplicationContext();
    }

    /**
     * 選択一覧作成
     *
     * @param src 一覧
     */
    private void makeSelectedTasksList(List<Asset> src) {
        if (LOG_I) {
            Log.i(TAG, "makeSelectedTasksList#enter");
        }
        List<Asset> dst = new ArrayList<>();
        try {
            for (Asset asset : src) {
                if (asset.selected) {
                    dst.add(asset);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mSelectedTasks.clear();
        mSelectedTasks.addAll(dst);
        if (LOG_I) {
            Log.i(TAG, "makeSelectedTasksList#leave");
        }
    }

    /**
     * 開始
     */
    private void beginTransaction() {
        mRedoList.clear();
        mUndoList.clear();
    }

    /**
     * 終了
     */
    private void endTransaction() {
        Collections.reverse(mRedoList);
        Collections.reverse(mUndoList);
    }

    /**
     * 終了
     */
    private void finishTransaction() {
        mRedoList.clear();
        mUndoList.clear();
    }
}
