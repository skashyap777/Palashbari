package com.palashbari.project;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.HashMap;


public class UserManagement {

    Context context;
    SharedPreferences sharedPreferences;
    public SharedPreferences.Editor editor;
    public static final String PREF_NAME = "User_Login";
    public static final String LOGIN = "is_user_login";
    public static final String USER_NAME = "userName";
    public static final String NAME = "name";
    public static final String USER_ID = "userId";
    public static final String TOKEN = "token";
    public static final String MOBILE_NO = "mobile";
    public static final String PROFILE_PIC = "profilePic";
    public static final String EMAIL = "email";
    public static final String REFRESH_TOKEN = "refreshToken";




    public UserManagement(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREF_NAME,Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

    }

    public boolean isUserLogin(){
        return sharedPreferences.getBoolean(LOGIN,false);
    }


    public void userSessionManage(String userName, String userId, String mobile, String name, String profilePic, String email){
        editor.putBoolean(LOGIN, true);
        editor.putString(USER_NAME,userName);
        editor.putString(USER_ID,userId);
        editor.putString(MOBILE_NO,mobile);
        editor.putString(NAME,name);
        editor.putString(PROFILE_PIC,profilePic);
        editor.putString(EMAIL,email);
        editor.apply();
    }


    public void token(String token, String refreshToken){
        editor.putBoolean(LOGIN, true);
        editor.putString(TOKEN,token);
        editor.putString(REFRESH_TOKEN,refreshToken);
        editor.apply();
    }

    public void checkLogin(){
        if (!this.isUserLogin()){
            Intent intent = new Intent(context,LoginActivity.class);
            context.startActivity(intent);
        }
    }

    public HashMap<String,String> userDetails(){
        HashMap<String,String> user = new HashMap<>();
        user.put(USER_NAME,sharedPreferences.getString(USER_NAME,null));
        user.put(NAME,sharedPreferences.getString(NAME,null));
        user.put(USER_ID,sharedPreferences.getString(USER_ID,null));
        user.put(TOKEN,sharedPreferences.getString(TOKEN,null));
        user.put(REFRESH_TOKEN,sharedPreferences.getString(REFRESH_TOKEN,null));
        user.put(PROFILE_PIC,sharedPreferences.getString(PROFILE_PIC,null));
        user.put(MOBILE_NO,sharedPreferences.getString(MOBILE_NO,null));
        user.put(EMAIL,sharedPreferences.getString(EMAIL,null));
        return user;
    }

    public void logout(Context activityContext){
        editor.clear();
        editor.commit();
        Intent intent = new Intent(activityContext, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);


        activityContext.startActivity(intent);

        if (activityContext instanceof Activity) {
            ((Activity) activityContext).finish();
        }
    }
}
