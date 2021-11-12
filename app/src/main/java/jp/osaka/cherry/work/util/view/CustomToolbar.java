package jp.osaka.cherry.work.util.view;

import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.Toolbar;

/**
 * カスタムツールバー
 */
public class CustomToolbar extends Toolbar {

    /**
     * コンストラクタ
     *
     * @param context コンテキスト
     */
    public CustomToolbar(Context context) {
        super(context);
    }

    /**
     * コンストラクタ
     *
     * @param context コンテキスト
     * @param attrs 属性
     */
    public CustomToolbar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * コンストラクタ
     *
     * @param context コンテキスト
     * @param attrs 属性
     * @param defStyleAttr スタイル属性
     */
    public CustomToolbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}

