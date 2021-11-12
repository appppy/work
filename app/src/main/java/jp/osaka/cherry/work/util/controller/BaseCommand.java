package jp.osaka.cherry.work.util.controller;

import android.os.Bundle;

/**
 * コマンド
 */
public abstract class BaseCommand {

    /**
     * @serial 引数
     */
    public Bundle args = new Bundle();
}
