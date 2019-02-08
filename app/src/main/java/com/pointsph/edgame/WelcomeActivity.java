package com.pointsph.edgame;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.ArrayMap;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;


import com.pointsph.edgame.Services.BackgroundMusic;
import com.pointsph.edgame.SharedPref.SharedPreferenceHelper;
import com.pointsph.edgame.Watcher.HomeWatcher;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class WelcomeActivity extends AppCompatActivity {

    private String ERROR_OCCURRED = "Ooops. Something is not right. Please try again.";
    private String INVALID_LOGIN = "Invalid username or password.";
    private FileHelper FileHelper;
    private User User;
    private String Username = "";
    private boolean isLogin = false;
    private RelativeLayout welcomeLayout;



    private boolean mIsBound = false;
    private BackgroundMusic mServ;
    HomeWatcher mHomeWatcher;

    private ServiceConnection Scon =new ServiceConnection(){
        public void onServiceConnected(ComponentName name, IBinder
                binder) {
            mServ = ((BackgroundMusic.ServiceBinder)binder).getService();
        }

        public void onServiceDisconnected(ComponentName name) {
            mServ = null;
        }
    };

    void doBindService(){
        bindService(new Intent(this,BackgroundMusic.class),
                Scon,Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService()
    {
        if(mIsBound)
        {
            unbindService(Scon);
            mIsBound = false;
        }
    }


    @Override
    protected void onDestroy() {
        doUnbindService();
        Intent music = new Intent();
        music.setClass(this,BackgroundMusic.class);
        stopService(music);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        if (mServ != null) {
            mServ.resumeMusic();
        }
        super.onResume();
    }


    @Override
    protected void onPause() {
        PowerManager pm = (PowerManager)
                getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = false;
        if (pm != null) {
            isScreenOn = pm.isScreenOn();
        }

        if (!isScreenOn) {
            if (mServ != null) {
                mServ.pauseMusic();
            }
        }
        super.onPause();
    }





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.FileHelper = new FileHelper(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_welcome);


        // remove title
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        doBindService();
        Intent music = new Intent();
        music.setClass(this, BackgroundMusic.class);
        startService(music);

        int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
        int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 2;
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

        mHomeWatcher = new HomeWatcher(this);
        mHomeWatcher.setOnHomePressedListener(new HomeWatcher.OnHomePressedListener() {
            @Override
            public void onHomePressed() {
                if (mServ != null) {
                    mServ.pauseMusic();
                }
            }
            @Override
            public void onHomeLongPressed() {
                if (mServ != null) {
                    mServ.pauseMusic();
                }
            }
        });
        mHomeWatcher.startWatch();


    }

    private void launchHomeScreen()
    {
        startActivity(new Intent(WelcomeActivity.this,LoginActivity.class));
        finish();
    }

    public void proceed(View v)
    {
        SharedPreferenceHelper.PREF_FILE = "last_login";
        MediaPlayer sfx = MediaPlayer.create(this, R.raw.click_sfx);
        sfx.start();
        this.Username = SharedPreferenceHelper
                                    .getSharedPreferenceString(getApplicationContext(),"username",null);
        if (this.Username != null) { // check if there's last login
            isUserAlreadyLogin(); //get the credentials
            if (isLogin) { //redirect to main activity
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra("firstName", this.User.FirstName);
                intent.putExtra("lastName", this.User.LastName);
                intent.putExtra("birthday", this.User.Birthday);
                intent.putExtra("username", this.Username);
                intent.putExtra("grammarUserLevel", this.User.GrammarUserLevel);
                intent.putExtra("pronunciationUserLevel", this.User.PronunciationUserLevel);
                intent.putExtra("spellingUserLevel", this.User.SpellingUserLevel);
                startActivity(intent);
                finish();
            } else {
                launchHomeScreen();
            }
        } else {
            launchHomeScreen();
        }
    }

    private void isUserAlreadyLogin()
    {
      String strLine;
      File userFile = new File(getExternalFilesDir(this.FileHelper.Filepath), this.FileHelper.UserFile);
      FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(userFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        DataInputStream dataInputStream = new DataInputStream(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(dataInputStream));
            ArrayMap<String, ArrayList<String>> arrayMap = new ArrayMap<>();
            if (fileInputStream != null) {
                if (dataInputStream != null) {
                    if (bufferedReader != null) {
                        try {
                            while ((strLine = bufferedReader.readLine()) != null) {
                                ArrayList<String> recordList = new ArrayList<>();
                                String[] splitData = strLine.split("\\s*,\\s*");
                                for (String aSplitData : splitData) {
                                    if (!(aSplitData == null)) {
                                        recordList.add(aSplitData.trim());
                                    }
                                }

                                // Username is in the third index.
                                arrayMap.put(recordList.get(3), recordList);
                            }

                            // Check if the username exists in the user file.
                            boolean hasKey = this.FileHelper.findKeyInArrayMap(arrayMap, this.Username);
                            if (hasKey) {
                                ArrayList<String> record = arrayMap.get(this.Username);
                                // Password is in the fourth index.
                                    String password = (record.get(4).trim());
                                    String firstName = (record.get(0).trim());
                                    String lastName = (record.get(1).trim());
                                    String birthday = (record.get(2).trim());
                                    String grammarUserLevel = (record.get(5).trim());
                                    String pronunciationUserLevel = (record.get(6).trim());
                                    String spellingUserLevel = (record.get(7).trim());
                                    // TODO: Set global variable for user info.
                                    this.User = new User();
                                    this.User.setFirstName(firstName);
                                    this.User.setLastName(lastName);
                                    this.User.setBirthday(birthday);
                                    this.User.setGrammarUserLevel(grammarUserLevel);
                                    this.User.setPronunciationUserLevel(pronunciationUserLevel);
                                    this.User.setSpellingUserLevel(spellingUserLevel);
                                    isLogin = true;
                            }
                    } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
            }
        }

    }
}
