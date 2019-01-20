package com.pointsph.edgame;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.pointsph.edgame.Helpers.SFXHelper;
import com.pointsph.edgame.Helpers.UserStatusHelper;
import com.pointsph.edgame.Services.BackgroundMusic;
import com.pointsph.edgame.SharedPref.SharedPreferenceHelper;
import com.pointsph.edgame.Watcher.HomeWatcher;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

public class EnglishGrammarActivity extends AppCompatActivity {

    private String Beginner = "Beginner";
    private String Advance = "Advance";
    private String Expert = "Expert";

    private User User;
    private Context Context;
    private FileHelper FileHelper;
    private Questionnaire Questionnaire;

    // UI References
    private TextView tNameLevel;
    private TextView tScore;
    private Spinner sMode;
    private TextView tQuestion;
    private Button bChoiceA;
    private Button bChoiceB;
    private Button bChoiceC;
    private Button bChoiceD;
    private Button bHome;
    private ConstraintLayout mainContainer;

    // This will serve as the unique id and sequence number of each question.
    private Integer Id = 0;
    // Key-value pair variable that will store the questionnaires.
    // The key is the unique id.
    // The value is the ArrayList<String> for the question, choices, and answer.
    private ArrayMap<Integer, ArrayList<String>> Questions = new ArrayMap<>();

