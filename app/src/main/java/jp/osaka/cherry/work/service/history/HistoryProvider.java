package jp.osaka.cherry.work.service.history;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.util.Objects;

import static jp.osaka.cherry.work.service.history.HistoryColumns.CONTENT_URI;
import static jp.osaka.cherry.work.service.history.HistoryColumns.PATH;
import static jp.osaka.cherry.work.service.history.HistoryColumns.TABLE;

/**
 * 履歴プロバイダ
 */
public class HistoryProvider extends ContentProvider {

    /**
     * @serial 権限
     */
    public static final String AUTHORITY = "jp.osaka.cherry.work.history.historyprovider";

    /**
     * @serial 履歴
     */
    private static final int HISTORY = 1;

    /**
     * @serial 履歴識別子
     */
    private static final int HISTORY_ID = 2;

    /**
     * @serial 照合
     */
    private static final UriMatcher sUriMatcher;
    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, PATH, HISTORY);
        sUriMatcher.addURI(AUTHORITY, PATH + "/#", HISTORY_ID);
    }

    /**
     * DBヘルパ
     */
    private HistoryHelper mDbHelper;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCreate() {
        mDbHelper = new HistoryHelper(getContext());
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        switch (sUriMatcher.match(uri)) {
            case HISTORY:
            case HISTORY_ID:
                queryBuilder.setTables(TABLE);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        return queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        String insertTable;
        Uri contentUri;
        if (sUriMatcher.match(uri) == HISTORY) {
            insertTable = TABLE;
            contentUri = CONTENT_URI;
        } else {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        long rowId = db.insert(insertTable, null, values);
        if (rowId > 0) {
            Uri returnUri = ContentUris.withAppendedId(contentUri, rowId);
            Objects.requireNonNull(getContext()).getContentResolver().notifyChange(returnUri, null);
            return returnUri;
        } else {
            throw new IllegalArgumentException("Failed to insert row into " + uri);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int count;
        count = db.update(TABLE, values, selection, selectionArgs);
        Objects.requireNonNull(getContext()).getContentResolver().notifyChange(uri, null);
        return count;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
            case HISTORY:
            case HISTORY_ID:
                count = db.delete(TABLE, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        Objects.requireNonNull(getContext()).getContentResolver().notifyChange(uri, null);
        return count;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }
}
