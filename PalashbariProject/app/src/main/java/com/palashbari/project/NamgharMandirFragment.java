package com.palashbari.project;

import android.app.Dialog;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
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

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayout;
import com.palashbari.project.util.API;
import com.palashbari.project.util.JsonAPICall;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class NamgharMandirFragment extends Fragment implements ConnectivityReceiver.ConnectivityListener{

    View view;
    String profile_image_uri,selectedPanchayat;
    UserManagement userManagement;
    private ConnectivityReceiver connectivityReceiver;
    boolean internetResult = false;
    LinearLayout namgharLayout,mandirLayout,filteredGpLayout;
    TextView namgharText,mandirText;
    ImageView filter;
    Dialog filterDialog;
    CardView showResult;
    private String activeTab = "NAMGHAR"; // default
    private String currentSearchText = "";
    private String tempSelectedPanchayat = null;
    EditText searchEditText;
    JSONArray gaoArray;
    private boolean isAPICallRunning = false;
    HorizontalScrollView filteredItemLayout;
    TextView filteredGp;
    String tempSelectedPanchayatName = null;
    String selectedPanchayatName = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_namghar_mandir, container, false);

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

        namgharLayout = view.findViewById(R.id.namgharLayout);
        mandirLayout = view.findViewById(R.id.mandirLayout);
        namgharText = view.findViewById(R.id.namgharText);
        mandirText = view.findViewById(R.id.mandirText);
        filter = view.findViewById(R.id.filter);
        searchEditText = view.findViewById(R.id.searchEditText);
        filteredItemLayout = view.findViewById(R.id.filteredItemLayout);
        filteredGpLayout = view.findViewById(R.id.filteredGpLayout);
        filteredGp = view.findViewById(R.id.filteredGp);

        filterDialog = new Dialog(requireContext());
        filterDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        filterDialog.setContentView(R.layout.layout_bottom_sheet_club);
        filterDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        filterDialog.setCancelable(true);
        filterDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        filterDialog.getWindow().setGravity(Gravity.BOTTOM);
        filterDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        TextView noRecGaonPanchayat = filterDialog.findViewById(R.id.noRecGaonPanchayat);
        LinearLayout panchayatLayout = filterDialog.findViewById(R.id.panchayatLayout);
        TextView panchayatText = filterDialog.findViewById(R.id.panchayatText);
        View dismiss = filterDialog.findViewById(R.id.dismiss);
        FlexboxLayout itemContainerGaonPanchayat = filterDialog.findViewById(R.id.itemContainerGaonPanchayat);
        CardView clear = filterDialog.findViewById(R.id.clear);
        showResult = filterDialog.findViewById(R.id.showResult);
        CardView searchPanchayat = filterDialog.findViewById(R.id.searchPanchayat);
        EditText searchEditTextPanchayat = filterDialog.findViewById(R.id.searchEditTextPanchayat);

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
        lblText.setText("Namghars & Mandirs");

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


        showFragment(new NamgharFragment());
        updateButtonStyles(namgharLayout,mandirLayout,namgharText,mandirText);
        activeTab = "NAMGHAR";

        namgharLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NamgharFragment namgharFragment = new NamgharFragment();
                Bundle bundle = new Bundle();
                bundle.putString("selectedPanchayat", selectedPanchayat);
                bundle.putString("currentSearchText", currentSearchText);
                namgharFragment.setArguments(bundle);

                showFragment(namgharFragment);
                updateButtonStyles(namgharLayout, mandirLayout, namgharText, mandirText);
                activeTab = "NAMGHAR";
            }
        });

        mandirLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MandirFragment mandirFragment = new MandirFragment();
                Bundle bundle = new Bundle();
                bundle.putString("selectedPanchayat", selectedPanchayat);
                bundle.putString("currentSearchText", currentSearchText);
                mandirFragment.setArguments(bundle);

                showFragment(mandirFragment);
                updateButtonStyles(mandirLayout, namgharLayout, mandirText, namgharText);
                activeTab = "MANDIR";
            }
        });



        filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                tempSelectedPanchayat = selectedPanchayat;
                tempSelectedPanchayatName = selectedPanchayatName;

                panchayatLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        itemContainerGaonPanchayat.removeAllViews();
                        searchEditTextPanchayat.setText("");
                        searchEditTextPanchayat.clearFocus();

                        panchayatLayout.setBackground(getResources().getDrawable(R.drawable.active_button));
                        panchayatText.setTextColor(getResources().getColor(R.color.green));
                        try {
                            if (gaoArray != null && gaoArray.length()>0){
                                searchPanchayat.setVisibility(View.VISIBLE);
                                showDialog(gaoArray,itemContainerGaonPanchayat,clear,showResult,filterDialog);
                            }else{
                                noRecGaonPanchayat.setVisibility(View.VISIBLE);
                                searchPanchayat.setVisibility(View.GONE);
                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });

                panchayatLayout.post(() -> {
                    panchayatLayout.performClick();
                });

                dismiss.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        filterDialog.dismiss();
                    }
                });

                searchEditTextPanchayat.setText("");
                searchEditTextPanchayat.clearFocus();

                filterDialog.show();
            }
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                currentSearchText = s.toString().trim();
                if (internetResult) {
                    if ("NAMGHAR".equals(activeTab)) {
                        NamgharFragment namgharFragment = new NamgharFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("selectedPanchayat", selectedPanchayat);
                        bundle.putString("currentSearchText",currentSearchText);
                        namgharFragment.setArguments(bundle);

                        showFragment(namgharFragment);
                        updateButtonStyles(namgharLayout, mandirLayout, namgharText, mandirText);

                    } else if ("MANDIR".equals(activeTab)) {
                        MandirFragment mandirFragment = new MandirFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("selectedPanchayat", selectedPanchayat);
                        bundle.putString("currentSearchText",currentSearchText);
                        mandirFragment.setArguments(bundle);

                        showFragment(mandirFragment);
                        updateButtonStyles(mandirLayout, namgharLayout, mandirText, namgharText);
                    }
                    searchEditText.post(() -> {
                        searchEditText.setSelection(searchEditText.getText().length());
                        searchEditText.requestFocus();
                    });
                }
            }
        });

        searchEditTextPanchayat.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString().trim();

                try {
                    JSONArray filteredArray = new JSONArray();

                    if (gaoArray != null && gaoArray.length() > 0) {
                        for (int i = 0; i < gaoArray.length(); i++) {
                            JSONObject obj = gaoArray.getJSONObject(i);
                            if (obj.getString("name").toLowerCase().contains(text.toLowerCase())) {
                                filteredArray.put(obj);
                            }
                        }

                        itemContainerGaonPanchayat.removeAllViews();

                        if (filteredArray.length() > 0) {
                            showDialog(filteredArray,  itemContainerGaonPanchayat, clear, showResult, filterDialog);
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        return view;
    }

    private void showDialog(JSONArray jsonArray, FlexboxLayout itemLayout, CardView clear, CardView showResult, Dialog potholeDialog) throws JSONException {
        itemLayout.removeAllViews();
        List<RadioButton> radioButtons = new ArrayList<>();
        List<JSONObject> jsonObjects = new ArrayList<>();
        for (int k = 0; k < jsonArray.length(); k++) jsonObjects.add(jsonArray.getJSONObject(k));

        // Separate selected item to put it first
        JSONObject selectedObject = null;
        List<JSONObject> otherObjects = new ArrayList<>();
        for (JSONObject obj : jsonObjects) {
            int id = obj.getInt("id");
            if (tempSelectedPanchayat != null && tempSelectedPanchayat.equals(String.valueOf(id))) {
                selectedObject = obj;
            } else {
                otherObjects.add(obj);
            }
        }

        // Sort remaining items alphabetically
        Collections.sort(otherObjects, (o1, o2) -> {
            try {
                return o1.getString("name").compareToIgnoreCase(o2.getString("name"));
            } catch (JSONException e) {
                return 0;
            }
        });

        // Build final list: selected item first
        List<JSONObject> finalList = new ArrayList<>();
        if (selectedObject != null) finalList.add(selectedObject);
        finalList.addAll(otherObjects);

        int initialItemCount = 10;
        boolean[] showAll = {false};

        for (int index = 0; index < finalList.size(); index++) {
            JSONObject json = finalList.get(index);
            String panchayat = json.getString("name");
            int id = json.getInt("id");

            // Inflate item layout
            View mainLayout = LayoutInflater.from(getActivity()).inflate(R.layout.text_item, itemLayout, false);
            RadioButton radio = mainLayout.findViewById(R.id.radio);
            TextView nameTextview = mainLayout.findViewById(R.id.nameTextview);
            nameTextview.setText(panchayat);
            radio.setTag(id);

            if (tempSelectedPanchayat != null && tempSelectedPanchayat.equals(String.valueOf(id))) {
                radio.setChecked(true);
            }

            radioButtons.add(radio);

            // Click listeners
            mainLayout.setOnClickListener(v -> {
                for (RadioButton rb : radioButtons) rb.setChecked(rb == radio);
                tempSelectedPanchayat = String.valueOf(id);
                tempSelectedPanchayatName = panchayat;
            });
            radio.setOnClickListener(v -> {
                for (RadioButton rb : radioButtons) rb.setChecked(rb == radio);
                tempSelectedPanchayat = String.valueOf(id);
                tempSelectedPanchayatName = panchayat;
            });

            FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(16, 8, 16, 8);
            mainLayout.setLayoutParams(params);

            // Initially show only first 10 items
            if (index >= initialItemCount && !showAll[0]) mainLayout.setVisibility(View.GONE);

            itemLayout.addView(mainLayout);
        }

        // Add "Show All" TextView below all items
        if (finalList.size() > initialItemCount) {
            TextView showMoreTv = new TextView(getActivity());
            showMoreTv.setText("Show All");
            showMoreTv.setTextColor(getResources().getColor(R.color.blue));
            showMoreTv.setTypeface(null, Typeface.BOLD);
            showMoreTv.setPadding(16, 16, 16, 16);
            showMoreTv.setGravity(Gravity.CENTER);
            showMoreTv.setVisibility(View.VISIBLE);

            itemLayout.addView(showMoreTv);

            showMoreTv.setOnClickListener(v -> {
                showAll[0] = true;
                for (int i = 0; i < itemLayout.getChildCount(); i++) {
                    View child = itemLayout.getChildAt(i);
                    child.setVisibility(View.VISIBLE);
                }
                showMoreTv.setVisibility(View.GONE);
            });
        }

        // Clear button
        clear.setOnClickListener(v -> {
            for (RadioButton rb : radioButtons) rb.setChecked(false);
            tempSelectedPanchayat = null;
            tempSelectedPanchayatName = null;
        });

        // Show result
        showResult.setOnClickListener(v -> {
            selectedPanchayat = tempSelectedPanchayat;
            selectedPanchayatName = tempSelectedPanchayatName;
            potholeDialog.dismiss();
            requireActivity().runOnUiThread(() -> {
                if (!TextUtils.isEmpty(selectedPanchayat)) {
                    filteredGpLayout.setVisibility(View.VISIBLE);
                    filteredItemLayout.setVisibility(View.VISIBLE);
                    filteredGp.setText(selectedPanchayatName);
                } else {
                    filteredGpLayout.setVisibility(View.GONE);
                    filteredItemLayout.setVisibility(View.GONE);
                }
            });
            if ("NAMGHAR".equals(activeTab)) {
                NamgharFragment namgharFragment = new NamgharFragment();
                Bundle bundle = new Bundle();
                bundle.putString("selectedPanchayat", selectedPanchayat);
                bundle.putString("currentSearchText", currentSearchText);
                namgharFragment.setArguments(bundle);

                showFragment(namgharFragment);
                updateButtonStyles(namgharLayout, mandirLayout, namgharText, mandirText);

            } else if ("MANDIR".equals(activeTab)) {
                MandirFragment mandirFragment = new MandirFragment();
                Bundle bundle = new Bundle();
                bundle.putString("selectedPanchayat", selectedPanchayat);
                bundle.putString("currentSearchText", currentSearchText);
                mandirFragment.setArguments(bundle);

                showFragment(mandirFragment);
                updateButtonStyles(mandirLayout, namgharLayout, mandirText, namgharText);
            }
        });
    }


    private void updateButtonStyles(LinearLayout read, LinearLayout notRead, TextView readText, TextView notReadText) {
        read.setBackground(getActivity().getDrawable(R.drawable.active_tab));
        notRead.setBackground(null);

        readText.setTextColor(getResources().getColor(R.color.green));
        notReadText.setTextColor(getResources().getColor(R.color.grey8));
    }

    private void showFragment(Fragment fragment) {
        FragmentManager fm = getChildFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        ft.replace(R.id.newFragment, fragment);
        ft.commit();
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
            showResult.setEnabled(true);
            if (!isAPICallRunning) {
                AsyncTaskPanchayat asyncTaskPanchayat = new AsyncTaskPanchayat();
                asyncTaskPanchayat.execute();
                isAPICallRunning = true;
            }
        }else{
            internetResult = false;
            showResult.setEnabled(false);
        }
    }

    public class AsyncTaskPanchayat extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            if (getActivity() != null) {
                JsonAPICall.makeGetRequestWithToken(getActivity(), API.gaon_panchayat, new JsonAPICall.VolleyCallback() {
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
                        Toast.makeText(getActivity(), "Internal Error. Please try again", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            return null;
        }


        private void handleResponse(String response) throws JSONException {
            if (response!= null){
                JSONObject jsonObject = new JSONObject(response);
                if (jsonObject.has("status")){
                    int status = jsonObject.getInt("status");
                    if (status==1){
                        gaoArray = jsonObject.getJSONArray("data");
                    }
                }
            }
        }
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