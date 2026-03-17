package com.palashbari.project;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.palashbari.project.util.API;
import com.palashbari.project.util.JsonAPICall;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EditProfileFragment extends Fragment implements ConnectivityReceiver.ConnectivityListener{

    View view;
    String profile_image_uri,name,email,phNo,token,refreshToken,userName;
    UserManagement userManagement;
    private ConnectivityReceiver connectivityReceiver;
    boolean internetResult = false;
    TextInputEditText nameEditText,numberEditText,emailEditText;
    ImageView imageView,imageShow;
    CardView cardViewCamera,saveCard;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int PICK_IMAGE = 2;
    private static final int REQUEST_CAMERA_PERMISSION = 1001;
    private Uri imageUri=null;
    boolean isNewImageSelected = false;
    ProgressBar progress,spin_kit;
    TextView saveLabel;
    private boolean isAPICallRunning = false;
    LinearLayout lin;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        userManagement = new UserManagement(getActivity());
        HashMap<String, String> user = userManagement.userDetails();
        token = user.get(userManagement.TOKEN);
        refreshToken =user.get(userManagement.REFRESH_TOKEN);
        userName = user.get(userManagement.USER_NAME);

        handleBackPress();

        View rootView = view.findViewById(R.id.fragment);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets systemInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars() | WindowInsetsCompat.Type.navigationBars());
            v.setPadding(0, 0, 0, systemInsets.bottom);
            return insets;
        });

        nameEditText = view.findViewById(R.id.nameEditText);
        imageView = view.findViewById(R.id.imageView);
        numberEditText = view.findViewById(R.id.numberEditText);
        cardViewCamera = view.findViewById(R.id.cardViewCamera);
        saveCard = view.findViewById(R.id.saveCard);
        progress = view.findViewById(R.id.progress);
        saveLabel = view.findViewById(R.id.saveLabel);
        emailEditText = view.findViewById(R.id.emailEditText);
        spin_kit = view.findViewById(R.id.spin_kit);
        lin = view.findViewById(R.id.lin);

        connectivityReceiver = new ConnectivityReceiver(this);
        LinearLayout linearLayout = getActivity().findViewById(R.id.linearLayout);

        if (linearLayout != null) {
            linearLayout.setVisibility(View.GONE);
        }

        LinearLayout backButton = view.findViewById(R.id.idMainToolBar);
        TextView lblText = view.findViewById(R.id.lbl);
        imageShow = view.findViewById(R.id.imageShow);

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

        lblText.setText("Edit Profile");

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment currentFragment = new SettingsFragment();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(0, 0);
                fragmentTransaction.replace(R.id.fragment, currentFragment);
                fragmentTransaction.commit();
            }
        });

        cardViewCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImageSourceDialog();
            }
        });

        saveCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = nameEditText.getText().toString().trim();
                phNo = numberEditText.getText().toString().trim();

                if (TextUtils.isEmpty(nameEditText.getText())) {
                    nameEditText.setError("Full name is required");
                    nameEditText.requestFocus();
                } else if (!nameEditText.getText().toString().trim().matches("[a-zA-Z ]+")) {
                    nameEditText.setError("Please enter a valid full name (letters only)");
                    nameEditText.requestFocus();
                }else if (TextUtils.isEmpty(numberEditText.getText())) {
                    numberEditText.setError("Phone number is required");
                    numberEditText.requestFocus();
                }else if (!phNo.matches("\\d{10}")) {
                    numberEditText.setError("Please enter a valid 10-digit number");
                    numberEditText.requestFocus();
                }else if (TextUtils.isEmpty(profile_image_uri) && !isNewImageSelected) {
                    Toast.makeText(getContext(), "Please add a profile picture", Toast.LENGTH_SHORT).show();
                } else {
                    Dialog updateDialog = new Dialog(requireContext());
                    updateDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    updateDialog.setContentView(R.layout.layout_bottom_sheet_update_confirm);
                    updateDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    updateDialog.setCancelable(true);
                    updateDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    updateDialog.getWindow().setGravity(Gravity.BOTTOM);
                    updateDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

                    CardView cancel = updateDialog.findViewById(R.id.cancelLabel);
                    CardView update = updateDialog.findViewById(R.id.updateLabel);
                    View dismiss = updateDialog.findViewById(R.id.dismiss);
                    TextView profileUpdate = updateDialog.findViewById(R.id.profileUpdate);
                    TextView profileUpdateDesc = updateDialog.findViewById(R.id.profileUpdateDesc);

                    profileUpdate.setVisibility(View.VISIBLE);
                    profileUpdateDesc.setVisibility(View.VISIBLE);

                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            updateDialog.dismiss();
                        }
                    });

                    update.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            updateDialog.dismiss();
                            saveCard.setEnabled(false);
                            progress.setVisibility(View.VISIBLE);
                            saveLabel.setVisibility(View.GONE);
                            Map<String, String> parameter = new HashMap<>();
                            parameter.put("name", name);
                            parameter.put("email", email);
                            parameter.put("phone_number", phNo);
                            sendProfileData(parameter, imageUri);
                        }
                    });

                    dismiss.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            updateDialog.dismiss();
                        }
                    });

                    updateDialog.show();
                }
            }
        });

        return view;
    }

    public void sendProfileData(Map<String, String> parameter, Uri imageUri) {
        TokenManager.regenerateToken(getActivity(), refreshToken, new TokenManager.TokenCallback() {
            @Override
            public void onTokenRefreshed(String newToken) {
                performProfileUploadWithToken(parameter, imageUri, newToken);
            }

            @Override
            public void onTokenUnavailable() {
                Toast.makeText(getActivity(), "Session expired. Please login again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performProfileUploadWithToken(Map<String, String> parameter, Uri imageUri, String token) {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request originalRequest = chain.request();
                    Request.Builder requestBuilder = originalRequest.newBuilder()
                            .header("Authorization", "Bearer " + token)
                            .method(originalRequest.method(), originalRequest.body());
                    Request request = requestBuilder.build();
                    return chain.proceed(request);
                })
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API.baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiServicePersonalDetails apiServicePersonalDetails = retrofit.create(ApiServicePersonalDetails.class);

        Map<String, RequestBody> requestBodyMap = createPartMap(parameter);


        if (isNewImageSelected && imageUri != null) {
            MultipartBody.Part imagePart = prepareImagePart("photo_url", imageUri);
            uploadNow(requestBodyMap, imagePart,apiServicePersonalDetails);
        } else if (!TextUtils.isEmpty(profile_image_uri)) {
            new Thread(() -> {
                try {
                    Bitmap bitmap = Glide.with(requireContext())
                            .asBitmap()
                            .load(profile_image_uri)
                            .submit()
                            .get(); // this blocks only the background thread, not UI

                    // Save bitmap to file
                    File file = createImageFile();
                    FileOutputStream outputStream = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    outputStream.flush();
                    outputStream.close();

                    // Prepare MultipartBody.Part
                    RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), file);
                    MultipartBody.Part imagePart = MultipartBody.Part.createFormData("profile_photo", file.getName(), requestFile);

                    // Call Retrofit on UI thread
                    requireActivity().runOnUiThread(() -> {
                        uploadNow(requestBodyMap, imagePart,apiServicePersonalDetails);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Failed to download profile image", Toast.LENGTH_SHORT).show();
                        progress.setVisibility(View.GONE);
                        saveLabel.setVisibility(View.VISIBLE);
                        saveCard.setEnabled(true);
                    });
                }
            }).start();
        }
    }

    private void uploadNow(Map<String, RequestBody> requestBodyMap, MultipartBody.Part imagePart, ApiServicePersonalDetails apiServicePersonalDetails) {
        Call<ResponseBody> call = apiServicePersonalDetails.createProfile(requestBodyMap, imagePart);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                saveLabel.setVisibility(View.VISIBLE);
                progress.setVisibility(View.GONE);
                saveCard.setEnabled(true);
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseBody);
                        int status = jsonObject.getInt("status");
                        if (status == 1){
                            JSONObject dataObject = jsonObject.getJSONObject("data");
                            int id = dataObject.getInt("id");
                            String name = dataObject.getString("name");
                            String mobile = dataObject.getString("phone");
                            String email = dataObject.getString("email");
                            String profilePic = dataObject.optString("photo_url", "null");
                            if (profilePic != null && !profilePic.equalsIgnoreCase("null") && !profilePic.isEmpty()) {
                                profilePic = "https://palasbari.h24x7.in/" + profilePic;
                            } else {
                                profilePic = "null";
                            }

                            userManagement.userSessionManage(userName, String.valueOf(id), mobile, name, profilePic, email);

                            if (getActivity() != null) {
                                String finalProfilePic = profilePic;
                                getActivity().runOnUiThread(() -> updateProfileUI(name, mobile, email, finalProfilePic));
                            }

                            Activity activity = getActivity();
                            if (activity instanceof MainActivity) {
                                MainActivity mainActivity = (MainActivity) activity;
                                if (profile_image_uri != null && !profile_image_uri.equalsIgnoreCase("null")) {
                                    Glide.with(requireContext()).load(profilePic).into(mainActivity.imageShow);
                                } else {
                                    mainActivity.imageShow.setImageDrawable(getResources().getDrawable(R.drawable.frame_161));
                                }
                            }
                            Toast.makeText(getContext(), "User Details updated successfully", Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        JSONObject jsonObject = new JSONObject(errorBody);
                        if (jsonObject.has("statusCode")){
                            int code = jsonObject.getInt("statusCode");
                            if (code == 400){
                                if (jsonObject.has("message")) {
                                    String message = jsonObject.getString("message");
                                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                                }
                            }else if (code == 500){
                                if (jsonObject.has("message")) {
                                    String message = jsonObject.getString("message");
                                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                                }
                            }
                        }else{
                            Toast.makeText(getContext(), "Internal Error. Please try again", Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException | JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                saveLabel.setVisibility(View.VISIBLE);
                progress.setVisibility(View.GONE);
                saveCard.setEnabled(true);
                if (t instanceof SocketTimeoutException) {
                    Log.e("Retry", "Retrying...");
                    call.clone().enqueue(this);
                } else {
                    Toast.makeText(getContext(), "Internal Error. Please try again", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private MultipartBody.Part prepareImagePart(String partName, Uri imageUri) {
        try {
            ContentResolver contentResolver = getContext().getContentResolver();

            // ✅ Get actual MIME type
            String mimeType = contentResolver.getType(imageUri);
            if (mimeType == null) {
                mimeType = "image/jpeg"; // fallback
            }

            InputStream inputStream = contentResolver.openInputStream(imageUri);
            byte[] bytes = getBytesFromInputStream(inputStream);

            // ✅ Infer filename extension from MIME type
            String extension = mimeType.equals("image/png") ? "png" : "jpeg";

            RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), bytes);
            return MultipartBody.Part.createFormData(partName, "image." + extension, requestFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    private byte[] getBytesFromInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    private Map<String, RequestBody> createPartMap(Map<String, String> paramMap) {
        Map<String, RequestBody> partMap = new HashMap<>();
        for (Map.Entry<String, String> entry : paramMap.entrySet()) {
            if (entry.getValue() != null) {
                partMap.put(entry.getKey(), RequestBody.create(MediaType.parse("text/plain"), entry.getValue()));
            }
        }
        return partMap;
    }

    private void showImageSourceDialog() {
        String[] options = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Select Image Source")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions(new String[]{android.Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                        } else {
                            openCamera(); // Permission is already granted
                        }
                    } else {
                        openGallery();
                    }
                })
                .show();
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = createImageFile(); // Create the image file
        if (photoFile != null) {
            imageUri = FileProvider.getUriForFile(getContext(), getContext().getPackageName() + ".fileprovider", photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                boolean cameraGranted = ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
                if (!cameraGranted) {
                    Toast.makeText(getContext(), "Camera permission is missing. Please grant it again.", Toast.LENGTH_LONG).show();
                    resetImageSelection();
                    return;
                }
                if (imageUri != null) {
                    try {
                        // Try to decode the image
                        InputStream inputStream = getContext().getContentResolver().openInputStream(imageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        inputStream.close();

                        if (bitmap != null) {
                            imageUri = resizeImageAndGetUri(imageUri);
                            isNewImageSelected = true;
                            validateImage(imageUri);
                        } else {
//                            Toast.makeText(getContext(), "No image captured", Toast.LENGTH_SHORT).show();
                            resetImageSelection();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        resetImageSelection();
                    }
                } else {
                    resetImageSelection();
                }
            } else if (requestCode == PICK_IMAGE) {
                if (data != null) {
                    imageUri = data.getData();
                    imageUri = resizeImageAndGetUri(imageUri);
                    isNewImageSelected = true;
                    validateImage(imageUri);
                } else {
                    resetImageSelection();
                }
            }
        } else {
            resetImageSelection();
        }
    }


    private void resetImageSelection() {
        imageUri = null;
        if (!profile_image_uri.equalsIgnoreCase("null")){
            Glide.with(getContext())
                    .load(profile_image_uri)
                    .into(imageView);
        }else{
            imageView.setImageDrawable(getResources().getDrawable(R.drawable.frame_161));
        }
    }


    private Uri resizeImageAndGetUri(Uri imageUri) {
        try {
            // Open input stream from the image URI
            InputStream inputStream = getContext().getContentResolver().openInputStream(imageUri);
            Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            // Reopen input stream to read EXIF (must be separate)
            inputStream = getContext().getContentResolver().openInputStream(imageUri);
            ExifInterface exif = new ExifInterface(inputStream);
            inputStream.close();

            // Get the orientation and rotate if necessary
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int rotationDegrees = 0;

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotationDegrees = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotationDegrees = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotationDegrees = 270;
                    break;
            }

            if (rotationDegrees != 0) {
                Matrix matrix = new Matrix();
                matrix.postRotate(rotationDegrees);
                originalBitmap = Bitmap.createBitmap(
                        originalBitmap, 0, 0,
                        originalBitmap.getWidth(), originalBitmap.getHeight(),
                        matrix, true
                );
            }

            // Resize image
            int newWidth = 400;
            int newHeight = (newWidth * originalBitmap.getHeight()) / originalBitmap.getWidth();
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true);

            // Save to a temporary file
            File resizedFile = createImageFile();
            FileOutputStream outStream = new FileOutputStream(resizedFile);
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
            outStream.flush();
            outStream.close();

            return Uri.fromFile(resizedFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }




    private void validateImage(Uri imageUri) {
        String mimeType = getContext().getContentResolver().getType(imageUri);
        if (mimeType != null && !mimeType.matches("image/(jpg|jpeg|png)")) {
            Toast.makeText(getContext(), "Please select a valid image (JPG, JPEG, PNG).", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            InputStream inputStream = getContext().getContentResolver().openInputStream(imageUri);
            if (inputStream != null) {
                int sizeKB = inputStream.available() / 1024; // Size in KB
                if (sizeKB > 5000) {
                    Toast.makeText(getContext(), "Image size must be less than 5 MB.", Toast.LENGTH_SHORT).show();
                    return;
                }
                Glide.with(getContext())
                        .load(imageUri)
                        .into(imageView);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File createImageFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return new File(storageDir, imageFileName + ".jpg");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                openCamera();
            } else {
                boolean cameraDeniedForever = !ActivityCompat.shouldShowRequestPermissionRationale((Activity) getContext(), Manifest.permission.CAMERA);

                if (cameraDeniedForever) {
                    showPermissionDeniedDialog();
                } else {
                    Toast.makeText(getContext(), "Camera permission is required.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void showPermissionDeniedDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Permissions Required")
                .setMessage("Camera permission is permanently denied. Please enable it in app settings.")
                .setPositiveButton("Go to Settings", (dialog, which) -> {
                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getContext().getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
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
            saveCard.setEnabled(true);
            if (!isAPICallRunning) {
                AsyncTaskUserData asyncTaskUserData = new AsyncTaskUserData();
                asyncTaskUserData.execute();
                isAPICallRunning = true;
            }
        }else{
            internetResult = false;
            saveCard.setEnabled(false);
            HashMap<String, String> user = userManagement.userDetails();
            updateProfileUI(
                    user.get(userManagement.NAME),
                    user.get(userManagement.MOBILE_NO),
                    user.get(userManagement.EMAIL),
                    user.get(userManagement.PROFILE_PIC)
            );
        }
    }

    public class AsyncTaskUserData extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            if (getActivity() != null) {
                JsonAPICall.makeGetRequestWithToken(getActivity(), API.user_data, new JsonAPICall.VolleyCallback() {
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
                        Toast.makeText(getActivity(), "Internal Error. Loading saved data...", Toast.LENGTH_SHORT).show();
                        HashMap<String, String> user = userManagement.userDetails();
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() ->
                                    updateProfileUI(
                                            user.get(userManagement.NAME),
                                            user.get(userManagement.MOBILE_NO),
                                            user.get(userManagement.EMAIL),
                                            user.get(userManagement.PROFILE_PIC)
                                    )
                            );
                        }
                    }
                });
            }
            return null;
        }

        private void handleResponse(String response) throws JSONException {
            if (response != null) {
                JSONObject jsonObject = new JSONObject(response);
                if (jsonObject.has("status")) {
                    int status = jsonObject.getInt("status");
                    if (status == 1) {
                        JSONObject userObject = jsonObject.getJSONObject("data");
                        int id = userObject.getInt("id");
                        String name = userObject.getString("name");
                        String username = userObject.getString("username");
                        String mobile = userObject.getString("phone_number");
                        String email = userObject.getString("email");
                        String profilePic = userObject.optString("photo_url", "null");
                        if (profilePic != null && !profilePic.equalsIgnoreCase("null") && !profilePic.isEmpty()) {
                            profilePic = "https://palasbari.h24x7.in/" + profilePic;
                        } else {
                            profilePic = "null";
                        }

                        userManagement.userSessionManage(username, String.valueOf(id), mobile, name, profilePic, email);

                        if (getActivity() != null) {
                            String finalProfilePic = profilePic;
                            getActivity().runOnUiThread(() -> updateProfileUI(name, mobile, email, finalProfilePic));
                        }
                    }
                }
            }
        }
    }

    private void updateProfileUI(String name, String phNo, String eMail, String image) {
        lin.setVisibility(View.VISIBLE);
        spin_kit.setVisibility(View.GONE);
        profile_image_uri = image;
        email = eMail ;

        if (name != null && !name.equalsIgnoreCase("null")) {
            nameEditText.setText(name);
        } else {
            nameEditText.setText("");
        }

        if (phNo != null && !phNo.equalsIgnoreCase("null")) {
            numberEditText.setText(phNo);
        } else {
            numberEditText.setText("");
        }
        if (email != null && !email.equalsIgnoreCase("null")) {
            emailEditText.setText(email);
        } else {
            emailEditText.setText("");
        }
        if (profile_image_uri != null && !profile_image_uri.equalsIgnoreCase("null")) {
            Glide.with(getContext()).load(profile_image_uri).into(imageShow);
            Glide.with(getContext()).load(profile_image_uri).into(imageView);
        } else {
            imageShow.setImageDrawable(getResources().getDrawable(R.drawable.frame_161));
            imageView.setImageDrawable(getResources().getDrawable(R.drawable.frame_161));
        }
    }


    private void handleBackPress() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Fragment currentFragment = new SettingsFragment();
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