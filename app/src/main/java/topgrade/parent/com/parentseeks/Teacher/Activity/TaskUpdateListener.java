package topgrade.parent.com.parentseeks.Teacher.Activity;

/**
 * Interface for activities that need to handle task updates
 */
public interface TaskUpdateListener {
    /**
     * Update task status and response
     * @param taskId Task ID to update
     * @param isCompleted Completion status ("0" or "1")
     * @param response Task response text
     */
    void updateTask(String taskId, String isCompleted, String response);
}

