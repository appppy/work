package jp.osaka.cherry.work.constants;

/**
 * コンテンツ
 */
public enum CONTENT {
    INBOX,
    ARCHIVE,
    TRASH;

    /**
     * コンテンツ変換
     *
     * @param value 値
     * @param defaultValue デフォルト値
     * @return コンテンツ
     */
    public static CONTENT toCONTENT(String value, CONTENT defaultValue) {
        CONTENT result = defaultValue;
        try {
            if (!value.isEmpty()) {
                result = CONTENT.valueOf(value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}