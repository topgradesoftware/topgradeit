package topgrade.parent.com.parentseeks.Parent.Utils;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.lang.ref.WeakReference;

public class HelperRequestQueue {
    private WeakReference<Context> contextRef;
    private static HelperRequestQueue request_instance;
    private RequestQueue requestQueue;

    private HelperRequestQueue(Context context) {
        this.contextRef = new WeakReference<>(context);
        requestQueue = getRequestQueue();
    }

    private RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            Context context = contextRef.get();
            if (context != null) {
                requestQueue = Volley.newRequestQueue(context);
            }
        }
        return requestQueue;
    }

    public static synchronized HelperRequestQueue getRequestInstance(Context context) {
        if (request_instance == null || request_instance.contextRef.get() == null) {
            request_instance = new HelperRequestQueue(context.getApplicationContext());
        }
        return request_instance;
    }

    public <T> void addRequest(Request<T> request) {
        RequestQueue queue = getRequestQueue();
        if (queue != null) {
            queue.add(request);
        }
    }

    /**
     * Cancel all requests for a specific tag
     * @param tag The tag to cancel requests for
     */
    public void cancelAll(Object tag) {
        RequestQueue queue = getRequestQueue();
        if (queue != null) {
            queue.cancelAll(tag);
        }
    }

    /**
     * Clear the singleton instance (useful for testing or memory management)
     */
    public static void clearInstance() {
        request_instance = null;
    }

    /**
     * Check if the context is still valid
     * @return true if context is available, false otherwise
     */
    public boolean isContextValid() {
        return contextRef.get() != null;
    }
}