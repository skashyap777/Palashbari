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


public class SHGDetailsFragment extends Fragment implements ConnectivityReceiver.ConnectivityListener{

    View view;
    String profile_image_uri,selectedPanchayat,selectedVillage,blockName,blockId;
    UserManagement userManagement;
    private ConnectivityReceiver connectivityReceiver;
    boolean internetResult = false;
    LinearLayout lin,noRec,paginationLayout,itemContainer,panchayatLayout,filteredVillLayout,filteredGpLayout;
    ProgressBar spin_kit;
    JSONArray gaoArray,villArray,groupArray;
    ImageView filter;
    Dialog filterDialog;
    CardView showResult,clear;
    TextView blockTextView,blockNameTextView;
    private boolean isAPICallRunning = false;
    private int itemsPerPage = 5;
    int totalPages;
    private int selectedPage = 0;
    private TextView selectedPageButton;
    private String currentSearchText = "";
    private String tempSelectedPanchayat = null;
    private String tempSelectedVill = null;
    EditText searchEditText;
    FlexboxLayout itemContainerVill;
    HorizontalScrollView filteredItemLayout;
    TextView filteredGp,filteredVill;
    String tempSelectedPanchayatName = null;
    String tempSelectedVillName = null;
    String selectedPanchayatName = null;
    String selectedVillName = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_s_h_g_details, container, false);

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
        paginationLayout = view.findViewById(R.id.paginationLayout);
        itemContainer = view.findViewById(R.id.itemContainer);
        filter = view.findViewById(R.id.filter);
        blockTextView = view.findViewById(R.id.blockTextView);
        blockNameTextView = view.findViewById(R.id.blockNameTextView);
        searchEditText = view.findViewById(R.id.searchEditText);
        filteredItemLayout = view.findViewById(R.id.filteredItemLayout);
        filteredVillLayout = view.findViewById(R.id.filteredVillLayout);
        filteredGpLayout = view.findViewById(R.id.filteredGpLayout);
        filteredGp = view.findViewById(R.id.filteredGp);
        filteredVill = view.findViewById(R.id.filteredVill);

        filterDialog = new Dialog(requireContext());
        filterDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        filterDialog.setContentView(R.layout.layout_bottom_sheet_shg);
        filterDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        filterDialog.setCancelable(true);
        filterDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        filterDialog.getWindow().setGravity(Gravity.BOTTOM);
        filterDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        TextView noRecVill = filterDialog.findViewById(R.id.noRecVill);
        TextView noRecGaonPanchayat = filterDialog.findViewById(R.id.noRecGaonPanchayat);
        LinearLayout villLayout = filterDialog.findViewById(R.id.villLayout);
        panchayatLayout = filterDialog.findViewById(R.id.panchayatLayout);
        TextView villText = filterDialog.findViewById(R.id.villText);
        TextView panchayatText = filterDialog.findViewById(R.id.panchayatText);
        View dismiss = filterDialog.findViewById(R.id.dismiss);
        itemContainerVill = filterDialog.findViewById(R.id.itemContainerVill);
        FlexboxLayout itemContainerGaonPanchayat = filterDialog.findViewById(R.id.itemContainerGaonPanchayat);
        clear = filterDialog.findViewById(R.id.clear);
        showResult = filterDialog.findViewById(R.id.showResult);
        CardView searchPanchayat = filterDialog.findViewById(R.id.searchPanchayat);
        CardView searchVill = filterDialog.findViewById(R.id.searchVill);
        EditText searchEditTextPanchayat = filterDialog.findViewById(R.id.searchEditTextPanchayat);
        EditText searchEditTextVill = filterDialog.findViewById(R.id.searchEditTextVill);

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
        lblText.setText("Self Help Groups");

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment currentFragment = new SHGFragment();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(0, 0);
                fragmentTransaction.replace(R.id.fragment, currentFragment);
                fragmentTransaction.commit();
            }
        });

        Bundle args = getArguments();

        if (args != null) {
            blockName = args.getString("blockName");
            blockId = args.getString("blockId");
            blockNameTextView.setText(blockName);
        }

        blockTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment currentFragment = new SHGFragment();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(0, 0);
                fragmentTransaction.replace(R.id.fragment, currentFragment);
                fragmentTransaction.commit();
            }
        });

        filter.setOnClickListener(view -> {
            tempSelectedPanchayat = selectedPanchayat;
            tempSelectedVill = selectedVillage;
            tempSelectedPanchayatName = selectedPanchayatName;
            tempSelectedVillName = selectedVillName;

            if (tempSelectedPanchayat != null && internetResult) {
                new AsyncTaskVillage(tempSelectedPanchayat).execute();
            }

            panchayatLayout.setOnClickListener(v -> {
                itemContainerVill.removeAllViews();
                itemContainerGaonPanchayat.removeAllViews();

                noRecVill.setVisibility(View.GONE);
                searchVill.setVisibility(View.GONE);

                itemContainerVill.setVisibility(View.GONE);
                itemContainerGaonPanchayat.setVisibility(View.VISIBLE);

                searchEditTextPanchayat.setText("");
                searchEditTextVill.setText("");
                searchEditTextPanchayat.clearFocus();
                searchEditTextVill.clearFocus();

                panchayatLayout.setBackground(getResources().getDrawable(R.drawable.active_button));
                panchayatText.setTextColor(getResources().getColor(R.color.green));
                villLayout.setBackground(null);
                villText.setTextColor(getResources().getColor(R.color.grey2));
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


            villLayout.setOnClickListener(v -> {
                itemContainerVill.removeAllViews();
                itemContainerGaonPanchayat.removeAllViews();

                noRecGaonPanchayat.setVisibility(View.GONE);
                searchPanchayat.setVisibility(View.GONE);

                itemContainerVill.setVisibility(View.VISIBLE);
                itemContainerGaonPanchayat.setVisibility(View.GONE);

                searchEditTextPanchayat.setText("");
                searchEditTextVill.setText("");
                searchEditTextPanchayat.clearFocus();
                searchEditTextVill.clearFocus();

                villLayout.setBackground(getResources().getDrawable(R.drawable.active_button));
                villText.setTextColor(getResources().getColor(R.color.green));
                panchayatLayout.setBackground(null);
                panchayatText.setTextColor(getResources().getColor(R.color.grey2));
                if (villArray != null && villArray.length() > 0) {
                    try {
                        searchVill.setVisibility(View.VISIBLE);
                        showDialog(villArray, "village", itemContainerVill, clear, showResult, filterDialog);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    noRecVill.setVisibility(View.VISIBLE);
                    searchVill.setVisibility(View.GONE);
                }
            });

            searchEditTextPanchayat.setText("");
            searchEditTextVill.setText("");
            searchEditTextPanchayat.clearFocus();
            searchEditTextVill.clearFocus();

            filterDialog.show();

            panchayatLayout.post(panchayatLayout::performClick);

            dismiss.setOnClickListener(v -> {
                filterDialog.dismiss();
            });

            filterDialog.setOnDismissListener(dialogInterface -> {
                tempSelectedPanchayat = null;
                tempSelectedVill = null;
                tempSelectedPanchayatName = null;
                tempSelectedVillName = null;
                villArray = null;
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

                        itemContainerVill.removeAllViews();
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

        searchEditTextVill.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString().trim();

                try {
                    JSONArray filteredArray = new JSONArray();

                    if (villArray != null && villArray.length() > 0) {
                        for (int i = 0; i < villArray.length(); i++) {
                            JSONObject obj = villArray.getJSONObject(i);
                            if (obj.getString("village_name").toLowerCase().contains(text.toLowerCase())) {
                                filteredArray.put(obj);
                            }
                        }

                        itemContainerVill.removeAllViews();
                        itemContainerGaonPanchayat.removeAllViews();

                        if (filteredArray.length() > 0) {
                            showDialog(filteredArray, "village", itemContainerVill, clear, showResult, filterDialog);
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
                    tempSelectedVill = null;
                    tempSelectedVillName = null;
                    itemContainerVill.removeAllViews();
                    if (internetResult)
                        new AsyncTaskVillage(tempSelectedPanchayat).execute();
                });

                radio.setOnClickListener(v -> {
                    for (RadioButton rb : radioButtons) rb.setChecked(rb == radio);
                    tempSelectedPanchayat = String.valueOf(id);
                    tempSelectedPanchayatName = panchayat;
                    tempSelectedVill = null;
                    tempSelectedVillName = null;
                    itemContainerVill.removeAllViews();
                    if (internetResult)
                        new AsyncTaskVillage(tempSelectedPanchayat).execute();
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
        } else if (type.equalsIgnoreCase("village")) {
            List<JSONObject> jsonObjects = new ArrayList<>();
            for (int k = 0; k < jsonArray.length(); k++) jsonObjects.add(jsonArray.getJSONObject(k));

            // Selected Ward first
            JSONObject selectedObject = null;
            List<JSONObject> otherObjects = new ArrayList<>();
            for (JSONObject obj : jsonObjects) {
                int id = obj.getInt("id");
                if (tempSelectedVill != null && tempSelectedVill.equals(String.valueOf(id))) {
                    selectedObject = obj;
                } else {
                    otherObjects.add(obj);
                }
            }

            // Sort alphabetically
            Collections.sort(otherObjects, (o1, o2) -> {
                try {
                    return o1.getString("village_name").compareToIgnoreCase(o2.getString("village_name"));
                } catch (JSONException e) {
                    return 0;
                }
            });

            List<JSONObject> finalList = new ArrayList<>();
            if (selectedObject != null) finalList.add(selectedObject);
            finalList.addAll(otherObjects);

            for (int index = 0; index < finalList.size(); index++) {
                JSONObject json = finalList.get(index);
                String wardName = json.getString("village_name");
                int id = json.getInt("id");

                View mainLayout = LayoutInflater.from(getActivity()).inflate(R.layout.text_item, itemLayout, false);
                RadioButton radio = mainLayout.findViewById(R.id.radio);
                TextView nameTextview = mainLayout.findViewById(R.id.nameTextview);
                nameTextview.setText(wardName);
                radio.setTag(id);

                if (tempSelectedVill != null && tempSelectedVill.equals(String.valueOf(id))) {
                    radio.setChecked(true);
                }

                radioButtons.add(radio);

                mainLayout.setOnClickListener(v -> {
                    for (RadioButton rb : radioButtons) rb.setChecked(false);
                    radio.setChecked(true);
                    tempSelectedVill = String.valueOf(id);
                    tempSelectedVillName = wardName;
                });

                radio.setOnClickListener(v -> {
                    for (RadioButton rb : radioButtons) rb.setChecked(false);
                    radio.setChecked(true);
                    tempSelectedVill = String.valueOf(id);
                    tempSelectedVillName = wardName;
                });

                FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(16, 8, 16, 8);
                mainLayout.setLayoutParams(params);

                if (index >= initialItemCount && !showAll[0]) mainLayout.setVisibility(View.GONE);
                itemLayout.addView(mainLayout);
            }

            // --- Show All for Ward ---
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

        clear.setOnClickListener(v -> {
            tempSelectedPanchayat = null;
            tempSelectedVill = null;
            tempSelectedPanchayatName = null;
            tempSelectedVillName = null;
            panchayatLayout.performClick();
            villArray = null;
            itemContainerVill.removeAllViews();
        });

        // --- Show Result ---
        showResult.setOnClickListener(v -> {
            selectedPanchayat = tempSelectedPanchayat;
            selectedVillage = tempSelectedVill;
            selectedPanchayatName = tempSelectedPanchayatName;
            selectedVillName = tempSelectedVillName;
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
                parameter.put("block_id", blockId);
                parameter.put("gaon_panchayat_id", "");
                parameter.put("village_id", "");
                parameter.put("self_help_group_id", "");

                AsyncTaskShgDetails asyncTaskShgDetails = new AsyncTaskShgDetails(parameter);
                asyncTaskShgDetails.execute();
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

    public class AsyncTaskVillage extends AsyncTask<Void, Void, Void> {
        String gaonPanchayatId;

        public AsyncTaskVillage(String gaonPanchayatId) {
            this.gaonPanchayatId = gaonPanchayatId;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            JSONObject jsonObject = new JSONObject();
            JSONArray gaonPanchayatArray = new JSONArray();

            try {
                gaonPanchayatArray.put(0, gaonPanchayatId);
                jsonObject.put("gaon_panchayat", gaonPanchayatArray);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

            JsonAPICall.makePostRequestWithToken(getActivity(), API.village_by_panchayat,jsonObject , new JsonAPICall.VolleyCallback() {
                @Override
                public void onSuccess(String response) {
                    try { handleWardResponse(response); } catch (JSONException e) { e.printStackTrace(); }
                }

                @Override
                public void onError(VolleyError error) {
                    Toast.makeText(getActivity(), "Internal Error. Please try again", Toast.LENGTH_SHORT).show();
                }
            });
            return null;
        }

        private void handleWardResponse(String response) throws JSONException {
            if (response != null) {
                JSONObject jsonObject = new JSONObject(response);
                if (jsonObject.has("status") && jsonObject.getInt("status") == 1) {
                    villArray = jsonObject.getJSONArray("data");
                }
                if (itemContainerVill.getVisibility() == View.VISIBLE) {
                    itemContainerVill.removeAllViews();
                    try {
                        showDialog(villArray, "village", itemContainerVill, clear, showResult, filterDialog);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public class AsyncTaskShgDetails extends AsyncTask<Void, Void, Void> {
        Map<String, String> parameter;

        public AsyncTaskShgDetails(Map<String, String> parameter) {
            this.parameter = parameter;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            JsonAPICall.makePostRequestWithTokenAndFormData(getActivity(),  API.shg_member_data, parameter, new JsonAPICall.VolleyCallback() {
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
            String panchayat = json.getString("gaon_panchayat_name");
            String group = json.getString("self_help_group_name");
            String phoneNumber = json.getString("phone_number");
            String village = json.getString("village_name");

            if (getActivity() != null) {

                View mainLayout = LayoutInflater.from(getActivity()).inflate(R.layout.shg_item, itemContainer, false);

                TextView nameTextview = mainLayout.findViewById(R.id.nameTextview);
                TextView panchayatTextview = mainLayout.findViewById(R.id.panchayatTextview);
                TextView groupTextview = mainLayout.findViewById(R.id.groupTextview);
                TextView villageTextview = mainLayout.findViewById(R.id.villageTextview);
                TextView numberTextview = mainLayout.findViewById(R.id.numberTextview);
                ImageView whatsappImage = mainLayout.findViewById(R.id.whatsappImage);
                ImageView phoneImage = mainLayout.findViewById(R.id.phoneImage);

                nameTextview.setText(name);
                panchayatTextview.setText(panchayat);
                groupTextview.setText(group);
                numberTextview.setText(phoneNumber);
                villageTextview.setText(village);

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
        parameter.put("block_id", blockId);
        parameter.put("gaon_panchayat_id", selectedPanchayat != null ? selectedPanchayat : "");
        parameter.put("village_id", selectedVillage != null ? selectedVillage : "");
        parameter.put("self_help_group_id", "");

        requireActivity().runOnUiThread(() -> {
            if (!TextUtils.isEmpty(selectedPanchayat) || !TextUtils.isEmpty(selectedVillage)) {
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

            if (!TextUtils.isEmpty(selectedVillage)) {
                filteredVillLayout.setVisibility(View.VISIBLE);
                filteredVill.setText(selectedVillName);
            } else {
                filteredVillLayout.setVisibility(View.GONE);
            }
        });

        AsyncTaskShgDetails asyncTaskShgDetails = new AsyncTaskShgDetails(parameter);
        asyncTaskShgDetails.execute();
    }


    private void handleBackPress() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Fragment currentFragment = new SHGFragment();
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