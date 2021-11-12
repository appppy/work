/*
 *  Copyright 2017 Google Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package jp.osaka.cherry.work.tasks;


import android.view.View;

import jp.osaka.cherry.work.data.Asset;

/**
 * Listener used with data binding to process user actions.
 */
public interface TaskItemUserActionsListener {
    /**
     * クリック
     *
     * @param view 表示
     * @param asset 項目
     */
    void onTaskLongClicked(View view, Asset asset);

    /**
     * クリック
     *
     * @param view 表示
     * @param asset 項目
     */
    void onTaskClicked(View view, Asset asset);

    /**
     * クリック
     *
     * @param view 表示
     * @param asset 項目
     */
    void onTaskProgressClicked(View view, Asset asset);

    /**
     * クリック
     *
     * @param view 表示
     * @param asset 項目
     */
    void onTaskPriorityClicked(View view, Asset asset);

    /**
     * クリック
     *
     * @param view 表示
     * @param asset 項目
     */
    void onTaskMoreClicked(View view, Asset asset);
}
