package org.telegram.messenger;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AccessCheckManager {

    private static final String PREFS_NAME = "telepro_prefs";
    private static final String PREF_DEVICE_UUID = "device_uuid";
    private static final String API_URL = "https://api.mebel-bruno.ru/api/check?uuid=";
    private static final int TIMEOUT_MS = 10000;

    public interface AccessCheckCallback {
        void onResult(boolean allowed);
        void onError(Exception e);
    }

    public static String getOrCreateDeviceUUID(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String uuid = prefs.getString(PREF_DEVICE_UUID, null);
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
            prefs.edit().putString(PREF_DEVICE_UUID, uuid).apply();
        }
        return uuid;
    }

    public static void checkAccess(Context context, AccessCheckCallback callback) {
        String uuid = getOrCreateDeviceUUID(context);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler mainHandler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                URL url = new URL(API_URL + uuid);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(TIMEOUT_MS);
                conn.setReadTimeout(TIMEOUT_MS);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) sb.append(line);
                    reader.close();
                    JSONObject json = new JSONObject(sb.toString());
                    boolean allowed = json.optBoolean("allowed", false);
                    mainHandler.post(() -> callback.onResult(allowed));
                } else {
                    mainHandler.post(() -> callback.onResult(true));
                }
                conn.disconnect();
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e));
            } finally {
                executor.shutdown();
            }
        });
    }
}
