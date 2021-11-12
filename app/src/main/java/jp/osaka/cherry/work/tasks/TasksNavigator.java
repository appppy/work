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

import java.util.List;

import jp.osaka.cherry.work.data.Asset;

/**
 * Defines the navigation actions that can be called from the task list screen.
 */
public interface TasksNavigator {
    /**
     * タイトル変更
     *
     * @param title タイトル
     */
    void onTitleChanged(String title);

    /**
     * 選択状態
     *
     * @param number 番号
     */
    void onSelectMode(int number);

    /**
     * 通常状態
     */
    void onNormalMode();

    /**
     * 一覧変更
     *
     * @param tasks 一覧
     */
    void onTasksChanged(List<Asset> tasks);
}
