package com.palashbari.project;

import android.app.Dialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
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


public class AnganwadiKarmiFragment extends Fragment implements ConnectivityReceiver.ConnectivityListener{

    View view;
    String profile_image_uri;
    UserManagement userManagement;
    private ConnectivityReceiver connectivityReceiver;
    boolean internetResult = false;
    LinearLayout lin,noRec,itemContainer,paginationLayout,panchayatLayout,filteredGpLayout,filteredBoothLayout;
    ProgressBar spin_kit;
    ImageView filter;
    JSONArray boothArray,gaoArray;
    Dialog filterDialog;
    CardView showResult,clear;
    private boolean isAPICallRunning = false;
    private int itemsPerPage = 5;
    int totalPages;
    private int selectedPage = 0;
    private TextView selectedPageButton;
    private String selectedPanchayat = null;
    private String selectedBooth = null;
    private String currentSearchText = "";
    private String tempSelectedPanchayat = null;
    private String tempSelectedBooth = null;
    EditText searchEditText;
    FlexboxLayout itemContainerBooth;
    HorizontalScrollView filteredItemLayout;
    TextView filteredGp,filteredBooth;
    String tempSelectedPanchayatName = null;
    String tempSelectedBoothName = null;
    String selectedPanchayatName = null;
    String selectedBoothName = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_anganwadi_karmi, container, false);

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

        lin = view.findViewById(R.id.lin);
        spin_kit = view.findViewById(R.id.spin_kit);
        noRec = view.findViewById(R.id.noRec);
        itemContainer = view.findViewById(R.id.itemContainer);
        filter = view.findViewById(R.id.filter);
        paginationLayout = view.findViewById(R.id.paginationLayout);
        searchEditText = view.findViewById(R.id.searchEditText);
        filteredItemLayout = view.findViewById(R.id.filteredItemLayout);
        filteredGpLayout = view.findViewById(R.id.filteredGpLayout);
        filteredBoothLayout = view.findViewById(R.id.filteredBoothLayout);
        filteredGp = view.findViewById(R.id.filteredGp);
        filteredBooth = view.findViewById(R.id.filteredBooth);

        filterDialog = new Dialog(requireContext());
        filterDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        filterDialog.setContentView(R.layout.layout_bottom_sheet_anganwadi_karmi);
        filterDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        filterDialog.setCancelable(true);
        filterDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        filterDialog.getWindow().setGravity(Gravity.BOTTOM);
        filterDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        TextView noRecBooth = filterDialog.findViewById(R.id.noRecBooth);
        TextView noRecGaonPanchayat = filterDialog.findViewById(R.id.noRecGaonPanchayat);
        LinearLayout boothLayout = filterDialog.findViewById(R.id.boothLayout);
        panchayatLayout = filterDialog.findViewById(R.id.panchayatLayout);
        TextView boothText = filterDialog.findViewById(R.id.boothText);
        TextView panchayatText = filterDialog.findViewById(R.id.panchayatText);
        View dismiss = filterDialog.findViewById(R.id.dismiss);
        itemContainerBooth = filterDialog.findViewById(R.id.itemContainerBooth);
        FlexboxLayout itemContainerGaonPanchayat = filterDialog.findViewById(R.id.itemContainerGaonPanchayat);
        clear = filterDialog.findViewById(R.id.clear);
        showResult = filterDialog.findViewById(R.id.showResult);
        CardView searchPanchayat = filterDialog.findViewById(R.id.searchPanchayat);
        CardView searchBooth = filterDialog.findViewById(R.id.searchBooth);
        EditText searchEditTextPanchayat = filterDialog.findViewById(R.id.searchEditTextPanchayat);
        EditText searchEditTextBooth = filterDialog.findViewById(R.id.searchEditTextBooth);

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
        lblText.setText("Anganwadi Karmi");

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


        filter.setOnClickListener(view -> {

            tempSelectedPanchayat = selectedPanchayat;
            tempSelectedBooth = selectedBooth;
            tempSelectedPanchayatName = selectedPanchayatName;
            tempSelectedBoothName = selectedBoothName;

            if (tempSelectedPanchayat != null && internetResult) {
                new AsyncTaskBooth(tempSelectedPanchayat).execute();
            }

            panchayatLayout.setOnClickListener(v -> {
                itemContainerBooth.removeAllViews();
                itemContainerGaonPanchayat.removeAllViews();

                noRecBooth.setVisibility(View.GONE);
                searchBooth.setVisibility(View.GONE);

                itemContainerBooth.setVisibility(View.GONE);
                itemContainerGaonPanchayat.setVisibility(View.VISIBLE);

                searchEditTextPanchayat.setText("");
                searchEditTextBooth.setText("");
                searchEditTextPanchayat.clearFocus();
                searchEditTextBooth.clearFocus();

                panchayatLayout.setBackground(getResources().getDrawable(R.drawable.active_button));
                panchayatText.setTextColor(getResources().getColor(R.color.green));
                boothLayout.setBackground(null);
                boothText.setTextColor(getResources().getColor(R.color.grey2));

                if (gaoArray != null && gaoArray.length() > 0) {
                    try {
                        searchPanchayat.setVisibility(View.VISIBLE);
                        showDialog(gaoArray, "panchayat", itemContainerGaonPanchayat, clear, showResult, filterDialog);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    noRecGaonPanchayat.setVisibility(View.VISIBLE);
                    searchPanchayat.setVisibility(View.GONE);
                }
            });

            boothLayout.setOnClickListener(v -> {
                itemContainerBooth.removeAllViews();
                itemContainerGaonPanchayat.removeAllViews();

                noRecGaonPanchayat.setVisibility(View.GONE);
                searchPanchayat.setVisibility(View.GONE);

                itemContainerBooth.setVisibility(View.VISIBLE);
                itemContainerGaonPanchayat.setVisibility(View.GONE);

                searchEditTextPanchayat.setText("");
                searchEditTextBooth.setText("");
                searchEditTextPanchayat.clearFocus();
                searchEditTextBooth.clearFocus();

                boothLayout.setBackground(getResources().getDrawable(R.drawable.active_button));
                boothText.setTextColor(getResources().getColor(R.color.green));
                panchayatLayout.setBackground(null);
                panchayatText.setTextColor(getResources().getColor(R.color.grey2));

                if (boothArray != null && boothArray.length() > 0) {
                    try {
                        searchBooth.setVisibility(View.VISIBLE);
                        showDialog(boothArray, "booth", itemContainerBooth, clear, showResult, filterDialog);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    noRecBooth.setVisibility(View.VISIBLE);
                    searchBooth.setVisibility(View.GONE);
                }
            });

            searchEditTextPanchayat.setText("");
            searchEditTextBooth.setText("");
            searchEditTextPanchayat.clearFocus();
            searchEditTextBooth.clearFocus();

            filterDialog.show();

            panchayatLayout.post(panchayatLayout::performClick);

            dismiss.setOnClickListener(v -> filterDialog.dismiss());

            filterDialog.setOnDismissListener(dialogInterface -> {
                tempSelectedBooth = null;
                tempSelectedPanchayat = null;
                tempSelectedBoothName = null;
                tempSelectedPanchayatName = null;
                boothArray = null;
            });
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
                    selectedPage = 0;
                    selectedPageButton = null;
                    fetchData(0, itemsPerPage, currentSearchText);
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

                        itemContainerBooth.removeAllViews();
                        itemContainerGaonPanchayat.removeAllViews();

                        if (filteredArray.length() > 0) {
                            showDialog(filteredArray, "panchayat", itemContainerGaonPanchayat, clear, showResult, filterDialog);
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        searchEditTextBooth.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString().trim();

                try {
                    JSONArray filteredArray = new JSONArray();

                    if (boothArray != null && boothArray.length() > 0) {
                        for (int i = 0; i < boothArray.length(); i++) {
                            JSONObject obj = boothArray.getJSONObject(i);
                            if (obj.getString("name").toLowerCase().contains(text.toLowerCase())) {
                                filteredArray.put(obj);
                            }
                        }

                        itemContainerBooth.removeAllViews();
                        itemContainerGaonPanchayat.removeAllViews();

                        if (filteredArray.length() > 0) {
                            showDialog(filteredArray, "booth", itemContainerBooth, clear, showResult, filterDialog);
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });


        return view;
    }

    private void showDialog(JSONArray jsonArray, String type, FlexboxLayout itemLayout, CardView clear, CardView showResult, Dialog dialog) throws JSONException {
        List<RadioButton> radioButtons = new ArrayList<>();
        itemLayout.removeAllViews();

        int initialItemCount = 10; // visible items before "Show More"
        boolean[] showAll = {false};

        // --- Panchayat List Handling ---
        if (type.equalsIgnoreCase("panchayat")) {
            List<JSONObject> jsonObjects = new ArrayList<>();
            for (int k = 0; k < jsonArray.length(); k++) jsonObjects.add(jsonArray.getJSONObject(k));

            // Separate selected Panchayat to display it first
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

            // Sort remaining Panchayats alphabetically
            Collections.sort(otherObjects, (o1, o2) -> {
                try {
                    return o1.getString("name").compareToIgnoreCase(o2.getString("name"));
                } catch (JSONException e) {
                    return 0;
                }
            });

            // Final list with selected item first
            List<JSONObject> finalList = new ArrayList<>();
            if (selectedObject != null) finalList.add(selectedObject);
            finalList.addAll(otherObjects);

            for (int index = 0; index < finalList.size(); index++) {
                JSONObject json = finalList.get(index);
                String panchayat = json.getString("name");
                int id = json.getInt("id");


                View mainLayout = LayoutInflater.from(getActivity()).inflate(R.layout.text_item, itemLayout, false);
                RadioButton radio = mainLayout.findViewById(R.id.radio);
                TextView nameTextview = mainLayout.findViewById(R.id.nameTextview);
                nameTextview.setText(panchayat);
                radio.setTag(id);

                if (tempSelectedPanchayat != null && tempSelectedPanchayat.equals(String.valueOf(id))) {
                    radio.setChecked(true);
                }

                radioButtons.add(radio);

                mainLayout.setOnClickListener(v -> {
                    for (RadioButton rb : radioButtons) rb.setChecked(rb == radio);
                    tempSelectedPanchayat = String.valueOf(id);
                    tempSelectedPanchayatName = panchayat;
                    tempSelectedBooth = null;
                    tempSelectedBoothName = null;
                    itemContainerBooth.removeAllViews();
                    if (internetResult)
                        new AsyncTaskBooth(tempSelectedPanchayat).execute();
                });

                radio.setOnClickListener(v -> {
                    for (RadioButton rb : radioButtons) rb.setChecked(rb == radio);
                    tempSelectedPanchayat = String.valueOf(id);
                    tempSelectedPanchayatName = panchayat;
                    tempSelectedBooth = null;
                    tempSelectedBoothName = null;
                    itemContainerBooth.removeAllViews();
                    if (internetResult)
                        new AsyncTaskBooth(tempSelectedPanchayat).execute();
                });

                FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(16, 8, 16, 8);
                mainLayout.setLayoutParams(params);

                if (index >= initialItemCount && !showAll[0]) mainLayout.setVisibility(View.GONE);
                itemLayout.addView(mainLayout);
            }

            // --- "Show All" Text ---
            if (finalList.size() > initialItemCount) {
                TextView showMoreTv = new TextView(getActivity());
                showMoreTv.setText("Show All");
                showMoreTv.setTextColor(getResources().getColor(R.color.blue));
                showMoreTv.setTypeface(null, Typeface.BOLD);
                showMoreTv.setPadding(16, 16, 16, 16);
                showMoreTv.setGravity(Gravity.CENTER);
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

        }

        else if (type.equalsIgnoreCase("booth")) {
            List<JSONObject> jsonObjects = new ArrayList<>();
            for (int k = 0; k < jsonArray.length(); k++) jsonObjects.add(jsonArray.getJSONObject(k));

            JSONObject selectedObject = null;
            List<JSONObject> otherObjects = new ArrayList<>();
            for (JSONObject obj : jsonObjects) {
                int id = obj.getInt("id");
                if (tempSelectedBooth != null && tempSelectedBooth.equals(String.valueOf(id))) {
                    selectedObject = obj;
                } else {
                    otherObjects.add(obj);
                }
            }

            // Sort alphabetically
            Collections.sort(otherObjects, (o1, o2) -> {
                try {
                    return o1.getString("name").compareToIgnoreCase(o2.getString("name"));
                } catch (JSONException e) {
                    return 0;
                }
            });

            List<JSONObject> finalList = new ArrayList<>();
            if (selectedObject != null) finalList.add(selectedObject);
            finalList.addAll(otherObjects);

            for (int index = 0; index < finalList.size(); index++) {
                JSONObject json = finalList.get(index);
                String boothName = json.getString("name");
                int id = json.getInt("id");

                View mainLayout = LayoutInflater.from(getActivity()).inflate(R.layout.text_item, itemLayout, false);
                RadioButton radio = mainLayout.findViewById(R.id.radio);
                TextView nameTextview = mainLayout.findViewById(R.id.nameTextview);
                nameTextview.setText(boothName);
                radio.setTag(id);

                if (tempSelectedBooth != null && tempSelectedBooth.equals(String.valueOf(id))) {
                    radio.setChecked(true);
                }

                radioButtons.add(radio);

                mainLayout.setOnClickListener(v -> {
                    for (RadioButton rb : radioButtons) rb.setChecked(false);
                    radio.setChecked(true);
                    tempSelectedBooth = String.valueOf(id);
                    tempSelectedBoothName = boothName;
                });

                radio.setOnClickListener(v -> {
                    for (RadioButton rb : radioButtons) rb.setChecked(false);
                    radio.setChecked(true);
                    tempSelectedBooth = String.valueOf(id);
                    tempSelectedBoothName = boothName;
                });

                FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(16, 8, 16, 8);
                mainLayout.setLayoutParams(params);

                if (index >= initialItemCount && !showAll[0]) mainLayout.setVisibility(View.GONE);
                itemLayout.addView(mainLayout);
            }

            if (finalList.size() > initialItemCount) {
                TextView showMoreTv = new TextView(getActivity());
                showMoreTv.setText("Show All");
                showMoreTv.setTextColor(getResources().getColor(R.color.blue));
                showMoreTv.setTypeface(null, Typeface.BOLD);
                showMoreTv.setPadding(16, 16, 16, 16);
                showMoreTv.setGravity(Gravity.CENTER);
                itemLayout.addView(showMoreTv);

                showMoreTv.setOnClickListener(v -> {
                    showAll[0] = true;
                    for (int i = 0; i < itemLayout.getChildCount(); i++) {
                        View child = itemLayout.getChildAt(i);
                        if (child.getVisibility() == View.GONE) {
                            child.setVisibility(View.VISIBLE);
                        }
                    }
                    showMoreTv.setVisibility(View.GONE);
                });
            }
        }

        // --- Clear Button ---
        clear.setOnClickListener(v -> {
            tempSelectedPanchayat = null;
            tempSelectedBooth = null;
            tempSelectedPanchayatName = null;
            tempSelectedBoothName = null;
            panchayatLayout.performClick();
            boothArray = null;
            itemContainerBooth.removeAllViews();
        });

        // --- Show Result ---
        showResult.setOnClickListener(v -> {
            selectedPanchayat = tempSelectedPanchayat;
            selectedBooth = tempSelectedBooth;
            selectedPanchayatName = tempSelectedPanchayatName;
            selectedBoothName = tempSelectedBoothName;
            selectedPage = 0;
            selectedPageButton = null;
            dialog.dismiss();
            fetchData(0, itemsPerPage, "");
        });
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
                spin_kit.setVisibility(View.VISIBLE);

                AsyncTaskPanchayat asyncTaskPanchayat = new AsyncTaskPanchayat();
                asyncTaskPanchayat.execute();

                Map<String, String> parameter = new HashMap<>();
                parameter.put("draw", "1");
                parameter.put("start", "0");
                parameter.put("length",  "5");
                parameter.put("search[value]", "");
                parameter.put("category", "anganwadi");
                parameter.put("type", "supervisor");
                parameter.put("gaon_panchayat", "");
                parameter.put("polling_booth", "");

                AsyncTaskAnganwadiKarmi asyncTaskAnganwadiKarmi = new AsyncTaskAnganwadiKarmi(parameter);
                asyncTaskAnganwadiKarmi.execute();
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

    public class AsyncTaskBooth extends AsyncTask<Void, Void, Void> {

        String gaonPanchayatId;

        public AsyncTaskBooth(String gaonPanchayatId) {
            this.gaonPanchayatId = gaonPanchayatId;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (getActivity() != null) {
                JSONObject jsonBody = new JSONObject();
                JSONArray gpArray = new JSONArray();
                gpArray.put(Integer.parseInt(gaonPanchayatId));
                try {
                    jsonBody.put("gaon_panchayat", gpArray);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                JsonAPICall.makePostRequestWithToken(getActivity(), API.booth_by_panchayat,jsonBody, new JsonAPICall.VolleyCallback() {
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
                        boothArray = jsonObject.getJSONArray("data");
                    }
                    if (itemContainerBooth.getVisibility() == View.VISIBLE) {
                        itemContainerBooth.removeAllViews();
                        try {
                            showDialog(boothArray, "booth", itemContainerBooth, clear, showResult, filterDialog);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public class AsyncTaskAnganwadiKarmi extends AsyncTask<Void, Void, Void> {

        Map<String, String> parameter;

        public AsyncTaskAnganwadiKarmi(Map<String, String> parameter) {
            this.parameter = parameter;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            JsonAPICall.makePostRequestWithTokenAndFormData(getActivity(),  API.asha_karmi_data, parameter, new JsonAPICall.VolleyCallback() {
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
            return null;
        }

        private void handleResponse(String response) throws JSONException {
            if (response != null) {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray dataArray = jsonObject.getJSONArray("data");
                spin_kit.setVisibility(View.GONE);
                lin.setVisibility(View.VISIBLE);
                if (dataArray.length() > 0) {
                    noRec.setVisibility(View.GONE);
                    int recordsTotal = jsonObject.getInt("recordsFiltered");
                    totalPages = (int) Math.ceil((double) recordsTotal / itemsPerPage);
                    createPagination(totalPages);
                    itemContainer.removeAllViews();
                    processValue(dataArray);
                } else {
                    itemContainer.removeAllViews();
                    paginationLayout.removeAllViews();
                    noRec.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private void processValue(JSONArray jsonArray) throws JSONException {
        for (int k = 0; k < jsonArray.length(); k++) {
            JSONObject json = jsonArray.getJSONObject(k);
            String name = json.getString("name");
            String karmiType = json.getString("type");
            String phoneNumber = json.getString("phone_number");


            if (getActivity() != null) {

                View mainLayout = LayoutInflater.from(getActivity()).inflate(R.layout.district_committee_item, itemContainer, false);

                TextView nameTextview = mainLayout.findViewById(R.id.nameTextview);
                TextView designationTextview = mainLayout.findViewById(R.id.designationTextview);
                TextView numberTextview = mainLayout.findViewById(R.id.numberTextview);
                ImageView whatsappImage = mainLayout.findViewById(R.id.whatsappImage);
                ImageView phoneImage = mainLayout.findViewById(R.id.phoneImage);

                nameTextview.setText(name);
                designationTextview.setText(karmiType);
                numberTextview.setText(phoneNumber);

                phoneImage.setOnClickListener(v -> {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + phoneNumber));
                    getActivity().startActivity(intent);
                });

                whatsappImage.setOnClickListener(v -> {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("https://wa.me/" + phoneNumber));
                        intent.setPackage("com.whatsapp");
                        getActivity().startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(getActivity(), "WhatsApp not installed", Toast.LENGTH_SHORT).show();
                    }
                });

                itemContainer.addView(mainLayout);
            }
        }
    }


    private void createPagination(int totalPages) {
        paginationLayout.removeAllViews();

        if (totalPages <= 1) {
            paginationLayout.setVisibility(View.GONE);
            return;
        }

        paginationLayout.setVisibility(View.VISIBLE);
        int maxPagesToShow = 5;
        int currentPage = selectedPage;

        int startPage = Math.max(0, currentPage - maxPagesToShow / 2);
        int endPage = Math.min(totalPages - 1, startPage + maxPagesToShow - 1);

        if (endPage - startPage + 1 < maxPagesToShow) {
            startPage = Math.max(0, endPage - maxPagesToShow + 1);
        }

        for (int i = 0; i < totalPages; i++) {
            if (i == 0 || i == totalPages - 1 || (i >= startPage && i <= endPage)) {
                TextView pageButton = new TextView(getActivity());
                pageButton.setText(String.valueOf(i + 1));
                pageButton.setPadding(16, 8, 16, 8);
                Typeface typeface = ResourcesCompat.getFont(getActivity(), R.font.inter_bold);
                pageButton.setTypeface(typeface);
                pageButton.setTextSize(15);


                if (i == selectedPage) {
                    pageButton.setTextColor(getActivity().getColor(R.color.red));
                    selectedPageButton = pageButton;
                } else {
                    pageButton.setTextColor(getActivity().getColor(R.color.black));
                }

                int finalI = i;
                pageButton.setOnClickListener(v -> {
                    if (selectedPageButton != null) {
                        selectedPageButton.setTextColor(getActivity().getColor(R.color.black));
                    }
                    pageButton.setTextColor(getActivity().getColor(R.color.red));
                    selectedPageButton = pageButton;
                    selectedPage = finalI;

                    int start = finalI * itemsPerPage;
                    fetchData(start, itemsPerPage, currentSearchText);
                });

                paginationLayout.addView(pageButton);

            } else if (i == startPage - 1 || i == endPage + 1) {
                TextView ellipsis = new TextView(getActivity());
                ellipsis.setText("...");
                ellipsis.setPadding(16, 8, 16, 8);
                ellipsis.setTextSize(15);
                ellipsis.setTextColor(getActivity().getColor(R.color.black));
                paginationLayout.addView(ellipsis);
            }
        }
    }

    private void fetchData(int start, int length, String searchText) {
        spin_kit.setVisibility(View.VISIBLE);
        lin.setVisibility(View.GONE);

        Map<String, String> parameter = new HashMap<>();
        parameter.put("draw", "1");
        parameter.put("start", String.valueOf(start));
        parameter.put("length", String.valueOf(length));
        if (searchText != null && !searchText.trim().isEmpty()) {
            parameter.put("search[value]", searchText.trim());
        } else {
            parameter.put("search[value]", "");
        }
        parameter.put("category", "anganwadi");
        parameter.put("type", "supervisor");
        parameter.put("polling_booth", selectedBooth != null ? selectedBooth : "");
        parameter.put("gaon_panchayat", selectedPanchayat != null ? selectedPanchayat : "");

        requireActivity().runOnUiThread(() -> {
            if (!TextUtils.isEmpty(selectedPanchayat) || !TextUtils.isEmpty(selectedBooth)) {
                filteredItemLayout.setVisibility(View.VISIBLE);
            } else {
                filteredItemLayout.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(selectedPanchayat)) {
                filteredGpLayout.setVisibility(View.VISIBLE);
                filteredGp.setText(selectedPanchayatName);
            } else {
                filteredGpLayout.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(selectedBooth)) {
                filteredBoothLayout.setVisibility(View.VISIBLE);
                filteredBooth.setText(selectedBoothName);
            } else {
                filteredBoothLayout.setVisibility(View.GONE);
            }
        });

        AsyncTaskAnganwadiKarmi asyncTaskAnganwadiKarmi = new AsyncTaskAnganwadiKarmi(parameter);
        asyncTaskAnganwadiKarmi.execute();
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