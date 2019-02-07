package com.pointsph.edgame.Helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.pointsph.edgame.SharedPref.SharedPreferenceHelper;

public class GameOverHelper {

    /**
     * Track the user mistake
     * @param context
     * @param username
     */
    public static void addMistake(Context context  , String username , String level , String activity)
    {
        SharedPreferenceHelper.PREF_FILE = "user_mistake";
        int userWrongAnswer = getUserMistake(context,username,level,activity);
        userWrongAnswer++;
        SharedPreferenceHelper.setSharedPreferenceInt(context,username.concat(activity+"mistakes"+level),userWrongAnswer);
    }

    public static void setMistake(Context context  , String username , String level , String activity)
    {
        SharedPreferenceHelper.PREF_FILE = "user_mistake";
        SharedPreferenceHelper.setSharedPreferenceInt(context,username.concat(activity+"mistakes").concat(level),0);
    }

    /**
     * Get the user mistakes
     * @param context
     * @param username
     */
    public static int getUserMistake(Context context, String username, String level , String activity)
    {
        SharedPreferenceHelper.PREF_FILE = "user_mistake";
        return SharedPreferenceHelper.getSharedPreferenceInt(context,username.concat(activity+"mistakes"+level),0);
    }

    /**
     * Checking if the user is game over or not
     * @param context
     * @param username
     */
    public static boolean isUserGameOver(Context context , String username, String level , String activity)
    {
        return getUserMistake(context,username,level,activity) >= 5;
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

    public static void rebaseUserMistakesInLevel(Context context , String username, String level , String activity)
    {
        GameOverHelper.setMistake(context,username,level,activity);
    }


}
