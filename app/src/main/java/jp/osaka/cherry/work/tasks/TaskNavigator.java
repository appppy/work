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

package jp.osaka.cherry.work.tasks;

import android.view.MenuItem;
import android.view.View;

import jp.osaka.cherry.work.data.Asset;

/**
 * Defines the navigation actions that can be called from the task list screen.
 */
public interface TaskNavigator {
    /**
     * 選択
     *
     * @param menu 項目
     */
    boolean onOptionsItemSelected(MenuItem menu);

    /**
     * 選択
     *
     * @param view 表示
     * @param asset 項目
     */
    void onSelectedMore(View view, Asset asset);

    /**
     * 選択
     *
     * @param view 表示
     * @param asset 項目
     */
    void onSelectedPriority(View view, Asset asset);

    /**
     * 選択
     *
     * @param view 表示
     * @param asset 項目
     */
    void onSelectedProgress(View view, Asset asset);
}
