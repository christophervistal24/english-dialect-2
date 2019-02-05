package com.pointsph.edgame.Helpers;

import android.content.Context;
import android.content.SharedPreferences;

import com.pointsph.edgame.PronunciationActivity;
import com.pointsph.edgame.SharedPref.SharedPreferenceHelper;

public class UserScoreHelper {
    // Levels:
    // 1 and 2 = Beginner
    // 3 and 4 = Advance
    // 5 = Expert

    private static String currentLevelOfUser;

    public static String convertLevelToWord(int level)
    {
        String userLevel = "";
        switch(level)  {
            case 1 :
                userLevel = "Beginner";
                break;

            case 2 :
                userLevel = "Beginner";
                break;

            case 3 :
                userLevel = "Advance";
                break;

            case 4 :
                userLevel = "Advance";
                break;

            case 5 :
                userLevel = "Expert";
                break;


        }
        return userLevel;
    }

    public static int convertWordToLevel(String word)
    {
        int level = 0;
        switch(word)  {
            case "Beginner" :
                level = 1;
                break;
            case "Advance" :
                level = 2;
                break;
            case "Expert" :
                level = 3;
                break;
        }
        return level;
    }
    public static void setLevel(int level)
    {
        UserScoreHelper.currentLevelOfUser = UserScoreHelper.convertLevelToWord(level);
    }

    public static String getLevel()
    {
        return currentLevelOfUser;
    }

    public static void setCurrentScoreInGrammar(Context context , String level , String username , int score)
    {
        SharedPreferenceHelper.PREF_FILE = "user_score";
        SharedPreferenceHelper.setSharedPreferenceInt(context,username.concat("_grammar_").concat(level),score);
    }

    public static void addCurrentScoreInGrammar(Context context , String level , String username)
    {
        SharedPreferenceHelper.PREF_FILE = "user_score";
        int currentScore = getCurrentScoreInGrammar(context,level,username);
        currentScore++;
        SharedPreferenceHelper.setSharedPreferenceInt(context,username.concat("_grammar_").concat(level),currentScore);
    }

    public static int getCurrentScoreInGrammar(Context context , String level , String username)
    {
        SharedPreferenceHelper.PREF_FILE = "user_score";
        return SharedPreferenceHelper.getSharedPreferenceInt(context,username.concat("_grammar_").concat(level),0);
    }

    public static void addCurrentScoreInSpelling(Context context , String level , String username)
    {
        SharedPreferenceHelper.PREF_FILE = "user_score";
        int currentScore = getCurrentScoreInSpelling(context,level,username);
        currentScore++;
        SharedPreferenceHelper.setSharedPreferenceInt(context,username.concat("_spelling_").concat(level),currentScore);
    }

    public static void setCurrentScoreInSpelling(Context context , String level , String username , int score)
    {
        SharedPreferenceHelper.PREF_FILE = "user_score";
        SharedPreferenceHelper.setSharedPreferenceInt(context,username.concat("_spelling_").concat(level),score);
    }


    public static int getCurrentScoreInSpelling(Context context , String level , String username)
    {
        SharedPreferenceHelper.PREF_FILE = "user_score";
        return SharedPreferenceHelper.getSharedPreferenceInt(context,username.concat("_spelling_").concat(level),0);
    }


    public static void addCurrentScoreInPronunciation(Context context, String level, String username) {
        SharedPreferenceHelper.PREF_FILE = "user_score";
        int currentScore = getCurrentScoreInPronunciation(context,level,username);
        currentScore++;
        SharedPreferenceHelper.setSharedPreferenceInt(context,username.concat("_pronunciation_").concat(level),currentScore);
    }

    public static void setCurrentScoreInPronunciation(Context context , String level , String username , int score)
    {
        SharedPreferenceHelper.PREF_FILE = "user_score";
        SharedPreferenceHelper.setSharedPreferenceInt(context,username.concat("_pronunciation_").concat(level),score);
    }

    public static boolean isUserHasPreviousScoreInPronunciation(Context context , String level , String username)
    {
        SharedPreferenceHelper.PREF_FILE = "user_score";
        int userPreviousScore = SharedPreferenceHelper.getSharedPreferenceInt(context,username.concat("_pronunciation_").concat(level),0);
        return userPreviousScore != 0;
    }

    public static boolean isUserHasPreviousScoreInGrammar(Context context , String level , String username)
    {
        SharedPreferenceHelper.PREF_FILE = "user_score";
        int userPreviousScore = SharedPreferenceHelper.getSharedPreferenceInt(context,username.concat("_grammar_").concat(level),0);
        return userPreviousScore != 0;
    }

    public static int getCurrentScoreInPronunciation(Context context , String level , String username)
    {
        SharedPreferenceHelper.PREF_FILE = "user_score";
        return SharedPreferenceHelper.getSharedPreferenceInt(context,username.concat("_pronunciation_").concat(level),0);
    }



    public static boolean isUserHasPreviousScoreInSpelling(Context context , String level , String username)
    {
        SharedPreferenceHelper.PREF_FILE = "user_score";
        int userPreviousScore = SharedPreferenceHelper.getSharedPreferenceInt(context,username.concat("_spelling_").concat(level),0);
        return userPreviousScore != 0;
    }

    public static void rebaseUserScore(Context context)
    {
        SharedPreferences settings = context.getSharedPreferences("user_score", Context.MODE_PRIVATE);
        settings.edit().clear().apply();
    }


}
