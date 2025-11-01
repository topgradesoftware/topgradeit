package topgrade.parent.com.parentseeks.Parent.Utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import topgrade.parent.com.parentseeks.BuildConfig;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Memory Leak Detector and Prevention Utility
 * Monitors activities, fragments, and views for potential memory leaks
 */
public class MemoryLeakDetector {
    
    private static final String TAG = "MemoryLeakDetector";
    private static final boolean DEBUG_MODE = BuildConfig.DEBUG;
    
    // Track activities and fragments
    private static final Map<String, WeakReference<Activity>> activityReferences = new ConcurrentHashMap<>();
    private static final Map<String, WeakReference<Fragment>> fragmentReferences = new ConcurrentHashMap<>();
    private static final Map<String, WeakReference<View>> viewReferences = new ConcurrentHashMap<>();
    
    // Memory thresholds
    private static final double MEMORY_WARNING_THRESHOLD = 0.75; // 75%
    private static final double MEMORY_CRITICAL_THRESHOLD = 0.90; // 90%
    
    /**
     * Register activity for memory leak detection
     */
    public static void registerActivity(@NonNull Activity activity) {
        if (!DEBUG_MODE) return;
        
        String key = activity.getClass().getSimpleName() + "_" + System.identityHashCode(activity);
        activityReferences.put(key, new WeakReference<>(activity));
        
        Log.d(TAG, "Registered activity: " + key + " (Total: " + activityReferences.size() + ")");
    }
    
    /**
     * Unregister activity
     */
    public static void unregisterActivity(@NonNull Activity activity) {
        if (!DEBUG_MODE) return;
        
        String key = activity.getClass().getSimpleName() + "_" + System.identityHashCode(activity);
        activityReferences.remove(key);
        
        Log.d(TAG, "Unregistered activity: " + key + " (Remaining: " + activityReferences.size() + ")");
    }
    
    /**
     * Register fragment for memory leak detection
     */
    public static void registerFragment(@NonNull Fragment fragment) {
        if (!DEBUG_MODE) return;
        
        String key = fragment.getClass().getSimpleName() + "_" + System.identityHashCode(fragment);
        fragmentReferences.put(key, new WeakReference<>(fragment));
        
        Log.d(TAG, "Registered fragment: " + key + " (Total: " + fragmentReferences.size() + ")");
    }
    
    /**
     * Unregister fragment
     */
    public static void unregisterFragment(@NonNull Fragment fragment) {
        if (!DEBUG_MODE) return;
        
        String key = fragment.getClass().getSimpleName() + "_" + System.identityHashCode(fragment);
        fragmentReferences.remove(key);
        
        Log.d(TAG, "Unregistered fragment: " + key + " (Remaining: " + fragmentReferences.size() + ")");
    }
    
    /**
     * Register view for memory leak detection
     */
    public static void registerView(@NonNull View view, String tag) {
        if (!DEBUG_MODE) return;
        
        String key = tag + "_" + System.identityHashCode(view);
        viewReferences.put(key, new WeakReference<>(view));
        
        Log.d(TAG, "Registered view: " + key + " (Total: " + viewReferences.size() + ")");
    }
    
    /**
     * Unregister view
     */
    public static void unregisterView(@NonNull View view, String tag) {
        if (!DEBUG_MODE) return;
        
        String key = tag + "_" + System.identityHashCode(view);
        viewReferences.remove(key);
        
        Log.d(TAG, "Unregistered view: " + key + " (Remaining: " + viewReferences.size() + ")");
    }
    
    /**
     * Check for memory leaks
     */
    public static void checkMemoryLeaks(Context context) {
        if (!DEBUG_MODE) return;
        
        Log.d(TAG, "=== Memory Leak Detection Report ===");
        
        // Check activities
        checkActivityLeaks();
        
        // Check fragments
        checkFragmentLeaks();
        
        // Check views
        checkViewLeaks();
        
        // Check memory usage
        checkMemoryUsage(context);
        
        Log.d(TAG, "=== End Memory Leak Report ===");
    }
    
