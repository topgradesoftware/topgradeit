package topgrade.parent.com.parentseeks.Parent.Utils;

import android.content.Context;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to provide safe methods for handling potentially null lists
 * This helps prevent NullPointerExceptions when working with lists from Paper DB
 */
public class ListSafetyUtils {
    
    /**
     * Safely gets the size of a list, returning 0 if the list is null
     * @param list The list to check
     * @return The size of the list, or 0 if null
     */
    public static int safeSize(List<?> list) {
        return list != null ? list.size() : 0;
    }
    
    /**
     * Safely checks if a list is not null and not empty
     * @param list The list to check
     * @return true if the list is not null and has elements, false otherwise
     */
    public static boolean isNotEmpty(List<?> list) {
        return list != null && !list.isEmpty();
    }
    
    /**
     * Safely checks if a list is null or empty
     * @param list The list to check
     * @return true if the list is null or empty, false otherwise
     */
    public static boolean isEmpty(List<?> list) {
        return list == null || list.isEmpty();
    }
    
    /**
     * Safely gets an element from a list at the specified position
     * @param list The list to get the element from
     * @param position The position of the element
     * @param <T> The type of the list elements
     * @return The element at the position, or null if the list is null or position is invalid
     */
    public static <T> T safeGet(List<T> list, int position) {
        if (list != null && position >= 0 && position < list.size()) {
            return list.get(position);
        }
        return null;
    }
    
    /**
     * Safely initializes a list if it's null
     * @param list The list to initialize
     * @param <T> The type of the list elements
     * @return The original list if not null, or a new ArrayList if null
     */
    public static <T> List<T> safeInit(List<T> list) {
        return list != null ? list : new ArrayList<>();
    }
    
    /**
     * Shows a toast message if a list is null or empty
     * @param context The context to show the toast
     * @param list The list to check
     * @param message The message to show if the list is null or empty
     * @return true if the list is null or empty (and toast was shown), false otherwise
     */
    public static boolean showToastIfEmpty(Context context, List<?> list, String message) {
        if (isEmpty(list)) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }
    
    /**
     * Safely iterates over a list with null check
     * @param list The list to iterate over
     * @param action The action to perform on each element
     * @param <T> The type of the list elements
     */
    public static <T> void safeForEach(List<T> list, ListAction<T> action) {
        if (isNotEmpty(list)) {
            for (int i = 0; i < list.size(); i++) {
                action.perform(list.get(i), i);
            }
        }
    }
    
    /**
     * Functional interface for list actions
     * @param <T> The type of the list elements
     */
    public interface ListAction<T> {
        void perform(T item, int position);
    }
}
