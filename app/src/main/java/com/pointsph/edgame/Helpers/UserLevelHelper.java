package com.pointsph.edgame.Helpers;

public class UserLevelHelper {

    public static int currentLevelOfUser(int level)
    {
        int currentLevel = 0;
        if (level == 0) {
            currentLevel = 1;
        } else if (level >= 1 && level <= 2 ) {
            currentLevel = 1;
        } else if (level >= 3 && level <= 4) {
            currentLevel = 2;
        } else {
            currentLevel = 3;
        }
        return currentLevel;
    }
}
