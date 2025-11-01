package topgrade.parent.com.parentseeks.Parent.Utils;

import android.util.Log;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

/**
 * Safe logging interceptor that doesn't consume the response body
 */
public class SafeLoggingInterceptor implements Interceptor {
    private static final String TAG = "SafeLogging";
    private final boolean isDebug;

    public SafeLoggingInterceptor(boolean isDebug) {
        this.isDebug = isDebug;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        
        if (isDebug) {
            Log.d(TAG, "Request URL: " + request.url());
            Log.d(TAG, "Request Method: " + request.method());
        }
        
        Response response = chain.proceed(request);
        
        if (isDebug && response.body() != null) {
            // Clone the response body so we don't consume it
            ResponseBody responseBody = response.body();
            BufferedSource source = responseBody.source();
            source.request(Long.MAX_VALUE); // Buffer the entire body
            Buffer buffer = source.getBuffer().clone(); // Clone so we don't consume
            
            String responseBodyString = buffer.readString(StandardCharsets.UTF_8);
            
            // Log first 500 characters
            int logLength = Math.min(500, responseBodyString.length());
            Log.d(TAG, "Response Code: " + response.code());
            Log.d(TAG, "Response Body (first " + logLength + " chars): " + 
                  responseBodyString.substring(0, logLength));
            
            // Check if response looks like double-encoded JSON
            if (responseBodyString.startsWith("\"") && responseBodyString.endsWith("\"")) {
                Log.w(TAG, "WARNING: Response appears to be double-encoded (wrapped in quotes)");
            }
        }
        
        return response;
    }
}

