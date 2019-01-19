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
import android.support.annotation.RequiresApi;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.pointsph.edgame.Services.BackgroundMusic;
import com.pointsph.edgame.Watcher.HomeWatcher;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private String ERROR_OCCURRED = "Ooops. Something is not right. Please try again.";
    private String STATUS_MESSAGE = "";

    private Context Context;

    // UI references.
    private EditText eFirstName;
    private EditText eLastName;
    private EditText eBirthday;
    private EditText eUserName;
    private EditText ePassword;
    private EditText eConfirmPassword;
    private Button bRegister;
    private Button bLogin;

    // Register variables
    private String FirstName;
    private String LastName;
    private String Birthday;
    private String UserName;
    private String Password;
    private String GrammarUserLevel = "1";
    private String PronunciationUserLevel = "1";
    private String SpellingUserLevel = "1";

    private FileHelper FileHelper;


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
        setContentView(R.layout.activity_register);

        int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
        int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 2;
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

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

        this.Context = this;

        // Set up the login form.
        this.eFirstName = findViewById(R.id.txtFirstName);
        this.eLastName = findViewById(R.id.txtLastName);
        this.eBirthday = findViewById(R.id.txtBirthday);
        this.eUserName = findViewById(R.id.txtUserName);
        this.ePassword = findViewById(R.id.txtPassword);
        this.eConfirmPassword = findViewById(R.id.txtConfirmPassword);
        this.bRegister = findViewById(R.id.btnRegister);
        this.bLogin = findViewById(R.id.btnLogin);

        this.FileHelper = new FileHelper(this);

        this.bRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MediaPlayer sfx = MediaPlayer.create(getApplicationContext(), R.raw.click_sfx);
                sfx.start();
                attemptRegister();
            }
        });

        this.bLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    // NOTE:
    // Will be called if the onClick event of the .xml file is defined.
    // android:onClick="onClick"
    // Would be redundant if the "setOnClickListener" of a button is defined in the onCreate();
    // Remove "implements View.OnClickListener" in the class level to prevent error if onClick is not used.
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onClick(View v) {
        Intent intent;

        switch (v.getId()) {
            case R.id.btnRegister:
                this.attemptRegister();
                break;

            case R.id.btnLogin:
                intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
                break;

            default:
                // Do nothing
                break;
        }
    }

    private void attemptRegister() {
        this.FirstName = this.eFirstName.getText().toString().trim();
        this.LastName = this.eLastName.getText().toString().trim();
        this.Birthday = this.eBirthday.getText().toString().trim();
        this.UserName = this.eUserName.getText().toString().trim();
        this.Password = this.ePassword.getText().toString().trim();

        if (ePassword.getText().toString().trim().length() < 6) {
            Message.show("Passwords must not be less than 6 characters.", Context);
            return;
        }
        if (!ePassword.getText().toString().toUpperCase().equals(eConfirmPassword.getText().toString().toUpperCase())) {
            Message.show("Passwords do not match. Please try again.", Context);
            return;
        }

        updateButtonState(this.bRegister,false);
        updateButtonState(this.bLogin,false);
        try {
            if (this.register()) {
//                StringBuilder stringBuilder = new StringBuilder();
//                stringBuilder.append(String.format("Welcome, %s. Click Login so we can start playing!", this.FirstName));
//                this.setStatusMessage(stringBuilder.toString());
//                this.setReady();

                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            } else {
                updateButtonState(bRegister,true);
                updateButtonState(bLogin,true);
                this.setStatusMessage(this.STATUS_MESSAGE);
            }
        } catch (Exception e) {
            //e.printStackTrace();
            updateButtonState(bRegister,true);
            updateButtonState(bLogin,true);
            this.setStatusMessage(this.ERROR_OCCURRED);
        }
    }

    /**
     * Registers a new user.
     * @return true or false.
     * @throws Exception
     */
    private boolean register() throws Exception {
        boolean isRegister = false;
        if (this.areValidEntries()) {
            File userFile = new File(getExternalFilesDir(this.FileHelper.Filepath), this.FileHelper.UserFile);
            if (this.FileHelper.hasFile(userFile)) {
                if (this.isNewUser()) {
                    if (this.createUser()) {
                        isRegister = true;
                    }
                } else {
                    this.STATUS_MESSAGE = "Username already exists.";
                }
            } else {
                this.STATUS_MESSAGE = this.ERROR_OCCURRED;
            }
        } else {
            this.STATUS_MESSAGE = "Invalid entries found. Refrain from using comma ','.";
        }

        return isRegister;
    }

    /**
     * Creates a new user in the user file.
     * @return true or false.
     */
    private boolean createUser() {
        boolean isCreate = false;
        BufferedWriter bufferedWriter = null;
        FileWriter fileWriter;
        StringBuilder stringBuilder = new StringBuilder();

        try {
            //if (isExternalStorageAvailable() || !isExternalStorageReadOnly()) {
            if (this.FileHelper.isExternalStorageAvailable() || !this.FileHelper.isExternalStorageReadOnly()) {
                fileWriter = new FileWriter(getExternalFilesDir(this.FileHelper.Filepath) + "/" + this.FileHelper.UserFile, true);
                bufferedWriter = new BufferedWriter(fileWriter);

                stringBuilder.append(this.FirstName);
                stringBuilder.append(",");
                stringBuilder.append(this.LastName);
                stringBuilder.append(",");
                stringBuilder.append(this.Birthday);
                stringBuilder.append(",");
                stringBuilder.append(this.UserName);
                stringBuilder.append(",");
                stringBuilder.append(this.Password); //TODO: Encrypt password.
                stringBuilder.append(",");
                stringBuilder.append(this.GrammarUserLevel);
                stringBuilder.append(",");
                stringBuilder.append(this.PronunciationUserLevel);
                stringBuilder.append(",");
                stringBuilder.append(this.SpellingUserLevel);

                bufferedWriter.write(stringBuilder.toString());
                bufferedWriter.newLine();

                isCreate = true;
            }
        } catch (IOException ex) {
            this.setStatusMessage(this.ERROR_OCCURRED);
        } finally {
            try {
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }
            } catch (Exception ex) {
                this.setStatusMessage(this.ERROR_OCCURRED);
            }
        }

        return isCreate;
    }

    /**
     * Determines whether the registered user is a new user.
     * @return true or false.
     * @throws FileNotFoundException
     */
    private boolean isNewUser() throws FileNotFoundException {
        boolean isNew = false;

        if (this.FileHelper.isExternalStorageAvailable()) {
            String strLine;
            File readingFile = new File(getExternalFilesDir(this.FileHelper.Filepath), this.FileHelper.UserFile);
            FileInputStream fileInputStream = new FileInputStream(readingFile);
            DataInputStream dataInputStream = new DataInputStream(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(dataInputStream));

            ArrayMap<String, ArrayList<String>> arrayMap = new ArrayMap<>();
            try {
                while ((strLine = bufferedReader.readLine()) != null) {
                    ArrayList<String> recordList = new ArrayList<>();
                    String[] splitData = strLine.split("\\s*,\\s*");
                    for (String aSplitData : splitData) {
                        if (!(aSplitData == null)) {
                            recordList.add(aSplitData.trim());
                        }
                    }

                    // Set the username as key for the key-value pair.
                    arrayMap.put(recordList.get(3), recordList);
                }

                // Check if the user exists in the user file.
                boolean hasKey = this.FileHelper.findKeyInArrayMap(arrayMap, this.UserName);
                if (!hasKey) {
                    isNew = true;
                }
            } catch (IOException e) {
                this.setStatusMessage(this.ERROR_OCCURRED);
            } finally {
                try {
                    bufferedReader.close();
                    dataInputStream.close();
                    fileInputStream.close();
                } catch (IOException e) {
                    this.setStatusMessage(this.ERROR_OCCURRED);
                }
            }
        }

        return isNew;
    }

    /**
     * Sets the status message.
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
        this.eFirstName.getText().clear();
        this.eLastName.getText().clear();
        this.eBirthday.getText().clear();
        this.eUserName.getText().clear();
        this.ePassword.getText().clear();
        this.eConfirmPassword.getText().clear();
        this.updateButtonState(bRegister,true);
        this.updateButtonState(bLogin,true);
    }

    /**
     * Sets the state of the button.
     */
    private void updateButtonState(Button button, boolean state) {
        button.setEnabled(state);
    }

    /**
     * Determines whether the entries are valid.
     * @return true or false.
     */
    private boolean areValidEntries() {
        if (this.FirstName.trim().contains(",")) return false;
        if (this.LastName.trim().contains(",")) return false;
        if (this.Birthday.trim().contains(",")) return false;
        if (this.UserName.trim().contains(",")) return false;
        if (this.Password.trim().contains(",")) return false;

        if (TextUtils.isEmpty(this.FirstName.trim())) return false;
        if (TextUtils.isEmpty(this.LastName.trim())) return false;
        if (TextUtils.isEmpty(this.Birthday.trim())) return false;
        if (TextUtils.isEmpty(this.UserName.trim())) return false;
        if (TextUtils.isEmpty(this.Password.trim())) return false;

        return true;
    }

}
