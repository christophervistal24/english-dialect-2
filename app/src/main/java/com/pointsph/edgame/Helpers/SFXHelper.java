package com.pointsph.edgame.Helpers;

import android.content.Context;
import android.media.MediaPlayer;

public class SFXHelper {

    public static void playMusic(Context context, int music)
    {
        MediaPlayer sfx = MediaPlayer.create(context,music);
        sfx.start();
        sfx.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer sfx) {
                sfx.release();
            }
        });
    }
}
