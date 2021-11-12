package jp.osaka.cherry.work.data;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;

/**
 * 履歴
 */
public class History implements Parcelable {

    /**
     * @serial 日付
     */
    public long date;

    /**
     * @serial タイトル
     */
    public String title;

    /**
     * @serial メッセージ
     */
    public String message;

    /**
     * @serial 名前
     */
    @ColumnInfo(name = "name")
    public String name;

    /**
     * @serial 詳細
     */
    @ColumnInfo(name = "description")
    public String description;

    /**
     * 詳細コンテンツ
     *
     * @return 0
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * パーシャル書き込み
     *
     * @param parcel パーシャル
     * @param i インデックス
     */
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(date);
        parcel.writeString(title);
        parcel.writeString(message);
    }

    /**
     * コンストラクタ
     *
     * @param parcel パーシャル
     */
    public History(Parcel parcel) {
        date = parcel.readLong();
        title = parcel.readString();
        message = parcel.readString();
    }

    /**
     * コンストラクタ
     */
    public History() {
    }

    /**
     * コンストラクタ
     *
     * @param date 日付
     * @param title タイトル
     * @param message メッセージ
     */
    public History(long date, String title, String message) {
        this.date = date;
        this.title = title;
        this.message = message;
    }

    /**
     * 作成
     */
    public static final Creator<History> CREATOR =
            new Creator<History>() {
                @Override
                public History createFromParcel(Parcel source) {
                    return new History(source);
                }

                @Override
                public History[] newArray(int size) {
                    return new History[size];
                }
            };
}
