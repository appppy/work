package jp.osaka.cherry.work.constants;

import java.util.HashMap;

import jp.osaka.cherry.work.R;

/**
 * 変換
 */
public class CONVERTER {

    /**
     * @serial 優先度
     */
    private static final HashMap<Integer, String> priorityMap;

    /**
     * @serial 進捗
     */
    private static final HashMap<Integer, String> progressMap;

    /**
     * @serial 日程
     */
    private static final HashMap<Integer, String> scheduleMap;

    static {
        priorityMap = new HashMap<>();
        priorityMap.put(R.id.menu_priority_high, PRIORITY.HIGH.name());
        priorityMap.put(R.id.menu_priority_middle, PRIORITY.MIDDLE.name());
        priorityMap.put(R.id.menu_priority_low, PRIORITY.LOW.name());

        progressMap = new HashMap<>();
        progressMap.put(R.id.menu_progress_not_start, PROGRESS.NOT_START.name());
        progressMap.put(R.id.menu_progress_completed, PROGRESS.COMPLETED.name());
        progressMap.put(R.id.menu_progress_inprogress, PROGRESS.INPROGRESS.name());
        progressMap.put(R.id.menu_progress_waiting, PROGRESS.WAITING.name());
        progressMap.put(R.id.menu_progress_postponement, PROGRESS.POSTPONEMENT.name());

        scheduleMap = new HashMap<>();
        scheduleMap.put(R.id.menu_schedule_this_week, SCHEDULE.THIS_WEEK.name());
        scheduleMap.put(R.id.menu_schedule_weekend, SCHEDULE.WEEK_END.name());
        scheduleMap.put(R.id.menu_schedule_next_week, SCHEDULE.NEXT_WEEK.name());
    }

    /**
     * 優先度取得
     *
     * @param id ID
     * @return 優先度
     */
    static public String getPriority(int id) {
        return priorityMap.get(id);
    }

    /**
     * 進捗取得
     *
     * @param id ID
     * @return 進捗
     */
    static public String getProgress(int id) {
        return progressMap.get(id);
    }

    /**
     * 日程取得
     *
     * @param id ID
     * @return 日程
     */
    static public String getSchedule(int id) {
        return scheduleMap.get(id);
    }
}
