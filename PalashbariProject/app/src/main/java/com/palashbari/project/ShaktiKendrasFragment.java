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
import android.util.Log;
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


public class ShaktiKendrasFragment extends Fragment implements ConnectivityReceiver.ConnectivityListener{

    View view;
    String profile_image_uri;
    UserManagement userManagement;
    private ConnectivityReceiver connectivityReceiver;
    boolean internetResult = false;
    LinearLayout lin,noRec,itemContainer,paginationLayout,mandalLayout,filteredMandalLayout,filteredBoothLayout;
    ProgressBar spin_kit;
    JSONArray mandalArray,boothArray;
    ImageView filter;
    Dialog filterDialog;
    CardView showResult,clear;
    private boolean isAPICallRunning = false;
    private int itemsPerPage = 5;
    int totalPages;
    private int selectedPage = 0;
    private TextView selectedPageButton;
    FlexboxLayout itemContainerBooth;
    private String selectedMandal = null;
    private String selectedBooth = null;
    EditText searchEditText;
    private String tempSelectedMandal = null;
    private String tempSelectedBooth = null;
    private String currentSearchText = "";
    HorizontalScrollView filteredItemLayout;
    TextView filteredMandal,filteredBooth;
    String tempSelectedMandalName = null;
    String tempSelectedBoothName = null;
    String selectedMandalName = null;
    String selectedBoothName = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_shakti_kendras, container, false);

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
        filteredMandalLayout = view.findViewById(R.id.filteredMandalLayout);
        filteredBoothLayout = view.findViewById(R.id.filteredBoothLayout);
        filteredItemLayout = view.findViewById(R.id.filteredItemLayout);
        filteredMandal = view.findViewById(R.id.filteredMandal);
        filteredBooth = view.findViewById(R.id.filteredBooth);

        filterDialog = new Dialog(requireContext());
        filterDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        filterDialog.setContentView(R.layout.layout_bottom_sheet_shakti_kendra);
        filterDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        filterDialog.setCancelable(true);
        filterDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        filterDialog.getWindow().setGravity(Gravity.BOTTOM);
        filterDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        TextView noRecBooth = filterDialog.findViewById(R.id.noRecBooth);
        TextView noRecMandal = filterDialog.findViewById(R.id.noRecMandal);
        LinearLayout boothLayout = filterDialog.findViewById(R.id.boothLayout);
        mandalLayout = filterDialog.findViewById(R.id.mandalLayout);
        TextView boothText = filterDialog.findViewById(R.id.boothText);
        TextView mandalText = filterDialog.findViewById(R.id.mandalText);
        View dismiss = filterDialog.findViewById(R.id.dismiss);
        itemContainerBooth = filterDialog.findViewById(R.id.itemContainerBooth);
        FlexboxLayout itemContainerMandal = filterDialog.findViewById(R.id.itemContainerMandal);
        clear = filterDialog.findViewById(R.id.clear);
        showResult = filterDialog.findViewById(R.id.showResult);
        CardView searchMandal = filterDialog.findViewById(R.id.searchMandal);
        CardView searchBooth = filterDialog.findViewById(R.id.searchBooth);
        EditText searchEditTextMandal = filterDialog.findViewById(R.id.searchEditTextMandal);
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
        lblText.setText("Shakti Kendra Ahbayak");

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


        filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tempSelectedMandal = selectedMandal;
                tempSelectedBooth = selectedBooth;
                tempSelectedMandalName = selectedMandalName;
                tempSelectedBoothName = selectedBoothName;

                if (tempSelectedMandal != null && internetResult) {
                    new AsyncTaskBooth(tempSelectedMandal).execute();
                }

                mandalLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        itemContainerMandal.removeAllViews();
                        itemContainerBooth.removeAllViews();

                        searchBooth.setVisibility(View.GONE);
                        noRecBooth.setVisibility(View.GONE);

                        itemContainerBooth.setVisibility(View.GONE);
                        itemContainerMandal.setVisibility(View.VISIBLE);

                        searchEditTextMandal.setText("");
                        searchEditTextBooth.setText("");
                        searchEditTextMandal.clearFocus();
                        searchEditTextBooth.clearFocus();

                        mandalLayout.setBackground(getResources().getDrawable(R.drawable.active_button));
                        mandalText.setTextColor(getResources().getColor(R.color.green));
                        boothText.setTextColor(getResources().getColor(R.color.grey2));
                        boothLayout.setBackground(null);
                        try {
                            if (mandalArray != null && mandalArray.length()>0){
                                searchMandal.setVisibility(View.VISIBLE);
                                showDialog(mandalArray,"mandal",itemContainerMandal,clear,showResult,filterDialog);
                            }else{
                                noRecMandal.setVisibility(View.VISIBLE);
                                searchMandal.setVisibility(View.GONE);
                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });

                boothLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        itemContainerMandal.removeAllViews();
                        itemContainerBooth.removeAllViews();

                        searchMandal.setVisibility(View.GONE);
                        noRecMandal.setVisibility(View.GONE);

                        itemContainerBooth.setVisibility(View.VISIBLE);
                        itemContainerMandal.setVisibility(View.GONE);

                        searchEditTextMandal.setText("");
                        searchEditTextBooth.setText("");
                        searchEditTextMandal.clearFocus();
                        searchEditTextBooth.clearFocus();

                        boothLayout.setBackground(getResources().getDrawable(R.drawable.active_button));
                        boothText.setTextColor(getResources().getColor(R.color.green));
                        mandalText.setTextColor(getResources().getColor(R.color.grey2));
                        mandalLayout.setBackground(null);

                        try {
                            if (boothArray != null && boothArray.length()>0){
                                searchBooth.setVisibility(View.VISIBLE);
                                showDialog(boothArray,"booth",itemContainerBooth,clear,showResult,filterDialog);
                            }else{
                                noRecBooth.setVisibility(View.VISIBLE);
                                searchBooth.setVisibility(View.GONE);
                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });

                searchEditTextMandal.setText("");
                searchEditTextBooth.setText("");
                searchEditTextMandal.clearFocus();
                searchEditTextBooth.clearFocus();

                filterDialog.show();

                mandalLayout.post(mandalLayout::performClick);

                dismiss.setOnClickListener(v -> filterDialog.dismiss());

                filterDialog.setOnDismissListener(dialogInterface -> {
                    tempSelectedBooth = null;
                    tempSelectedMandal = null;
                    tempSelectedBoothName = null;
                    tempSelectedMandalName = null;
                    boothArray = null;
                });
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

        searchEditTextMandal.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString().trim();

                try {
                    JSONArray filteredArray = new JSONArray();

                    if (mandalArray != null && mandalArray.length() > 0) {
                        for (int i = 0; i < mandalArray.length(); i++) {
                            JSONObject obj = mandalArray.getJSONObject(i);
                            if (obj.getString("name").toLowerCase().contains(text.toLowerCase())) {
                                filteredArray.put(obj);
                            }
                        }

                        itemContainerBooth.removeAllViews();
                        itemContainerMandal.removeAllViews();

                        if (filteredArray.length() > 0) {
                            showDialog(filteredArray, "mandal", itemContainerMandal, clear, showResult, filterDialog);
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
                        itemContainerMandal.removeAllViews();

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
        itemLayout.removeAllViews();

        int initialItemCount = 10;
        boolean[] showAll = {false};

        if (type.equalsIgnoreCase("mandal")) {
            List<RadioButton> radioButtons = new ArrayList<>();
            List<JSONObject> jsonObjects = new ArrayList<>();
            for (int k = 0; k < jsonArray.length(); k++) jsonObjects.add(jsonArray.getJSONObject(k));

            JSONObject selectedObject = null;
            List<JSONObject> otherObjects = new ArrayList<>();
            for (JSONObject obj : jsonObjects) {
                int id = obj.getInt("id");
                if (tempSelectedMandal != null && tempSelectedMandal.equals(String.valueOf(id))) {
                    selectedObject = obj;
                } else {
                    otherObjects.add(obj);
                }
            }

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
                String mandal = json.getString("name");
                int id = json.getInt("id");

                View mainLayout = LayoutInflater.from(getActivity()).inflate(R.layout.text_item, itemLayout, false);
                RadioButton radio = mainLayout.findViewById(R.id.radio);
                TextView nameTextview = mainLayout.findViewById(R.id.nameTextview);
                nameTextview.setText(mandal);
                radio.setTag(id);

                if (tempSelectedMandal != null && tempSelectedMandal.equals(String.valueOf(id))) {
                    radio.setChecked(true);
                }

                radioButtons.add(radio);

                mainLayout.setOnClickListener(v -> {
                    for (RadioButton rb : radioButtons) rb.setChecked(rb == radio);
                    tempSelectedMandal = String.valueOf(id);
                    tempSelectedMandalName = mandal;
                    tempSelectedBooth = null;
                    tempSelectedBoothName = null;
                    itemContainerBooth.removeAllViews();
                    if (internetResult)
                        new AsyncTaskBooth(tempSelectedMandal).execute();
                });

                radio.setOnClickListener(v -> {
                    for (RadioButton rb : radioButtons) rb.setChecked(rb == radio);
                    tempSelectedMandal = String.valueOf(id);
                    tempSelectedMandalName = mandal;
                    tempSelectedBooth = null;
                    itemContainerBooth.removeAllViews();
                    tempSelectedBoothName = null;
                    if (internetResult)
                        new AsyncTaskBooth(tempSelectedMandal).execute();
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

            List<RadioButton> radioButtons = new ArrayList<>();

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

                View.OnClickListener selectionListener = v -> {
                    for (RadioButton rb : radioButtons) rb.setChecked(rb == radio);
                    radio.setChecked(true);
                    tempSelectedBooth = String.valueOf(id);
                    tempSelectedBoothName = boothName;
                };

                mainLayout.setOnClickListener(selectionListener);
                radio.setOnClickListener(selectionListener);

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
            tempSelectedMandal = null;
            tempSelectedBooth = null;
            tempSelectedMandalName = null;
            tempSelectedBoothName = null;
            mandalLayout.performClick();
            boothArray = null;
            itemContainerBooth.removeAllViews();
        });

        // --- Show Result ---
        showResult.setOnClickListener(v -> {
            selectedMandal = tempSelectedMandal;
            selectedBooth = tempSelectedBooth;
            selectedMandalName = tempSelectedMandalName;
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

                AsyncTaskMandal asyncTaskMandal = new AsyncTaskMandal();
                asyncTaskMandal.execute();

                Map<String, String> parameter = new HashMap<>();
                parameter.put("draw", "1");
                parameter.put("start", "0");
                parameter.put("length",  "5");
                parameter.put("search[value]", "");
                parameter.put("polling_booth", "");
                parameter.put("mandal", "");

                AsyncTaskShaktiKendra asyncTaskShaktiKendra = new AsyncTaskShaktiKendra(parameter);
                asyncTaskShaktiKendra.execute();
                isAPICallRunning = true;
            }
        }else{
            internetResult = false;
            showResult.setEnabled(false);
        }
    }

    public class AsyncTaskMandal extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            if (getActivity() != null) {
                JsonAPICall.makeGetRequestWithToken(getActivity(), API.mandal, new JsonAPICall.VolleyCallback() {
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
                        mandalArray = jsonObject.getJSONArray("data");
                    }
                }
            }
        }
    }

    public class AsyncTaskBooth extends AsyncTask<Void, Void, Void> {
        String mandalId;

        public AsyncTaskBooth(String mandalId) {
            this.mandalId = mandalId;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (getActivity() != null) {
                Map<String, String> parameter = new HashMap<>();
                parameter.put("mandal", mandalId);

                JsonAPICall.makePostRequestWithTokenAndFormData(getActivity(), API.booth_by_mandal, parameter, new JsonAPICall.VolleyCallback() {
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
                if (jsonObject.has("status") && jsonObject.getInt("status") == 1) {
                    boothArray = jsonObject.getJSONObject("data").getJSONArray("polling_booth");
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

    public class AsyncTaskShaktiKendra extends AsyncTask<Void, Void, Void> {
        Map<String, String> parameter;

        public AsyncTaskShaktiKendra(Map<String, String> parameter) {
            this.parameter = parameter;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            JsonAPICall.makePostRequestWithTokenAndFormData(getActivity(),  API.shakti_kendra_data, parameter, new JsonAPICall.VolleyCallback() {
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
            String boothName = json.getString("polling_booth_name");
            String name = json.getString("pramukh_name");
            String phoneNumber = json.getString("phone_number");


            if (getActivity() != null) {

                View mainLayout = LayoutInflater.from(getActivity()).inflate(R.layout.shakti_kendras_item, itemContainer, false);

                TextView boothNameTextview = mainLayout.findViewById(R.id.boothNameTextview);
                TextView nameTextview = mainLayout.findViewById(R.id.nameTextview);
                TextView numberTextview = mainLayout.findViewById(R.id.numberTextview);
                ImageView whatsappImage = mainLayout.findViewById(R.id.whatsappImage);
                ImageView phoneImage = mainLayout.findViewById(R.id.phoneImage);

                boothNameTextview.setText(boothName);
                nameTextview.setText(name);
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
        parameter.put("polling_booth", selectedBooth != null ? selectedBooth : "");
        parameter.put("mandal", selectedMandal != null ? selectedMandal : "");

        requireActivity().runOnUiThread(() -> {
            if (!TextUtils.isEmpty(selectedMandal) || !TextUtils.isEmpty(selectedBooth)) {
                filteredItemLayout.setVisibility(View.VISIBLE);
            } else {
                filteredItemLayout.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(selectedMandal)) {
                filteredMandalLayout.setVisibility(View.VISIBLE);
                filteredMandal.setText(selectedMandalName);
            } else {
                filteredMandalLayout.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(selectedBooth)) {
                filteredBoothLayout.setVisibility(View.VISIBLE);
                filteredBooth.setText(selectedBoothName);
            } else {
                filteredBoothLayout.setVisibility(View.GONE);
            }
        });

        AsyncTaskShaktiKendra asyncTaskShaktiKendra = new AsyncTaskShaktiKendra(parameter);
        asyncTaskShaktiKendra.execute();
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