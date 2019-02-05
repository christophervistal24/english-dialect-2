package com.pointsph.edgame;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.ArrayMap;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.pointsph.edgame.Helpers.ConfettiHelper;
import com.pointsph.edgame.Helpers.GameOverHelper;
import com.pointsph.edgame.Helpers.RandomHelper;
import com.pointsph.edgame.Helpers.SFXHelper;
import com.pointsph.edgame.Helpers.UserLevelHelper;
import com.pointsph.edgame.Helpers.UserScoreHelper;
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
import java.util.Collections;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;

public class SpellingActivity extends AppCompatActivity {

    private String Beginner = "Beginner";
    private String Advance = "Advance";
    private String Expert = "Expert";

    private User User;
    private Context Context;
    private FileHelper FileHelper;

    // UI References
    private TextView tNameLevel;
    private TextView tScore;
    private TextView tLevel;
    private Spinner sMode;
    private Button bPlay;
    private EditText eAnswer;
    private Button bCheck;
    private Button bHome;
    private FrameLayout mainContainer;

    // This will serve as the unique id and sequence number of each question.
    private Integer Id = 0;
    private ArrayList<String> Spellings;
    private String CurrentSpellingFolder = "";

    private Integer SpellingsCount = 0;
    private Integer Score = 0;
    private Integer WrongAnswers = 0;
    private static int currentLevel = 0;

    private boolean isFinish = false;
    private boolean isBackward = true;

    HomeWatcher mHomeWatcher;

