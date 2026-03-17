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


public class MorchasDetailsFragment extends Fragment implements ConnectivityReceiver.ConnectivityListener{

    View view;
    String profile_image_uri,selectedDesignation,morchaName,morchaId,morchaType;
    UserManagement userManagement;
    private ConnectivityReceiver connectivityReceiver;
    boolean internetResult = false;
    LinearLayout lin,noRec,paginationLayout,itemContainer,filteredDesignationLayout;
    ProgressBar spin_kit;
    ImageView filter;
    JSONArray designationArray;
    Dialog filterDialog;
    CardView showResult;
    TextView morchaTextView,morchaNameTextView;
    private boolean isAPICallRunning = false;
    private int itemsPerPage = 5;
    int totalPages;
    private int selectedPage = 0;
    private TextView selectedPageButton;
    private String currentSearchText = "";
    private String tempSelectedDesignation = null;
    EditText searchEditText;
    HorizontalScrollView filteredItemLayout;
    TextView filteredDesignation;
    String tempSelectedDesignationName = null;
    String selectedDesignationName = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_morchas_details, container, false);

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
        morchaTextView = view.findViewById(R.id.morchaTextView);
        morchaNameTextView = view.findViewById(R.id.morchaNameTextView);
        searchEditText = view.findViewById(R.id.searchEditText);
        filteredItemLayout = view.findViewById(R.id.filteredItemLayout);
        filteredDesignationLayout = view.findViewById(R.id.filteredDesignationLayout);
        filteredDesignation = view.findViewById(R.id.filteredDesignation);

        filterDialog = new Dialog(requireContext());
        filterDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        filterDialog.setContentView(R.layout.layout_bottom_sheet_district_committee);
        filterDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        filterDialog.setCancelable(true);
        filterDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        filterDialog.getWindow().setGravity(Gravity.BOTTOM);
        filterDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        TextView noRecDesignation = filterDialog.findViewById(R.id.noRecDesignation);
        LinearLayout designationLayout = filterDialog.findViewById(R.id.designationLayout);
        TextView designationText = filterDialog.findViewById(R.id.designationText);
        View dismiss = filterDialog.findViewById(R.id.dismiss);
        FlexboxLayout itemContainerDesignation = filterDialog.findViewById(R.id.itemContainerDesignation);
        CardView clear = filterDialog.findViewById(R.id.clear);
        showResult = filterDialog.findViewById(R.id.showResult);
        CardView searchDesignation = filterDialog.findViewById(R.id.searchDesignation);
        EditText searchEditTextDesignation = filterDialog.findViewById(R.id.searchEditTextDesignation);

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
        lblText.setText("Morchas");

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment currentFragment = new MorchasFragment();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(0, 0);
                fragmentTransaction.replace(R.id.fragment, currentFragment);
                fragmentTransaction.commit();
            }
        });

        Bundle args = getArguments();

        if (args != null) {
            morchaName = args.getString("morchaName");
            morchaId = args.getString("morchaId");
            morchaType = args.getString("morchaType");
            morchaNameTextView.setText(morchaName);
        }

        morchaTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment currentFragment = new MorchasFragment();
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

                tempSelectedDesignation = selectedDesignation;
                tempSelectedDesignationName = selectedDesignationName;

                designationLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        itemContainerDesignation.removeAllViews();
                        searchEditTextDesignation.setText("");
                        searchEditTextDesignation.clearFocus();

                        designationLayout.setBackground(getResources().getDrawable(R.drawable.active_button));
                        designationText.setTextColor(getResources().getColor(R.color.green));
                        try {
                            if (designationArray != null && designationArray.length()>0){
                                searchDesignation.setVisibility(View.VISIBLE);
                                showDialog(designationArray,itemContainerDesignation,clear,showResult,filterDialog);
                            }else{
                                noRecDesignation.setVisibility(View.VISIBLE);
                                searchDesignation.setVisibility(View.GONE);
                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });

                designationLayout.post(() -> {
                    designationLayout.performClick();
                });

                dismiss.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        filterDialog.dismiss();
                    }
                });

                searchEditTextDesignation.setText("");
                searchEditTextDesignation.clearFocus();

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

        searchEditTextDesignation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString().trim();

                try {
                    JSONArray filteredArray = new JSONArray();

                    if (designationArray != null && designationArray.length() > 0) {
                        for (int i = 0; i < designationArray.length(); i++) {
                            JSONObject obj = designationArray.getJSONObject(i);
                            if (obj.getString("name").toLowerCase().contains(text.toLowerCase())) {
                                filteredArray.put(obj);
                            }
                        }

                        itemContainerDesignation.removeAllViews();

                        if (filteredArray.length() > 0) {
                            showDialog(filteredArray,  itemContainerDesignation, clear, showResult, filterDialog);
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
            if (tempSelectedDesignation != null && tempSelectedDesignation.equals(String.valueOf(id))) {
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
            String designation = json.getString("name");
            int id = json.getInt("id");


            // Inflate item layout
            View mainLayout = LayoutInflater.from(getActivity()).inflate(R.layout.text_item, itemLayout, false);
            RadioButton radio = mainLayout.findViewById(R.id.radio);
            TextView nameTextview = mainLayout.findViewById(R.id.nameTextview);
            nameTextview.setText(designation);
            radio.setTag(id);

            if (tempSelectedDesignation != null && tempSelectedDesignation.equals(String.valueOf(id))) {
                radio.setChecked(true);
            }

            radioButtons.add(radio);

            // Click listeners
            mainLayout.setOnClickListener(v -> {
                for (RadioButton rb : radioButtons) rb.setChecked(rb == radio);
                tempSelectedDesignation = String.valueOf(id);
                tempSelectedDesignationName = designation;
            });
            radio.setOnClickListener(v -> {
                for (RadioButton rb : radioButtons) rb.setChecked(rb == radio);
                tempSelectedDesignation = String.valueOf(id);
                tempSelectedDesignationName = designation;
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
            tempSelectedDesignation = null;
            tempSelectedDesignationName = null;
        });

        // Show result
        showResult.setOnClickListener(v -> {
            selectedDesignation = tempSelectedDesignation;
            selectedDesignationName = tempSelectedDesignationName;
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

                AsyncTaskDesignation asyncTaskDesignation = new AsyncTaskDesignation();
                asyncTaskDesignation.execute();

                Map<String, String> parameter = new HashMap<>();
                parameter.put("draw", "1");
                parameter.put("start", "0");
                parameter.put("length",  "5");
                parameter.put("search[value]", "");
                parameter.put("morcha_id", morchaId);
                parameter.put("type", morchaType);
                parameter.put("designation", "");

                AsyncTaskMorchaDetails asyncTaskMorchaDetails = new AsyncTaskMorchaDetails(parameter);
                asyncTaskMorchaDetails.execute();
                isAPICallRunning = true;
            }
        }else{
            internetResult = false;
            showResult.setEnabled(false);
        }
    }

    public class AsyncTaskDesignation extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            if (getActivity() != null) {
                JsonAPICall.makeGetRequestWithToken(getActivity(), API.designation, new JsonAPICall.VolleyCallback() {
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
                        designationArray = jsonObject.getJSONArray("data");
                    }
                }
            }
        }
    }

    public class AsyncTaskMorchaDetails extends AsyncTask<Void, Void, Void> {

        Map<String, String> parameter;


        public AsyncTaskMorchaDetails(Map<String, String> parameter) {
            this.parameter = parameter;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            JsonAPICall.makePostRequestWithTokenAndFormData(getActivity(),  API.morcha_data, parameter, new JsonAPICall.VolleyCallback() {
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
            String designation = json.getString("designation_name");
            String phoneNumber = json.getString("phone_number");


            if (getActivity() != null) {

                View mainLayout = LayoutInflater.from(getActivity()).inflate(R.layout.district_committee_item, itemContainer, false);

                TextView nameTextview = mainLayout.findViewById(R.id.nameTextview);
                TextView designationTextview = mainLayout.findViewById(R.id.designationTextview);
                TextView numberTextview = mainLayout.findViewById(R.id.numberTextview);
                ImageView whatsappImage = mainLayout.findViewById(R.id.whatsappImage);
                ImageView phoneImage = mainLayout.findViewById(R.id.phoneImage);

                nameTextview.setText(name);
                designationTextview.setText(designation);
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
        parameter.put("morcha_id", morchaId);
        parameter.put("type", morchaType);
        parameter.put("designation", selectedDesignation != null ? selectedDesignation : "");

        requireActivity().runOnUiThread(() -> {
            if (!TextUtils.isEmpty(selectedDesignation)) {
                filteredItemLayout.setVisibility(View.VISIBLE);
                filteredDesignationLayout.setVisibility(View.VISIBLE);
                filteredDesignation.setText(selectedDesignationName);
            } else {
                filteredItemLayout.setVisibility(View.GONE);
                filteredDesignationLayout.setVisibility(View.GONE);
            }
        });

        AsyncTaskMorchaDetails asyncTaskMorchaDetails = new AsyncTaskMorchaDetails(parameter);
        asyncTaskMorchaDetails.execute();
    }

    private void handleBackPress() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Fragment currentFragment = new MorchasFragment();
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