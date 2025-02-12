package org.lineageos.setupwizard.download;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * RetryInterceptor retries a network request up to 3 times if it fails.
 * Throws IOException if all retry attempts fail.
 */

public class RetryInterceptor implements Interceptor {
    private final String TAG = "RetryInterceptor";
    private final String CANCELED = "Canceled";

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = null;
        boolean responseOK = false;
        byte tryCount = 0;
        while (!responseOK && tryCount < 3) {
            response = chain.proceed(request);
            responseOK = response.isSuccessful();
            tryCount++;
        }
        if (response == null || !responseOK) {
            throw new IOException("Failed to execute request after " + tryCount + " attempts");
        }
        return response;
    }
}
