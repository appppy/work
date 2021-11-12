package jp.osaka.cherry.work.addedittask;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.DatePicker;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import jp.osaka.cherry.work.R;
import jp.osaka.cherry.work.constants.PROGRESS;
import jp.osaka.cherry.work.data.Asset;
import jp.osaka.cherry.work.databinding.NewTaskActivityBinding;

import static jp.osaka.cherry.work.Config.LOG_I;
import static jp.osaka.cherry.work.constants.EXTRA.EXTRA_ASSET;
import static jp.osaka.cherry.work.constants.INVALID.INVALID_LONG_VALUE;
import static jp.osaka.cherry.work.constants.INVALID.INVALID_STRING_VALUE;
import static jp.osaka.cherry.work.constants.PRIORITY.HIGH;
import static jp.osaka.cherry.work.constants.PRIORITY.LOW;
import static jp.osaka.cherry.work.constants.PRIORITY.MIDDLE;
import static jp.osaka.cherry.work.constants.PROGRESS.INPROGRESS;
import static jp.osaka.cherry.work.constants.PROGRESS.NOT_START;

/**
 * 編集画面
 */
public class TaskEditActivity extends AppCompatActivity {

    /**
     * @serial バインディング
     */
    private NewTaskActivityBinding mBinding;

    /**
     * @serial データセット
     */
    private Asset mDataSet;

    /**
     * @serial バックアップ
     */
    private Asset mBackup;

    /**
     * @serial 開始時間編集ダイアログ
     */
    private DatePickerDialog mStartDatePickerDialog;

