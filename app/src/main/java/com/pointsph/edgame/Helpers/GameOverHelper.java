package com.pointsph.edgame.Helpers;

import android.content.Context;
import android.content.SharedPreferences;

import com.pointsph.edgame.SharedPref.SharedPreferenceHelper;

public class GameOverHelper {

    /**
     * Track the user mistake
     * @param context
     * @param username
     */
    public static void addMistake(Context context  , String username , String level)
    {
        SharedPreferenceHelper.PREF_FILE = "user_mistake";
        int userWrongAnswer = getUserMistake(context,username,level);
        userWrongAnswer++;
        SharedPreferenceHelper.setSharedPreferenceInt(context,username.concat("mistakes"+level),userWrongAnswer);
    }

    /**
     * Get the user mistakes
     * @param context
     * @param username
     */
    public static int getUserMistake(Context context, String username, String level)
    {
        SharedPreferenceHelper.PREF_FILE = "user_mistake";
        return SharedPreferenceHelper.getSharedPreferenceInt(context,username.concat("mistakes"+level),0);
    }

    /**
     * Checking if the user is game over or not
     * @param context
     * @param username
     */
    public static boolean isUserGameOver(Context context , String username, String level)
    {
        return getUserMistake(context,username,level) >= 5;
    }


    /**
     * rebase or in short deleting all records
     * @param context
     */
    public static void rebaseUserMistakes(Context context)
    {
        SharedPreferences settings = context.getSharedPreferences("user_mistake", Context.MODE_PRIVATE);
        settings.edit().clear().apply();
    }
}
