/*
 * Copyright 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.osaka.cherry.work;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

/**
 * Extends {@link SwipeRefreshLayout} to support non-direct descendant scrolling views.
 * <p>
 * {@link SwipeRefreshLayout} works as expected when a scroll view is a direct child: it triggers
 * the refresh only when the view is on top. This class adds a way (@link #setScrollUpChild} to
 * define which view controls this behavior.
 */
@SuppressLint("ViewConstructor")
public class ScrollChildSwipeRefreshLayout extends SwipeRefreshLayout {

    /**
     * @serial スクロールアップチャイルド
     */
    private final View mScrollUpChild;

    /**
     * コンストラクタ
     *
     * @param context コンテキスト
     * @param attrs 属性
     * @param mScrollUpChild スクロールアップ
     */
    public ScrollChildSwipeRefreshLayout(Context context, AttributeSet attrs, View mScrollUpChild) {
        super(context, attrs);
        this.mScrollUpChild = mScrollUpChild;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canChildScrollUp() {
        if (mScrollUpChild != null) {
            return mScrollUpChild.canScrollVertically(-1);
        }
        return super.canChildScrollUp();
    }

}