    private Integer QuestionsCount = 0;
    private Integer Score = 0;
    private Integer WrongAnswers = 0;

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
                Scon,BIND_AUTO_CREATE);
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
                getSystemService(POWER_SERVICE);
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
        setContentView(R.layout.activity_english_grammar);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);




        this.User = new User();
        this.Context = this;
        this.FileHelper = new FileHelper(this);
        this.Questionnaire = new Questionnaire();

        this.tNameLevel = findViewById(R.id.lblNameLevel);
        this.tQuestion = findViewById(R.id.lblQuestion);
        this.tScore = findViewById(R.id.lblScore);
        this.sMode = findViewById(R.id.spinner_mode);
        this.bChoiceA = findViewById(R.id.button_a);
        this.bChoiceB = findViewById(R.id.button_b);
        this.bChoiceC = findViewById(R.id.button_c);
        this.bChoiceD = findViewById(R.id.button_d);
        this.bHome = findViewById(R.id.button_home);
        this.mainContainer = findViewById(R.id.main_container);

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

        /*
        * Check the user option for background music ADDED by Vistal
        * */

        this.initLevels();
        // Sets the level of the user.
        this.setNameLevel(this.User.GrammarUserLevel);
        // Sets the level selection.
        this.setLevelSelection(this.User.GrammarUserLevel);
        this.checkUserSoundOption();


        /*
        try {
            this.initBackgroundImage();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }*/

        // Sets an event listener for the home button.
        this.bHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initMain();
            }
        });

        this.sMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Levels:
                // 1 and 2 = Beginner
                // 3 and 4 = Advance
                // 5 = Expert
                String level = parent.getItemAtPosition(position).toString();

                if (level.equals(Beginner)) {
                    // Get the questionnaires based on the selected mode.
                    getQuestionnaires(level);
                    showQuestion();
                    Score = 0;
                    setScore();
                }

                if (level.equals(Advance)) {
                    if (User.GrammarUserLevel.equals("1") || User.GrammarUserLevel.equals("2")) {
                        Message.show("Level not yet reached.", Context);
                        sMode.setSelection(position - 1);
                    } else {
                        // Get the questionnaires based on the selected mode.
                        getQuestionnaires(level);
                        showQuestion();
                        Score = 0;
                        setScore();
                    }
                }

                if (level.equals(Expert)) {
                    if (User.GrammarUserLevel.equals("1") || User.GrammarUserLevel.equals("2")) {
                        Message.show("Level not yet reached.", Context);
                        sMode.setSelection(position - 2);
                    } else if (User.GrammarUserLevel.equals("3") || User.GrammarUserLevel.equals("4")) {
                        Message.show("Level not yet reached.", Context);
                        sMode.setSelection(position - 1);
                    } else {
                        // Get the questionnaires based on the selected mode.
                        getQuestionnaires(level);
                        showQuestion();
                        Score = 0;
                        setScore();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

                // sometimes you need nothing here
            }
        });
    }

    @Override
    public void onBackPressed() {
        this.initMain();
        return;
    }

    public void initMain() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
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

    // Initializes the items in the spinner.
    private void initLevels() {
        String[] items = new String[]{this.Beginner, this.Advance, this.Expert};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        this.sMode.setAdapter(adapter);
    }

    // Sets the score.
    private void setScore() {
        if (this.Score <= this.QuestionsCount)
            this.tScore.setText(String.format("Score: %s / %s", this.Score.toString(), this.QuestionsCount.toString()));
}

    // Sets the name and the level of the user.
    private void setNameLevel(String userLevel) {
        String level = "";
        if (Integer.parseInt(userLevel) <= 2) level = this.Beginner;
        if (Integer.parseInt(userLevel) == 3 || Integer.parseInt(userLevel) == 4) level = this.Advance;
        if (Integer.parseInt(userLevel) == 5) level = this.Expert;
        this.tNameLevel.setText(String.format("%s: %s", this.User.FirstName, level));
    }

    // Shows a question from the questionnaire.
    private void showQuestion() {
        this.bChoiceA = findViewById(R.id.button_a);
        this.bChoiceB = findViewById(R.id.button_b);
        this.bChoiceC = findViewById(R.id.button_c);
        this.bChoiceD = findViewById(R.id.button_d);

        // Check if questions object is not empty.
        if (!this.Questions.isEmpty()) {
            this.QuestionsCount = this.Questions.size();
            Random rnd = new Random();
            // Generate a random integer between 0 and the length of the questionnaire.
            // The result will be used as the id of the key-value pair.
            int id = rnd.nextInt(this.QuestionsCount);
            // Get a single question based on the unique id.
            ArrayList<String> question = this.Questions.get(id);
            this.Questionnaire.setQuestion(question.get(0).trim());
            this.Questionnaire.setChoice_A(question.get(1).trim());
            this.Questionnaire.setChoice_B(question.get(2).trim());
            this.Questionnaire.setChoice_C(question.get(3).trim());
            this.Questionnaire.setChoice_D(question.get(4).trim());
            this.Questionnaire.setAnswer(question.get(5).trim());

            // Set the question label.
            this.tQuestion.setText(this.Questionnaire.Question);
            // Set the texts of the buttons based on the choices.
            this.bChoiceA.setText(this.Questionnaire.Choice_A);
            this.bChoiceB.setText(this.Questionnaire.Choice_B);
            this.bChoiceC.setText(this.Questionnaire.Choice_C);
            this.bChoiceD.setText(this.Questionnaire.Choice_D);

            // Add event listeners to the buttons.
            this.bChoiceA.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String text = bChoiceA.getText().toString().trim();
                    checkAnswer(text, Questionnaire.Answer);
                }
            });
            this.bChoiceB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String text = bChoiceB.getText().toString().trim();
                    checkAnswer(text, Questionnaire.Answer);
                }
            });
            this.bChoiceC.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String text = bChoiceC.getText().toString().trim();
                    checkAnswer(text, Questionnaire.Answer);
                }
            });
            this.bChoiceD.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String text = bChoiceD.getText().toString().trim();
                    checkAnswer(text, Questionnaire.Answer);
                }
            });
            // Add event listeners to the buttons.
        } else {
            this.showMessage(Message.ERROR_OCCURRED);
        }
    }

    // Checks whether the clicked button is the correct answer.
    public void checkAnswer(String choice, String answer) {
        String msg;
        boolean isShowLevelUpMsg = false;
        boolean isCorrect = choice.trim().toUpperCase().equals(answer.trim().toUpperCase());
        if (isCorrect) {
            msg = "Very good, that is correct!";
            // Add score.
            this.Score++;
            this.setScore();
            //music for correct
            SFXHelper.playMusic(getApplicationContext(),R.raw.correct);
            //track and set correct answer
            UserStatusHelper
                    .setEnglishGrammarCorrect(getApplicationContext(),User.Username);
        }  else {
            msg = "Sorry, that is incorrect. The correct answer is " + answer + ".";
            this.WrongAnswers++;
            //music for wrong
            SFXHelper.playMusic(getApplicationContext(),R.raw.wrong);
            //track and set wrong answer
            UserStatusHelper
                    .setEnglishGrammarWrong(getApplicationContext(),User.Username);
        }
        // Determine level up.
        if (this.isLevelUp()) {
            isShowLevelUpMsg = true;
            // Update user level of the user profile.
            Integer newLevel = this.levelUpUser();
            this.User.setGrammarUserLevel(newLevel.toString());
            this.setNameLevel(newLevel.toString());
            // Sets the level selection.
            this.setLevelSelection(this.User.GrammarUserLevel);
        }

        if (isShowLevelUpMsg) {
            Message.show("Congratulations! Level-up acquired!", this.Context);
            SFXHelper.playMusic(getApplicationContext(),R.raw.level_up);
        }

        Message.show(msg, this.Context);

        // Show another question.
        this.showQuestion();
    }

    // Shows a modal message.
    private void showMessage(String message) {
        Message.show(message, this.Context);
    }

    // Redirects to the main.
    private void redirectToMain() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra("firstName", User.FirstName);
        intent.putExtra("lastName", User.LastName);
        intent.putExtra("birthday", User.Birthday);
        intent.putExtra("username", User.Username);
        intent.putExtra("userLevel", User.GrammarUserLevel);
        startActivity(intent);
        finish();
    }

    // Get the questionnaires from the text file.
    private void getQuestionnaires(String levelMode) {
        if (!this.FileHelper.isExternalStorageAvailable() || this.FileHelper.isExternalStorageReadOnly()) {
            this.showMessage(Message.ERROR_OCCURRED);
        } else {
            try {
                String strLine;
                //File questionnaireFile = new File(getExternalFilesDir(this.FileHelper.Filepath), this.FileHelper.QuestionnaireFile);
                String qFile = "";

//                if (Integer.parseInt(userLevel) <= 2) qFile = this.FileHelper.BeginnerFile;
//                if (Integer.parseInt(userLevel) == 3 || Integer.parseInt(userLevel) == 4) qFile = this.FileHelper.AdvanceFile;
//                if (Integer.parseInt(userLevel) == 5) qFile = this.FileHelper.ExpertFile;
                if (levelMode.equals(this.Beginner)) qFile = this.FileHelper.BeginnerFile;
                if (levelMode.equals(this.Advance)) qFile = this.FileHelper.AdvanceFile;
                if (levelMode.equals(this.Expert)) qFile = this.FileHelper.ExpertFile;

                File questionnaireFile = new File(getExternalFilesDir(this.FileHelper.Filepath + "/" + this.FileHelper.GrammarFolder), qFile);
                FileInputStream fileInputStream = new FileInputStream(questionnaireFile);
                DataInputStream dataInputStream = new DataInputStream(fileInputStream);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(dataInputStream));
                if (fileInputStream != null) {
                    if (dataInputStream != null) {
                        if (bufferedReader != null) {
                            try {
                                this.Questions = new ArrayMap<>();
                                this.Id = 0;
                                // Read each line of the text file.
                                while ((strLine = bufferedReader.readLine()) != null) {
                                    ArrayList<String> recordList = new ArrayList<>();
                                    // Split each read line using the pipe.
                                    String[] splitData = strLine.split("\\s*\\|\\s*");
                                    for (String aSplitData : splitData) {
                                        if (!(aSplitData == null)) {
                                            // Add each item the ArrayList<String>.
                                            recordList.add(aSplitData.trim());
                                        }
                                    }

                                    // Set the Id as the key.
                                    // Set the recordList as the ArrayList<String>.
                                    // Store the key-value pair in the ArrayMap<Integer, ArrayList<String>> - Questions global variable.
                                    this.Questions.put(this.Id, recordList);
                                    // Increment the id.
                                    this.Id++;
                                }
                            } catch (IOException e) {
                                this.showMessage(Message.ERROR_OCCURRED);
                            }
                            try {
                                bufferedReader.close();
                            } catch (IOException e) {
                                this.showMessage(Message.ERROR_OCCURRED);
                            }
                        }
                    }
                }
            } catch (FileNotFoundException e) {
                this.showMessage(Message.ERROR_OCCURRED);
            }
        }
    }

    // Determines if the user if for level up.
    private boolean isLevelUp() {
        boolean isLevel = false;
        int userLevel = Integer.parseInt(this.User.getGrammarUserLevel());
        int selectedLevel = this.sMode.getSelectedItemPosition();
        String selectedLevelName = this.sMode.getItemAtPosition(selectedLevel).toString();

        String userLevelName = "";
        if (userLevel <= 2) userLevelName = this.Beginner;
        if (userLevel == 3 || userLevel == 4) userLevelName = this.Advance;
        if (userLevel == 5) userLevelName = this.Expert;

        // The level of the user must be equal to the selected level in order to level up the user.
        if (selectedLevelName.trim().toUpperCase().equals(userLevelName.trim().toUpperCase())) {
            //int actualScore = this.Score - this.WrongAnswers;
            int actualScore = this.Score;
            double levelTwoRef = (this.QuestionsCount * 0.25);
            double levelThreeRef = (this.QuestionsCount * 0.5);
            double levelFourRef = (this.QuestionsCount * 0.7);
            double levelFiveRef = (this.QuestionsCount * 0.9);

            if (actualScore > Math.round(levelTwoRef)) {
                if (userLevel < 2) {
                    isLevel = true;
                }
            }
            if (actualScore > Math.round(levelThreeRef)) {
                if (userLevel < 3) {
                    isLevel = true;
                }
            }
            if (actualScore > Math.round(levelFourRef)) {
                if (userLevel < 4) {
                    isLevel = true;
                }
            }
            if (actualScore > Math.round(levelFiveRef)) {
                if (userLevel < 5) {
                    isLevel = true;
                }
            }
        }

        return isLevel;
    }

    // Level up the user.
    private Integer levelUpUser() {
        Integer newUserLevel = Integer.parseInt(this.User.GrammarUserLevel);
        if (this.FileHelper.isExternalStorageAvailable() || !this.FileHelper.isExternalStorageReadOnly()) {
            try {
                File userFile = new File(getExternalFilesDir(this.FileHelper.Filepath), this.FileHelper.UserFile);
                // Input the file content to the StringBuilder.
                BufferedReader file = new BufferedReader(new FileReader(userFile.getPath()));
                String strLine;
                StringBuilder stringBuilder = new StringBuilder();
                ArrayMap<String, ArrayList<String>> arrayMap = new ArrayMap<>();

                while ((strLine = file.readLine()) != null) {
                    stringBuilder.append(strLine);
                    stringBuilder.append("\n");

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

                String inputStr = stringBuilder.toString();
                file.close();

                // Check if the username exists in the user file.
                boolean hasKey = this.FileHelper.findKeyInArrayMap(arrayMap, this.User.Username);
                if (hasKey) {
                    // Get the record based on the username.
                    ArrayList<String> record = arrayMap.get(this.User.Username);

                    // Holds the old user profile.
                    StringBuilder oldUserProfile = new StringBuilder();
                    oldUserProfile.append(record.get(0).trim()); // first name
                    oldUserProfile.append(",");
                    oldUserProfile.append(record.get(1).trim()); // last name
                    oldUserProfile.append(",");
                    oldUserProfile.append(record.get(2).trim()); // birthday
                    oldUserProfile.append(",");
                    oldUserProfile.append(record.get(3).trim()); // username
                    oldUserProfile.append(",");
                    oldUserProfile.append(record.get(4).trim()); // password
                    oldUserProfile.append(",");
                    oldUserProfile.append(record.get(5).trim()); // grammar user level
                    oldUserProfile.append(",");
                    oldUserProfile.append(record.get(6).trim()); // pronunciation user level
                    oldUserProfile.append(",");
                    oldUserProfile.append(record.get(7).trim()); // spelling user level
                    oldUserProfile.append("\n");

                    Integer userLevel = Integer.parseInt(record.get(5).trim()); // grammar user level
                    userLevel++;
                    // Holds the new user profile.
                    StringBuilder newUserProfile = new StringBuilder();
                    newUserProfile.append(record.get(0).trim()); // first name
                    newUserProfile.append(",");
                    newUserProfile.append(record.get(1).trim()); // last name
                    newUserProfile.append(",");
                    newUserProfile.append(record.get(2).trim()); // birthday
                    newUserProfile.append(",");
                    newUserProfile.append(record.get(3).trim()); // username
                    newUserProfile.append(",");
                    newUserProfile.append(record.get(4).trim()); // password
                    newUserProfile.append(",");
                    newUserProfile.append(userLevel.toString()); // grammar user level
                    newUserProfile.append(",");
                    newUserProfile.append(record.get(6).trim()); // pronunciation user level
                    newUserProfile.append(",");
                    newUserProfile.append(record.get(7).trim()); // spelling user level
                    newUserProfile.append("\n");

                    inputStr = inputStr.replace(oldUserProfile.toString(), newUserProfile.toString());

                    // Write the new String with the replaced line OVER the same file
                    FileOutputStream fileOut = new FileOutputStream(userFile.getPath());
                    fileOut.write(inputStr.getBytes());

                    newUserLevel++;
                    try {
                        fileOut.close();
                    } catch (IOException e) {
                        this.showMessage(Message.ERROR_OCCURRED);
                    }
                }
            } catch (Exception e) {
                this.showMessage(Message.ERROR_OCCURRED);
            }
        }

        return newUserLevel;
    }

    // Set the level selection.
    private void setLevelSelection(String userLevel) {
        if (userLevel.equals("3") || userLevel.equals("4"))
            this.sMode.setSelection(1); // Advance
        if (userLevel.equals("5"))
            this.sMode.setSelection(2); // Expert
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
                this.mainContainer.setBackground(background);
            }
        } else {
            Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
            BitmapDrawable background = new BitmapDrawable(bitmap);
            this.mainContainer.setBackground(background);
        }
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
     * Check the user option for background music */
    private void checkUserSoundOption() {
        SharedPreferenceHelper.PREF_FILE = "bg_music";
        boolean state = SharedPreferenceHelper.getSharedPreferenceBoolean(getApplicationContext(),  this.User.Username+"sound",true);
        if (state) {
            bindAndPlayMusic();
            SharedPreferenceHelper.setSharedPreferenceBoolean(getApplicationContext(), this.User.Username+"sound", true);
        } else {
            doUnbindService();
            if (BackgroundMusic.mPlayer != null) {
                if (BackgroundMusic.mPlayer.isPlaying()) {
                    BackgroundMusic.mPlayer.pause();
                    SharedPreferenceHelper.setSharedPreferenceBoolean(getApplicationContext(), this.User.Username+"sound", false);
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

        mHomeWatcher = new HomeWatcher(getApplicationContext());
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

}