package topgrade.parent.com.parentseeks.Teacher.Adaptor;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import topgrade.parent.com.parentseeks.R;
import topgrade.parent.com.parentseeks.Teacher.Activity.StaffAssignTaskResponse;
import topgrade.parent.com.parentseeks.Teacher.Activity.TaskUpdateListener;
import topgrade.parent.com.parentseeks.Teacher.Model.Task;
import topgrade.parent.com.parentseeks.Teacher.Utils.Constant;
import topgrade.parent.com.parentseeks.Teacher.Utils.Util;

public class TaskAdaptor extends ListAdapter<Task, TaskAdaptor.Holder> {

    private Context context;
    private TaskUpdateListener taskUpdateListener;
    private String filterType = "";

    // DiffUtil callback for efficient list updates
    public static final DiffUtil.ItemCallback<Task> DIFF_CALLBACK = new DiffUtil.ItemCallback<Task>() {
        @Override
        public boolean areItemsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
            return oldItem.getTaskId().equals(newItem.getTaskId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
            return oldItem.getTaskTitle().equals(newItem.getTaskTitle()) &&
                   oldItem.getTaskBody().equals(newItem.getTaskBody()) &&
                   oldItem.getIsCompleted().equals(newItem.getIsCompleted()) &&
                   oldItem.getTaskResponse().equals(newItem.getTaskResponse());
        }
    };

    public TaskAdaptor(List<Task> list, Context context, TaskUpdateListener taskUpdateListener) {
        super(DIFF_CALLBACK);
        this.context = context;
        this.taskUpdateListener = taskUpdateListener;
        submitList(list);
    }
    
    public TaskAdaptor(List<Task> list, Context context, TaskUpdateListener taskUpdateListener, String filterType) {
        super(DIFF_CALLBACK);
        this.context = context;
        this.taskUpdateListener = taskUpdateListener;
        this.filterType = filterType;
        submitList(list);
    }

