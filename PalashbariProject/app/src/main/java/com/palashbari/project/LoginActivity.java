package com.palashbari.project;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.android.volley.VolleyError;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.appupdate.AppUpdateOptions;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.palashbari.project.util.API;
import com.palashbari.project.util.CheckInternetConnection;
import com.palashbari.project.util.JsonAPICall;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    CardView logIn;
    TextInputEditText userNameEditText,passwordEditText;
    LinearLayout noInternetLayout,selectRole;
    private Handler handler;
    private Runnable runnable;
    TextView logInLabel;
    UserManagement userManagement;
    ProgressBar progress;
    private ActivityResultLauncher activityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userManagement = new UserManagement(this);

        passwordEditText = findViewById(R.id.passwordEditText);
        userNameEditText = findViewById(R.id.userNameEditText);
        noInternetLayout = findViewById(R.id.noInternetLayout);
        logIn = findViewById(R.id.logIn);
        logInLabel = findViewById(R.id.logInLabel);
        progress = findViewById(R.id.progress);


        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    checkInternetConnectivity();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                handler.postDelayed(this, 5000);
            }
        };

        checkForUpdate();


        logIn.setOnClickListener(v -> {
            String userName = userNameEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            if (TextUtils.isEmpty(userNameEditText.getText())) {
                userNameEditText.setError("User Name is required");
                userNameEditText.requestFocus();
            } else if (TextUtils.isEmpty(passwordEditText.getText())) {
                passwordEditText.setError("Password is required");
                passwordEditText.requestFocus();
            } else {
                logInLabel.setVisibility(View.GONE);
                progress.setVisibility(View.VISIBLE);
                logIn.setEnabled(false);

                Map<String, String> params = new HashMap<>();
                params.put("username", userName);
                params.put("password", password);

                AsyncTaskLogin asyncTaskLogin = new AsyncTaskLogin(params);
                asyncTaskLogin.execute();
            }
        });
    }

    private void checkForUpdate(){
        AppUpdateManager appUpdateManager = AppUpdateManagerFactory.create(this);

        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        activityResultLauncher,
                        AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build());
            }
        });
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {

                if (result.getResultCode() != RESULT_OK) {
                }
            }
        });
    }

    public class AsyncTaskLogin extends AsyncTask<Void, Void, Void> {
        Map<String, String> formData;

        public AsyncTaskLogin(Map<String, String> params) {
            this.formData = params;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            JsonAPICall.makePostRequest(LoginActivity.this, API.log_in, formData, new JsonAPICall.VolleyCallback() {
                @Override
                public void onSuccess(String response) {
                    try {
                        handleResponse(response);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void onError(VolleyError error) {
                    String errorMessage = "Error: " + error.getMessage();
                    if (error.networkResponse.statusCode == 400) {
                        try {
                            String responseBody = new String(error.networkResponse.data);
                            JSONObject jsonObject = new JSONObject(responseBody);
                            if (jsonObject.has("message")) {
                                String message = jsonObject.getString("message");
                                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else if (error.networkResponse.statusCode == 401) {
                        try {
                            String responseBody = new String(error.networkResponse.data);
                            JSONObject jsonObject = new JSONObject(responseBody);
                            if (jsonObject.has("message")) {
                                String message = jsonObject.getString("message");
                                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace(); // Log any JSON parsing error
                        }
                    } else if (error.networkResponse.statusCode == 403) {
                        try {
                            String responseBody = new String(error.networkResponse.data);
                            JSONObject jsonObject = new JSONObject(responseBody);
                            if (jsonObject.has("status")) {
                                String status = jsonObject.getString("status");
                                if (status.equalsIgnoreCase("failed")){
                                    if (jsonObject.has("data")){
                                        String message = jsonObject.getJSONObject("data").getJSONArray("username").getString(0);
                                        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Internal Error. Please try again", Toast.LENGTH_SHORT).show();
                    }
                    logInLabel.setVisibility(View.VISIBLE);
                    progress.setVisibility(View.GONE);
                    logIn.setEnabled(true);
                }
            });
            return null;
        }

        private void handleResponse(String response) throws JSONException {
            if (response != null) {
                JSONObject jsonObject = new JSONObject(response);
                if (jsonObject.has("status")) {
                    String status = jsonObject.getString("status");
                    if (status.equalsIgnoreCase("success")) {
                        JSONObject tokenObject = jsonObject.getJSONObject("token");
                        String token = tokenObject.getString("access_token");
                        String refreshToken = tokenObject.getString("refresh_token");
                        userManagement.token(token,refreshToken);
                        JSONObject userObject = jsonObject.getJSONObject("user");
                        int id = userObject.getInt("id");
                        String name = userObject.getString("name");
                        String username = userObject.getString("username");
                        String mobile = userObject.getString("phone_number");
                        String email = userObject.getString("email");
                        String profilePic = userObject.optString("photo_url", "null");
                        if (profilePic != null && !profilePic.equalsIgnoreCase("null") && !profilePic.isEmpty()) {
                            profilePic = "https://palasbari.h24x7.in/" + profilePic;
                        } else {
                            profilePic = "null";
                        }
                        userManagement.userSessionManage(username, String.valueOf(id),mobile,name,profilePic,email);
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();
                    }
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.postDelayed(runnable, 0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
    }

    private void checkInternetConnectivity() throws IOException {
        if (!CheckInternetConnection.isNetworkAvailable(getApplicationContext())) {
            noInternetLayout.setVisibility(View.VISIBLE);
            logIn.setEnabled(false);
        } else {
            noInternetLayout.setVisibility(View.GONE);
            logIn.setEnabled(true);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}