/*
 * Copyright (C) 2013 The Android Open Source Project
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

package jp.osaka.cherry.work.data;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.UUID;

import jp.osaka.cherry.work.R;
import jp.osaka.cherry.work.constants.CONTENT;
import jp.osaka.cherry.work.constants.PRIORITY;
import jp.osaka.cherry.work.constants.PROGRESS;

import static jp.osaka.cherry.work.constants.INVALID.INVALID_INT_VALUE;
import static jp.osaka.cherry.work.constants.INVALID.INVALID_LONG_VALUE;
import static jp.osaka.cherry.work.constants.INVALID.INVALID_STRING_VALUE;
import static jp.osaka.cherry.work.constants.PRIORITY.*;

/**
 * 資産
 */
public class Asset implements Parcelable {

    /**
     * @serial ID
     */
    public String id;

    /**
     * @serial UUID
     */
    @ColumnInfo(name = "entryid")
    public String uuid;

    /**
     * @serial 作成日
     */
    public long creationDate;

    /**
     * @serial 編集日
     */
    public long modifiedDate;

    /**
     * @serial 表示名
     */
    public String displayName;

    /**
     * @serial 開始日時
     */
    public long startDate;

    /**
     * @serial 終了日時
     */
    public long endDate;

    /**
     * @serial 進捗状態
     */
    public PROGRESS progressState;

    /**
     * @serial 優先度
     */
    public PRIORITY priority;

    /**
     * @serial レート
     */
    public int rate;

    /**
     * @serial ノート
     */
    public String note;

    /**
     * @serial コンテンツ
     */
    public CONTENT content;

    /**
     * @serial タイムスタンプ
     */
    public long timestamp;

    /**
     * @serial 選択状態
     */
    @ColumnInfo(name = "selected")
    public boolean selected = false;

    /**
     * @serial タイトル
     */
    @ColumnInfo(name = "title")
    public String title;

    /**
     * @serial 詳細
     */
    @ColumnInfo(name = "description")
    public String description;

    /**
     * コンストラクタ
     */
    public Asset() {
    }

    /**
     * インスタンス生成
     *
     * @return インスタンス
     */
    public static Asset createInstance() {
        return new Asset(
                String.valueOf(UUID.randomUUID()),
                String.valueOf(UUID.randomUUID()),
                System.currentTimeMillis(),
                System.currentTimeMillis(),
                INVALID_STRING_VALUE,
                System.currentTimeMillis(),
                System.currentTimeMillis(),
                PROGRESS.NOT_START,
                MIDDLE,
                0,
                INVALID_STRING_VALUE,
                CONTENT.INBOX,
                INVALID_LONG_VALUE);
    }

    /**
     * @serial 作成
     */
    public static final Creator<Asset> CREATOR =
            new Creator<Asset>() {

                /**
                 * Parcelableクラス作成
                 * @see Creator#createFromParcel(Parcel)
                 */
                @Override
                public Asset createFromParcel(Parcel source) {
                    return new Asset(source);
                }

                /**
                 * 配列生成
                 * @see Creator#newArray(int)
                 */
                @Override
                public Asset[] newArray(int size) {
                    return new Asset[size];
                }
            };

    /**
     * コンストラクタ
     *
     * @param id ID
     * @param uuid UUID
     * @param creationDate 作成日時
     * @param modifiedDate 編集日時
     * @param displayName 表示名
     * @param startDate 開始日時
     * @param endDate　終了日時
     * @param progressState 進捗状態
     * @param priority 優先度
     * @param rate レート
     * @param note ノート
     * @param content コンテンツ
     * @param timestamp タイムスタンプ
     */
    public Asset(
            String id,
            String uuid,
            long creationDate,
            long modifiedDate,
            String displayName,
            long startDate,
            long endDate,
            PROGRESS progressState,
            PRIORITY priority,
            int rate,
            String note,
            CONTENT content,
            long timestamp
    ) {
        this.id = id;
        this.uuid = uuid;
        this.creationDate = creationDate;
        this.modifiedDate = modifiedDate;
        this.displayName = displayName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.progressState = progressState;
        this.priority = priority;
        this.rate = rate;
        this.note = note;
        this.content = content;
        this.timestamp = timestamp;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(uuid);
        dest.writeLong(creationDate);
        dest.writeLong(modifiedDate);
        dest.writeString(displayName);
        dest.writeLong(startDate);
        dest.writeLong(endDate);
        dest.writeString(progressState.name());
        dest.writeString(priority.name());
        dest.writeInt(rate);
        dest.writeString(note);
        dest.writeString(content.name());
        dest.writeLong(timestamp);
    }

    /**
     * コンストラクタ
     *
     * @param parcel パーシャルデータ
     */
    public Asset(Parcel parcel) {
        id = parcel.readString();
        uuid = parcel.readString();
        creationDate = parcel.readLong();
        modifiedDate = parcel.readLong();
        displayName = parcel.readString();
        startDate = parcel.readLong();
        endDate = parcel.readLong();
        progressState = PROGRESS.valueOf(parcel.readString());
        priority = valueOf(parcel.readString());
        rate = parcel.readInt();
        note = parcel.readString();
        content = CONTENT.valueOf(parcel.readString());
        timestamp = parcel.readLong();
    }

