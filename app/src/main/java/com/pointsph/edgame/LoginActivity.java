package com.pointsph.edgame;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.pointsph.edgame.Services.BackgroundMusic;
import com.pointsph.edgame.Watcher.HomeWatcher;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via username/password.
 */
public class LoginActivity extends AppCompatActivity {

    // TODO: Can be placed into a single java file so that the messages can be reusable.
    private String ERROR_OCCURRED = "Ooops. Something is not right. Please try again.";
    private String INVALID_LOGIN = "Invalid username or password.";


    // UI references.
    private EditText eUserName;
    private EditText ePassword;
    private View mProgressView;
    private View mLoginFormView;
    private Button bSignIn;
    private Button bRegister;
    private FileHelper FileHelper;

    private String STATUS_MESSAGE = "";
    private String Username = "";
    private String Password = "";
    private User User;

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
        setContentView(R.layout.activity_login);

        doBindService();
        Intent music = new Intent();
        music.setClass(this, BackgroundMusic.class);
        startService(music);

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


        this.FileHelper = new FileHelper(this);
        this.bSignIn = findViewById(R.id.sign_in_button);
        this.bRegister = findViewById(R.id.register_button);
        this.eUserName = findViewById(R.id.username);
        this.ePassword = findViewById(R.id.password);

        int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
        int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 2;
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

        this.ePassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        this.bSignIn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                MediaPlayer sfx = MediaPlayer.create(getApplicationContext(), R.raw.click_sfx);
                sfx.start();
                attemptLogin();
            }
        });

        this.bRegister.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                MediaPlayer sfx = MediaPlayer.create(getApplicationContext(), R.raw.click_sfx);
                sfx.start();
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });

//        this.mLoginFormView = findViewById(R.id.login_form);
        this.mProgressView = findViewById(R.id.login_progress);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid username, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        // Reset errors.
        this.eUserName.setError(null);
        this.ePassword.setError(null);

        // Store values at the time of the login attempt.
        this.Username = this.eUserName.getText().toString().trim();
        this.Password = this.ePassword.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(this.Password) && !isValidPassword(this.Password)) {
            this.ePassword.setError(getString(R.string.error_invalid_password));
            focusView = this.ePassword;
            cancel = true;
        }

        // Check for a valid username.
        if (TextUtils.isEmpty(this.Username)) {
            this.eUserName.setError(getString(R.string.error_field_required));
            focusView = this.eUserName;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            //showProgress(true);
            //mAuthTask = new UserLoginTask(username, password);
            //mAuthTask.execute((Void) null);
            if (login()) {
                // Store user info for global variable.
                // Login the user and redirect to the main.
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
                this.setStatusMessage(this.STATUS_MESSAGE);
            }
        }
    }

    private boolean login() {
        boolean isLogin = false;

        if (!this.FileHelper.isExternalStorageAvailable() || this.FileHelper.isExternalStorageReadOnly()) {
            this.STATUS_MESSAGE = this.ERROR_OCCURRED;
        } else {
            try {
                String strLine;
                File userFile = new File(getExternalFilesDir(this.FileHelper.Filepath), this.FileHelper.UserFile);
                FileInputStream fileInputStream = new FileInputStream(userFile);
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
                                    if (this.Password.equals(password)) {
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
                                    } else {
                                        this.STATUS_MESSAGE = this.INVALID_LOGIN;
                                    }
                                } else {
                                    this.STATUS_MESSAGE = this.INVALID_LOGIN;
                                }
                            } catch (IOException e) {
                                this.STATUS_MESSAGE = this.ERROR_OCCURRED;
                            }
                            try {
                                bufferedReader.close();
                            } catch (IOException e) {
                                this.STATUS_MESSAGE = this.ERROR_OCCURRED;
                            }
                        }
                    }
                }
            } catch (FileNotFoundException e) {
                //e.printStackTrace();
                this.STATUS_MESSAGE = this.ERROR_OCCURRED;
            }
        }

        return isLogin;
    }

    /**
     * Sets the status message.
     *
     * @param status, The message status.
     */
    private void setStatusMessage(String status) {
        TextView lblStatus = findViewById(R.id.lblStatus);
        lblStatus.setText(status);
    }

    /**
     * Sets the application to ready.
     */
    public void setReady() {
        this.eUserName.getText().clear();
        this.ePassword.getText().clear();
        this.updateButtonState(bRegister, true);
        this.updateButtonState(bSignIn, true);
    }

    /**
     * Sets the state of the button.
     */
    private void updateButtonState(Button button, boolean state) {
        button.setEnabled(state);
    }

    private boolean isValidEmail(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isValidPassword(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

}

