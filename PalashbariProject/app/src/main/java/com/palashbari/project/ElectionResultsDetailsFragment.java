package com.palashbari.project;

import android.app.Dialog;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

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

import com.bumptech.glide.Glide;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class ElectionResultsDetailsFragment extends Fragment implements ConnectivityReceiver.ConnectivityListener{

    View view;
    String profile_image_uri,typeName,yearName,selectedBooth;
    UserManagement userManagement;
    private ConnectivityReceiver connectivityReceiver;
    boolean internetResult = false;
    LinearLayout lin,noRec,searchLayout,itemContainer;
    ProgressBar spin_kit;
    JSONArray mainJsonArray;
    TextView electionResultsTextView,electionResultsTypeTextView,electionResultsYearTextView;
    ImageView filter;
    Dialog filterDialog;
    CardView showResult;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_election_results_details, container, false);

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
        searchLayout = view.findViewById(R.id.searchLayout);
        itemContainer = view.findViewById(R.id.itemContainer);
        electionResultsTextView = view.findViewById(R.id.electionResultsTextView);
        electionResultsTypeTextView = view.findViewById(R.id.electionResultsTypeTextView);
        electionResultsYearTextView = view.findViewById(R.id.electionResultsYearTextView);
        filter = view.findViewById(R.id.filter);

        filterDialog = new Dialog(requireContext());
        filterDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        filterDialog.setContentView(R.layout.layout_bottom_sheet_election_result_details);
        filterDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        filterDialog.setCancelable(true);
        filterDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        filterDialog.getWindow().setGravity(Gravity.BOTTOM);
        filterDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        TextView noRecBooth = filterDialog.findViewById(R.id.noRecBooth);
        LinearLayout boothLayout = filterDialog.findViewById(R.id.boothLayout);
        TextView boothText = filterDialog.findViewById(R.id.boothText);
        View dismiss = filterDialog.findViewById(R.id.dismiss);
        FlexboxLayout itemContainerBooth = filterDialog.findViewById(R.id.itemContainerBooth);
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
        lblText.setText("Election Results");

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment currentFragment = new ElectionResultsFragment();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(0, 0);
                fragmentTransaction.replace(R.id.fragment, currentFragment);
                fragmentTransaction.commit();
            }
        });

        Bundle args = getArguments();

        if (args != null) {
            typeName = args.getString("type");
            yearName = args.getString("year");
            electionResultsTypeTextView.setText(typeName+" / ");
            electionResultsYearTextView.setText(yearName);
        }

        electionResultsTypeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment currentFragment = new ElectionResultsFragment();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(0, 0);
                fragmentTransaction.replace(R.id.fragment, currentFragment);
                fragmentTransaction.commit();
            }
        });

        electionResultsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment currentFragment = new ElectionResultsFragment();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(0, 0);
                fragmentTransaction.replace(R.id.fragment, currentFragment);
                fragmentTransaction.commit();
            }
        });

        String json = "[\n" +
                "  {\n" +
                "    \"booth\": \"Amtola Kaibartapara Bortola L.P. School (L/W)\",\n" +
                "    \"pdf_url\": \"https://www.adobe.com/support/products/enterprise/knowledgecenter/media/c4611_sample_explain.pdf\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"booth\": \"Amtola Kaibartapara Bortola L.P. School (R/W)\",\n" +
                "    \"pdf_url\": \"https://morth.nic.in/sites/default/files/dd12-13_0.pdf\"\n" +
                "  }\n" +
                "]";

        String boothJson = "[\n" +
                "  { \"id\": 1, \"booth\": \"Amtola Kaibartapara Bortola L.P. School (L/W)\" },\n" +
                "  { \"id\": 2, \"booth\": \"Amtola Kaibartapara Bortola L.P. School (R/W)\" },\n" +
                "  { \"id\": 3, \"booth\": \"Amtola Satra L.P. School L/W\" },\n" +
                "  { \"id\": 4, \"booth\": \"Bihdia L.P. School R/W\" },\n" +
                "  { \"id\": 4, \"booth\": \"Bihdia L.P. School L/W\" },\n" +
                "  { \"id\": 4, \"booth\": \"Haropara Girls L.P. School (L/W)\" },\n" +
                "  { \"id\": 4, \"booth\": \"Jarabari L.P. School\" },\n" +
                "  { \"id\": 4, \"booth\": \"Kukurmara L.P. School L/W\" },\n" +
                "  { \"id\": 4, \"booth\": \"Magurpara L.P. School (R/W)\" },\n" +
                "  { \"id\": 4, \"booth\": \"No. 1 Jarobori L P School\" },\n" +
                "  { \"id\": 4, \"booth\": \"No. 29 Rangamati L P School L-W\" },\n" +
                "  { \"id\": 13, \"booth\": \"Rangamati High School (L/W)\" }\n" +
                "]";

        try {
            mainJsonArray = new JSONArray(json);
            if (mainJsonArray.length()>0) {
                processValue(mainJsonArray);
            }else{
                spin_kit.setVisibility(View.GONE);
                noRec.setVisibility(View.VISIBLE);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                boothLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        itemContainerBooth.removeAllViews();
                        boothLayout.setBackground(getResources().getDrawable(R.drawable.active_button));
                        boothText.setTextColor(getResources().getColor(R.color.green));
                        try {
                            JSONArray boothArray = new JSONArray(boothJson);
                            if (boothArray.length()>0){
                                showDialog(boothArray,itemContainerBooth,clear,showResult,filterDialog,selectedBooth);
                            }else{
                                noRecBooth.setVisibility(View.VISIBLE);
                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });

                boothLayout.post(() -> {
                    boothLayout.performClick();
                });

                dismiss.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        filterDialog.dismiss();
                    }
                });

                filterDialog.show();
            }
        });

        return view;
    }

    private void showDialog(JSONArray jsonArray,FlexboxLayout itemLayout, CardView clear, CardView showResult, Dialog potholeDialog, String selectedItem) throws JSONException {
        List<RadioButton> radioButtons = new ArrayList<>();
        List<JSONObject> jsonObjects = new ArrayList<>();
        for (int k = 0; k < jsonArray.length(); k++) jsonObjects.add(jsonArray.getJSONObject(k));
        Collections.sort(jsonObjects, (o1, o2) -> {
            try { return o1.getString("booth").compareToIgnoreCase(o2.getString("booth")); }
            catch (JSONException e) { return 0; }
        });



        for (JSONObject json : jsonObjects) {
            String booth = json.getString("booth");


            View mainLayout = LayoutInflater.from(getActivity()).inflate(R.layout.text_item, itemLayout, false);
            RadioButton radio = mainLayout.findViewById(R.id.radio);
            TextView nameTextview = mainLayout.findViewById(R.id.nameTextview);
            nameTextview.setText(booth);
            radio.setTag(booth);

            if (selectedItem != null && !selectedItem.isEmpty() && selectedItem.equalsIgnoreCase(booth)) {
                radio.setChecked(true);
            }

            radioButtons.add(radio);

            mainLayout.setOnClickListener(v -> {
                for (RadioButton rb : radioButtons) {
                    rb.setChecked(rb == radio);
                }
            });

            radio.setOnClickListener(v -> {
                for (RadioButton rb : radioButtons) {
                    rb.setChecked(rb == radio);
                }
            });

            FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(16, 8, 16, 8);
            mainLayout.setLayoutParams(params);

            itemLayout.addView(mainLayout);
        }

        clear.setOnClickListener(v -> {
            for (RadioButton rb : radioButtons) {
                rb.setChecked(false);
            }
            selectedBooth = null;
        });

        showResult.setOnClickListener(v -> {
            for (RadioButton rb : radioButtons) {
                if (rb.isChecked()) {
                    selectedBooth = (String) rb.getTag();
                    spin_kit.setVisibility(View.VISIBLE);
                    lin.setVisibility(View.GONE);
                    potholeDialog.dismiss();
                    try {
                        processValue(mainJsonArray);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                }else{
                    spin_kit.setVisibility(View.VISIBLE);
                    lin.setVisibility(View.GONE);
                    potholeDialog.dismiss();
                    try {
                        processValue(mainJsonArray);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }

    private void processValue(JSONArray jsonArray) throws JSONException {
        spin_kit.setVisibility(View.GONE);
        lin.setVisibility(View.VISIBLE);
        itemContainer.removeAllViews();

        boolean hasItems = false;

        // Group by booth
        Map<String, List<JSONObject>> groupedMap = new LinkedHashMap<>();
        for (int k = 0; k < jsonArray.length(); k++) {
            JSONObject json = jsonArray.getJSONObject(k);
            String booth = json.getString("booth");

            if (!groupedMap.containsKey(booth)) {
                groupedMap.put(booth, new ArrayList<>());
            }
            groupedMap.get(booth).add(json);
        }

        for (Map.Entry<String, List<JSONObject>> entry : groupedMap.entrySet()) {
            String type = entry.getKey();

            // Check if this booth should be shown
            if (selectedBooth != null && !selectedBooth.isEmpty() &&
                    !type.equalsIgnoreCase(selectedBooth)) {
                continue; // skip booths that don’t match
            }

            List<JSONObject> items = entry.getValue();

            TextView typeHeader = new TextView(getActivity());
            typeHeader.setText(type);
            typeHeader.setTextSize(14);
            Typeface robotoMedium = ResourcesCompat.getFont(getContext(), R.font.roboto_medium);
            typeHeader.setTypeface(robotoMedium);
            typeHeader.setTextColor(ContextCompat.getColor(getActivity(), R.color.black1));
            typeHeader.setPadding(16, 16, 16, 36);

            CardView cardView = new CardView(getActivity());
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            cardParams.setMargins(16, 0, 16, 16);
            cardView.setLayoutParams(cardParams);
            cardView.setRadius(20f);
            cardView.setCardElevation(4f);

            LinearLayout yearContainer = new LinearLayout(getActivity());
            yearContainer.setOrientation(LinearLayout.VERTICAL);

            boolean hasVisibleItemsInGroup = false;

            for (JSONObject json : items) {
                String pdfUrl = json.getString("pdf_url");

                // Inflate item layout
                View yearItem = LayoutInflater.from(getActivity())
                        .inflate(R.layout.election_results_details_item, yearContainer, false);

                WebView pdfWebView = yearItem.findViewById(R.id.pdfWebView);
                pdfWebView.getSettings().setJavaScriptEnabled(true);
                pdfWebView.loadUrl("https://docs.google.com/gview?embedded=true&url=" + pdfUrl);

                yearContainer.addView(yearItem);
                hasVisibleItemsInGroup = true;
                hasItems = true;
            }

            if (hasVisibleItemsInGroup) {
                itemContainer.addView(typeHeader);
                cardView.addView(yearContainer);
                itemContainer.addView(cardView);
            }
        }

        noRec.setVisibility(hasItems ? View.GONE : View.VISIBLE);
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
        }else{
            internetResult = false;
        }
    }


    private void handleBackPress() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Fragment currentFragment = new ElectionResultsFragment();
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