package jp.osaka.cherry.work.constants;

/**
 * 値
 */
public class VALUE {

    /**
     * Int変換
     *
     * @param value 値
     * @param defaultValue デフォルト値
     * @return Int値
     */
    public static int toIntegerValue(String value, int defaultValue) {
        int result = defaultValue;
        try {
            if (!value.isEmpty()) {
                result = Integer.parseInt(value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

}
