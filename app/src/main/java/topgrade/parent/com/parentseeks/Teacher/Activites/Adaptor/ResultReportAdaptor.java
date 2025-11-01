package topgrade.parent.com.parentseeks.Teacher.Activites.Adaptor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Teacher.Activites.Model.ProgressReport;

public class ResultReportAdaptor extends RecyclerView.Adapter<ResultReportAdaptor.SubjectNameHolder> {


    View v;
    List<ProgressReport> list;

    public ResultReportAdaptor(List<ProgressReport> list) {
        this.list = list;
    }


    @NonNull
    @Override
    public SubjectNameHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.subject_progress_item2, viewGroup,
                false);
        return new ResultReportAdaptor.SubjectNameHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectNameHolder holder, int position) {
        if (position < list.size()) {
            ProgressReport report = list.get(position);
            
            if (report != null) {
                // Set subject and class name
                String subjectText = "";
                if (report.getClassName() != null) {
                    subjectText += report.getClassName();
                }
                if (report.getSubjectName() != null) {
                    subjectText += " (" + report.getSubjectName() + " )";
                }
                holder.Subjects.setText(subjectText);

                // Set numeric values with null checks
                holder.Test_No.setText(report.getNoOfTest() != null ? String.valueOf(report.getNoOfTest()) : "0");
                holder.Students_Appeared.setText(report.getStudentsAppeared() != null ? String.valueOf(report.getStudentsAppeared()) : "0");
                holder.Total_Students.setText(report.getStudents() != null ? String.valueOf(report.getStudents()) : "0");
                
                // Set percentage values with validation
                if (report.getPassedpercent() != null) {
                    holder.Passed_per.setText(report.getPassedpercent() + "%");
                } else {
                    holder.Passed_per.setText("0%");
                }
                
                if (report.getFailedpercent() != null) {
                    holder.Failed_per.setText(report.getFailedpercent() + "%");
                } else {
                    holder.Failed_per.setText("0%");
                }
                
                // Set percentage range values with validation
                if (report.get_90percent() != null) {
                    holder.above_90.setText(String.valueOf(report.get_90percent()));
                } else {
                    holder.above_90.setText("0");
                }
                
                if (report.get_80percent() != null) {
                    holder.per_81_90.setText(String.valueOf(report.get_80percent()));
                } else {
                    holder.per_81_90.setText("0");
                }
                
                if (report.get_70percent() != null) {
                    holder.per_71_80.setText(String.valueOf(report.get_70percent()));
                } else {
                    holder.per_71_80.setText("0");
                }
                
                if (report.get_60percent() != null) {
                    holder.per_61_70.setText(String.valueOf(report.get_60percent()));
                } else {
                    holder.per_61_70.setText("0");
                }
                
                if (report.get_50percent() != null) {
                    holder.per_51_60.setText(String.valueOf(report.get_50percent()));
                } else {
                    holder.per_51_60.setText("0");
                }
                
                if (report.get_33percent() != null) {
                    holder.per_33_50.setText(String.valueOf(report.get_33percent()));
                } else {
                    holder.per_33_50.setText("0");
                }
                
                if (report.get_25percent() != null) {
                    holder.per_25_33.setText(String.valueOf(report.get_25percent()));
                } else {
                    holder.per_25_33.setText("0");
                }
                
                // Validate that percentages add up correctly
                int totalPercent = (report.getPassedpercent() != null ? report.getPassedpercent() : 0) + 
                                 (report.getFailedpercent() != null ? report.getFailedpercent() : 0);
                
                if (totalPercent != 100) {
                    System.out.println("WARNING: Total percentage is " + totalPercent + "% instead of 100%");
                }
                
                // Validate that percentage ranges add up to passed students
                int totalInRanges = (report.get_90percent() != null ? report.get_90percent() : 0) +
                                   (report.get_80percent() != null ? report.get_80percent() : 0) +
                                   (report.get_70percent() != null ? report.get_70percent() : 0) +
                                   (report.get_60percent() != null ? report.get_60percent() : 0) +
                                   (report.get_50percent() != null ? report.get_50percent() : 0) +
                                   (report.get_33percent() != null ? report.get_33percent() : 0) +
                                   (report.get_25percent() != null ? report.get_25percent() : 0);
                
                int passedStudents = report.getPassed() != null ? report.getPassed() : 0;
                
                if (totalInRanges != passedStudents) {
                    System.out.println("WARNING: Percentage ranges total (" + totalInRanges + 
                                     ") doesn't match passed students (" + passedStudents + ")");
                }
                
                // Set passed/failed values with validation
                Integer passed = report.getPassed();
                Integer failed = report.getFailed();
                Integer passedPercent = report.getPassedpercent();
                Integer failedPercent = report.getFailedpercent();
                
                // Validate passed/failed values
                if (passed != null && passed < 0) passed = 0;
                if (failed != null && failed < 0) failed = 0;
                
                holder.Passed.setText(passed != null ? String.valueOf(passed) : "0");
                holder.Failed.setText(failed != null ? String.valueOf(failed) : "0");
                
                // Validate and fix percentage values
                if (passedPercent != null) {
                    if (passedPercent < 0) passedPercent = 0;
                    if (passedPercent > 100) passedPercent = 100;
                }
                if (failedPercent != null) {
                    if (failedPercent < 0) failedPercent = 0;
                    if (failedPercent > 100) failedPercent = 100;
                }
                
                // Ensure total percentage doesn't exceed 100%
                if (passedPercent != null && failedPercent != null) {
                    int total = passedPercent + failedPercent;
                    if (total > 100) {
                        // Adjust to make total 100%
                        if (passedPercent > failedPercent) {
                            passedPercent = 100 - failedPercent;
                        } else {
                            failedPercent = 100 - passedPercent;
                        }
                    }
                }
                
                holder.Passed_per.setText(passedPercent != null ? String.valueOf(passedPercent) : "0");
                holder.Failed_per.setText(failedPercent != null ? String.valueOf(failedPercent) : "0");
            }
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class SubjectNameHolder extends RecyclerView.ViewHolder {

        public TextView Subjects, Test_No, Students_Appeared;
        public TextView Total_Students, above_90, per_81_90;
        public TextView per_71_80, per_61_70, per_51_60;
        public TextView per_33_50, per_25_33, Passed;
        public TextView Failed, Passed_per, Failed_per;

        public SubjectNameHolder(@NonNull View itemView) {
            super(itemView);
            Subjects = itemView.findViewById(R.id.Subjects);
            Test_No = itemView.findViewById(R.id.Test_No);
            Students_Appeared = itemView.findViewById(R.id.Students_Appeared);
            Total_Students = itemView.findViewById(R.id.Total_Students);
            above_90 = itemView.findViewById(R.id.above_90);
            per_81_90 = itemView.findViewById(R.id.per_81_90);
            per_71_80 = itemView.findViewById(R.id.per_71_80);
            per_61_70 = itemView.findViewById(R.id.per_61_70);
            per_51_60 = itemView.findViewById(R.id.per_51_60);
            per_33_50 = itemView.findViewById(R.id.per_33_50);
            per_25_33 = itemView.findViewById(R.id.per_25_33);
            Passed = itemView.findViewById(R.id.Passed);
            Failed = itemView.findViewById(R.id.Failed);
            Passed_per = itemView.findViewById(R.id.Passed_per);
            Failed_per = itemView.findViewById(R.id.Failed_per);

        }
    }
}