    /**
     * @serial データセットリスナ
     */
    final DatePickerDialog.OnDateSetListener StartDateSetListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(android.widget.DatePicker datePicker, int year,
                              int monthOfYear, int dayOfMonth) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, monthOfYear, dayOfMonth);
            mDataSet.startDate = calendar.getTime().getTime();
            setupStartDate();
        }
    };

    /**
     * @serial 終了時間編集ダイアログ
     */
    private DatePickerDialog mEndDatePickerDialog;

    /**
     * @serial 終了時間編集ダイアログ
     */
    final DatePickerDialog.OnDateSetListener EndDateSetListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(android.widget.DatePicker datePicker, int year,
                              int monthOfYear, int dayOfMonth) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, monthOfYear, dayOfMonth);
            mDataSet.endDate = calendar.getTime().getTime();
            setupEndDate();
        }
    };

    /**
     * インテント作成
     *
     * @param context コンテキスト
     * @param task データ
     * @return インテント
     */
    public static Intent createIntent(Context context, Asset task) {
        Intent intent = new Intent(context, TaskEditActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_ASSET, task);
        intent.putExtras(bundle);
        return intent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String TAG = "TaskEditActivity";
        if (LOG_I) {
            Log.i(TAG, "onCreate#enter");
        }

        // インテントの取得
        Intent intent = getIntent();
        mDataSet = intent.getParcelableExtra(EXTRA_ASSET);
        mBackup = Asset.createInstance();
        mBackup.copy(mDataSet);

        // レイアウトの設定
        mBinding = DataBindingUtil.setContentView(this, R.layout.new_task_activity);

        // 日付情報の初期設定
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR); // 年
        int monthOfYear = calendar.get(Calendar.MONTH); // 月
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH); // 日

        // 開始日付設定ダイアログの作成・リスナの登録
        mStartDatePickerDialog = new DatePickerDialog(this, StartDateSetListener, year, monthOfYear, dayOfMonth);

        // 終了日付設定ダイアログの作成・リスナの登録
        mEndDatePickerDialog = new DatePickerDialog(this, EndDateSetListener, year, monthOfYear, dayOfMonth);

        // ツールバーの設定
        setSupportActionBar(mBinding.toolbar);
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
        }

        // 表示の設定
        setupName();
        setupNote();
        setupStartDate();
        setupEndDate();
        setupProgress();
        setupPiriority();
        setupRate();

        if (LOG_I) {
            Log.i(TAG, "onCreate#leave");
        }
    }

    /**
     * 名前設定
     */
    void setupName() {
        mBinding.detailContainer.editName.setText(mDataSet.displayName);
    }

    /**
     * ノート設定
     */
    void setupNote() {
        mBinding.detailContainer.editNote.setText(mDataSet.note);
    }

    /**
     * 開始時間設定
     */
    void setupStartDate() {
        if (mDataSet.startDate != INVALID_LONG_VALUE) {
            mBinding.detailContainer.editStartDate.setText(DateFormat.getDateInstance().format(mDataSet.startDate));
        }
        mBinding.detailContainer.layoutStartDate.setOnClickListener(v -> {
            // 日付設定ダイアログの表示
            mStartDatePickerDialog.show();
            // 日付設定ダイアログの取得
            DatePicker datePicker = mStartDatePickerDialog.getDatePicker();
            // 日付設定
            Calendar calendar = Calendar.getInstance();
            if (mDataSet.startDate != INVALID_LONG_VALUE) {
                calendar.setTime(new Date(mDataSet.startDate));
            }
            datePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), null);
        });
    }

    /**
     * 終了時間設定
     */
    void setupEndDate() {
        if (mDataSet.endDate != INVALID_LONG_VALUE) {
            mBinding.detailContainer.editEndDate.setText(DateFormat.getDateInstance().format(mDataSet.endDate));
        }
        mBinding.detailContainer.layoutEndDate.setOnClickListener(v -> {
            // 日付設定ダイアログの表示
            mEndDatePickerDialog.show();
            // 日付設定ダイアログの取得
            DatePicker datePicker = mEndDatePickerDialog.getDatePicker();
            // 日付設定
            Calendar calendar = Calendar.getInstance();
            if (mDataSet.endDate != INVALID_LONG_VALUE) {
                calendar.setTime(new Date(mDataSet.endDate));
            }
            datePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), null);

        });
    }

    /**
     * プログレス設定
     */
    void setupProgress() {
        final String[] progress_items = getResources().getStringArray(R.array.progress_items);
        switch (mDataSet.progressState) {
            case NOT_START: {
                mBinding.detailContainer.iconProgress.setImageResource(R.drawable.ic_do_not_disturb_on_black_24dp);
                mBinding.detailContainer.editProgress.setText(progress_items[0]);
                break;
            }
            case INPROGRESS: {
                mBinding.detailContainer.iconProgress.setImageResource(R.drawable.ic_play_circle_filled_black_24dp);
                mBinding.detailContainer.editProgress.setText(progress_items[1]);
                break;
            }
            case COMPLETED: {
                mBinding.detailContainer.iconProgress.setImageResource(R.drawable.ic_check_circle_black_24dp);
                mBinding.detailContainer.editProgress.setText(progress_items[2]);
                break;
            }
            case WAITING: {
                mBinding.detailContainer.iconProgress.setImageResource(R.drawable.ic_pause_circle_filled_black_24dp);
                mBinding.detailContainer.editProgress.setText(progress_items[3]);
                break;
            }
            case POSTPONEMENT: {
                mBinding.detailContainer.iconProgress.setImageResource(R.drawable.ic_cancel_black_24dp);
                mBinding.detailContainer.editProgress.setText(progress_items[4]);
                break;
            }
            default: {
                mBinding.detailContainer.iconProgress.setImageResource(R.drawable.ic_lens_black_24dp);
                mBinding.detailContainer.editProgress.setText(progress_items[0]);
                break;
            }
        }
        mBinding.detailContainer.layoutProgress.setOnClickListener(v -> {
            final String[] items = getResources().getStringArray(R.array.progress_items);
            int defaultItem; // デフォルトでチェックされている項目
            switch (mDataSet.progressState) {
                case INPROGRESS: {
                    defaultItem = 1;
                    break;
                }
                case COMPLETED: {
                    defaultItem = 2;
                    break;
                }
                case WAITING: {
                    defaultItem = 3;
                    break;
                }
                case POSTPONEMENT: {
                    defaultItem = 4;
                    break;
                }
                default: {
                    defaultItem = 0;
                    break;
                }
            }

            final List<Integer> checkedItems = new ArrayList<>();

            checkedItems.add(defaultItem);
            new AlertDialog.Builder(TaskEditActivity.this)
                    .setTitle(getResources().getString(R.string.progress))
                    .setSingleChoiceItems(items, defaultItem, (dialog, which) -> {
                        checkedItems.clear();
                        checkedItems.add(which);
                    })
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        if (!checkedItems.isEmpty()) {
                            switch (checkedItems.get(0)) {
                                case 1: {
                                    mDataSet.progressState = INPROGRESS;
                                    break;
                                }
                                case 2: {
                                    mDataSet.progressState = PROGRESS.COMPLETED;
                                    break;
                                }
                                case 3: {
                                    mDataSet.progressState = PROGRESS.WAITING;
                                    break;
                                }
                                case 4: {
                                    mDataSet.progressState = PROGRESS.POSTPONEMENT;
                                    break;
                                }
                                default: {
                                    mDataSet.progressState = NOT_START;
                                    break;
                                }
                            }
                            setupProgress();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();

        });
    }

    /**
     * 優先度設定
     */
    void setupPiriority() {
        final String[] priority_items = getResources().getStringArray(R.array.priority_items);
        switch (mDataSet.priority) {
            case HIGH: {
                mBinding.detailContainer.iconPriority.setImageResource(R.drawable.ic_trending_up_black_24dp);
                mBinding.detailContainer.editPriority.setText(priority_items[0]);
                break;
            }
            case MIDDLE: {
                mBinding.detailContainer.iconPriority.setImageResource(R.drawable.ic_trending_flat_black_24dp);
                mBinding.detailContainer.editPriority.setText(priority_items[1]);
                break;
            }
            case LOW: {
                mBinding.detailContainer.iconPriority.setImageResource(R.drawable.ic_trending_down_black_24dp);
                mBinding.detailContainer.editPriority.setText(priority_items[2]);
                break;
            }
            default: {
                mBinding.detailContainer.iconPriority.setImageResource(R.drawable.ic_trending_flat_black_24dp);
                mBinding.detailContainer.editPriority.setText(priority_items[0]);
                break;
            }
        }
        mBinding.detailContainer.layoutPriority.setOnClickListener(v -> {
            final String[] items = getResources().getStringArray(R.array.priority_items);
            int defaultItem; // デフォルトでチェックされている項目
            switch (mDataSet.priority) {
                case HIGH: {
                    defaultItem = 0;
                    break;
                }
                case MIDDLE: {
                    defaultItem = 1;
                    break;
                }
                default: {
                    defaultItem = 2;
                    break;
                }
            }
            final List<Integer> checkedItems = new ArrayList<>();
            checkedItems.add(defaultItem);
            new AlertDialog.Builder(TaskEditActivity.this)
                    .setTitle(getResources().getString(R.string.priority))
                    .setSingleChoiceItems(items, defaultItem, (dialog, which) -> {
                        checkedItems.clear();
                        checkedItems.add(which);
                    })
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        if (!checkedItems.isEmpty()) {
                            switch (checkedItems.get(0)) {
                                case 0: {
                                    mDataSet.priority = HIGH;
                                    break;
                                }
                                case 2: {
                                    mDataSet.priority = LOW;
                                    break;
                                }
                                default: {
                                    mDataSet.priority = MIDDLE;
                                    break;
                                }
                            }
                            setupPiriority();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        });
    }

    /**
     * レート設定
     */
    public void setupRate() {
        final String[] rate_items = getResources().getStringArray(R.array.rate_items);
        mBinding.detailContainer.editRate.setText(rate_items[mDataSet.rate]);
        mBinding.detailContainer.layoutRate.setOnClickListener(v -> {
            final String[] items = getResources().getStringArray(R.array.rate_items);
            int defaultItem = mDataSet.rate; // デフォルトでチェックされている項目
            final List<Integer> checkedItems = new ArrayList<>();
            checkedItems.add(defaultItem);
            new AlertDialog.Builder(TaskEditActivity.this)
                    .setTitle(getResources().getString(R.string.rate))
                    .setSingleChoiceItems(items, defaultItem, (dialog, which) -> {
                        checkedItems.clear();
                        checkedItems.add(which);
                    })
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        if (!checkedItems.isEmpty()) {
                            mDataSet.rate = which;
                        }
                        setupRate();
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBackPressed() {
        //結果の設定
        setResult();
        super.onBackPressed();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {//結果の設定
            setResult();
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 結果設定
     */
    private void setResult() {
        Intent intent = getIntent();
        if (mBinding.detailContainer.editName.getText().toString().equals(INVALID_STRING_VALUE)) {
            setResult(RESULT_CANCELED, intent);
        } else if (mBinding.detailContainer.editName.getText().toString().equals(mDataSet.displayName)
                && mBinding.detailContainer.editNote.getText().toString().equals(mDataSet.note)
                ) {
            if(mDataSet.progressState != mBackup.progressState
                    || mDataSet.priority != mBackup.priority
                    || mDataSet.rate != mBackup.rate
                    || mDataSet.startDate !=  mBackup.startDate
                    || mDataSet.endDate != mBackup.endDate
                    ) {
                // 変更ありの場合、編集日を最新にする
                mDataSet.modifiedDate = System.currentTimeMillis();
                Bundle bundle = new Bundle();
                bundle.putParcelable(EXTRA_ASSET, mDataSet);
                intent.putExtras(bundle);
                setResult(RESULT_OK, intent);
            } else {
                setResult(RESULT_CANCELED, intent);
            }
        } else {
            mDataSet.displayName = mBinding.detailContainer.editName.getText().toString();
            mDataSet.note = mBinding.detailContainer.editNote.getText().toString();
            // 変更ありの場合、編集日を最新にする
            mDataSet.modifiedDate = System.currentTimeMillis();
            Bundle bundle = new Bundle();
            bundle.putParcelable(EXTRA_ASSET, mDataSet);
            intent.putExtras(bundle);
            setResult(RESULT_OK, intent);
        }
    }
}
