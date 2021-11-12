package jp.osaka.cherry.work.tasksdetails;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jp.osaka.cherry.work.R;
import jp.osaka.cherry.work.constants.PROGRESS;
import jp.osaka.cherry.work.data.Asset;
import jp.osaka.cherry.work.databinding.TasksDetailsActivityBinding;

import static jp.osaka.cherry.work.constants.EXTRA.EXTRA_ASSETS;

/**
 * タスク詳細画面
 */
public class TasksDetailsActivity extends AppCompatActivity {

    /**
     * @serial データセット
     */
    private ArrayList<Asset> mDataSet;

    /**
     * @serial チャート
     */
    private PieChart mPieChart;

    /**
     * インテント生成
     *
     * @param context コンテキスト
     * @param assets 一覧
     * @return インテント
     */
    public static Intent createIntent(Context context, ArrayList<Asset> assets) {
        Intent intent = new Intent(context, TasksDetailsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(EXTRA_ASSETS, assets);
        intent.putExtras(bundle);
        return intent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // インテントの取得
        Intent intent = getIntent();
        mDataSet = intent.getParcelableArrayListExtra(EXTRA_ASSETS);

        // レイアウトの設定
        TasksDetailsActivityBinding mBinding = DataBindingUtil.setContentView(this, R.layout.tasks_details_activity);

        // ツールバーの設定
        setSupportActionBar(mBinding.toolbar);
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
        }

        mPieChart = mBinding.pieChart;

        setupPieChartView();
    }

    /**
     * 円チャート設定
     */
    private void setupPieChartView() {
        float a_count = 0;
        float b_count = 0;
        float c_count = 0;

        mPieChart.setUsePercentValues(true);
        mPieChart.setDescription(" ");

        Legend legend = mPieChart.getLegend();
        legend.setPosition(Legend.LegendPosition.BELOW_CHART_LEFT);

        for(Asset task : mDataSet) {
            if(task.progressState.equals(PROGRESS.NOT_START)) {
                ++a_count;
            }
            if (task.progressState.equals(PROGRESS.INPROGRESS)) {
                ++b_count;
            }
            if(task.progressState.equals(PROGRESS.COMPLETED)) {
                ++c_count;
            }
        }

        List<Float> values = Arrays.asList(c_count, b_count, a_count);
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            entries.add(new Entry(values.get(i), i));
        }

        PieDataSet dataSet = new PieDataSet(entries, " ");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setDrawValues(true);

        List<String> labels = Arrays.asList(
                getString(R.string.progress_completed),
                getString(R.string.progress_inprogress),
                getString(R.string.progress_not_start)
        );
        PieData pieData = new PieData(labels, dataSet);
        pieData.setValueFormatter(new PercentFormatter());
        pieData.setValueTextSize(14f);
        pieData.setValueTextColor(Color.WHITE);

        mPieChart.setData(pieData);
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
     * 結果の設定
     */
    private void setResult() {
        Intent intent = getIntent();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(EXTRA_ASSETS, mDataSet);
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
    }

}
