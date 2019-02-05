package com.pointsph.edgame.Helpers;

import android.content.Context;

import com.pointsph.edgame.SharedPref.SharedPreferenceHelper;

public class UserLoginHelper {

    public static void rememberThisUser(Context context, String username) {
        SharedPreferenceHelper.PREF_FILE = "last_login";
        SharedPreferenceHelper
                .setSharedPreferenceString(context,"username",username);

        //get the user login count by username
        int loginCount = SharedPreferenceHelper
                .getSharedPreferenceInt(context,username.concat("login_count"),0);
        loginCount++;
        //is new user is first time to login ?
        SharedPreferenceHelper
                .setSharedPreferenceInt(context,username.concat("login_count"),loginCount);

    }

    private static int getUserLoginCount(Context context, String username)
    {
        SharedPreferenceHelper.PREF_FILE = "last_login";
        return SharedPreferenceHelper
                .getSharedPreferenceInt(context,username.concat("login_count"),0);
    }

    public static boolean isFirstTime(Context context, String username)
    {
        return UserLoginHelper.getUserLoginCount(context , username) <= 3;
    }





}
