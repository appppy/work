package jp.osaka.cherry.work.constants;

/**
 * 優先度
 */
public enum PRIORITY {
    HIGH,   // 高
    MIDDLE, // 中
    LOW;    // 低

    /**
     * 優先度変換
     *
     * @param value 値
     * @param defaultValue デフォルト値
     * @return 優先度
     */
    public static PRIORITY toPRIORITY(String value, PRIORITY defaultValue) {
        PRIORITY result = defaultValue;
        try {
            if (!value.isEmpty()) {
                result = PRIORITY.valueOf(value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