    private nl.dionsegijn.konfetti.KonfettiView viewKonfetti;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spelling);



        this.User = new User();
        this.Context = this;
        this.FileHelper = new FileHelper(this);
        this.Spellings = new ArrayList<>();

        this.tNameLevel = findViewById(R.id.lblNameLevel);
        this.tScore = findViewById(R.id.lblScore);
        this.tLevel = findViewById(R.id.level);
        this.sMode = findViewById(R.id.spinner_mode_spelling);
        this.bPlay = findViewById(R.id.button_play);
        this.eAnswer = findViewById(R.id.txtAnswer);
        this.bCheck = findViewById(R.id.button_check);
        this.bHome = findViewById(R.id.button_home);
        this.mainContainer = findViewById(R.id.main_container);
        this.viewKonfetti = findViewById(R.id.viewKonfetti);

        //listener if the user press the home button
        mHomeWatcher = new HomeWatcher(Context);
        mHomeWatcher.setOnHomePressedListener(new HomeWatcher.OnHomePressedListener() {
            @Override
            public void onHomePressed() {
                isUserPressHomeButton();
            }
            @Override
            public void onHomeLongPressed() {
                isUserPressHomeButton();
            }
        });
        mHomeWatcher.startWatch();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            this.User.setUsername(extras.getString("username"));
            this.User.setGrammarUserLevel(extras.getString("grammarUserLevel"));
            this.User.setPronunciationUserLevel(extras.getString("pronunciationUserLevel"));
            this.User.setSpellingUserLevel(extras.getString("spellingUserLevel"));
        } else {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        }

        this.setNameLevel(this.User.SpellingUserLevel);
        this.initLevels();
        // Sets the level selection.
        this.setLevelSelection(this.User.SpellingUserLevel);

        // Set current level for a user in UI
        this.displaySetLevel();

        // Sets an event listener for the home button.
        this.bHome.setOnClickListener(view -> initMain());

        // Default the pronunciation folder to the beginner folder.
        this.CurrentSpellingFolder = this.FileHelper.SpellingBeginnerFolder;
        //when first open we need to set the level of the user
        UserScoreHelper.setLevel(Integer.parseInt(User.getSpellingUserLevel()));
        this.sMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Levels:
                // 1 and 2 = Beginner
                // 3 and 4 = Advance
                // 5 = Expert
                String level = parent.getItemAtPosition(position).toString();

                if (level.equals(Beginner)) {
                    CurrentSpellingFolder = FileHelper.SpellingBeginnerFolder;
                }

                if (level.equals(Advance)) {
                    if (User.SpellingUserLevel.equals("1") || User.SpellingUserLevel.equals("2")) {
                        Message.show("Level not yet reached. you need to answer " + (int) Math.ceil(((SpellingsCount+1) * .50) - Score) + " questions" +
                                " " +
                                "before you can jump to advance", Context);
                        isBackward = false;
                        sMode.setSelection(position - 1);
                        CurrentSpellingFolder = FileHelper.SpellingBeginnerFolder;
                    } else {
                        isBackward = true;
                        CurrentSpellingFolder = FileHelper.SpellingAdvanceFolder;
                    }
                }

                if (level.equals(Expert)) {
                    if (User.SpellingUserLevel.equals("1") || User.SpellingUserLevel.equals("2")) {
                        Message.show("Level not yet reached. you need to answer " + (int) Math.ceil(((SpellingsCount+1) * .50) - Score)  + "" +
                                " questions to proceed in advance then answer " + (int) Math.ceil((SpellingsCount+1) * 0.9) + " questions" +
                                " so you can jump to expert", Context);
                        isBackward = false;
                        sMode.setSelection(position - 2);
                        CurrentSpellingFolder = FileHelper.SpellingBeginnerFolder;
                    } else if (User.SpellingUserLevel.equals("3") || User.SpellingUserLevel.equals("4")) {
                        Message.show("Level not yet reached. you need to answer " + (int) Math.ceil(((SpellingsCount+1) * 0.9) - Score) + " questions" +
                                " " +
                                "before you can jump to expert", Context);
                        isBackward = false;
                        sMode.setSelection(position - 1);
                        CurrentSpellingFolder = FileHelper.SpellingAdvanceFolder;
                    } else {
                        isBackward = true;
                        CurrentSpellingFolder = FileHelper.SpellingExpertFolder;
                    }
                }
                initSpellings();
                showSpelling();
                Score = 0;
                setScore();

                //get the user choose
                //compare to it's current level
                boolean itIsEqualToCurrentLevel = level.equals(UserScoreHelper.convertLevelToWord(Integer.parseInt(User.getSpellingUserLevel())));
                //if the not equal to current level perform the action
                if  (!itIsEqualToCurrentLevel && isBackward) {
                    //set UserScoreHelper level to = what the user choose
                    UserScoreHelper.setLevel(UserScoreHelper.convertWordToLevel(level));
                    //rebase the user score on that particular level
                    UserScoreHelper.setCurrentScoreInSpelling(Context,UserScoreHelper.getLevel(),User.Username,0);
                    setScore();
                    initSpellings();
                    showSpelling();
                } else {
                    UserScoreHelper.setLevel(Integer.parseInt(User.getSpellingUserLevel()));
                    setScore();
                    initSpellings();
                    showSpelling();
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

                // sometimes you need nothing here
            }
        });

        //at the first the score of user is 0 so we need to call setScore
        //to resume the previous score of the user
        this.setScore();

        //give information to the user about question that needs to answer to acquired level up
        this.giveMessageDependingOnLevel();

        //checking if the user finish all the stages
        this.isUserFinishAllStages();
    }

    private void isUserPressHomeButton() {
        if (Context instanceof SpellingActivity) {
            GameOverHelper.rebaseUserMistakes(getApplicationContext());
            UserScoreHelper.rebaseUserScore(getApplicationContext());
            ConfettiHelper.rebaseGrammarFinishConffeti(getApplicationContext());
            ConfettiHelper.rebaseSpellingFinishConfetti(getApplicationContext());
            ConfettiHelper.rebasePronunFinishConfetti(getApplicationContext());
        }
    }


    @Override
    protected void onResume() {
        this.setScore();
        super.onResume();
    }

    private void isUserFinishAllStages() {
        //set level to get the no of questions
        initSpellings();
        showSpelling();
        // Check if user done all stages
        isFinish = this.checkIsUserDoneAllStage(
                this.Score,
                Integer.parseInt(this.User.getSpellingUserLevel()),
                this.Spellings.size()
        );

        // is expert level finish
        if (isFinish) {
            tLevel.setText(R.string.stages_done);
            if (!ConfettiHelper.isConfettiInSpellingAlreadyDisplay(this,User.Username)) {
                this.generateConfetti();
                SFXHelper.playMusic(getApplicationContext(),R.raw.level_up);
            }
        }
    }

    //display information according to user level
    private void giveMessageDependingOnLevel() {
        //get the spellings to easily calculate the no of questions that the user need to answer
        initSpellings();
        showSpelling();
        this.setScore(); //update the user score
        //check level of the user and give appropriate message
        if (User.getSpellingUserLevel().equals("1") || User.getSpellingUserLevel().equals("2")) {
            Message.show("You are in Beginner, you need to answer " + (int) Math.ceil(((this.Spellings.size() + 1) * .50) - Score) + " questions" +
                    " " +
                    "before you can jump to advance", Context);
        } else if (User.getSpellingUserLevel().equals("3") || User.getSpellingUserLevel().equals("4")) {
            Message.show("You are in Advance, you need to answer " + (int) Math.ceil(((this.Spellings.size() + 1) * 0.9) - Score) + " questions" +
                    " so you can jump to expert", Context);
        } else if (User.getSpellingUserLevel().equals("5") && this.Score < this.Spellings.size()) {
            Message.show("You are in Expert,  you need to answer " + (int) Math.ceil((this.Spellings.size()) - Score) + " questions" +
                    " " +
                    "to finish this level", Context);
        }
    }
    @Override
    public void onBackPressed() {
        this.initMain();
        return;
    }

    public void initMain() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra("username", User.Username);
        intent.putExtra("grammarUserLevel", User.GrammarUserLevel);
        intent.putExtra("pronunciationUserLevel", User.PronunciationUserLevel);
        intent.putExtra("spellingUserLevel", User.SpellingUserLevel);
        startActivity(intent);
        finish();
    }

    // Sets the name and the level of the user.
    private void setNameLevel(String userLevel) {
        String level = "";
        if (Integer.parseInt(userLevel) <= 2) level = this.Beginner;
        if (Integer.parseInt(userLevel) == 3 || Integer.parseInt(userLevel) == 4) level = this.Advance;
        if (Integer.parseInt(userLevel) == 5) level = this.Expert;
        this.tNameLevel.setText(String.format("%s: %s", this.User.Username, level));
    }

    // Sets the score.
    private void setScore() {

        if (this.Score <= this.SpellingsCount)
            //checking if the user has a previous score in session
            if (UserScoreHelper.isUserHasPreviousScoreInSpelling(this,UserScoreHelper.getLevel(),User.Username)) {
                this.Score = UserScoreHelper.getCurrentScoreInSpelling(this,UserScoreHelper.getLevel(),User.Username);
            } else { // rebase the score to 0
                this.Score = 0;
            }
//          this.tScore.setText(String.format("Score: %s / %s", this.Score.toString(), this.QuestionsCount.toString()));
        this.tScore.setText(String.format("Score: %s", this.Score.toString()));
    }

    // Gets a list of spelling files from the storage.
    // Returns a list of string - filename.
    private ArrayList<String> getSpellings() {
        ArrayList<String> spellings =  new ArrayList<>();
        if (!this.FileHelper.isExternalStorageAvailable() || this.FileHelper.isExternalStorageReadOnly()) {
            this.showMessage(Message.ERROR_OCCURRED);
        } else {
            try {
                String path = Objects.requireNonNull(getExternalFilesDir(this.FileHelper.Filepath + "/" + this.CurrentSpellingFolder)).toString();
                File directory = new File(path);
                File[] files = directory.listFiles();
                for (File file : files) {
                    String fileName = file.getName();
                    if (fileName.indexOf(".") > 0)
                        fileName = fileName.substring(0, fileName.lastIndexOf("."));

                    spellings.add(fileName.trim());
                }
            } catch (Exception e) {
                this.showMessage(Message.ERROR_OCCURRED);
            }
        }

        return spellings;
    }

    // Shows a modal message.
    private void showMessage(String message) {
        Message.show(message, this.Context);
    }

    // Initializes the spelling files.
    private void initSpellings() {
        this.Spellings = this.getSpellings();
        if (!this.Spellings.isEmpty()) {
            Collections.sort(this.Spellings);
        } else {
            this.showMessage(Message.ERROR_OCCURRED);
        }
    }

    // Shows a question from the spelling.
    private void showSpelling() {
        // Check if questions object is not empty.
        if (!this.Spellings.isEmpty()) {
            this.SpellingsCount = this.Spellings.size();
            // Generate a random integer between 0 and the length of the audio files.
            // The result will be used as the id of the item.
            int id = RandomHelper.generateRandomNumber(this.SpellingsCount);
            // Get a single audio based on the unique id.
            final String audio = this.Spellings.get(id);
            // Add event listeners to the buttons.
            this.bPlay.setOnClickListener(view -> playAudio(audio));
            this.bCheck.setOnClickListener(view -> {
                String text = eAnswer.getText().toString().trim();
                checkAnswer(text, audio);
                // Show another question.
                showSpelling();
                eAnswer.setText("");
            });
            // Add event listeners to the buttons.
        } else {
            this.showMessage(Message.ERROR_OCCURRED);
        }
    }

    // Initializes the items in the spinner.
    private void initLevels() {
        String[] items = new String[]{this.Beginner, this.Advance, this.Expert};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        this.sMode.setAdapter(adapter);
    }

    // Checks for the correct answer.
    public void checkAnswer(String inputAnswer, String correctAnswer) {
        String msg;
        boolean isShowLevelUpMsg = false;
        boolean isCorrect = inputAnswer.trim().toUpperCase().equals(correctAnswer.trim().toUpperCase());
        if (isCorrect) {
            msg = "Very good, that is correct!";
            // Add score.
            this.Score++;
            //music for correct answer
            SFXHelper.playMusic(getApplicationContext(),R.raw.correct);
            //tracking the score of the user depending on level
            UserScoreHelper.addCurrentScoreInSpelling(this,UserScoreHelper.getLevel(),User.Username);
            this.setScore();
        }  else {
            msg = "Sorry, that is incorrect. The correct answer is " + correctAnswer + ".";
            this.WrongAnswers++;
            //music for wrong answer
            SFXHelper.playMusic(getApplicationContext(),R.raw.wrong);
            //track and set wrong answer

            //TODO uncomment this after development mode
            //add mistake to user
//            GameOverHelper.addMistake(this,User.Username,User.getSpellingUserLevel());
        }

        //checking if the user is game over or not
        if (GameOverHelper.isUserGameOver(this,User.Username,User.getSpellingUserLevel())) {
            int noOfMistakes = GameOverHelper.getUserMistake(this,User.Username,User.getSpellingUserLevel());
            Toast.makeText(this, "Game over no. of mistake : " + String.valueOf(noOfMistakes), Toast.LENGTH_SHORT).show();
            //rebase the mistakes count of the user
            GameOverHelper.rebaseUserMistakes(this);
            //rebase the current score of the user in shared pref
            UserScoreHelper.rebaseUserScore(this);
        }


        // Determine level up.
        if (this.isLevelUp()) {
            isShowLevelUpMsg = true;
            // Update user level of the user profile.
            Integer newLevel = this.levelUpUser();
            this.User.setSpellingUserLevel(newLevel.toString());
            this.setNameLevel(newLevel.toString());
            // Sets the level selection.
            this.setLevelSelection(this.User.SpellingUserLevel);
        }

        //to avoid double level up I add this condition
        if (isShowLevelUpMsg) {
            //we also need to set a new level for user while answering some questions
            UserScoreHelper.setLevel(Integer.parseInt(User.getSpellingUserLevel()));
            switch(currentLevel) {

                case 2:
                    this.giveMessageDependingOnLevel();
                    RandomHelper.rebaseListNumber();
                    SFXHelper.playMusic(getApplicationContext(),R.raw.level_up);
                    break;

                case 4:
                    this.giveMessageDependingOnLevel();
                    RandomHelper.rebaseListNumber();
                    SFXHelper.playMusic(getApplicationContext(),R.raw.level_up);
                    break;

                case 5:
                    this.giveMessageDependingOnLevel();
                    RandomHelper.rebaseListNumber();
                    SFXHelper.playMusic(getApplicationContext(),R.raw.level_up);
                    break;
               }

        }
        //rebase the current score of the user since promoted to next level
        this.setScore();
        Message.show(msg, this.Context);

        // Set current level for a user in UI
        this.displaySetLevel();
        this.isUserFinishAllStages();




        // Show another question.
        //this.showSpelling();
    }

    private void generateConfetti() {
        viewKonfetti.build()
                .addColors(Color.parseColor("#ffff00"), Color.parseColor("#0000FF"),Color.parseColor("#ff0000"))
                .setDirection(50, 359.0)
                .setSpeed(1f, 5f)
                .setFadeOutEnabled(true)
                .setTimeToLive(2000L)
                .addShapes(Shape.CIRCLE)
                .addSizes(new Size(13, 5f))
                .setPosition(-50f, viewKonfetti.getWidth() + 50f, -50f, -50f)
                .streamFor(300, 5000L);
                ConfettiHelper.setSpellingConfettiAlreadyDisplayed(this,User.Username);
    }

    // Determines if the user if for level up.
    private boolean isLevelUp() {
        boolean isLevel = false;
        int userLevel = Integer.parseInt(this.User.getSpellingUserLevel());
        int selectedLevel = this.sMode.getSelectedItemPosition();
        String selectedLevelName = this.sMode.getItemAtPosition(selectedLevel).toString();
        currentLevel = userLevel;
        String userLevelName = "";
        if (userLevel <= 2) userLevelName = this.Beginner;
        if (userLevel == 3 || userLevel == 4) userLevelName = this.Advance;
        if (userLevel == 5) userLevelName = this.Expert;

        // The level of the user must be equal to the selected level in order to level up the user.
        if (selectedLevelName.trim().toUpperCase().equals(userLevelName.trim().toUpperCase())) {
            //int actualScore = this.Score - this.WrongAnswers;
            int actualScore = this.Score;
            double levelTwoRef = (this.SpellingsCount * 0.25);
            double levelThreeRef = (this.SpellingsCount * 0.5);
            double levelFourRef = (this.SpellingsCount * 0.7);
            double levelFiveRef = (this.SpellingsCount * 0.9);

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
        Integer newUserLevel = Integer.parseInt(this.User.SpellingUserLevel);
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

                    Integer userLevel = Integer.parseInt(record.get(7).trim()); // spelling user level
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
                    newUserProfile.append(record.get(5).trim()); // grammar user level
                    newUserProfile.append(",");
                    newUserProfile.append(record.get(6).trim()); // pronunciation user level
                    newUserProfile.append(",");
                    newUserProfile.append(userLevel.toString()); // spelling user level
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

    // Plays the audio file based on the selected item.
    private void playAudio(String item) {
        MediaPlayer mPlayer = new MediaPlayer();
        final String filename = Objects.requireNonNull(getExternalFilesDir(this.FileHelper.Filepath
                + "/" + this.CurrentSpellingFolder + "/" + item + ".mp3")).toString();
        try {
            mPlayer.setDataSource(this, Uri.parse(filename));
            mPlayer.prepare();
            mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(final MediaPlayer mp) {
                    mp.start();
                }
            });
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(final MediaPlayer mp) {
                    mp.stop();
                    mp.reset();
                    mp.release();
                }
            });
        } catch (Exception e) {
            this.showMessage(Message.ERROR_OCCURRED);
        }
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

    private void displaySetLevel() {

        int currentUserLevel = UserLevelHelper
                .currentLevelOfUser(Integer.parseInt(this.User.getSpellingUserLevel()));
        tLevel.setText(String.format(Locale.getDefault(),"Level : %d",currentUserLevel));
    }

    private boolean checkIsUserDoneAllStage(int currentScore , int currentLevel , int noOfQuestions) {
        return currentScore >= noOfQuestions && currentLevel == 5;
    }
}
