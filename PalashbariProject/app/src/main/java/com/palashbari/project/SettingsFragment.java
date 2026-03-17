package com.palashbari.project;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

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

import com.bumptech.glide.Glide;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.switchmaterial.SwitchMaterial;

import org.json.JSONArray;

import java.util.HashMap;


public class SettingsFragment extends Fragment {

    View view;
    UserManagement userManagement;
    String profile_image_uri,userName,email;
    TextView nameTextView,emailTextView;
    ImageView imageView;
    LinearLayout aboutAppLayout,logOutLayout,editProfileLayout,changePasswordLayout;
//    SwitchMaterial notificationSwitch;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_settings, container, false);

        userManagement = new UserManagement(getActivity());
        HashMap<String, String> user = userManagement.userDetails();
        profile_image_uri = user.get(userManagement.PROFILE_PIC);
        userName = user.get(userManagement.NAME);
        email = user.get(userManagement.EMAIL);

        handleBackPress();

        View rootView = view.findViewById(R.id.fragment);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets systemInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars() | WindowInsetsCompat.Type.navigationBars());
            v.setPadding(0, 0, 0, systemInsets.bottom);
            return insets;
        });

        nameTextView = view.findViewById(R.id.nameTextView);
        emailTextView = view.findViewById(R.id.emailTextView);
        imageView = view.findViewById(R.id.imageView);
        aboutAppLayout = view.findViewById(R.id.aboutAppLayout);
        logOutLayout = view.findViewById(R.id.logOutLayout);
//        notificationSwitch = view.findViewById(R.id.material_switch);
        editProfileLayout = view.findViewById(R.id.editProfileLayout);
        changePasswordLayout = view.findViewById(R.id.changePasswordLayout);

        if (userName != null && !userName.equalsIgnoreCase("null")) {
            nameTextView.setText(userName);
        }

        if (email != null && !email.equalsIgnoreCase("null")) {
            emailTextView.setText(email);
        }

        LinearLayout linearLayout = getActivity().findViewById(R.id.linearLayout);

        if (linearLayout != null) {
            linearLayout.setVisibility(View.GONE);
        }

        LinearLayout backButton = view.findViewById(R.id.idMainToolBar);
        TextView lblText = view.findViewById(R.id.lbl);
        ImageView imageShow = view.findViewById(R.id.imageShow);

        if (profile_image_uri != null && !profile_image_uri.equalsIgnoreCase("null")) {
            Glide.with(getContext()).load(profile_image_uri).into(imageShow);
            Glide.with(getContext()).load(profile_image_uri).into(imageView);
        } else {
            imageShow.setImageDrawable(getResources().getDrawable(R.drawable.frame_161));
            imageView.setImageDrawable(getResources().getDrawable(R.drawable.frame_161));
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

        imageView.setOnClickListener(v -> {
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

        lblText.setText("Settings");

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment currentFragment = new HomeFragment();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(0, 0);
                fragmentTransaction.replace(R.id.fragment, currentFragment);
                fragmentTransaction.commit();
            }
        });

        editProfileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment currentFragment = new EditProfileFragment();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(0, 0);
                fragmentTransaction.replace(R.id.fragment, currentFragment);
                fragmentTransaction.commit();
            }
        });

        changePasswordLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment currentFragment = new ChangePasswordFragment();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(0, 0);
                fragmentTransaction.replace(R.id.fragment, currentFragment);
                fragmentTransaction.commit();
            }
        });

        aboutAppLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment currentFragment = new AboutAppFragment();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(0, 0);
                fragmentTransaction.replace(R.id.fragment, currentFragment);
                fragmentTransaction.commit();
            }
        });

        logOutLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog logoutDialog = new Dialog(requireContext());
                logoutDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                logoutDialog.setContentView(R.layout.layout_bottom_sheet_logout);
                logoutDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                logoutDialog.setCancelable(true);
                logoutDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                logoutDialog.getWindow().setGravity(Gravity.BOTTOM);
                logoutDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

                CardView cancel = logoutDialog.findViewById(R.id.cancelLabel);
                CardView signOut = logoutDialog.findViewById(R.id.signOutLabel);
                View dismiss = logoutDialog.findViewById(R.id.dismiss);

                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        logoutDialog.dismiss();
                    }
                });

                signOut.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        userManagement.logout(getActivity());
                    }
                });

                dismiss.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        logoutDialog.dismiss();
                    }
                });

                logoutDialog.show();
            }
        });

