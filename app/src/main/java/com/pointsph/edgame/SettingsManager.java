package com.pointsph.edgame;

public class SettingsManager {
    private static SettingsManager settingsManager;
    private String imageUrl;

    public static SettingsManager getInstance(){
        if(settingsManager == null) {
            settingsManager = new SettingsManager();
        }

        return settingsManager;
    }

    private SettingsManager(){}

    public String getImageUrl(){ return imageUrl; }
    public void setImageUrl(String imageUrl){ this.imageUrl = imageUrl; }
}
