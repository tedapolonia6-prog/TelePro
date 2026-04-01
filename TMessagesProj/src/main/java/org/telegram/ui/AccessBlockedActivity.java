package org.telegram.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.telegram.messenger.AccessCheckManager;

public class AccessBlockedActivity extends Activity {

    private TextView statusText;
    private Button retryButton;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        buildUI();
        startAccessCheck();
    }

    private void buildUI() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setBackgroundColor(Color.parseColor("#1A1A2E"));
        root.setPadding(dp(32), dp(32), dp(32), dp(32));

        TextView icon = new TextView(this);
        icon.setText("\uD83D\uDD12");
        icon.setTextSize(TypedValue.COMPLEX_UNIT_SP, 64);
        icon.setGravity(Gravity.CENTER);
        root.addView(icon);

        TextView title = new TextView(this);
        title.setText("TelePro");
        title.setTextColor(Color.WHITE);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
        title.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        title.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tp.topMargin = dp(16);
        root.addView(title, tp);

        statusText = new TextView(this);
        statusText.setText("\u0414\u043e\u0441\u0442\u0443\u043f \u043a \u043f\u0440\u0438\u043b\u043e\u0436\u0435\u043d\u0438\u044e \u0437\u0430\u0431\u043b\u043e\u043a\u0438\u0440\u043e\u0432\u0430\u043d.\n\n\u0414\u043b\u044f \u0430\u043a\u0442\u0438\u0432\u0430\u0446\u0438\u0438 \u043e\u0431\u0440\u0430\u0442\u0438\u0442\u0435\u0441\u044c \u043a \u0430\u0434\u043c\u0438\u043d\u0438\u0441\u0442\u0440\u0430\u0442\u043e\u0440\u0443.");
        statusText.setTextColor(Color.parseColor("#CCCCCC"));
        statusText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        statusText.setGravity(Gravity.CENTER);
        statusText.setLineSpacing(0, 1.4f);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        sp.topMargin = dp(24);
        root.addView(statusText, sp);

        progressBar = new ProgressBar(this);
        progressBar.setVisibility(View.GONE);
        LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(dp(48), dp(48));
        pp.gravity = Gravity.CENTER_HORIZONTAL;
        pp.topMargin = dp(24);
        root.addView(progressBar, pp);

        retryButton = new Button(this);
        retryButton.setText("\u041f\u043e\u0432\u0442\u043e\u0440\u0438\u0442\u044c \u043f\u0440\u043e\u0432\u0435\u0440\u043a\u0443");
        retryButton.setBackgroundColor(Color.parseColor("#4A90E2"));
        retryButton.setTextColor(Color.WHITE);
        retryButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        retryButton.setOnClickListener(v -> startAccessCheck());
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        bp.topMargin = dp(32);
        root.addView(retryButton, bp);

        String uuid = AccessCheckManager.getOrCreateDeviceUUID(this);
        TextView uuidText = new TextView(this);
        uuidText.setText("ID: " + uuid);
        uuidText.setTextColor(Color.parseColor("#555555"));
        uuidText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        uuidText.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams up = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        up.topMargin = dp(40);
        root.addView(uuidText, up);

        setContentView(root);
    }

    private void startAccessCheck() {
        progressBar.setVisibility(View.VISIBLE);
        retryButton.setEnabled(false);
        statusText.setText("\u041f\u0440\u043e\u0432\u0435\u0440\u043a\u0430 \u0434\u043e\u0441\u0442\u0443\u043f\u0430...");

        AccessCheckManager.checkAccess(this, new AccessCheckManager.AccessCheckCallback() {
            @Override
            public void onResult(boolean allowed) {
                progressBar.setVisibility(View.GONE);
                retryButton.setEnabled(true);
                if (allowed) {
                    launchApp();
                } else {
                    statusText.setText("\u0414\u043e\u0441\u0442\u0443\u043f \u043a \u043f\u0440\u0438\u043b\u043e\u0436\u0435\u043d\u0438\u044e \u0437\u0430\u0431\u043b\u043e\u043a\u0438\u0440\u043e\u0432\u0430\u043d.\n\n\u0414\u043b\u044f \u0430\u043a\u0442\u0438\u0432\u0430\u0446\u0438\u0438 \u043e\u0431\u0440\u0430\u0442\u0438\u0442\u0435\u0441\u044c \u043a \u0430\u0434\u043c\u0438\u043d\u0438\u0441\u0442\u0440\u0430\u0442\u043e\u0440\u0443.");
                }
            }
            @Override
            public void onError(Exception e) {
                progressBar.setVisibility(View.GONE);
                retryButton.setEnabled(true);
                launchApp();
            }
        });
    }

    private void launchApp() {
        Intent intent = new Intent(this, LaunchActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                getResources().getDisplayMetrics());
    }

    @Override
    public void onBackPressed() {
        // Block back button
    }
}