    /**
     * JSONオブジェクト変換
     *
     * @return JSONオブジェクト
     */
    public JSONObject toJSONObject() {
        JSONObject object = new JSONObject();
        try {
            object.put("id", id);
            object.put("uuid",uuid);
            object.put("creationDate",creationDate);
            object.put("modifiedDate",modifiedDate);
            object.put("displayName",displayName);
            object.put("startDate", startDate);
            object.put("endDate",endDate);
            object.put("progressState", progressState.name());
            object.put("priority", priority.name());
            object.put("rate", rate);
            object.put("note", note);
            object.put("content", content.name());
            object.put("timestamp", timestamp);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }

    /**
     * コンストラクタ
     */
    public Asset(JSONObject object) {
        try {
            id = object.getString("id");
            uuid = object.getString("uuid");
            creationDate = object.getLong("creationDate");
            modifiedDate = object.getLong("modifiedDate");
            displayName = object.getString("displayName");
            startDate = object.getLong("startDate");
            endDate = object.getLong("endDate");
            progressState = PROGRESS.valueOf(object.getString("progressState"));
            priority = valueOf(object.getString("priority"));
            rate = object.getInt("rate");
            note = object.getString("note");
            content = CONTENT.valueOf(object.getString("content"));
            timestamp = object.getLong("timestamp");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * コピー
     *
     * @param item 項目
     */
    public void copy(Asset item) {
        // selected,title,descriptionはコピーしない。
        id = item.id;
        uuid = item.uuid;
        creationDate = item.creationDate;
        modifiedDate = item.modifiedDate;
        displayName = item.displayName;
        startDate = item.startDate;
        endDate = item.endDate;
        progressState = item.progressState;
        priority = item.priority;
        rate = item.rate;
        note = item.note;
        content = item.content;
        timestamp = item.timestamp;
    }

    /**
     * パラメータ設定
     *
     * @param item 項目
     */
    public void setParams(Asset item) {
        // id,uuid,selected,title,descriptionはコピーしない。
        creationDate = item.creationDate;
        modifiedDate = item.modifiedDate;
        displayName = item.displayName;
        startDate = item.startDate;
        endDate = item.endDate;
        progressState = item.progressState;
        priority = item.priority;
        rate = item.rate;
        note = item.note;
        content = item.content;
        timestamp = item.timestamp;
    }

    /**
     * 文字変換
     *
     * @param context コンテンツ
     * @return 文字
     */
    public String toString(Context context) {
        StringBuilder sb = new StringBuilder();
        // 表示名
        sb.append(context.getString(R.string.name)).append(":").append(displayName);
        // 開始日
        if(startDate != INVALID_LONG_VALUE) {
            sb.append(", ").append(context.getString(R.string.start_date)).append(":").append(DateFormat.getDateTimeInstance().format(startDate));
        }
        // 期限
        if(endDate != INVALID_LONG_VALUE) {
            sb.append(", ").append(context.getString(R.string.end_date)).append(":").append(DateFormat.getDateTimeInstance().format(endDate));
        }
        // 進捗状況
        switch (progressState) {
            case INPROGRESS: {
                sb.append(", ").append(context.getString(R.string.progress)).append(":").append(context.getString(R.string.progress_inprogress));
                break;
            }
            case COMPLETED: {
                sb.append(", ").append(context.getString(R.string.progress)).append(":").append(context.getString(R.string.progress_completed));
                break;
            }
            case WAITING: {
                sb.append(", ").append(context.getString(R.string.progress)).append(":").append(context.getString(R.string.progress_waiting));
                break;
            }
            case POSTPONEMENT: {
                sb.append(", ").append(context.getString(R.string.progress)).append(":").append(context.getString(R.string.progress_postponement));
                break;
            }
            default: {
                sb.append(", ").append(context.getString(R.string.progress)).append(":").append(context.getString(R.string.progress_not_start));
                break;
            }
        }
        // 優先度
        switch (priority) {
            case HIGH: {
                sb.append(", ").append(context.getString(R.string.priority)).append(":").append(context.getString(R.string.priority_high));
                break;
            }
            case LOW: {
                sb.append(", ").append(context.getString(R.string.priority)).append(":").append(context.getString(R.string.priority_low));
                break;
            }
            default: {
                sb.append(", ").append(context.getString(R.string.priority)).append(":").append(context.getString(R.string.priority_middle));
                break;
            }
        }
        // 達成率
        if(rate != INVALID_INT_VALUE) {
            sb.append(", ").append(context.getString(R.string.rate)).append(":").append(rate);
        }
        // メモ
        if(note != null && !note.equals(INVALID_STRING_VALUE)) {
            sb.append(", ").append(context.getString(R.string.note)).append(":").append(note);
        }
        // コンテンツ
        sb.append(", ").append("Content").append(":").append(content.name());
        String s = sb.toString();
        sb.delete(0, sb.length());
        return s;
    }

    /**
     * 比較
     *
     * @param o オブジェクト
     * @return 比較結果
     */
    public boolean equal(Object o) {
        boolean result = false;
        if (o instanceof Asset) {
            Asset item = (Asset) o;
            if (item.uuid.equals(uuid)) {
                result = true;
            }
        }
        return result;
    }

    /**
     * @serial カテゴリ
     */
    public int mCategory = 2;

    /**
     * カテゴリ取得
     *
     * @return カテゴリ
     */
    public int getCategory() {
        return mCategory;
    }
}
