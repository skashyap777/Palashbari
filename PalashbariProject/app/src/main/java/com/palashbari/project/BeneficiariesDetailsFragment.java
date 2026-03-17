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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

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


public class BeneficiariesDetailsFragment extends Fragment implements ConnectivityReceiver.ConnectivityListener{

    View view;
    String profile_image_uri,schemeName,schemeId,blockName,blockId;
    UserManagement userManagement;
    private ConnectivityReceiver connectivityReceiver;
    boolean internetResult = false;
    LinearLayout lin,noRec,paginationLayout,itemContainer;
    ProgressBar spin_kit;
    TextView beneficiariesTextView,beneficiariesNameTextView,blockNameTextView;
    EditText searchEditText;
    private boolean isAPICallRunning = false;
    private int itemsPerPage = 5;
    int totalPages;
    private int selectedPage = 0;
    private TextView selectedPageButton;
    private String currentSearchText = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_beneficiaries_details, container, false);

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
        beneficiariesTextView = view.findViewById(R.id.beneficiariesTextView);
        beneficiariesNameTextView = view.findViewById(R.id.beneficiariesNameTextView);
        searchEditText = view.findViewById(R.id.searchEditText);
        blockNameTextView = view.findViewById(R.id.blockNameTextView);

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
        lblText.setText("Beneficiaries");

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment newFragment = new BeneficiariesBlockDetailsFragment();
                Bundle bundle = new Bundle();
                bundle.putString("schemeName", schemeName);
                bundle.putString("schemeId", schemeId);
                newFragment.setArguments(bundle);

                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(0, 0);
                fragmentTransaction.replace(R.id.fragment, newFragment);
                fragmentTransaction.commit();
            }
        });

        Bundle args = getArguments();

        if (args != null) {
            schemeName = args.getString("schemeName");
            schemeId = args.getString("schemeId");
            blockName = args.getString("blockName");
            blockId = args.getString("blockId");
            beneficiariesNameTextView.setText(schemeName);
            blockNameTextView.setText(" / "+blockName);
        }

        beneficiariesTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment currentFragment = new BeneficiariesFragment();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(0, 0);
                fragmentTransaction.replace(R.id.fragment, currentFragment);
                fragmentTransaction.commit();
            }
        });

        beneficiariesNameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment currentFragment = new BeneficiariesFragment();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(0, 0);
                fragmentTransaction.replace(R.id.fragment, currentFragment);
                fragmentTransaction.commit();
            }
        });

        blockNameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment newFragment = new BeneficiariesBlockDetailsFragment();
                Bundle bundle = new Bundle();
                bundle.putString("schemeName", schemeName);
                bundle.putString("schemeId", schemeId);
                newFragment.setArguments(bundle);

                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(0, 0);
                fragmentTransaction.replace(R.id.fragment, newFragment);
                fragmentTransaction.commit();
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

        return view;
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
            if (!isAPICallRunning) {
                spin_kit.setVisibility(View.VISIBLE);

                Map<String, String> parameter = new HashMap<>();
                parameter.put("draw", "1");
                parameter.put("start", "0");
                parameter.put("length",  "5");
                parameter.put("search[value]", "");
                parameter.put("scheme", schemeId);
                parameter.put("block", blockId);

                AsyncTaskBeneficiariesDetails asyncTaskBeneficiariesDetails = new AsyncTaskBeneficiariesDetails(parameter);
                asyncTaskBeneficiariesDetails.execute();
                isAPICallRunning = true;
            }
        }else{
            internetResult = false;
        }
    }

    public class AsyncTaskBeneficiariesDetails extends AsyncTask<Void, Void, Void> {
        Map<String, String> parameter;

        public AsyncTaskBeneficiariesDetails(Map<String, String> parameter) {
            this.parameter = parameter;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            JsonAPICall.makePostRequestWithTokenAndFormData(getActivity(),  API.beneficiaries_data, parameter, new JsonAPICall.VolleyCallback() {
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
            String phoneNumber = json.getString("phone_number");


            if (getActivity() != null) {

                View mainLayout = LayoutInflater.from(getActivity()).inflate(R.layout.district_committee_item, itemContainer, false);

                TextView nameTextview = mainLayout.findViewById(R.id.nameTextview);
                TextView designationTextview = mainLayout.findViewById(R.id.designationTextview);
                TextView numberTextview = mainLayout.findViewById(R.id.numberTextview);
                ImageView whatsappImage = mainLayout.findViewById(R.id.whatsappImage);
                ImageView phoneImage = mainLayout.findViewById(R.id.phoneImage);

                nameTextview.setText(name);
                designationTextview.setVisibility(View.GONE);
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
        parameter.put("scheme", schemeId);
        parameter.put("block", blockId);

        AsyncTaskBeneficiariesDetails asyncTaskBeneficiariesDetails = new AsyncTaskBeneficiariesDetails(parameter);
        asyncTaskBeneficiariesDetails.execute();
    }

    private void handleBackPress() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Fragment newFragment = new BeneficiariesBlockDetailsFragment();
                Bundle bundle = new Bundle();
                bundle.putString("schemeName", schemeName);
                bundle.putString("schemeId", schemeId);
                newFragment.setArguments(bundle);

                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(0, 0);
                fragmentTransaction.replace(R.id.fragment, newFragment);
                fragmentTransaction.commit();
            }
        };

        requireActivity().getOnBackPressedDispatcher().addCallback(getActivity(), callback);
    }
}