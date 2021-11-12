
package jp.osaka.cherry.work.util.view.recyclerview.adapter;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

import static jp.osaka.cherry.work.Config.LOG_I;

/**
 * ArrayAdapterのようなRecycerView.Adapter
 */
public abstract class RecyclerArrayAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    /**
     * @serial ロック
     */
    private final Object lock = new Object();

    /**
     * @serial 一覧
     */
    private final List<T> objects;

    /**
     * コンストラクタ
     *
     * @param objects オブジェクト
     */
    public RecyclerArrayAdapter(List<T> objects) {
        this.objects = objects;
    }

    /**
     * 挿入
     *
     * @param position 位置
     * @param object オブジェクト
     */
    public void insert(int position, @NonNull T object) {
        if (LOG_I) {
            Log.i("RecyclerArrayAdapter", "insert(" + position + ")#enter");
        }
        synchronized (lock) {
            objects.add(position, object);
        }
        notifyItemInserted(position);
        if (LOG_I) {
            Log.i("RecyclerArrayAdapter", "insert()#leave");
        }
    }

    /**
     * 追加
     *
     * @param object オブジェクト
     */
    public void add(@NonNull T object) {
        final int position;
        synchronized (lock) {
            position = objects.size();
            objects.add(object);
        }
        notifyItemInserted(position);
    }

    /**
     * 設定
     *
     * @param position 位置
     * @param object オブジェクト
     */
    public void set(int position, @NonNull T object) {
        synchronized (lock) {
            objects.remove(position);
            objects.add(position, object);
        }
        notifyItemChanged(position);
    }

    /**
     * 削除
     *
     * @param position 位置
     */
    public void remove(int position) {
        notifyItemRemoved(position);
    }

    /**
     * 移動
     *
     * @param from 移動元
     * @param to 移動先
     */
    public void move(int from, int to) {
        synchronized (lock) {
            if (from < to) {
                for (int i = from; i < to; i++) {
                    Collections.swap(objects, i, i + 1);
                }
            } else {
                for (int i = from; i > to; i--) {
                    Collections.swap(objects, i, i - 1);
                }
            }
        }
        notifyItemMoved(from, to);
    }

    /**
     * 一覧取得
     *
     * @return 一覧
     */
    public List<T> getList() {
        return objects;
    }
}