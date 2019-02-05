package com.pointsph.edgame;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.pointsph.edgame.Helpers.ConfettiHelper;
import com.pointsph.edgame.Helpers.GameOverHelper;
import com.pointsph.edgame.Helpers.UserInstructionHelper;
import com.pointsph.edgame.Helpers.UserLoginHelper;
import com.pointsph.edgame.Helpers.UserScoreHelper;
import com.pointsph.edgame.Services.BackgroundMusic;
import com.pointsph.edgame.SharedPref.SharedPreferenceHelper;
import com.pointsph.edgame.Watcher.HomeWatcher;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {

    private String STATUS_MESSAGE = "";

    // UI References
    private TextView tStatus;
   // private TextView tUserLevel;
    private Button bEnglishGrammar;
    private Button bSpelling;
    private Button bPronunciation;
    private Button bLogout;
    private Button bAbout;
    private ToggleButton soundStatus;
    private CoordinatorLayout lMainContainer;

    private User User;
    private android.content.Context Context;
    private FileHelper FileHelper;

    private static int RESULT_LOAD_IMAGE = 1;

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
        //method for checking the sound option of a user add by vistal
        boolean state = SharedPreferenceHelper.getSharedPreferenceBoolean(getApplicationContext(),User.Username+"sound",true);
        if  (state)
        {
            if (mServ != null) {
                mServ.resumeMusic();
            }
            soundStatus.setBackgroundResource(R.drawable.bg_music_icon);
        } else {
            soundStatus.setBackgroundResource(R.drawable.bg_music_icon_off);
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
    public void onBackPressed() {
        GameOverHelper.rebaseUserMistakes(this);
        UserScoreHelper.rebaseUserScore(this);
        ConfettiHelper.rebaseGrammarFinishConffeti(this);
        ConfettiHelper.rebaseSpellingFinishConfetti(getApplicationContext());
        ConfettiHelper.rebasePronunFinishConfetti(getApplicationContext());
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);

//        setSupportActionBar(toolbar);


        // remove title
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        this.User = new User();
        this.FileHelper = new FileHelper(this);
        this.Context = this;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            this.User.setFirstName(extras.getString("firstName"));
            this.User.setLastName(extras.getString("lastName"));
            this.User.setBirthday(extras.getString("birthday"));
            this.User.setUsername(extras.getString("username"));
            this.User.setGrammarUserLevel(extras.getString("grammarUserLevel"));
            this.User.setPronunciationUserLevel(extras.getString("pronunciationUserLevel"));
            this.User.setSpellingUserLevel(extras.getString("spellingUserLevel"));
        } else {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        }

        this.tStatus = findViewById(R.id.lblStatus);
        //this.tUserLevel = findViewById(R.id.lblUserLevel);
        this.bEnglishGrammar = findViewById(R.id.button_english_grammar);
        this.bSpelling = findViewById(R.id.button_spelling);
        this.bPronunciation = findViewById(R.id.button_pronunciation);
        this.bLogout = findViewById(R.id.button_log_out);
        this.lMainContainer = findViewById(R.id.main_container);
        this.bAbout = findViewById(R.id.button_about);

        /*my attach switch button for music*/
        this.soundStatus = findViewById(R.id.soundStatus);
        //check and generate user message
        UserLoginHelper.rememberThisUser(this,this.User.Username);
        this.generateMessageForUserStatus();
        //this.tUserLevel.setText(String.format("%s", this.User.GrammarUserLevel));

        /*AMO INE ANG PAGA SET UP NA BACKGROUND NG IYO PROGRAMMER IN CANCEL KO
        * PARA MA CHANGE KO AND BACKGROUND IMAGE
        * */
        /*try {
            this.initBackgroundImage();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }*/


        this.bEnglishGrammar.setOnClickListener(view -> {
            boolean checkStatus = UserInstructionHelper
                                        .isGrammarInstructMessageDone(getApplicationContext(),User.Username);
            if (checkStatus) {

                //redirect the user with the corresponding information
                setUserCredentials(EnglishGrammarActivity.class);
            } else {

                //the instruction message is display set to true now
                UserInstructionHelper.setGrammarInstructMessageDone(getApplicationContext(),User.Username);

                //display the instruction and redirect the user after hitting the ok button
                    displayInstructorForUserAndRedirect("Choose the correct answer to complete " +
                    "the given sentence " +
                            "\n" +
                            "\n" +
                    "Remember : You could not proceed to the next level " +
                    "unless you reach the level-up portion",EnglishGrammarActivity.class);
            }

        });


        this.bSpelling.setOnClickListener(view -> {
            boolean checkStatus = UserInstructionHelper
                    .isSpellingInstructMessageDone(getApplicationContext(),User.Username);
            if (checkStatus) {

                //redirect the user with the corresponding information
                setUserCredentials(SpellingActivity.class);

            } else {

                //the instruction message is display set to true now
                UserInstructionHelper.setSpellingInstructMessageDone(getApplicationContext(),User.Username);

                //display the instruction and redirect the user after hitting the ok button
                displayInstructorForUserAndRedirect("Tap the play button to hear the word given " +
                        "audio , then " +
                        "type the correct spelling of the word " +
                        "\n" +
                        "\n" +
                        "Remember : You could not proceed to the next level unless you reach " +
                        "level-up portion",SpellingActivity.class);
            }

        });

        this.bPronunciation.setOnClickListener(view -> {
            boolean checkStatus = UserInstructionHelper
                    .isPronunciationInstructMessageDone(getApplicationContext(),User.Username);
            if (checkStatus) {
                //redirect the user with the corresponding information
                setUserCredentials(PronunciationActivity.class);

            } else {
                //the instruction message is display set to true now
                UserInstructionHelper.setPronunciationInstructMessageDone(getApplicationContext(),User.Username);

                //display the instruction and redirect the user after hitting the ok button
                displayInstructorForUserAndRedirect("Tap the play button to hear " +
                        "the word given in audio, then choose the corresponding " +
                        "spelling of it's correct answer " +
                        "\n" +
                        "\n" +
                        "Remember : You could not proceed to the next level unless you reach " +
                        "level-up portion",PronunciationActivity.class);
            }

        });

        this.bLogout.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            intent.putExtra("firstName", "");
            intent.putExtra("lastName", "");
            intent.putExtra("birthday", "");
            intent.putExtra("username", "");
            intent.putExtra("grammarUserLevel", "");
            intent.putExtra("pronunciationUserLevel", "");
            intent.putExtra("spellingUserLevel", "");
            removeRememberUser();
            GameOverHelper.rebaseUserMistakes(getApplicationContext());
            UserScoreHelper.rebaseUserScore(getApplicationContext());
            ConfettiHelper.rebaseGrammarFinishConffeti(getApplicationContext());
            ConfettiHelper.rebaseSpellingFinishConfetti(getApplicationContext());
            ConfettiHelper.rebasePronunFinishConfetti(getApplicationContext());
            startActivity(intent);
            finish();
        });

        /*
         * REDIRECT TO HELP ACTIVITY ADD BY Vistal */
        bAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),HelpActivity.class);
                startActivity(intent);
            }
        });

        //method for checking the sound option of a user add by vistal
        this.checkUserSoundOption();

        /*
        *    attach listener for sound option button add by vistal
        */
       soundStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferenceHelper.PREF_FILE = "bg_music";
                if (isChecked) {
                    /*
                     * resume background music                                                                                                                                                                                 background music for this activity
                     */
                    bindAndPlayMusic();
                    SharedPreferenceHelper.setSharedPreferenceBoolean(getApplicationContext(),User.Username+"sound",true);
                    soundStatus.setBackgroundResource(R.drawable.bg_music_icon);
                } else {
                    doUnbindService();
                    if (BackgroundMusic.mPlayer != null) {
                        if (BackgroundMusic.mPlayer.isPlaying()) {
                            BackgroundMusic.mPlayer.pause();
                            SharedPreferenceHelper.setSharedPreferenceBoolean(getApplicationContext(),User.Username+"sound",false);
                            soundStatus.setBackgroundResource(R.drawable.bg_music_icon_off);
                        }
                    }
                }
            }
        });


      /*  FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, RESULT_LOAD_IMAGE);
        });*/
    }

    private void setUserCredentials(Class<?> activity) {
        Intent intent = new Intent(getApplicationContext(), activity);
        intent.putExtra("firstName", User.FirstName);
        intent.putExtra("lastName", User.LastName);
        intent.putExtra("birthday", User.Birthday);
        intent.putExtra("username", User.Username);
        intent.putExtra("grammarUserLevel", User.GrammarUserLevel);
        intent.putExtra("pronunciationUserLevel", User.PronunciationUserLevel);
        intent.putExtra("spellingUserLevel", User.SpellingUserLevel);
        startActivity(intent);
        finish();
    }

    private void displayInstructorForUserAndRedirect(String message,final Class<?> activity) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setMessage(message);
            alertDialog.setPositiveButton("OK", (dialog, whichButton) -> setUserCredentials(activity));
            alertDialog.setCancelable(false);
            alertDialog.create();
            alertDialog.show();
    }

    //generate a message for user added by vistal
    private void generateMessageForUserStatus() {
        //if login count is greater than equal to 3 means not first time
        String message = (UserLoginHelper.isFirstTime(this, this.User.Username))
                ? "Welcome" : "Welcome Back";
        this.tStatus.setText(String.format("%s, %s!",message, this.User.Username));
    }

    /*
    * Check the user option for background music */
    private void checkUserSoundOption() {
        SharedPreferenceHelper.PREF_FILE = "bg_music";
        boolean state = SharedPreferenceHelper.getSharedPreferenceBoolean(getApplicationContext(),User.Username+"sound",true);
        soundStatus.setChecked(state);
        if (soundStatus.isChecked()) {
            bindAndPlayMusic();
            SharedPreferenceHelper.setSharedPreferenceBoolean(getApplicationContext(), User.Username+"sound", true);
            soundStatus.setBackgroundResource(R.drawable.bg_music_icon);
        } else {
            doUnbindService();
            if (BackgroundMusic.mPlayer != null) {
                if (BackgroundMusic.mPlayer.isPlaying()) {
                    BackgroundMusic.mPlayer.pause();
                    SharedPreferenceHelper.setSharedPreferenceBoolean(getApplicationContext(), User.Username+"sound", false);
                    soundStatus.setBackgroundResource(R.drawable.bg_music_icon_off);
                }
            }
        }
    }

    /*
    * Attach background music to service
    * */
    private void bindAndPlayMusic() {
        doBindService();
        Intent music = new Intent();
        music.setClass(getApplicationContext(), BackgroundMusic.class);
        startService(music);

        mHomeWatcher = new HomeWatcher(Context);
        mHomeWatcher.setOnHomePressedListener(new HomeWatcher.OnHomePressedListener() {
            @Override
            public void onHomePressed() {
                if (mServ != null) {
                    mServ.pauseMusic();
                }
                isUserPressHomeButton();
            }
            @Override
            public void onHomeLongPressed() {
                if (mServ != null) {
                    mServ.pauseMusic();
                }
                isUserPressHomeButton();
            }
        });
        mHomeWatcher.startWatch();
    }

    private void isUserPressHomeButton() {
        if(Context instanceof MainActivity) {
            GameOverHelper.rebaseUserMistakes(getApplicationContext());
            UserScoreHelper.rebaseUserScore(getApplicationContext());
            ConfettiHelper.rebaseGrammarFinishConffeti(getApplicationContext());
            ConfettiHelper.rebaseSpellingFinishConfetti(getApplicationContext());
            ConfettiHelper.rebasePronunFinishConfetti(getApplicationContext());
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            SettingsManager.getInstance().setImageUrl(picturePath);
            cursor.close();
        }

        //Recreate this Activity
        //startActivity(new Intent(this, new MainActivity()));
        startActivity(new Intent(this.getIntent()));
    }

    private void initBackgroundImage() throws FileNotFoundException {
        String picturePath;
        picturePath = SettingsManager.getInstance().getImageUrl();
        if (picturePath == null || picturePath.trim().equals("")) {
            picturePath = this.getBackgroundImage();
            if (picturePath == null || picturePath.trim().equals("")) {
                //Set some default image that will be visible before selecting image
            } else {
                Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
                BitmapDrawable background = new BitmapDrawable(bitmap);
                this.lMainContainer.setBackground(background);
            }
        } else {
            Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
            BitmapDrawable background = new BitmapDrawable(bitmap);
            this.lMainContainer.setBackground(background);
            this.updateBackgroundImage(picturePath);
        }
    }

    private void updateBackgroundImage(String picturePath) {
        if (this.FileHelper.isExternalStorageAvailable() || !this.FileHelper.isExternalStorageReadOnly()) {
            try {
                File settingsFile = new File(getExternalFilesDir(this.FileHelper.Filepath), this.FileHelper.SettingsFile);
                // Write the new String with the replaced line OVER the same file
                FileOutputStream fileOut = new FileOutputStream(settingsFile.getPath());
                fileOut.write(picturePath.getBytes());
                try {
                    fileOut.close();
                } catch (IOException e) {
                    this.showMessage(Message.ERROR_OCCURRED);
                }
            } catch (Exception e) {
                this.showMessage(Message.ERROR_OCCURRED);
            }
        }
    }

    // Shows a modal message.
    private void showMessage(String message) {
        Message.show(message, this.Context);
    }

    private String getBackgroundImage() throws FileNotFoundException {
        String picturePath = "";
        if (this.FileHelper.isExternalStorageAvailable()) {
            String strLine;
            File settingsFile = new File(getExternalFilesDir(this.FileHelper.Filepath), this.FileHelper.SettingsFile);
            FileInputStream fileInputStream = new FileInputStream(settingsFile);
            DataInputStream dataInputStream = new DataInputStream(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(dataInputStream));

            try {
                while ((strLine = bufferedReader.readLine()) != null) {
                    picturePath = strLine;
                }

            } catch (IOException e) {
                picturePath = "";
            } finally {
                try {
                    bufferedReader.close();
                    dataInputStream.close();
                    fileInputStream.close();
                } catch (IOException e) {
                    picturePath = "";
                }
            }
        }

        return picturePath;
    }

    /*
     * save the last login user
     * */
    private void removeRememberUser() {
        SharedPreferenceHelper.PREF_FILE = "last_login";
        SharedPreferenceHelper
                .setSharedPreferenceString(getApplicationContext(),"username",null);
    }


}