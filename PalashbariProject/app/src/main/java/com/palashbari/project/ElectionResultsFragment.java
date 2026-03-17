package com.palashbari.project;

import android.app.Dialog;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
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

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

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


public class ElectionResultsFragment extends Fragment implements ConnectivityReceiver.ConnectivityListener{

    View view;
    String profile_image_uri;
    UserManagement userManagement;
    private ConnectivityReceiver connectivityReceiver;
    boolean internetResult = false;
    LinearLayout lin,noRec,searchLayout,itemContainer;
    ProgressBar spin_kit;
    JSONArray mainJsonArray;
    private LinearLayout selectedCard = null;
    private TextView selectedText = null;
    private ImageView selectedImageView = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view =  inflater.inflate(R.layout.fragment_election_results, container, false);

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
                Fragment currentFragment = new HomeFragment();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(0, 0);
                fragmentTransaction.replace(R.id.fragment, currentFragment);
                fragmentTransaction.commit();
            }
        });

        String json = "[{\"type\":\"Lok Sabha\",\"year\":2001},{\"type\":\"Lok Sabha\",\"year\":2006},{\"type\":\"Lok Sabha\",\"year\":2011},{\"type\":\"Assembly\",\"year\":2003},{\"type\":\"Assembly\",\"year\":2008}]";


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



        return view;
    }




    private void processValue(JSONArray jsonArray) throws JSONException {
        spin_kit.setVisibility(View.GONE);
        lin.setVisibility(View.VISIBLE);

        itemContainer.removeAllViews();


        Map<String, List<JSONObject>> groupedMap = new LinkedHashMap<>();
        for (int k = 0; k < jsonArray.length(); k++) {
            JSONObject json = jsonArray.getJSONObject(k);
            String type = json.getString("type");

            if (!groupedMap.containsKey(type)) {
                groupedMap.put(type, new ArrayList<>());
            }
            groupedMap.get(type).add(json);
        }


        for (Map.Entry<String, List<JSONObject>> entry : groupedMap.entrySet()) {
            String type = entry.getKey();
            List<JSONObject> items = entry.getValue();


            TextView typeHeader = new TextView(getActivity());
            typeHeader.setText(type);
            typeHeader.setTextSize(14);
            Typeface robotoMedium = ResourcesCompat.getFont(getContext(), R.font.roboto_medium);
            typeHeader.setTypeface(robotoMedium);
            typeHeader.setTextColor(ContextCompat.getColor(getActivity(), R.color.black1));
            typeHeader.setPadding(16, 16, 16, 36);
            itemContainer.addView(typeHeader);


            CardView cardView = new CardView(getActivity());
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            cardParams.setMargins(16, 0, 16, 16);
            cardView.setLayoutParams(cardParams);
            cardView.setRadius(20f);
            cardView.setCardElevation(4f);

            // LinearLayout inside the card for years
            LinearLayout yearContainer = new LinearLayout(getActivity());
            yearContainer.setOrientation(LinearLayout.VERTICAL);

            // Add each year as a separate item inside the card
            for (JSONObject json : items) {
                String year = json.getString("year");

                View yearItem = LayoutInflater.from(getActivity())
                        .inflate(R.layout.election_results_item, yearContainer, false);

                TextView yearTextview = yearItem.findViewById(R.id.yearTextview);
                LinearLayout mainCardLayout = yearItem.findViewById(R.id.mainCardLayout);
                ImageView imageView = mainCardLayout.findViewById(R.id.imageView);

                yearTextview.setText(year);
                mainCardLayout.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.grey3));
                yearTextview.setTextColor(ContextCompat.getColor(getActivity(), R.color.black));
                imageView.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.vector11));

                mainCardLayout.setOnClickListener(v -> {
                    if (selectedCard != null && selectedImageView != null) {
                        selectedCard.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.grey3));
                        selectedImageView.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.vector11));
                        selectedText.setTextColor(ContextCompat.getColor(getActivity(), R.color.black));
                    }

                    mainCardLayout.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.green));
                    imageView.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.icons8_forward_24));
                    yearTextview.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));

                    selectedCard = mainCardLayout;
                    selectedImageView = imageView;
                    selectedText = yearTextview;

                    Fragment newFragment = new ElectionResultsDetailsFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("type", type);
                    bundle.putString("year", year);
                    newFragment.setArguments(bundle);

                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.setCustomAnimations(0, 0);
                    fragmentTransaction.replace(R.id.fragment, newFragment);
                    fragmentTransaction.commit();
                });

                yearContainer.addView(yearItem);
            }

            cardView.addView(yearContainer);
            itemContainer.addView(cardView);
        }
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