package topgrade.parent.com.parentseeks.Parent.Adaptor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import topgrade.parent.com.parentseeks.R;

public class TestAdapto extends RecyclerView.Adapter<TestAdapto.TestViewHolder> {

    private final List<String> testNameList;
    private final Context context;

    public TestAdapto(List<String> testNameList) {
        this.testNameList = testNameList;
        this.context = null; // Context not needed for simple text display
    }

    @NonNull
    @Override
    public TestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.test_item_layout, parent, false);
        return new TestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TestViewHolder holder, int position) {
        String testName = testNameList.get(position);
        if (testName != null) {
            holder.tvTestName.setText(testName);
        } else {
            holder.tvTestName.setText("N/A");
        }
    }

    @Override
    public int getItemCount() {
        return testNameList != null ? testNameList.size() : 0;
    }

    public static class TestViewHolder extends RecyclerView.ViewHolder {
        TextView tvTestName;

        public TestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTestName = itemView.findViewById(R.id.tv_test_name);
        }
    }
}
