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
import androidx.core.content.ContextCompat;
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


public class SchoolCollegeFragment extends Fragment implements ConnectivityReceiver.ConnectivityListener{

    View view;
    String profile_image_uri,selectedPanchayat,selectedType;
    UserManagement userManagement;
    private ConnectivityReceiver connectivityReceiver;
    boolean internetResult = false;
    LinearLayout lin,noRec,paginationLayout,itemContainer,filteredCatLayout,filteredGpLayout;
    ProgressBar spin_kit;
    JSONArray gaoArray,typeArray;
    private LinearLayout selectedCard = null;
    private TextView selectedNameText = null;
    private TextView selectedAddressText = null;
    Dialog filterDialog;
    CardView showResult;
    ImageView filter;
    private boolean isAPICallRunning = false;
    private int itemsPerPage = 5;
    int totalPages;
    private int selectedPage = 0;
    private TextView selectedPageButton;
    private String currentSearchText = "";
    private String tempSelectedType = null;
    private String tempSelectedPanchayat = null;
    EditText searchEditText;
    private ImageView selectedImageView = null;
    HorizontalScrollView filteredItemLayout;
    TextView filteredCat,filteredGp;
    String tempSelectedPanchayatName = null;
    String tempSelectedTypeName = null;
    String selectedPanchayatName = null;
    String selectedTypeName = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_school_college, container, false);

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
        searchEditText = view.findViewById(R.id.searchEditText);
        filteredItemLayout = view.findViewById(R.id.filteredItemLayout);
        filteredCatLayout = view.findViewById(R.id.filteredCatLayout);
        filteredGpLayout = view.findViewById(R.id.filteredGpLayout);
        filteredCat = view.findViewById(R.id.filteredCat);
        filteredGp = view.findViewById(R.id.filteredGp);

        filterDialog = new Dialog(requireContext());
        filterDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        filterDialog.setContentView(R.layout.layout_bottom_sheet_school_college);
        filterDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        filterDialog.setCancelable(true);
        filterDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        filterDialog.getWindow().setGravity(Gravity.BOTTOM);
        filterDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        CardView searchPanchayat = filterDialog.findViewById(R.id.searchPanchayat);
        CardView searchCategory = filterDialog.findViewById(R.id.searchCategory);
        EditText searchEditTextPanchayat = filterDialog.findViewById(R.id.searchEditTextPanchayat);
        EditText searchEditTextCategory = filterDialog.findViewById(R.id.searchEditTextCategory);

        TextView noRecGaonPanchayat = filterDialog.findViewById(R.id.noRecGaonPanchayat);
        TextView noRecType = filterDialog.findViewById(R.id.noRecType);
        LinearLayout panchayatLayout = filterDialog.findViewById(R.id.panchayatLayout);
        LinearLayout typeLayout = filterDialog.findViewById(R.id.typeLayout);
        TextView panchayatText = filterDialog.findViewById(R.id.panchayatText);
        TextView typeText = filterDialog.findViewById(R.id.typeText);
        View dismiss = filterDialog.findViewById(R.id.dismiss);
        FlexboxLayout itemContainerGaonPanchayat = filterDialog.findViewById(R.id.itemContainerGaonPanchayat);
        FlexboxLayout itemContainerType = filterDialog.findViewById(R.id.itemContainerType);
        CardView clear = filterDialog.findViewById(R.id.clear);
        showResult = filterDialog.findViewById(R.id.showResult);

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
        lblText.setText("Schools");

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

        String jsonString = "{\n" +
                "  \"data\": [\n" +
                "    {\n" +
                "      \"value\": \"HS\",\n" +
                "      \"name\": \"HS\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"value\": \"HSS\",\n" +
                "      \"name\": \"HSS\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"value\": \"LPS\",\n" +
                "      \"name\": \"LPS\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"value\": \"UPS\",\n" +
                "      \"name\": \"UPS\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                tempSelectedType = selectedType;
                tempSelectedPanchayat = selectedPanchayat;
                tempSelectedTypeName = selectedTypeName;
                tempSelectedPanchayatName = selectedPanchayatName;


                typeLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        itemContainerType.removeAllViews();
                        itemContainerGaonPanchayat.removeAllViews();

                        searchPanchayat.setVisibility(View.GONE);
                        noRecGaonPanchayat.setVisibility(View.GONE);

                        itemContainerGaonPanchayat.setVisibility(View.GONE);
                        itemContainerType.setVisibility(View.VISIBLE);

                        searchEditTextPanchayat.setText("");
                        searchEditTextCategory.setText("");
                        searchEditTextPanchayat.clearFocus();
                        searchEditTextCategory.clearFocus();


                        typeLayout.setBackground(getResources().getDrawable(R.drawable.active_button));
                        typeText.setTextColor(getResources().getColor(R.color.green));
                        panchayatText.setTextColor(getResources().getColor(R.color.grey2));
                        panchayatLayout.setBackground(null);
                        try {
                            JSONObject jsonObject = new JSONObject(jsonString);
                            typeArray = jsonObject.getJSONArray("data");
                            if (typeArray != null && typeArray.length()>0){
                                searchCategory.setVisibility(View.VISIBLE);
                                showDialog(typeArray,"type",itemContainerType,clear,showResult,filterDialog);
                            }else{
                                noRecType.setVisibility(View.VISIBLE);
                                searchCategory.setVisibility(View.GONE);
                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });

                panchayatLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        itemContainerType.removeAllViews();
                        itemContainerGaonPanchayat.removeAllViews();

                        searchCategory.setVisibility(View.GONE);
                        noRecType.setVisibility(View.GONE);

                        itemContainerType.setVisibility(View.GONE);
                        itemContainerGaonPanchayat.setVisibility(View.VISIBLE);

                        searchEditTextPanchayat.setText("");
                        searchEditTextCategory.setText("");
                        searchEditTextPanchayat.clearFocus();
                        searchEditTextCategory.clearFocus();

                        panchayatLayout.setBackground(getResources().getDrawable(R.drawable.active_button));
                        panchayatText.setTextColor(getResources().getColor(R.color.green));
                        typeText.setTextColor(getResources().getColor(R.color.grey2));
                        typeLayout.setBackground(null);

                        try {
                            if (gaoArray != null && gaoArray.length()>0){
                                searchPanchayat.setVisibility(View.VISIBLE);
                                showDialog(gaoArray,"panchayat",itemContainerGaonPanchayat,clear,showResult,filterDialog);
                            }else{
                                noRecGaonPanchayat.setVisibility(View.VISIBLE);
                                searchPanchayat.setVisibility(View.GONE);
                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });

                typeLayout.post(() -> {
                    typeLayout.performClick();
                });

                dismiss.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        filterDialog.dismiss();
                    }
                });

                searchEditTextPanchayat.setText("");
                searchEditTextCategory.setText("");
                searchEditTextPanchayat.clearFocus();
                searchEditTextCategory.clearFocus();

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

                        itemContainerType.removeAllViews();
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

        searchEditTextCategory.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString().trim();

                try {
                    JSONArray filteredArray = new JSONArray();

                    if (typeArray != null && typeArray.length() > 0) {
                        for (int i = 0; i < typeArray.length(); i++) {
                            JSONObject obj = typeArray.getJSONObject(i);
                            if (obj.getString("name").toLowerCase().contains(text.toLowerCase())) {
                                filteredArray.put(obj);
                            }
                        }

                        itemContainerType.removeAllViews();
                        itemContainerGaonPanchayat.removeAllViews();

                        if (filteredArray.length() > 0) {
                            showDialog(filteredArray, "type", itemContainerType, clear, showResult, filterDialog);
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        return view;
    }

    private void showDialog(JSONArray jsonArray, String type, FlexboxLayout itemLayout, CardView clear, CardView showResult, Dialog potholeDialog) throws JSONException {
        itemLayout.removeAllViews();
        int initialItemCount = 10;
        boolean[] showAll = {false};

        if (type.equalsIgnoreCase("type")) {
            List<RadioButton> radioButtons = new ArrayList<>();
            List<JSONObject> jsonObjects = new ArrayList<>();
            for (int k = 0; k < jsonArray.length(); k++) jsonObjects.add(jsonArray.getJSONObject(k));

            // Move selected item to top
            JSONObject selectedObject = null;
            List<JSONObject> otherObjects = new ArrayList<>();
            for (JSONObject obj : jsonObjects) {
                String value = obj.getString("value");
                if (tempSelectedType != null && tempSelectedType.equalsIgnoreCase(value)) selectedObject = obj;
                else otherObjects.add(obj);
            }

            Collections.sort(otherObjects, (o1, o2) -> {
                try { return o1.getString("name").compareToIgnoreCase(o2.getString("name")); }
                catch (JSONException e) { return 0; }
            });

            List<JSONObject> finalList = new ArrayList<>();
            if (selectedObject != null) finalList.add(selectedObject);
            finalList.addAll(otherObjects);

            for (int index = 0; index < finalList.size(); index++) {
                JSONObject json = finalList.get(index);
                String typeString = json.getString("name");
                String value = json.getString("value");

                View mainLayout = LayoutInflater.from(getActivity()).inflate(R.layout.text_item, itemLayout, false);
                RadioButton radio = mainLayout.findViewById(R.id.radio);
                TextView nameTextview = mainLayout.findViewById(R.id.nameTextview);
                nameTextview.setText(typeString);
                radio.setTag(value);

                if (tempSelectedType != null && tempSelectedType.equalsIgnoreCase(value)) radio.setChecked(true);

                radioButtons.add(radio);

                mainLayout.setOnClickListener(v -> {
                    for (RadioButton rb : radioButtons) rb.setChecked(rb == radio);
                    tempSelectedType = value;
                    tempSelectedTypeName = typeString;
                });

                radio.setOnClickListener(v -> {
                    for (RadioButton rb : radioButtons) rb.setChecked(rb == radio);
                    tempSelectedType = value;
                    tempSelectedTypeName = typeString;
                });

                FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(16, 8, 16, 8);
                mainLayout.setLayoutParams(params);

                // Hide items beyond initialItemCount
                if (index >= initialItemCount && !showAll[0]) mainLayout.setVisibility(View.GONE);

                itemLayout.addView(mainLayout);
            }

            // Show All TextView
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
                        // Simply set all children visible
                        child.setVisibility(View.VISIBLE);
                    }
                    showMoreTv.setVisibility(View.GONE);
                });
            }

            clear.setOnClickListener(v -> {
                for (RadioButton rb : radioButtons) rb.setChecked(false);
                tempSelectedType = null;
                tempSelectedTypeName = null;
            });
        } else if (type.equalsIgnoreCase("panchayat")) {
            List<RadioButton> radioButtons = new ArrayList<>();
            List<JSONObject> jsonObjects = new ArrayList<>();
            for (int k = 0; k < jsonArray.length(); k++) jsonObjects.add(jsonArray.getJSONObject(k));

            // Move selected Panchayat to top
            JSONObject selectedObject = null;
            List<JSONObject> otherObjects = new ArrayList<>();
            for (JSONObject obj : jsonObjects) {
                int id = obj.getInt("id");
                if (tempSelectedPanchayat != null && tempSelectedPanchayat.equals(String.valueOf(id))) selectedObject = obj;
                else otherObjects.add(obj);
            }

            Collections.sort(otherObjects, (o1, o2) -> {
                try { return o1.getString("name").compareToIgnoreCase(o2.getString("name")); }
                catch (JSONException e) { return 0; }
            });

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

                if (tempSelectedPanchayat != null && tempSelectedPanchayat.equals(String.valueOf(id))) radio.setChecked(true);

                radioButtons.add(radio);

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

                if (index >= initialItemCount && !showAll[0]) mainLayout.setVisibility(View.GONE);

                itemLayout.addView(mainLayout);
            }

            // Show All TextView
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

            clear.setOnClickListener(v -> {
                for (RadioButton rb : radioButtons) rb.setChecked(false);
                tempSelectedPanchayat = null;
                tempSelectedPanchayatName = null;
            });
        }

        showResult.setOnClickListener(v -> {
            selectedPanchayat = tempSelectedPanchayat;
            selectedType = tempSelectedType;
            selectedPanchayatName = tempSelectedPanchayatName;
            selectedTypeName = tempSelectedTypeName;
            selectedPage = 0;
            selectedPageButton = null;
            potholeDialog.dismiss();
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
                parameter.put("school_type","");
                parameter.put("gaon_panchayat", "");

                AsyncTaskSchools asyncTaskSchools = new AsyncTaskSchools(parameter);
                asyncTaskSchools.execute();
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

    public class AsyncTaskSchools extends AsyncTask<Void, Void, Void> {
        Map<String, String> parameter;

        public AsyncTaskSchools(Map<String, String> parameter) {
            this.parameter = parameter;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            JsonAPICall.makePostRequestWithTokenAndFormData(getActivity(),  API.school_data, parameter, new JsonAPICall.VolleyCallback() {
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
            String schoolName = json.getString("name");
            String address = json.getString("address");
            int schoolId = json.getInt("id");

            if (getActivity() != null) {
                View mainLayout = LayoutInflater.from(getActivity()).inflate(R.layout.school_college_item, itemContainer, false);

                TextView nameTextview = mainLayout.findViewById(R.id.nameTextview);
                TextView addressTextview = mainLayout.findViewById(R.id.addressTextview);
                LinearLayout mainCardLayout = mainLayout.findViewById(R.id.mainCardLayout);
                ImageView imageView = mainLayout.findViewById(R.id.imageView);

                nameTextview.setText(schoolName);
                addressTextview.setText(address);

                mainCardLayout.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.grey3));
                addressTextview.setTextColor(ContextCompat.getColor(getActivity(), R.color.black));
                nameTextview.setTextColor(ContextCompat.getColor(getActivity(), R.color.black));
                imageView.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.vector11));

                mainCardLayout.setOnClickListener(v -> {
                    if (selectedCard != null && selectedNameText != null && selectedAddressText != null) {
                        selectedCard.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.grey3));
                        selectedNameText.setTextColor(ContextCompat.getColor(getActivity(), R.color.black));
                        selectedAddressText.setTextColor(ContextCompat.getColor(getActivity(), R.color.black));
                        selectedImageView.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.vector11));
                    }

                    addressTextview.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));
                    mainCardLayout.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.green));
                    nameTextview.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));
                    imageView.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.icons8_forward_24));

                    selectedCard = mainCardLayout;
                    selectedAddressText = addressTextview;
                    selectedNameText = nameTextview;
                    selectedImageView = imageView;

                    Fragment newFragment = new SchoolCollegeDetailsFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("schoolName", schoolName);
                    bundle.putString("schoolId", String.valueOf(schoolId));
                    newFragment.setArguments(bundle);

                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.setCustomAnimations(0, 0);
                    fragmentTransaction.replace(R.id.fragment, newFragment);
                    fragmentTransaction.commit();
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
        parameter.put("school_type", selectedType != null ? selectedType : "");
        parameter.put("gaon_panchayat", selectedPanchayat != null ? selectedPanchayat : "");

        requireActivity().runOnUiThread(() -> {
            if (!TextUtils.isEmpty(selectedPanchayat) || !TextUtils.isEmpty(selectedType)) {
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

            if (!TextUtils.isEmpty(selectedType)) {
                filteredCatLayout.setVisibility(View.VISIBLE);
                filteredCat.setText(selectedTypeName);
            } else {
                filteredCatLayout.setVisibility(View.GONE);
            }
        });

        AsyncTaskSchools asyncTaskSchools = new AsyncTaskSchools(parameter);
        asyncTaskSchools.execute();
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