package jp.osaka.cherry.work.taskdetails;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import java.text.DateFormat;

import jp.osaka.cherry.work.R;
import jp.osaka.cherry.work.data.Asset;
import jp.osaka.cherry.work.databinding.TaskDetailsActivityBinding;

import static jp.osaka.cherry.work.Config.LOG_I;
import static jp.osaka.cherry.work.constants.EXTRA.EXTRA_ASSET;
import static jp.osaka.cherry.work.constants.INVALID.INVALID_LONG_VALUE;
import static jp.osaka.cherry.work.constants.INVALID.INVALID_STRING_VALUE;

/**
 * 履歴内のタスク詳細画面
 */
public class TaskDetailsInHistoryActivity extends AppCompatActivity {

    /**
     * @serial バインディング
     */
    private TaskDetailsActivityBinding mBinding;

    /**
     * @serial データセット
     */
    private Asset mDataSet;

    /**
     * インテント生成
     *
     * @param context コンテキスト
     * @param asset アセット
     * @return インテント
     */
    public static Intent createIntent(Context context, Asset asset) {
        Intent intent = new Intent(context, TaskDetailsInHistoryActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_ASSET, asset);
        intent.putExtras(bundle);
        return intent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String TAG = "TaskDetailsInHistoryAct";
        if (LOG_I) {
            Log.i(TAG, "onCreate#enter");
        }

        // インテントの取得
        Intent intent = getIntent();
        mDataSet = intent.getParcelableExtra(EXTRA_ASSET);

        // レイアウトの設定
        mBinding = DataBindingUtil.setContentView(this, R.layout.task_details_activity);

        // ツールバーの設定
        setSupportActionBar(mBinding.toolbar);
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
        }

        // 表示の設定
        setView();

        if (LOG_I) {
            Log.i(TAG, "onCreate#leave");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            setResult();
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 表示設定
     */
    private void setView() {
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setTitle(mDataSet.displayName);
        }

        // ノート
        if(mDataSet.note.equals(INVALID_STRING_VALUE)) {
            mBinding.detailContainer.layoutNote.setVisibility(View.GONE);
        } else {
            mBinding.detailContainer.iconNote.setVisibility(View.VISIBLE);
            mBinding.detailContainer.noteText.setText(mDataSet.note);
            mBinding.detailContainer.layoutNote.setVisibility(View.VISIBLE);
        }

        // 開始日
        if(mDataSet.startDate == INVALID_LONG_VALUE) {
            mBinding.detailContainer.layoutStartDate.setVisibility(View.GONE);
        } else {
            mBinding.detailContainer.iconStartDate.setVisibility(View.VISIBLE);
            mBinding.detailContainer.startDateText.setText(DateFormat.getDateInstance().format(mDataSet.startDate));
            mBinding.detailContainer.layoutStartDate.setVisibility(View.VISIBLE);
        }

        // 終了日
        if(mDataSet.endDate == INVALID_LONG_VALUE) {
            mBinding.detailContainer.layoutEndDate.setVisibility(View.GONE);
        } else {
            mBinding.detailContainer.iconEndDate.setVisibility(View.VISIBLE);
            mBinding.detailContainer.endDateText.setText(DateFormat.getDateInstance().format(mDataSet.endDate));
            mBinding.detailContainer.layoutEndDate.setVisibility(View.VISIBLE);
        }

        // 進捗
        final String[] progress_items = getResources().getStringArray(R.array.progress_items);
        switch (mDataSet.progressState) {
            case NOT_START: {
                mBinding.detailContainer.iconProgress.setImageResource(R.drawable.ic_do_not_disturb_on_black_24dp);
                mBinding.detailContainer.progressText.setText(progress_items[0]);
                break;
            }
            case INPROGRESS: {
                mBinding.detailContainer.iconProgress.setImageResource(R.drawable.ic_play_circle_filled_black_24dp);
                mBinding.detailContainer.progressText.setText(progress_items[1]);
                break;
            }
            case COMPLETED: {
                mBinding.detailContainer.iconProgress.setImageResource(R.drawable.ic_lens_completed_24dp);
                mBinding.detailContainer.progressText.setText(progress_items[2]);
                break;
            }
            case WAITING: {
                mBinding.detailContainer.iconProgress.setImageResource(R.drawable.ic_pause_circle_filled_black_24dp);
                mBinding.detailContainer.progressText.setText(progress_items[3]);
                break;
            }
            case POSTPONEMENT: {
                mBinding.detailContainer.iconProgress.setImageResource(R.drawable.ic_cancel_black_24dp);
                mBinding.detailContainer.progressText.setText(progress_items[4]);
                break;
            }
            default: {
                mBinding.detailContainer.iconProgress.setImageResource(R.drawable.ic_lens_black_24dp);
                mBinding.detailContainer.progressText.setText(progress_items[0]);
                break;
            }
        }

        // 優先度
        final String[] priority_items = getResources().getStringArray(R.array.priority_items);
        switch (mDataSet.priority) {
            case HIGH: {
                mBinding.detailContainer.iconPriority.setImageResource(R.drawable.ic_trending_up_black_24dp);
                mBinding.detailContainer.priorityText.setText(priority_items[0]);
                break;
            }
            case MIDDLE: {
                mBinding.detailContainer.iconPriority.setImageResource(R.drawable.ic_trending_flat_black_24dp);
                mBinding.detailContainer.priorityText.setText(priority_items[1]);
                break;
            }
            case LOW: {
                mBinding.detailContainer.iconPriority.setImageResource(R.drawable.ic_trending_down_black_24dp);
                mBinding.detailContainer.priorityText.setText(priority_items[2]);
                break;
            }
            default: {
                mBinding.detailContainer.iconPriority.setImageResource(R.drawable.ic_trending_flat_black_24dp);
                mBinding.detailContainer.priorityText.setText(priority_items[0]);
                break;
            }
        }

        // レート
        final String[] rate_items = getResources().getStringArray(R.array.rate_items);
        mBinding.detailContainer.rateText.setText(rate_items[mDataSet.rate]);
    }

    /**
     * 結果設定
     */
    private void setResult() {
        Intent intent = getIntent();
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_ASSET, mDataSet);
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
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
}
