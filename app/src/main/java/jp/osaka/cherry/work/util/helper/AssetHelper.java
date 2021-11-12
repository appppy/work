package jp.osaka.cherry.work.util.helper;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import jp.osaka.cherry.work.data.Asset;
import jp.osaka.cherry.work.data.File;

/**
 * アセットヘルパ
 */
public class AssetHelper {

    /**
     * @serial 目印
     */
    public static String TAG = "AssetHelper";

    /**
     * 検索
     *
     * @param collection 一覧
     * @return 検索一覧
     */
    public static Collection<Asset> toSortByDateModifiedCollection(Collection<Asset> collection) {
        Collections.sort((List<Asset>) collection, (lhs, rhs) -> (int) (lhs.modifiedDate - rhs.modifiedDate));
        return collection;
    }

    /**
     * 検索
     *
     * @param collection 一覧
     * @return 検索一覧
     */
    public static Collection<Asset> toSortByDateCreatedCollection(Collection<Asset> collection) {
        Collections.sort((List<Asset>) collection, (lhs, rhs) -> (int) (lhs.creationDate - rhs.creationDate));
        return collection;
    }

    /**
     * 検索
     *
     * @param collection 一覧
     * @return 検索一覧
     */
    public static Collection<Asset> toSortByNameCollection(Collection<Asset> collection) {
        Collections.sort((List<Asset>) collection, (lhs, rhs) -> lhs.displayName.compareTo(rhs.displayName));
        return collection;
    }

    /**
     * 複製
     *
     * @param dest　複製先
     * @param src 複製前
     */
    public static void copy(ArrayList<Asset> dest, ArrayList<Asset> src) {
        dest.clear();
        for (Asset s : src) {
            Asset d = Asset.createInstance();
            d.copy(s);
            dest.add(d);
        }
    }

    /**
     * 編集の有無
     *
     * @param dest 編集後
     * @param src 編集前
     * @return 編集の有無
     */
    public static boolean isModified(Asset dest, Asset src) {
        boolean result = false;
        if (dest.uuid.equals(src.uuid)) {
            if ((dest.creationDate != src.creationDate)
                    || (dest.modifiedDate != src.modifiedDate)
                    || (!dest.displayName.equals(src.displayName))
                    || (dest.startDate != src.startDate)
                    || (dest.endDate != src.endDate)
                    || (dest.progressState != src.progressState)
                    || (dest.priority != src.priority)
                    || (dest.rate != src.rate)
                    || (!dest.note.equals(src.note))
                    || (dest.content != src.content))
            {
                result = true;
            }
        }
        return result;
    }

    /**
     * 選択状態の確認
     *
     * @return 選択状態
     */
    public static boolean isSelected(List<Asset> collection) {
        boolean result = false;
        // 製品の選択状態を確認する
        for (Asset item : collection) {
            // 選択状態を確認した
            if (item.selected) {
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * 複数選択状態の確認
     *
     * @return 複数選択状態
     */
    public static boolean isMultiSelected(List<Asset> collection) {
        int count = 0;
        // 製品の選択状態を確認する
        for (Asset item : collection) {
            // 選択状態を確認した
            if (item.selected) {
                count++;
            }
        }
        return (count > 1);
    }

    /**
     * JSON変換
     *
     * @param collection 一覧
     * @return JSON
     */
    public static String toJSONString(ArrayList<Asset> collection) {
        JSONArray array = new JSONArray();
        for (Asset item : collection) {
            array.put(item.toJSONObject());
        }
        return array.toString();
    }

    /**
     * 一覧変換
     *
     * @param JSONString JSON
     * @return 一覧
     */
    public static ArrayList<Asset> toAssets(String JSONString) {
        ArrayList<Asset> results = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(JSONString);
            int count = array.length();
            for (int i=0; i<count; i++){
                results.add(new Asset(array.getJSONObject(i)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }


    /***
     * タスクのコピー
     *
     * @param dest コピー先
     * @param src  コピー元
     */
    public static void copy(Asset dest, Asset src) {
        dest.creationDate = src.creationDate;
        dest.modifiedDate = src.modifiedDate;
        dest.displayName = src.displayName;
        dest.startDate = src.startDate;
        dest.endDate = src.endDate;
        dest.progressState = src.progressState;
        dest.priority = src.priority;
        dest.rate = src.rate;
        dest.note = src.note;
        dest.content = src.content;
    }

    /**
     * 名前でソートした一覧の取得
     *
     * @param collection 一覧
     * @return 名前でソートした一覧
     */
    public static Collection<File> toSortByNameFileCollection(Collection<File> collection) {
        Collections.sort((List<File>) collection, (lhs, rhs) -> lhs.name.compareTo(rhs.name));
        return collection;
    }

    /**
     * タスクの取得
     *
     * @param uuid  識別子
     * @param tasks タスク
     * @return タスク
     */
    public static Asset getTask(String uuid, List<Asset> tasks) {
        Asset result = null;
        for (Asset dest : tasks) {
            if (uuid.equals(dest.uuid)) {
                result = dest;
                break;
            }
        }
        return result;
    }

    /**
     * 一覧のコピー
     *
     * @param dest コピー先
     * @param src  コピー元
     */
    public static void copyTasks(ArrayList<Asset> dest, ArrayList<Asset> src) {
        dest.clear();
        for (Asset s : src) {
            Asset d = Asset.createInstance();
            d.copy(s);
            dest.add(d);
        }
    }
}
