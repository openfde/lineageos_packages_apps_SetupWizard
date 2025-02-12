package org.lineageos.setupwizard.download;

import android.util.Log;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HttpUtils {
    private static final String TAG = "HttpUtils";
    //
    public static String APP_INFO_URL = "https://gitee.com/openfde/provision/releases/download/1.3.2/apps.json";

    private static final OkHttpClient client;

    static {
        // Creating a Dispatcher instance and setting the number of parallel requests
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(2);
        dispatcher.setMaxRequestsPerHost(1);

        client = new OkHttpClient.Builder()
                .connectTimeout(3, TimeUnit.SECONDS)
                .addInterceptor(new RetryInterceptor())
                .dispatcher(dispatcher)
                .build();
    }

    public static Call get(String url, final HttpCallback callback) {
        Request request = new Request.Builder().url(url).build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "onFailure, Exception = " + e.getMessage());
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (response.isSuccessful()) {
                    callback.onResponse(response);
                } else {
                    Log.e(TAG, "onResponse, Unexpected code = " + response);
                    callback.onError(new IOException("Unexpected code " + response));
                }
            }
        });
        return call;
    }

    public interface HttpCallback {
        void onResponse(Response response);

        void onFailure(Exception e);

        void onError(IOException e);
    }
}
