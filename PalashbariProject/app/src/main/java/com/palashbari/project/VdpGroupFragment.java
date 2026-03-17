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


public class VdpGroupFragment extends Fragment implements ConnectivityReceiver.ConnectivityListener{

    View view;
    String profile_image_uri,selectedVill;
    UserManagement userManagement;
    private ConnectivityReceiver connectivityReceiver;
    boolean internetResult = false;
    LinearLayout lin,noRec,paginationLayout,itemContainer,filteredVillLayout;
    ProgressBar spin_kit;
    ImageView filter;
    JSONArray villArray;
    Dialog filterDialog;
    CardView showResult;
    private boolean isAPICallRunning = false;
    private int itemsPerPage = 5;
    int totalPages;
    private int selectedPage = 0;
    private TextView selectedPageButton;
    private String currentSearchText = "";
    private String tempSelectedVill = null;
    EditText searchEditText;
    HorizontalScrollView filteredItemLayout;
    TextView filteredVill;
    String tempSelectedVillName = null;
    String selectedVillName = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_vdp_group, container, false);

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
        filteredVillLayout = view.findViewById(R.id.filteredVillLayout);
        filteredVill = view.findViewById(R.id.filteredVill);

        filterDialog = new Dialog(requireContext());
        filterDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        filterDialog.setContentView(R.layout.layout_bottom_sheet_vdp_group);
        filterDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        filterDialog.setCancelable(true);
        filterDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        filterDialog.getWindow().setGravity(Gravity.BOTTOM);
        filterDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);


        TextView noRecVill = filterDialog.findViewById(R.id.noRecVill);
        LinearLayout villLayout = filterDialog.findViewById(R.id.villLayout);
        TextView villText = filterDialog.findViewById(R.id.villText);
        View dismiss = filterDialog.findViewById(R.id.dismiss);
        FlexboxLayout itemContainerVill = filterDialog.findViewById(R.id.itemContainerVill);
        CardView clear = filterDialog.findViewById(R.id.clear);
        showResult = filterDialog.findViewById(R.id.showResult);
        CardView searchVill = filterDialog.findViewById(R.id.searchVill);
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
        lblText.setText("VDP Members");

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

                tempSelectedVill = selectedVill;
                tempSelectedVillName = selectedVillName;

                villLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        itemContainerVill.removeAllViews();
                        searchEditTextVill.setText("");
                        searchEditTextVill.clearFocus();

                        villLayout.setBackground(getResources().getDrawable(R.drawable.active_button));
                        villText.setTextColor(getResources().getColor(R.color.green));
                        try {
                            if (villArray != null && villArray.length()>0){
                                searchVill.setVisibility(View.VISIBLE);
                                showDialog(villArray,itemContainerVill,clear,showResult,filterDialog);
                            }else{
                                noRecVill.setVisibility(View.VISIBLE);
                                searchVill.setVisibility(View.VISIBLE);
                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });


                villLayout.post(() -> {
                    villLayout.performClick();
                });

                dismiss.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        filterDialog.dismiss();
                    }
                });

                searchEditTextVill.setText("");
                searchEditTextVill.clearFocus();

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

                        if (filteredArray.length() > 0) {
                            showDialog(filteredArray,  itemContainerVill, clear, showResult, filterDialog);
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


        Collections.sort(jsonObjects, (o1, o2) -> {
            try {
                return o1.getString("village_name").compareToIgnoreCase(o2.getString("village_name"));
            } catch (JSONException e) {
                return 0;
            }
        });

        // Move previously selected item to top if any
        JSONObject selectedObject = null;
        List<JSONObject> otherObjects = new ArrayList<>();
        for (JSONObject obj : jsonObjects) {
            String idStr = String.valueOf(obj.getInt("id"));
            if ((tempSelectedVill != null && tempSelectedVill.equals(idStr))) {
                selectedObject = obj;
            } else {
                otherObjects.add(obj);
            }
        }
        List<JSONObject> finalList = new ArrayList<>();
        if (selectedObject != null) finalList.add(selectedObject);
        finalList.addAll(otherObjects);

        boolean[] showAll = {false};
        int initialItemCount = 10;

        for (int index = 0; index < finalList.size(); index++) {
            JSONObject json = finalList.get(index);
            String name = json.getString("village_name");
            int id = json.getInt("id");

            // Inflate item layout
            View mainLayout = LayoutInflater.from(getActivity()).inflate(R.layout.text_item, itemLayout, false);
            RadioButton radio = mainLayout.findViewById(R.id.radio);
            TextView nameTextview = mainLayout.findViewById(R.id.nameTextview);
            nameTextview.setText(name);
            radio.setTag(id);

            // Preselect if previously selected
            if ((tempSelectedVill != null && tempSelectedVill.equals(String.valueOf(id)))) {
                radio.setChecked(true);
            }

            radioButtons.add(radio);

            mainLayout.setOnClickListener(v -> {
                for (RadioButton rb : radioButtons) rb.setChecked(rb == radio);
                tempSelectedVill = String.valueOf(id);
                tempSelectedVillName = name;
            });

            radio.setOnClickListener(v -> {
                for (RadioButton rb : radioButtons) rb.setChecked(rb == radio);
                tempSelectedVill = String.valueOf(id);
                tempSelectedVillName = name;
            });

            FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
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
            tempSelectedVill = null;
            tempSelectedVillName = null;
        });

        showResult.setOnClickListener(v -> {
            selectedVill = tempSelectedVill;
            selectedVillName = tempSelectedVillName;
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

                AsyncTaskVill asyncTaskVill = new AsyncTaskVill();
                asyncTaskVill.execute();

                Map<String, String> parameter = new HashMap<>();
                parameter.put("draw", "1");
                parameter.put("start", "0");
                parameter.put("length",  "5");
                parameter.put("search[value]", "");
                parameter.put("village", "");

                AsyncTaskVdpMembers asyncTaskVdpMembers = new AsyncTaskVdpMembers(parameter);
                asyncTaskVdpMembers.execute();
                isAPICallRunning = true;
            }
        }else{
            internetResult = false;
            showResult.setEnabled(false);
        }
    }

    public class AsyncTaskVill extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            if (getActivity() != null) {
                JsonAPICall.makeGetRequestWithToken(getActivity(), API.revenue_vill_data, new JsonAPICall.VolleyCallback() {
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
                        villArray = jsonObject.getJSONArray("data");
                    }
                }
            }
        }
    }

    public class AsyncTaskVdpMembers extends AsyncTask<Void, Void, Void> {
        Map<String, String> parameter;

        public AsyncTaskVdpMembers(Map<String, String> parameter) {
            this.parameter = parameter;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            JsonAPICall.makePostRequestWithTokenAndFormData(getActivity(),  API.vdp_members_data, parameter, new JsonAPICall.VolleyCallback() {
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
            String designation = json.getString("designation_name");
            String name = json.getString("name");
            String address = json.getString("address");
            String phoneNumber = json.getString("phone_number");


            if (getActivity() != null) {
                View mainLayout = LayoutInflater.from(getActivity()).inflate(R.layout.vdp_group_item, itemContainer, false);

                TextView nameTextview = mainLayout.findViewById(R.id.nameTextview);
                TextView designationTextview = mainLayout.findViewById(R.id.designationTextview);
                TextView numberTextview = mainLayout.findViewById(R.id.numberTextview);
                TextView addressTextview = mainLayout.findViewById(R.id.addressTextview);
                ImageView whatsappImage = mainLayout.findViewById(R.id.whatsappImage);
                ImageView phoneImage = mainLayout.findViewById(R.id.phoneImage);

                nameTextview.setText(name);
                designationTextview.setText(designation);
                numberTextview.setText(phoneNumber);
                addressTextview.setText(address);

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
        parameter.put("village", selectedVill != null ? selectedVill : "");

        requireActivity().runOnUiThread(() -> {
            if (!TextUtils.isEmpty(selectedVill)) {
                filteredVillLayout.setVisibility(View.VISIBLE);
                filteredItemLayout.setVisibility(View.VISIBLE);
                filteredVill.setText(selectedVillName);
            } else {
                filteredVillLayout.setVisibility(View.GONE);
                filteredItemLayout.setVisibility(View.GONE);
            }
        });

        AsyncTaskVdpMembers asyncTaskVdpMembers = new AsyncTaskVdpMembers(parameter);
        asyncTaskVdpMembers.execute();
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