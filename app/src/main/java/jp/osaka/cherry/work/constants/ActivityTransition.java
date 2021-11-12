package jp.osaka.cherry.work.constants;

/**
 * アクティビティ遷移
 */
public enum ActivityTransition {
    REQUEST_CREATE_TASK,
    REQUEST_EDIT_TASK,
    REQUEST_DETAIL_TASK,
    REQUEST_SYNC_FILE,
    REQUEST_OPEN_FILE,
    REQUEST_CHART,
    REQUEST_FILE_DETAIL_LIST,
    REQUEST_OPEN_HISTORY;
    /**
     * 値に合致する enum 定数を返す。
     *
     * @param index インデックス
     * @return メッセージ
     */
    public static ActivityTransition get(int index) {
        // 値から enum 定数を特定して返す処理
        for (ActivityTransition request : ActivityTransition.values()) {
            if (request.ordinal() == index) {
                return request;
            }
        }
        return null; // 特定できない場合
    }
}