package com.palashbari.project;

import android.app.Dialog;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.palashbari.project.util.API;
import com.palashbari.project.util.JsonAPICall;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class ChangePasswordFragment extends Fragment implements ConnectivityReceiver.ConnectivityListener{

    View view;
    String profile_image_uri;
    UserManagement userManagement;
    private ConnectivityReceiver connectivityReceiver;
    boolean internetResult = false;
    CardView saveCard;
    ProgressBar progress;
    TextView saveLabel;
    TextInputEditText oldPassEditText,newPassEditText,confirmPassEditText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_change_password, container, false);

        userManagement = new UserManagement(getActivity());
        HashMap<String, String> user = userManagement.userDetails();
        profile_image_uri = user.get(userManagement.PROFILE_PIC);

        handleBackPress();

        View rootView = view.findViewById(R.id.fragment);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets systemInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars() | WindowInsetsCompat.Type.navigationBars());
            v.setPadding(0, 0, 0, systemInsets.bottom);
            return insets;
        });


        saveCard = view.findViewById(R.id.saveCard);
        progress = view.findViewById(R.id.progress);
        saveLabel = view.findViewById(R.id.saveLabel);
        oldPassEditText = view.findViewById(R.id.oldPassEditText);
        newPassEditText = view.findViewById(R.id.newPassEditText);
        confirmPassEditText = view.findViewById(R.id.confirmPassEditText);

        connectivityReceiver = new ConnectivityReceiver(this);
        LinearLayout linearLayout = getActivity().findViewById(R.id.linearLayout);

        if (linearLayout != null) {
            linearLayout.setVisibility(View.GONE);
        }

        LinearLayout backButton = view.findViewById(R.id.idMainToolBar);
        TextView lblText = view.findViewById(R.id.lbl);
        ImageView imageShow = view.findViewById(R.id.imageShow);

        if (profile_image_uri != null && !profile_image_uri.equalsIgnoreCase("null")) {
            Glide.with(getContext()).load(profile_image_uri).into(imageShow);
        } else {
            imageShow.setImageDrawable(getResources().getDrawable(R.drawable.frame_161));
        }

        imageShow.setOnClickListener(v -> {
            Dialog dialog = new Dialog(requireContext());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_profile_image);
            dialog.getWindow().setBackgroundDrawable(
                    new ColorDrawable(Color.TRANSPARENT)
            );

            ImageView dialogImage = dialog.findViewById(R.id.dialogImage);

            if (profile_image_uri != null && !profile_image_uri.equalsIgnoreCase("null")) {
                Glide.with(requireContext())
                        .load(profile_image_uri)
                        .circleCrop()   // ✅ makes image round
                        .into(dialogImage);
            } else {
                dialogImage.setImageDrawable(
                        getResources().getDrawable(R.drawable.frame_161)
                );
            }

            dialog.show();
        });

        lblText.setText("Change Password");

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment currentFragment = new SettingsFragment();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(0, 0);
                fragmentTransaction.replace(R.id.fragment, currentFragment);
                fragmentTransaction.commit();
            }
        });

        saveCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldPass = oldPassEditText.getText().toString().trim();
                String newPass = newPassEditText.getText().toString().trim();
                String confirmPass = confirmPassEditText.getText().toString().trim();

                if (TextUtils.isEmpty(oldPassEditText.getText())) {
                    oldPassEditText.setError("Old password is required");
                    oldPassEditText.requestFocus();
                } else if (!oldPass.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$")) {
                    oldPassEditText.setError("Password must have at least 8 characters, 1 uppercase, 1 lowercase, and 1 special character.");
                    oldPassEditText.requestFocus();
                }else if (TextUtils.isEmpty(newPassEditText.getText())) {
                    newPassEditText.setError("New password is required");
                    newPassEditText.requestFocus();
                }else if (!newPass.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$")) {
                    newPassEditText.setError("Password must have at least 8 characters, 1 uppercase, 1 lowercase, and 1 special character.");
                    newPassEditText.requestFocus();
                }else if (TextUtils.isEmpty(confirmPassEditText.getText())) {
                    confirmPassEditText.setError("Confirm password is required");
                    confirmPassEditText.requestFocus();
                }else if (!confirmPass.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$")) {
                    confirmPassEditText.setError("Password must have at least 8 characters, 1 uppercase, 1 lowercase, and 1 special character.");
                    confirmPassEditText.requestFocus();
                }else if (!newPass.equals(confirmPass)) {
                    confirmPassEditText.setError("Confirm password must match New password");
                    confirmPassEditText.requestFocus();
                } else {
                    Dialog updateDialog = new Dialog(requireContext());
                    updateDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    updateDialog.setContentView(R.layout.layout_bottom_sheet_update_confirm);
                    updateDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    updateDialog.setCancelable(true);
                    updateDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    updateDialog.getWindow().setGravity(Gravity.BOTTOM);
                    updateDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

                    CardView cancel = updateDialog.findViewById(R.id.cancelLabel);
                    CardView update = updateDialog.findViewById(R.id.updateLabel);
                    View dismiss = updateDialog.findViewById(R.id.dismiss);
                    TextView passwordUpdate = updateDialog.findViewById(R.id.passwordUpdate);
                    TextView passwordUpdateDesc = updateDialog.findViewById(R.id.passwordUpdateDesc);

                    passwordUpdate.setVisibility(View.VISIBLE);
                    passwordUpdateDesc.setVisibility(View.VISIBLE);

                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            updateDialog.dismiss();
                        }
                    });

                    update.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (internetResult) {
                                updateDialog.dismiss();
                                saveCard.setEnabled(false);
                                progress.setVisibility(View.VISIBLE);
                                saveLabel.setVisibility(View.GONE);
                                Map<String, String> parameter = new HashMap<>();
                                parameter.put("old_password", oldPass);
                                parameter.put("new_password", newPass);
                                parameter.put("new_password_confirm", confirmPass);

                                AsyncTaskChangePass asyncTaskChangePass = new AsyncTaskChangePass(parameter);
                                asyncTaskChangePass.execute();
                            }
                        }
                    });

                    dismiss.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            updateDialog.dismiss();
                        }
                    });

                    updateDialog.show();
                }
            }
        });

        return view;
    }

    public class AsyncTaskChangePass extends AsyncTask<Void, Void, Void> {

        Map<String, String> parameter;


        public AsyncTaskChangePass(Map<String, String> parameter) {
            this.parameter = parameter;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            JsonAPICall.makePostRequestWithTokenAndFormData(getActivity(),  API.change_password, parameter, new JsonAPICall.VolleyCallback() {
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
                    if (error.networkResponse.statusCode == 422) {
                        try {
                            String responseBody = new String(error.networkResponse.data);
                            JSONObject jsonObject = new JSONObject(responseBody);
                            if (jsonObject.has("message")) {
                                String message = jsonObject.getString("message");
                                if (jsonObject.has("data")){
                                    String newMessage = jsonObject.getJSONObject("data").getJSONArray("new_password").getString(0);
                                    Toast.makeText(getContext(), newMessage, Toast.LENGTH_SHORT).show();
                                }else {
                                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }else {
                        Toast.makeText(getActivity(), "Internal Error. Please try again", Toast.LENGTH_SHORT).show();
                    }
                    saveLabel.setVisibility(View.VISIBLE);
                    progress.setVisibility(View.GONE);
                    saveCard.setEnabled(true);
                }
            });
            return null;
        }

        private void handleResponse(String response) throws JSONException {
            if (response != null) {
                JSONObject jsonObject = new JSONObject(response);
                int status = jsonObject.getInt("status");
                if (status == 1) {
                    oldPassEditText.setText("");
                    newPassEditText.setText("");
                    confirmPassEditText.setText("");
                    Toast.makeText(getContext(), "Password updated successfully", Toast.LENGTH_SHORT).show();
                }
                saveLabel.setVisibility(View.VISIBLE);
                progress.setVisibility(View.GONE);
                saveCard.setEnabled(true);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        getActivity().registerReceiver(connectivityReceiver, intentFilter);

    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(connectivityReceiver);
    }

    public void onConnectivityChanged(boolean isConnected) {
        if (isConnected) {
            internetResult = true;
            saveCard.setEnabled(true);
        }else{
            internetResult = false;
            saveCard.setEnabled(false);
        }
    }


    private void handleBackPress() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Fragment currentFragment = new SettingsFragment();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(0, 0);
                fragmentTransaction.replace(R.id.fragment, currentFragment);
                fragmentTransaction.commit();
            }
        };

        requireActivity().getOnBackPressedDispatcher().addCallback(getActivity(), callback);
    }
}