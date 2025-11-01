package topgrade.parent.com.parentseeks.Parent.Utils;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.List;

public class DayAxisValueFormatter extends ValueFormatter {

    private List<String> subject_name_list;
    private final BarLineChartBase<?> chart;

    public DayAxisValueFormatter(BarLineChartBase<?> chart, List<String> subject_name_list) {
        this.chart = chart;
        this.subject_name_list = subject_name_list;
    }

    @Override
    public String getFormattedValue(float value) {
        try {

            int days = (int) value;
            return subject_name_list.get(days);

        } catch (Exception e) {
            return "error";

        }
    }


}
