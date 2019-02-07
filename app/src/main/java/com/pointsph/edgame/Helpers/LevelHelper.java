package com.pointsph.edgame.Helpers;

public class LevelHelper {

    public static String convertLevelToWord(String level)
    {
        String levelWord = null;
        switch(level) {
            case "1":
                levelWord = "Beginner";
                break;

            case "2":
                levelWord = "Beginner";
                break;

            case "3":
                levelWord = "Advance";
                break;

            case "4":
                levelWord = "Advance";
                break;

            case "5":
                levelWord = "Expert";
                break;
        }
        return levelWord;
    }
}
