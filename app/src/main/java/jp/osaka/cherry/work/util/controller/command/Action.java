package jp.osaka.cherry.work.util.controller.command;

/**
 * アクション
 */
public class Action<T> {

    /**
     * アクション一覧
     */
    public enum ACTION {
        INSERT,
        CHANGE,
        REMOVE,
        CREATE,
        MODIFY
    }

    /**
     * @serial アクション
     */
    public ACTION action;

    /**
     * @serial 引数
     */
    public int arg;

    /**
     * @serial オブジェクト
     */
    public T object;

    /**
     * コンストラクタ
     *
     * @param action アクション
     * @param position 位置
     * @param object オブジェクト
     */
    public Action(ACTION action, int position, T object) {
        this.action = action;
        this.arg = position;
        this.object = object;
    }
}
