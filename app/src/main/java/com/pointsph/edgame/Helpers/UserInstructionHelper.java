package com.pointsph.edgame.Helpers;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.pointsph.edgame.EnglishGrammarActivity;
import com.pointsph.edgame.SharedPref.SharedPreferenceHelper;

public class UserInstructionHelper {


    /*
    *
    * check if grammar button checking if the user already got the instruction message*/
    public static boolean isGrammarInstructMessageDone(Context context , String username)
    {
        SharedPreferenceHelper.PREF_FILE = "instruction";
        return SharedPreferenceHelper.getSharedPreferenceBoolean(context,username.concat("grammar_message"),false);
    }

    /*
     *
     * set a boolean value for grammar message instruction*/
    public static void setGrammarInstructMessageDone(Context context , String username)
    {
        SharedPreferenceHelper.PREF_FILE = "instruction";
        SharedPreferenceHelper.setSharedPreferenceBoolean(context,username.concat("grammar_message"),true);
    }

    /*
     *
     * check if spelling button checking if the user already got the instruction message*/
    public static boolean isSpellingInstructMessageDone(Context context , String username)
    {
        SharedPreferenceHelper.PREF_FILE = "instruction";
        return SharedPreferenceHelper.getSharedPreferenceBoolean(context,username.concat("spelling_message"),false);
    }

    /*
     *
     * set a boolean value for spelling message instruction*/
    public static void setSpellingInstructMessageDone(Context context , String username)
    {
        SharedPreferenceHelper.PREF_FILE = "instruction";
        SharedPreferenceHelper.setSharedPreferenceBoolean(context,username.concat("spelling_message"),true);
    }

    /*
     *
     * check if pronunciation button checking if the user already got the instruction message*/
    public static boolean isPronunciationInstructMessageDone(Context context , String username)
    {
        SharedPreferenceHelper.PREF_FILE = "instruction";
        return SharedPreferenceHelper.getSharedPreferenceBoolean(context,username.concat("pronun_message"),false);
    }

    /*
     *
     * set a boolean value for pronunciation message instruction*/
    public static void setPronunciationInstructMessageDone(Context context , String username)
    {
        SharedPreferenceHelper.PREF_FILE = "instruction";
        SharedPreferenceHelper.setSharedPreferenceBoolean(context,username.concat("pronun_message"),true);
    }


    /**
     *
     * @param context
     * @param username of the user
     */
    public static void setMessageForGrammarBeginner(Context context , String username)
    {
        SharedPreferenceHelper.PREF_FILE = "instruction";
        SharedPreferenceHelper.setSharedPreferenceBoolean(context,username.concat("grammar_beginner"),true);
    }

    public static boolean isMessageForGrammarBeginnerDone(Context context , String username)
    {
        SharedPreferenceHelper.PREF_FILE = "instruction";
        return SharedPreferenceHelper.getSharedPreferenceBoolean(context,username.concat("grammar_beginner"),false);
    }

    public static void setMessageForGrammarAdvance(Context context , String username)
    {
        SharedPreferenceHelper.PREF_FILE = "instruction";
        SharedPreferenceHelper.setSharedPreferenceBoolean(context,username.concat("grammar_advance"),true);
    }

    public static boolean isMessageForGrammarAdvanceDone(Context context , String username)
    {
        SharedPreferenceHelper.PREF_FILE = "instruction";
        return SharedPreferenceHelper.getSharedPreferenceBoolean(context,username.concat("grammar_advance"),false);
    }

    public static void setMessageForGrammarExpert(Context context , String username)
    {
        SharedPreferenceHelper.PREF_FILE = "instruction";
        SharedPreferenceHelper.setSharedPreferenceBoolean(context,username.concat("grammar_expert"),true);
    }

    public static boolean isMessageForGrammarExpertDone(Context context , String username)
    {
        SharedPreferenceHelper.PREF_FILE = "instruction";
        return SharedPreferenceHelper.getSharedPreferenceBoolean(context,username.concat("grammar_expert"),false);
    }

    public static void setMessage(Activity activity , String message)
    {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
        alertDialog.setMessage("Expert").setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                }).create().show();
    }






}