//        notificationSwitch.setOnCheckedChangeListener(notificationSwitchListener);
//
//        if (NotificationManagerCompat.from(getContext()).areNotificationsEnabled()) {
//            notificationSwitch.setChecked(true);
//        } else {
//            notificationSwitch.setChecked(false);
//        }
//
//        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
//            if (isChecked) {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                    if (ContextCompat.checkSelfPermission(getContext(),
//                            android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
//
//                        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
//                                android.Manifest.permission.POST_NOTIFICATIONS)) {
//                            // User denied before but can still ask again
//                            ActivityCompat.requestPermissions(getActivity(),
//                                    new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1001);
//                        } else {
//                            // "Don't ask again" → system dialog is blocked
//                            showPermissionDialog(); // your own dialog → take user to Settings
//                        }
//                    }
//                }
//                else {
//                    // Below Android 13 → always allowed
//                    notificationSwitch.setChecked(true);
//                }
//            } else {
//                // User wants OFF → open settings to let them disable manually
//                Intent intent = new Intent();
//                intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
//                intent.putExtra("android.provider.extra.APP_PACKAGE", getContext().getPackageName());
//                startActivity(intent);
//            }
//        });



        return view;
    }

//    private void showPermissionDialog() {
//        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
//                .setTitle("Enable Notifications")
//                .setMessage("Notifications are disabled. To receive updates, please enable notifications in Settings.")
//                .setCancelable(false)
//                .setPositiveButton("Go to Settings", (dialog, which) -> {
//                    Intent intent = new Intent();
//                    intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
//                    intent.putExtra("android.provider.extra.APP_PACKAGE", requireContext().getPackageName());
//                    startActivity(intent);
//                })
//                .setNegativeButton("Cancel", (dialog, which) -> {
//                    dialog.dismiss();
//
//                    // ✅ Remove listener before updating the switch
//                    notificationSwitch.setOnCheckedChangeListener(null);
//                    notificationSwitch.setChecked(false);
//
//                    // ✅ Reattach listener
//                    notificationSwitch.setOnCheckedChangeListener(notificationSwitchListener);
//                })
//                .show();
//    }
//
//    private final SwitchMaterial.OnCheckedChangeListener notificationSwitchListener =
//            (buttonView, isChecked) -> {
//                if (isChecked) {
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                        if (ContextCompat.checkSelfPermission(getContext(),
//                                android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
//
//                            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
//                                    android.Manifest.permission.POST_NOTIFICATIONS)) {
//                                ActivityCompat.requestPermissions(getActivity(),
//                                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1001);
//                            } else {
//                                showPermissionDialog();
//                            }
//                        }
//                    } else {
//                        notificationSwitch.setChecked(true);
//                    }
//                } else {
//                    Intent intent = new Intent();
//                    intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
//                    intent.putExtra("android.provider.extra.APP_PACKAGE", getContext().getPackageName());
//                    startActivity(intent);
//                }
//            };
//
//
//
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode,
//                                           @NonNull String[] permissions,
//                                           @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == 1001) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                notificationSwitch.setChecked(true);
//            } else {
//                notificationSwitch.setChecked(false);
//            }
//        }
//    }

    @Override
    public void onResume() {
        super.onResume();
//        boolean enabled = NotificationManagerCompat.from(getContext()).areNotificationsEnabled();
//        notificationSwitch.setChecked(enabled);
    }

    private void handleBackPress() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Fragment currentFragment = new HomeFragment();
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