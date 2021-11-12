package jp.osaka.cherry.work.data;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;

/**
 * ファイル
 */
public class File implements Parcelable {

    /**
     * @serial 日付
     */
    public long date;

    /**
     * @serial 名前
     */
    public String name;

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
     * 詳細コンテンツ取得
     *
     * @return 詳細コンテンツ
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
        parcel.writeString(name);
    }

    /**
     * コンストラクタ
     *
     * @param parcel パーシャル
     */
    private File(Parcel parcel) {
        date = parcel.readLong();
        name = parcel.readString();
    }

    /**
     * コンストラクタ
     */
    public File() {
    }

    /**
     * コンストラクタ
     *
     * @param date 日付
     * @param message メッセージ
     */
    public File(long date, String message) {
        this.date = date;
        this.name = message;
    }

    /**
     * 作成
     */
    public static final Creator<File> CREATOR =
            new Creator<File>() {
                @Override
                public File createFromParcel(Parcel source) {
                    return new File(source);
                }

                @Override
                public File[] newArray(int size) {
                    return new File[size];
                }
            };
}
