package jp.osaka.cherry.work.service;

import android.content.Context;
import android.util.Log;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jp.osaka.cherry.work.data.Asset;

import static com.google.common.base.Ascii.MAX;
import static jp.osaka.cherry.work.Config.LOG_I;
import static jp.osaka.cherry.work.constants.EXTRA.EXTRA_JSON_OBJECTS;

/**
 * データベース
 */
public class SimpleDatabase {

    /**
     * @serial 目印
     */
    private static final String TAG = "SimpleDatabase";

    /**
     * @serial プリファレンス
     */
    private final SimpleStore mPref;

    /**
     * @serial プリファレンス
     */
    private final CollectionStore<JSONObject> mPrefs;

    /**
     * @serial 一覧
     */
    private final Collection<Asset> mCollection = new ArrayList<>();

    /**
     * コンストラクタ
     *
     * @param context コンテキスト
     */
    SimpleDatabase(Context context) {
        mPref = new SimpleStore(context);
        mPrefs = new CollectionStore<>(context);
    }

    /**
     * リストア
     */
    public void restore() {
        if (LOG_I) {
            Log.i(TAG, "restore#enter");
        }

        // クリア
        mCollection.clear();

        // 下位互換
        for (int i = 0; i < MAX; i++) {
            Asset person = mPref.get(String.valueOf(i));
            if (null != person) {
                mCollection.add(person);
            }
        }
        if (!mCollection.isEmpty()) {
            for (int i = 0; i < MAX; i++) {
                mPref.clear(String.valueOf(i));
            }
            List<JSONObject> objects = new ArrayList<>();
            for(Asset item : mCollection) {
                objects.add(item.toJSONObject());
            }
            mPrefs.set(EXTRA_JSON_OBJECTS, objects);
        }

        // リストア
        try {
            List<JSONObject> objects = (List<JSONObject>) mPrefs.get(EXTRA_JSON_OBJECTS);
            for(JSONObject object : objects) {
                mCollection.add(new Asset(object));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (LOG_I) {
            Log.i(TAG, "restore#leave");
        }
    }

    /**
     * バックアップ
     */
    public void backup(Collection<Asset> assets) {
        if (LOG_I) {
            Log.i(TAG, "backup#enter");
        }

        mCollection.clear();
        mCollection.addAll(assets);

        // バックアップ
        List<JSONObject> objects = new ArrayList<>();
        for(Asset asset : assets) {
            objects.add(asset.toJSONObject());
        }
        mPrefs.set(EXTRA_JSON_OBJECTS, objects);

        if (LOG_I) {
            Log.i(TAG, "backup#leave");
        }
    }

    /**
     * 一覧取得
     *
     * @return 一覧
     */
    public Collection<Asset> getTasks() {
        return mCollection;
    }
}
