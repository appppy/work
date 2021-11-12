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

package jp.osaka.cherry.work.history;


import android.view.View;

import jp.osaka.cherry.work.data.History;

/**
 * Listener used with data binding to process user actions.
 */
public interface HistoryItemUserActionsListener {
    /**
     * 履歴クリック
     *
     * @param view 表示
     * @param history 履歴
     */
    void onHistoryClicked(View view, History history);

    /**
     * 履歴詳細クリック
     *
     * @param view 表示
     * @param history 履歴
     */
    void onHistoryMoreClicked(View view, History history);
}
