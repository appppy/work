package jp.osaka.cherry.work.service;

import android.app.backup.BackupAgentHelper;
import android.app.backup.SharedPreferencesBackupHelper;

/**
 * バックアップ
 */
public class BackupAgent extends BackupAgentHelper{

    /**
     * @serial キー
     */
    private static final String PREFS_BACKUP_KEY = "prefs";

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate() {
        super.onCreate();

        String sharedPreference = SimpleStore.SHARED_PREFERENCE_NAME;

        SharedPreferencesBackupHelper helper;
        helper = new SharedPreferencesBackupHelper(this, sharedPreference);

        addHelper(PREFS_BACKUP_KEY, helper);
    }
}
