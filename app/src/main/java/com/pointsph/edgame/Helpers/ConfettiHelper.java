package com.pointsph.edgame.Helpers;

import android.content.Context;
import android.content.SharedPreferences;

import com.pointsph.edgame.SharedPref.SharedPreferenceHelper;

public class ConfettiHelper {

    public static void setGrammarConfettiAlreadyDisplayed(Context context , String username)
    {
        SharedPreferenceHelper.PREF_FILE ="grammar_confetti";
        SharedPreferenceHelper.setSharedPreferenceBoolean(context,username.concat("_confetti"),true);
    }


    public static void setSpellingConfettiAlreadyDisplayed(Context context , String username)
    {
        SharedPreferenceHelper.PREF_FILE ="spelling_confetti";
        SharedPreferenceHelper.setSharedPreferenceBoolean(context,username.concat("_confetti"),true);
    }

    public static boolean isConfettiInSpellingAlreadyDisplay(Context context , String username)
    {
        SharedPreferenceHelper.PREF_FILE ="spelling_confetti";
        return SharedPreferenceHelper.getSharedPreferenceBoolean(context,username.concat("_confetti"),false);
    }

    public static boolean isConfettiInPronunciationAlreadyDisplay(Context context , String username)
    {
        SharedPreferenceHelper.PREF_FILE ="pronun_confetti";
        return SharedPreferenceHelper.getSharedPreferenceBoolean(context,username.concat("_confetti"),false);
    }

    public static void setPronunciationAlreadyDisplayed(Context context , String username)
    {
        SharedPreferenceHelper.PREF_FILE ="pronun_confetti";
        SharedPreferenceHelper.setSharedPreferenceBoolean(context,username.concat("_confetti"),true);
    }




    public static boolean isConfettiInGrammarAlreadyDisplay(Context context , String username)
    {
        SharedPreferenceHelper.PREF_FILE ="grammar_confetti";
        return SharedPreferenceHelper.getSharedPreferenceBoolean(context,username.concat("_confetti"),false);
    }

    public static void rebaseGrammarFinishConffeti(Context context)
    {
        SharedPreferences settings = context.getSharedPreferences("grammar_confetti", Context.MODE_PRIVATE);
        settings.edit().clear().apply();
    }

    public static void rebaseSpellingFinishConfetti(Context context)
    {
        SharedPreferences settings = context.getSharedPreferences("spelling_confetti", Context.MODE_PRIVATE);
        settings.edit().clear().apply();
    }

    public static void rebasePronunFinishConfetti(Context context) {
        SharedPreferences settings = context.getSharedPreferences("pronun_confetti", Context.MODE_PRIVATE);
        settings.edit().clear().apply();
    }
}
