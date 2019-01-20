package com.pointsph.edgame.Helpers;

import android.content.Context;

import com.pointsph.edgame.SharedPref.SharedPreferenceHelper;

public class UserStatusHelper {
    //spelling
    //pronunciation

    /*
    * begin
    * this methods are, for tracking the correct and wrong
    * of the user in english grammar
    */
    public static void setEnglishGrammarCorrect(Context context , String username)
    {
        SharedPreferenceHelper.PREF_FILE = username.concat("_english_grammar");
        int oldCorrect = getEnglishGrammarCorrect(context,username);
        oldCorrect++;
        SharedPreferenceHelper
                    .setSharedPreferenceInt(context,"correct",oldCorrect);
    }

    public static int getEnglishGrammarCorrect(Context context, String username)
    {
        SharedPreferenceHelper.PREF_FILE = username.concat("_english_grammar");
        return SharedPreferenceHelper
                .getSharedPreferenceInt(context,"correct",0);
    }

    public static void setEnglishGrammarWrong(Context context , String username)
    {
        SharedPreferenceHelper.PREF_FILE = username.concat("_english_grammar");
       int oldWrong = getEnglishGrammarWrong(context,username);
       oldWrong++;
        SharedPreferenceHelper
                .setSharedPreferenceInt(context,"wrong",oldWrong);
    }


    public static int getEnglishGrammarWrong(Context context , String username)
    {
        SharedPreferenceHelper.PREF_FILE = username.concat("_english_grammar");
        return SharedPreferenceHelper
                .getSharedPreferenceInt(context,"wrong",0);
    }
    /* end */


    /*
     * begin
     * methods for tracking the correct and wrong
     * of the user in spelling
     */

    public static void setSpellingCorrect(Context context , String username)
    {
        SharedPreferenceHelper.PREF_FILE = username.concat("_spelling");
        int oldCorrect = getSpellingCorrect(context,username);
        oldCorrect++;
        SharedPreferenceHelper
                .setSharedPreferenceInt(context,"correct",oldCorrect);
    }

    public static int getSpellingCorrect(Context context, String username)
    {
        SharedPreferenceHelper.PREF_FILE = username.concat("_spelling");
        return SharedPreferenceHelper
                .getSharedPreferenceInt(context,"correct",0);
    }

    public static void setSpellingWrong(Context context , String username)
    {
        SharedPreferenceHelper.PREF_FILE = username.concat("_spelling");
        int oldWrong = getSpellingWrong(context,username);
        oldWrong++;
        SharedPreferenceHelper
                .setSharedPreferenceInt(context,"wrong",oldWrong);
    }


    public static int getSpellingWrong(Context context , String username)
    {
        SharedPreferenceHelper.PREF_FILE = username.concat("_spelling");
        return SharedPreferenceHelper
                .getSharedPreferenceInt(context,"wrong",0);
    }
    /* end */

    /*
     * begin
     * methods for tracking the correct and wrong
     * of the user in pronunciation
     */

    public static void setPronunciationCorrect(Context context , String username)
    {
        SharedPreferenceHelper.PREF_FILE = username.concat("_pronunciation");
        int oldCorrect = getPronunciationCorrect(context,username);
        oldCorrect++;
        SharedPreferenceHelper
                .setSharedPreferenceInt(context,"correct",oldCorrect);
    }

    public static int getPronunciationCorrect(Context context, String username)
    {
        SharedPreferenceHelper.PREF_FILE = username.concat("_pronunciation");
        return SharedPreferenceHelper
                .getSharedPreferenceInt(context,"correct",0);
    }

    public static void setPronunciationWrong(Context context , String username)
    {
        SharedPreferenceHelper.PREF_FILE = username.concat("_pronunciation");
        int oldWrong = getPronunciationWrong(context,username);
        oldWrong++;
        SharedPreferenceHelper
                .setSharedPreferenceInt(context,"wrong",oldWrong);
    }


    public static int getPronunciationWrong(Context context , String username)
    {
        SharedPreferenceHelper.PREF_FILE = username.concat("_pronunciation");
        return SharedPreferenceHelper
                .getSharedPreferenceInt(context,"wrong",0);
    }
    /* end */
}
