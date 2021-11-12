package jp.osaka.cherry.work.util.helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;

import java.util.ArrayList;

import jp.osaka.cherry.work.R;
import jp.osaka.cherry.work.addedittask.NewTaskActivity;
import jp.osaka.cherry.work.addedittask.TaskEditActivity;
import jp.osaka.cherry.work.constants.ActivityTransition;
import jp.osaka.cherry.work.data.Asset;
import jp.osaka.cherry.work.file.FilesActivity;
import jp.osaka.cherry.work.history.HistoryListActivity;
import jp.osaka.cherry.work.taskdetails.TaskDetailsActivity;
import jp.osaka.cherry.work.taskdetails.TaskDetailsInFileActivity;
import jp.osaka.cherry.work.taskdetails.TaskDetailsInHistoryActivity;
import jp.osaka.cherry.work.tasks.view.ArchiveTasksActivity;
import jp.osaka.cherry.work.tasks.view.CompletedTasksActivity;
import jp.osaka.cherry.work.tasks.view.InprogresstTasksActivity;
import jp.osaka.cherry.work.tasks.view.NextWeekTasksActivity;
import jp.osaka.cherry.work.tasks.view.NotStartTasksActivity;
import jp.osaka.cherry.work.tasks.view.RecentTasksActivity;
import jp.osaka.cherry.work.tasks.view.SearchTasksActivity;
import jp.osaka.cherry.work.tasks.view.TasksActivity;
import jp.osaka.cherry.work.tasks.view.ThisWeekTasksActivity;
import jp.osaka.cherry.work.tasks.view.TrashTasksActivity;
import jp.osaka.cherry.work.tasks.view.WeekEndTasksActivity;
import jp.osaka.cherry.work.tasksdetails.TasksDetailsActivity;

import static jp.osaka.cherry.work.constants.ActivityTransition.REQUEST_CHART;
import static jp.osaka.cherry.work.constants.ActivityTransition.REQUEST_DETAIL_TASK;
import static jp.osaka.cherry.work.constants.ActivityTransition.REQUEST_EDIT_TASK;

/**
 * 画面ヘルパ
 */
public class ActivityHelper {