    /**
     * Check for activity memory leaks
     */
    private static void checkActivityLeaks() {
        List<String> leakedActivities = new ArrayList<>();
        
        for (Map.Entry<String, WeakReference<Activity>> entry : activityReferences.entrySet()) {
            Activity activity = entry.getValue().get();
            if (activity == null) {
                leakedActivities.add(entry.getKey());
            } else if (activity.isFinishing() || activity.isDestroyed()) {
                leakedActivities.add(entry.getKey());
            }
        }
        
        if (!leakedActivities.isEmpty()) {
            Log.w(TAG, "Potential activity memory leaks detected: " + leakedActivities.size());
            for (String leaked : leakedActivities) {
                Log.w(TAG, "  - " + leaked);
            }
        } else {
            Log.d(TAG, "No activity memory leaks detected");
        }
    }
    
    /**
     * Check for fragment memory leaks
     */
    private static void checkFragmentLeaks() {
        List<String> leakedFragments = new ArrayList<>();
        
        for (Map.Entry<String, WeakReference<Fragment>> entry : fragmentReferences.entrySet()) {
            Fragment fragment = entry.getValue().get();
            if (fragment == null) {
                leakedFragments.add(entry.getKey());
            } else if (fragment.isDetached() || fragment.isRemoving()) {
                leakedFragments.add(entry.getKey());
            }
        }
        
        if (!leakedFragments.isEmpty()) {
            Log.w(TAG, "Potential fragment memory leaks detected: " + leakedFragments.size());
            for (String leaked : leakedFragments) {
                Log.w(TAG, "  - " + leaked);
            }
        } else {
            Log.d(TAG, "No fragment memory leaks detected");
        }
    }
    
    /**
     * Check for view memory leaks
     */
    private static void checkViewLeaks() {
        List<String> leakedViews = new ArrayList<>();
        
        for (Map.Entry<String, WeakReference<View>> entry : viewReferences.entrySet()) {
            View view = entry.getValue().get();
            if (view == null) {
                leakedViews.add(entry.getKey());
            } else if (view.getParent() == null) {
                leakedViews.add(entry.getKey());
            }
        }
        
        if (!leakedViews.isEmpty()) {
            Log.w(TAG, "Potential view memory leaks detected: " + leakedViews.size());
            for (String leaked : leakedViews) {
                Log.w(TAG, "  - " + leaked);
            }
        } else {
            Log.d(TAG, "No view memory leaks detected");
        }
    }
    
    /**
     * Check memory usage
     */
    private static void checkMemoryUsage(Context context) {
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(memoryInfo);
        
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        long maxMemory = runtime.maxMemory();
        double memoryUsage = (double) usedMemory / maxMemory;
        
        Log.d(TAG, String.format("Memory Usage: %.1f%% (%.1f MB / %.1f MB)", 
                memoryUsage * 100, 
                usedMemory / 1024.0 / 1024.0, 
                maxMemory / 1024.0 / 1024.0));
        
        if (memoryUsage > MEMORY_CRITICAL_THRESHOLD) {
            Log.e(TAG, "CRITICAL: Memory usage is very high! Consider clearing caches.");
        } else if (memoryUsage > MEMORY_WARNING_THRESHOLD) {
            Log.w(TAG, "WARNING: Memory usage is high. Consider clearing caches.");
        }
    }
    
    /**
     * Clear all references (for testing or cleanup)
     */
    public static void clearAllReferences() {
        activityReferences.clear();
        fragmentReferences.clear();
        viewReferences.clear();
        Log.d(TAG, "All references cleared");
    }
    
    /**
     * Get memory leak statistics
     */
    public static String getMemoryLeakStats() {
        StringBuilder stats = new StringBuilder();
        stats.append("Memory Leak Statistics:\n");
        stats.append("Activities: ").append(activityReferences.size()).append("\n");
        stats.append("Fragments: ").append(fragmentReferences.size()).append("\n");
        stats.append("Views: ").append(viewReferences.size()).append("\n");
        
        return stats.toString();
    }
    
    /**
     * Force garbage collection and check for leaks
     */
    public static void forceGcAndCheckLeaks(Context context) {
        Log.d(TAG, "Forcing garbage collection...");
        System.gc();
        
        // Wait a bit for GC to complete
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        checkMemoryLeaks(context);
    }
} 