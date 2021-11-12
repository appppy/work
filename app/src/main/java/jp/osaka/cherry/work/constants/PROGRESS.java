package jp.osaka.cherry.work.constants;

/**
 * 進捗度
 */
public enum PROGRESS {
    NOT_START,     // 未開始
    INPROGRESS,    // 進行中
    COMPLETED,     // 完了
    WAITING,       // 待ち
    POSTPONEMENT;  // 延期

    /**
     * 進捗変換
     *
     * @param value 値
     * @param defaultValue デフォルト値
     * @return 進捗
     */
    public static PROGRESS toPROGRESS(String value, PROGRESS defaultValue) {
        PROGRESS result = defaultValue;
        try {
            if (!value.isEmpty()) {
                result = PROGRESS.valueOf(value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}