    /**
     * 履歴画面開始
     *
     * @param activity 履歴画面
     */
    public static void startHistoryListActivity(Activity activity) {
        try {
            Intent intent = HistoryListActivity.createIntent(activity);
            activity.startActivityForResult(intent, ActivityTransition.REQUEST_OPEN_HISTORY.ordinal());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * タスク詳細画面開始
     *
     * @param activity タスク詳細画面
     * @param tasks タスク
     */
    public static void startTasksDetailsActivity(Activity activity, ArrayList<Asset> tasks) {
        try {
            Intent intent = TasksDetailsActivity.createIntent(activity, tasks);
            activity.startActivityForResult(intent, REQUEST_CHART.ordinal());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ファイル内のタスク詳細画面開始
     *
     * @param activity ファイル内のタスク詳細画面
     * @param context コンテキスト
     * @param v 表示
     * @param item 項目
     */
    public static void startTaskDetailsInFileActivity(Activity activity, Context context, View v, Asset item) {
        if (Build.VERSION.SDK_INT >= 21) {
            ActivityOptionsCompat opts = ActivityOptionsCompat.makeScaleUpAnimation(
                    v, 0, 0, v.getWidth(), v.getHeight());
            Intent intent = TaskDetailsInFileActivity.createIntent(context, item);
            ActivityCompat.startActivityForResult(activity, intent, REQUEST_DETAIL_TASK.ordinal(), opts.toBundle());
        } else {
            startTaskDetailsInFileActivity(activity, context, item);
        }
    }

    /**
     * ファイル内のタスク詳細画面開始
     *
     * @param activity ファイル内のタスク詳細画面
     * @param context コンテキスト
     * @param item 項目
     */
    public static void startTaskDetailsInFileActivity(Activity activity, Context context, Asset item) {
        Intent intent = TaskDetailsInFileActivity.createIntent(context, item);
        activity.startActivityForResult(intent, REQUEST_DETAIL_TASK.ordinal());
    }

    /**
     * 履歴内のタスク詳細画面開始
     *
     * @param activity 履歴内のタスク詳細画面
     * @param context コンテキスト
     * @param v 表示
     * @param item 項目
     */
    public static void startTaskDetailsInHistoryActivity(Activity activity, Context context, View v, Asset item) {
        if (Build.VERSION.SDK_INT >= 21) {
            ActivityOptionsCompat opts = ActivityOptionsCompat.makeScaleUpAnimation(
                    v, 0, 0, v.getWidth(), v.getHeight());
            Intent intent = TaskDetailsInHistoryActivity.createIntent(context, item);
            ActivityCompat.startActivityForResult(activity, intent, REQUEST_DETAIL_TASK.ordinal(), opts.toBundle());
        } else {
            startTaskDetailsInHistoryActivity(activity, context, item);
        }
    }

    /**
     * 履歴内のタスク詳細画面開始
     *
     * @param activity 履歴内のタスク詳細画面
     * @param context コンテキスト
     * @param item 項目
     */
    public static void startTaskDetailsInHistoryActivity(Activity activity, Context context, Asset item) {
        Intent intent = TaskDetailsInHistoryActivity.createIntent(context, item);
        activity.startActivityForResult(intent, REQUEST_DETAIL_TASK.ordinal());
    }

    /**
     * タスク詳細画面開始
     *
     * @param activity タスク詳細画面開始
     * @param context コンテキスト
     * @param v 表示
     * @param item 項目
     */
    public static void startTaskDetailsActivity(Activity activity, Context context, View v, Asset item) {
        if (Build.VERSION.SDK_INT >= 21) {
            ActivityOptionsCompat opts = ActivityOptionsCompat.makeScaleUpAnimation(
                    v, 0, 0, v.getWidth(), v.getHeight());
            Intent intent = TaskDetailsActivity.createIntent(context, item);
            ActivityCompat.startActivityForResult(activity, intent, REQUEST_DETAIL_TASK.ordinal(), opts.toBundle());
        } else {
            startTaskDetailsActivity(activity, context, item);
        }
    }

    /**
     * タスク詳細画面開始
     *
     * @param activity タスク詳細画面開始
     * @param context コンテキスト
     * @param item 項目
     */
    public static void startTaskDetailsActivity(Activity activity, Context context, Asset item) {
        Intent intent = TaskDetailsActivity.createIntent(context, item);
        activity.startActivityForResult(intent, REQUEST_DETAIL_TASK.ordinal());
    }

    /**
     * 編集画面開始
     *
     * @param activity 編集画面
     * @param context コンテキスト
     * @param v 表示
     * @param item 項目
     */
    public static void startEditActivity(Activity activity, Context context, View v, Asset item) {
        if (Build.VERSION.SDK_INT >= 21) {
            ActivityOptionsCompat opts = ActivityOptionsCompat.makeScaleUpAnimation(
                    v, 0, 0, v.getWidth(), v.getHeight());
            Intent intent = TaskEditActivity.createIntent(context, item);
            ActivityCompat.startActivityForResult(activity, intent, REQUEST_EDIT_TASK.ordinal(), opts.toBundle());
        } else {
            startEditActivity(activity, context, item);
        }
    }

    /**
     * 編集画面開始
     *
     * @param activity 編集画面
     * @param context コンテキスト
     * @param item 項目
     */
    public static void startEditActivity(Activity activity, Context context, Asset item) {
        Intent intent = TaskEditActivity.createIntent(context, item);
        activity.startActivityForResult(intent, REQUEST_EDIT_TASK.ordinal());
    }

    /**
     * 新規画面開始
     *
     * @param activity 新規画面
     * @param context コンテキスト
     * @param v 表示
     * @param item 項目
     */
    public static void startNewTaskActivity(Activity activity, Context context, View v, Asset item) {
        if (Build.VERSION.SDK_INT >= 21) {
            ActivityOptionsCompat opts = ActivityOptionsCompat.makeScaleUpAnimation(
                    v, 0, 0, v.getWidth(), v.getHeight());
            Intent intent = NewTaskActivity.createIntent(context, item);
            ActivityCompat.startActivityForResult(activity, intent, ActivityTransition.REQUEST_CREATE_TASK.ordinal(), opts.toBundle());
        } else {
            Intent intent = NewTaskActivity.createIntent(context, item);
            activity.startActivityForResult(intent, ActivityTransition.REQUEST_CREATE_TASK.ordinal());
       }
    }

    /**
     * 新規画面開始
     *
     * @param activity 新規画面
     * @param context コンテキスト
     * @param item 項目
     */
    public static void startNewTaskActivity(Activity activity, Context context, Asset item) {
        Intent intent = NewTaskActivity.createIntent(context, item);
        activity.startActivityForResult(intent, ActivityTransition.REQUEST_CREATE_TASK.ordinal());
    }

    /**
     * フォルダ画面開始
     *
     * @param activity フォルダ画面
     */
    public static void startFolderActivity(Activity activity) {
        Intent intent = FilesActivity.createIntent(activity);
        activity.startActivityForResult(intent, ActivityTransition.REQUEST_OPEN_FILE.ordinal());
    }

    /**
     * 検索画面開始
     *
     * @param activity 検索画面
     */
    public static void startSearchTasksActivity(Activity activity) {
        try {
            Intent intent = SearchTasksActivity.createIntent(activity);
            activity.startActivity(intent);
            activity.overridePendingTransition(R.animator.fade_out, R.animator.fade_in);
            activity.finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 画面開始
     *
     * @param id 識別子
     */
    public static Class<?> getStartActivity(int id) {
        Class<?> c = TasksActivity.class;

        switch (id) {
            case R.id.task: {
                c = TasksActivity.class;
                break;
            }
            case R.id.this_week: {
                c = ThisWeekTasksActivity.class;
                break;
            }
            case R.id.weekend: {
                c = WeekEndTasksActivity.class;
                break;
            }
            case R.id.next_week: {
                c = NextWeekTasksActivity.class;
                break;
            }
            case R.id.archive: {
                c = ArchiveTasksActivity.class;
                break;
            }
            case R.id.trash: {
                c = TrashTasksActivity.class;
                break;
            }
            case R.id.not_start: {
                c = NotStartTasksActivity.class;
                break;
            }
            case R.id.inprogress: {
                c = InprogresstTasksActivity.class;
                break;
            }
            case R.id.completed: {
                c = CompletedTasksActivity.class;
                break;
            }
            case R.id.recent: {
                c = RecentTasksActivity.class;
                break;
            }
            default: {
                break;
            }
        }
        return c;
    }

}
