package com.palashbari.project;

import android.app.Dialog;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;


public class MandalCommitteeTypeDetailsFragment extends Fragment implements ConnectivityReceiver.ConnectivityListener{

    View view;
    String profile_image_uri,mandalName,mandalId,jsonString;
    UserManagement userManagement;
    private ConnectivityReceiver connectivityReceiver;
    boolean internetResult = false;
    LinearLayout lin,noRec,itemContainer;
    ProgressBar spin_kit;
    private LinearLayout selectedCard = null;
    private TextView selectedMandalName = null;
    private boolean isAPICallRunning = false;
    TextView mandalCommitteeTextView,mandalCommitteeNameTextView;
    EditText searchEditText;
    private ImageView selectedImageView = null;
    private String currentSearchText = "";
    JSONArray sizeArray;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_mandal_committee_type_details, container, false);

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
        searchEditText = view.findViewById(R.id.searchEditText);
        mandalCommitteeTextView = view.findViewById(R.id.mandalCommitteeTextView);
        mandalCommitteeNameTextView = view.findViewById(R.id.mandalCommitteeNameTextView);

        jsonString = "{\n" +
                "  \"data\": [\n" +
                "    {\n" +
                "      \"id\": 1,\n" +
                "      \"name\": \"3 Members Committee\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": 2,\n" +
                "      \"name\": \"Mandal Main Committee\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": 3,\n" +
                "      \"name\": \"Ahbayak Committee\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

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
        lblText.setText("Mandal Committee");

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment currentFragment = new MandalCommitteeFragment();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(0, 0);
                fragmentTransaction.replace(R.id.fragment, currentFragment);
                fragmentTransaction.commit();
            }
        });

        Bundle args = getArguments();

        if (args != null) {
            mandalName = args.getString("mandalName");
            mandalId = args.getString("mandalId");
            mandalCommitteeNameTextView.setText(mandalName);
        }

        mandalCommitteeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment currentFragment = new MandalCommitteeFragment();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(0, 0);
                fragmentTransaction.replace(R.id.fragment, currentFragment);
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
                    fetchData(currentSearchText);
                    searchEditText.post(() -> {
                        searchEditText.setSelection(searchEditText.getText().length());
                        searchEditText.requestFocus();
                    });
                }
            }
        });

        return view;
    }

    private void fetchData(String searchText) {
        spin_kit.setVisibility(View.VISIBLE);
        lin.setVisibility(View.GONE);

        if (sizeArray == null || sizeArray.length() == 0) {
            noRec.setVisibility(View.VISIBLE);
            return;
        }

        JSONArray filteredArray = new JSONArray();

        try {
            for (int i = 0; i < sizeArray.length(); i++) {
                JSONObject obj = sizeArray.getJSONObject(i);
                String name = obj.getString("name").toLowerCase();

                if (searchText.isEmpty() || name.contains(searchText)) {
                    filteredArray.put(obj);
                }
            }

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                spin_kit.setVisibility(View.GONE);
                lin.setVisibility(View.VISIBLE);
                if (filteredArray.length() > 0) {
                    noRec.setVisibility(View.GONE);
                    itemContainer.removeAllViews();
                    try {
                        processValue(filteredArray);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    itemContainer.removeAllViews();
                    noRec.setVisibility(View.VISIBLE);
                }
            }, 500);

        } catch (JSONException e) {
            e.printStackTrace();
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

    private void handleBackPress() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Fragment currentFragment = new MandalCommitteeFragment();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(0, 0);
                fragmentTransaction.replace(R.id.fragment, currentFragment);
                fragmentTransaction.commit();
            }
        };

        requireActivity().getOnBackPressedDispatcher().addCallback(getActivity(), callback);
    }

    @Override
    public void onConnectivityChanged(boolean isConnected) {
        if (isConnected) {
            internetResult = true;
            if (!isAPICallRunning) {
                spin_kit.setVisibility(View.VISIBLE);
                try {
                    JSONObject jsonObject = new JSONObject(jsonString);
                    sizeArray = jsonObject.getJSONArray("data");

                    new Handler(Looper.getMainLooper()).postDelayed(() -> {

                        spin_kit.setVisibility(View.GONE);
                        lin.setVisibility(View.VISIBLE);

                        if (sizeArray.length() > 0) {
                            noRec.setVisibility(View.GONE);
                            itemContainer.removeAllViews();
                            try {
                                processValue(sizeArray);
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            itemContainer.removeAllViews();
                            noRec.setVisibility(View.VISIBLE);
                        }

                    }, 1000); // ✅ 1 second delay

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                isAPICallRunning = true;
            }
        }else{
            internetResult = false;
        }
    }

    private void processValue(JSONArray jsonArray) throws JSONException {
        for (int k = 0; k < jsonArray.length(); k++) {
            JSONObject json = jsonArray.getJSONObject(k);
            String typeName = json.getString("name");
            int typeId = json.getInt("id");

            if (getActivity() != null) {
                View mainLayout = LayoutInflater.from(getActivity()).inflate(R.layout.mandal_item, itemContainer, false);

                TextView mandalNameTextview = mainLayout.findViewById(R.id.mandalNameTextview);
                LinearLayout mainCardLayout = mainLayout.findViewById(R.id.mainCardLayout);
                ImageView imageView = mainLayout.findViewById(R.id.imageView);

                mandalNameTextview.setText(typeName);

                mainCardLayout.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.grey3));
                mandalNameTextview.setTextColor(ContextCompat.getColor(getActivity(), R.color.black));
                imageView.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.vector11));

                mainCardLayout.setOnClickListener(v -> {
                    if (selectedCard != null && selectedMandalName != null && selectedImageView != null) {
                        selectedCard.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.grey3));
                        selectedMandalName.setTextColor(ContextCompat.getColor(getActivity(), R.color.black));
                        selectedImageView.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.vector11));
                    }

                    mandalNameTextview.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));
                    mainCardLayout.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.green));
                    imageView.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.icons8_forward_24));

                    selectedCard = mainCardLayout;
                    selectedMandalName = mandalNameTextview;
                    selectedImageView = imageView;

                    Fragment newFragment = new MandalCommitteeDetailsFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("mandalName", mandalName);
                    bundle.putString("mandalId", mandalId);
                    bundle.putString("typeName", typeName);
                    bundle.putString("typeId", String.valueOf(typeId));
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
}