package com.palashbari.project;

import android.app.Dialog;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.palashbari.project.util.API;
import com.palashbari.project.util.JsonAPICall;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class NamgharFragment extends Fragment implements ConnectivityReceiver.ConnectivityListener{

    View view;
    String selectedPanchayat,currentSearchText;
    private ConnectivityReceiver connectivityReceiver;
    boolean internetResult = false;
    LinearLayout lin,noRec,itemContainer,paginationLayout;
    ProgressBar spin_kit;
    private boolean isAPICallRunning = false;
    private int itemsPerPage = 5;
    int totalPages;
    private int selectedPage = 0;
    private TextView selectedPageButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_namghar, container, false);

        lin = view.findViewById(R.id.lin);
        spin_kit = view.findViewById(R.id.spin_kit);
        noRec = view.findViewById(R.id.noRec);
        itemContainer = view.findViewById(R.id.itemContainer);
        paginationLayout = view.findViewById(R.id.paginationLayout);

        connectivityReceiver = new ConnectivityReceiver(this);

        Bundle args = getArguments();

        if (args != null) {
            selectedPanchayat = args.getString("selectedPanchayat");
            currentSearchText = args.getString("currentSearchText");
        }


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
                parameter.put("search[value]", currentSearchText != null ? currentSearchText : "");
                parameter.put("type", "namghar");
                parameter.put("gaon_panchayat", selectedPanchayat != null ? selectedPanchayat : "");

                AsyncTaskNamghar asyncTaskNamghar = new AsyncTaskNamghar(parameter);
                asyncTaskNamghar.execute();
                isAPICallRunning = true;
            }
        }else{
            internetResult = false;
        }
    }

    public class AsyncTaskNamghar extends AsyncTask<Void, Void, Void> {
        Map<String, String> parameter;

        public AsyncTaskNamghar(Map<String, String> parameter) {
            this.parameter = parameter;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            JsonAPICall.makePostRequestWithTokenAndFormData(getActivity(),  API.namghar_mandir_data, parameter, new JsonAPICall.VolleyCallback() {
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
            String namgharName = json.getString("name");
            String address = "";
            if (!json.isNull("address")) {
                address = json.getString("address");
            }

            if (getActivity() != null) {
                View mainLayout = LayoutInflater.from(getActivity()).inflate(R.layout.namghar_mandir_item, itemContainer, false);

                TextView nameTextview = mainLayout.findViewById(R.id.nameTextview);
                TextView addressTextview = mainLayout.findViewById(R.id.addressTextview);

                nameTextview.setText(namgharName);
                if (address != null && !address.trim().isEmpty()) {
                    addressTextview.setText(address);
                    addressTextview.setVisibility(View.VISIBLE);
                }else{
                    addressTextview.setVisibility(View.GONE);
                }

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
                    fetchData(start, itemsPerPage);
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

    private void fetchData(int start, int length) {
        spin_kit.setVisibility(View.VISIBLE);
        lin.setVisibility(View.GONE);

        Map<String, String> parameter = new HashMap<>();
        parameter.put("draw", "1");
        parameter.put("start", String.valueOf(start));
        parameter.put("length", String.valueOf(length));
        parameter.put("search[value]", currentSearchText != null ? currentSearchText : "");
        parameter.put("type", "namghar");
        parameter.put("gaon_panchayat", selectedPanchayat != null ? selectedPanchayat : "");

        AsyncTaskNamghar asyncTaskNamghar = new AsyncTaskNamghar(parameter);
        asyncTaskNamghar.execute();
    }
}