    @NonNull
    @Override
    public TaskAdaptor.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.solve_unsolve_application_layout, parent, false);
        return new TaskAdaptor.Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskAdaptor.Holder holder, int position) {
        try {
            Task task = getItem(position);
            
            // Set serial number
            if (holder.serial_number != null) {
                holder.serial_number.setText(String.valueOf(position + 1));
            }
            
            if (holder.head != null) {
                holder.head.setText(task.getTaskTitle());
            }
            
            // Check task status
            boolean isCompleted = "1".equals(task.getIsCompleted());
            String taskResponse = task.getTaskResponse();
            boolean hasResponse = !TextUtils.isEmpty(taskResponse) && !taskResponse.equals("null");
            
            if (holder.body != null) {
                // Hide task body for completed tasks and incomplete tasks (tasks with response)
                if (isCompleted || hasResponse) {
                    holder.body.setVisibility(View.GONE);
                } else {
                    // Only show body for pending tasks (no response yet)
                    holder.body.setVisibility(View.VISIBLE);
                    holder.body.setText(task.getTaskBody());
                }
            }
            
            if (holder.date != null) {
                String time = Util.formatDate("yyyy-MM-dd h:mm:ss",
                        "dd-MM-yyyy hh:mm a", task.getTimestamp());
                holder.date.setText(time);
            }
            
            // Set existing response if available
            if (holder.response_body != null && holder.response_layout != null) {
                String existingResponse = task.getTaskResponse();
                if (!TextUtils.isEmpty(existingResponse) && !existingResponse.equals("null")) {
                    holder.response_body.setText(existingResponse);
                    holder.response_layout.setVisibility(View.VISIBLE);
                } else {
                    holder.response_layout.setVisibility(View.GONE);
                }
            }
            
            // Show lock icon for completed tasks
            updateLockIcon(holder, task);
            
            // Set header color based on task status (only for "View All Tasks")
            updateHeaderColor(holder, task);
            
            // Set click listener for the entire card
            holder.itemView.setOnClickListener(v -> openResponseScreen(task));
            
            // Keep buttons hidden for uniform design
            if (holder.completed != null) {
                holder.completed.setVisibility(View.GONE);
            }
            if (holder.incomplete != null) {
                holder.incomplete.setVisibility(View.GONE);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Update lock icon visibility based on task completion status
     */
    private void updateLockIcon(TaskAdaptor.Holder holder, Task task) {
        if (holder.lock_icon != null) {
            String isCompleted = task.getIsCompleted();
            if ("1".equals(isCompleted)) {
                holder.lock_icon.setVisibility(View.VISIBLE);
            } else {
                holder.lock_icon.setVisibility(View.GONE);
            }
        }
    }
    
    /**
     * Update header color based on task status and filter type
     */
    private void updateHeaderColor(TaskAdaptor.Holder holder, Task task) {
        // Find the header LinearLayout in the card
        View headerLayout = holder.itemView.findViewById(R.id.header_layout);
        if (headerLayout != null) {
            int backgroundColor;
            
            // If viewing all tasks, color by task status
            if (Constant.FILTER_ALL.equals(filterType)) {
                String isCompleted = task.getIsCompleted();
                String taskResponse = task.getTaskResponse();
                
                // Determine task status and set appropriate color
                if ("1".equals(isCompleted)) {
                    // Completed task - Green
                    backgroundColor = context.getResources().getColor(R.color.success_500);
                } else if (!TextUtils.isEmpty(taskResponse) && !taskResponse.equals("null")) {
                    // Has response but not completed - Red (Incomplete)
                    backgroundColor = context.getResources().getColor(R.color.error_500);
                } else {
                    // No response - Orange (Pending)
                    backgroundColor = context.getResources().getColor(R.color.orange);
                }
            } else {
                // For filtered views, use the filter type color
                if (Constant.FILTER_PENDING.equals(filterType)) {
                    backgroundColor = context.getResources().getColor(R.color.orange);
                } else if (Constant.FILTER_INCOMPLETE.equals(filterType)) {
                    backgroundColor = context.getResources().getColor(R.color.error_500);
                } else if (Constant.FILTER_COMPLETED.equals(filterType)) {
                    backgroundColor = context.getResources().getColor(R.color.success_500);
                } else {
                    // Default navy blue
                    backgroundColor = context.getResources().getColor(R.color.navy_blue);
                }
            }
            
            headerLayout.setBackgroundColor(backgroundColor);
        }
    }
    
    /**
     * Open dedicated response screen for task
     */
    private void openResponseScreen(Task task) {
        Intent intent = new Intent(context, StaffAssignTaskResponse.class);
        intent.putExtra("task_id", task.getTaskId());
        intent.putExtra("task_title", task.getTaskTitle());
        intent.putExtra("task_body", task.getTaskBody());
        intent.putExtra("task_timestamp", task.getTimestamp());
        intent.putExtra("task_response", task.getTaskResponse());
        intent.putExtra("is_completed", task.getIsCompleted());
        
        // Start activity for result so we can refresh the list when coming back
        if (context instanceof Activity) {
            ((Activity) context).startActivityForResult(intent, 100);
        } else {
            context.startActivity(intent);
        }
    }

    /**
     * Update the list data using DiffUtil for efficient updates
     * This improves performance and maintains scroll position
     */
    public void updateList(List<Task> newList) {
        submitList(newList);
    }


    public static class Holder extends RecyclerView.ViewHolder {
        private final TextView head;
        private final TextView date;
        private final TextView body;
        private final TextView response_body;
        private final Button completed;
        private final Button incomplete;
        private final android.widget.ImageView lock_icon;
        private final TextView serial_number;
        private final View response_layout;

        public Holder(@NonNull View itemView) {
            super(itemView);

            serial_number = itemView.findViewById(R.id.serial_number);
            head = itemView.findViewById(R.id.head);
            date = itemView.findViewById(R.id.date_header_text);
            body = itemView.findViewById(R.id.body);
            response_body = itemView.findViewById(R.id.response_body);
            response_layout = itemView.findViewById(R.id.Response_layout);
            completed = itemView.findViewById(R.id.completed);
            incomplete = itemView.findViewById(R.id.incomplete);
            lock_icon = itemView.findViewById(R.id.lock_icon);
        }
    }
}
