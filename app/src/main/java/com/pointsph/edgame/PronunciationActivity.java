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
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.pointsph.edgame.Helpers.ConfettiHelper;
import com.pointsph.edgame.Helpers.GameOverHelper;
import com.pointsph.edgame.Helpers.RandomHelper;
import com.pointsph.edgame.Helpers.SFXHelper;
import com.pointsph.edgame.Helpers.UserLevelHelper;
import com.pointsph.edgame.Helpers.UserScoreHelper;
import com.pointsph.edgame.Helpers.UserStatusHelper;
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

public class PronunciationActivity extends AppCompatActivity {

    private FileHelper FileHelper;
    private User User;
    private Context Context;

    private String Beginner = "Beginner";
    private String Advance = "Advance";
    private String Expert = "Expert";

    // UI References
    private TextView tNameLevel;
    private TextView tScore;
    private TextView tLevel;
    private ListView lAudio;
    private Spinner sMode;
    private Button bPlay;
    private Button bChoiceA;
    private Button bChoiceB;
    private Button bHome;
    private FrameLayout mainContainer;

    private ArrayList<String> AudioFiles;
    private String CurrentPronunciationFolder = "";

    private Integer AudioFilesCount = 0;
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
        setContentView(R.layout.activity_pronunciation);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        this.FileHelper = new FileHelper(this);
        this.User = new User();
        this.Context = this;
        this.AudioFiles = new ArrayList<>();




        this.tNameLevel = findViewById(R.id.lblNameLevel);
        //this.lAudio = findViewById(R.id.audio_list);
        this.tLevel = findViewById(R.id.level);
        this.tScore = findViewById(R.id.lblScore);
        this.sMode = findViewById(R.id.spinner_mode);
        this.bPlay = findViewById(R.id.button_play);
        this.bChoiceA = findViewById(R.id.button_a);
        this.bChoiceB = findViewById(R.id.button_b);
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

        // Default the pronunciation folder to the beginner folder.
        this.CurrentPronunciationFolder = this.FileHelper.PronunciationBeginnerFolder;
        this.initLevels();
        this.setNameLevel(this.User.PronunciationUserLevel);

        // Sets the level selection.
        this.setLevelSelection(this.User.PronunciationUserLevel);

        // Set current level for a user in UI
        this.displaySetLevel();

        // Sets an event listener for the home button.
        this.bHome.setOnClickListener(view -> initMain());

        //when first open we need to set the level of the user
        UserScoreHelper.setLevel(Integer.parseInt(User.getPronunciationUserLevel()));

        this.sMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Levels:
                // 1 and 2 = Beginner
                // 3 and 4 = Advance
                // 5 = Expert
                String level = parent.getItemAtPosition(position).toString();

                if (level.equals(Beginner)) {
                    CurrentPronunciationFolder = FileHelper.PronunciationBeginnerFolder;
                }

                if (level.equals(Advance)) {
                    if (User.PronunciationUserLevel.equals("1") || User.PronunciationUserLevel.equals("2")) {
                        Message.show("Level not yet reached. you need to answer " + (int) Math.ceil(((AudioFiles.size()+1) * .50) - Score) + " questions" +
                                " " +
                                "before you can jump to advance", Context);
                        isBackward = false;
                        sMode.setSelection(position - 1);
                        CurrentPronunciationFolder = FileHelper.PronunciationBeginnerFolder;
                    } else {
                        isBackward = true;
                        CurrentPronunciationFolder = FileHelper.PronunciationAdvanceFolder;
                    }
                }

                if (level.equals(Expert)) {
                    if (User.PronunciationUserLevel.equals("1") || User.PronunciationUserLevel.equals("2")) {
                        Message.show("Level not yet reached. you need to answer " + (int) Math.ceil(((AudioFiles.size()+1) * .50) - Score)  + "" +
                                " questions to proceed in advance then answer " + (int) Math.ceil((AudioFiles.size()+1) * 0.9) + " questions" +
                                " so you can jump to expert", Context);
                        isBackward = false;
                        sMode.setSelection(position - 2);
                        CurrentPronunciationFolder = FileHelper.PronunciationBeginnerFolder;
                    } else if (User.PronunciationUserLevel.equals("3") || User.PronunciationUserLevel.equals("4")) {
                        Message.show("Level not yet reached. you need to answer " + (int) Math.ceil(((AudioFiles.size()+1) * 0.9) - Score) + " questions" +
                                " " +
                                "before you can jump to expert", Context);
                        isBackward = false;
                        sMode.setSelection(position - 1);
                        CurrentPronunciationFolder = FileHelper.PronunciationAdvanceFolder;
                    } else {
                        CurrentPronunciationFolder = FileHelper.PronunciationExpertFolder;
                        isBackward = true;
                    }
                }

                initAudioFiles();
                showQuestion();
                Score = 0;
                setScore();

                //get the user choose
                //compare to it's current level
                boolean itIsEqualToCurrentLevel = level.equals(UserScoreHelper.convertLevelToWord(Integer.parseInt(User.getPronunciationUserLevel())));
                //if the not equal to current level perform the action
                if  (!itIsEqualToCurrentLevel && isBackward) {
                    //set UserScoreHelper level to = what the user choose
                    UserScoreHelper.setLevel(UserScoreHelper.convertWordToLevel(level));
                    //rebase the user score on that particular level
                    UserScoreHelper.setCurrentScoreInPronunciation(Context,UserScoreHelper.getLevel(),User.Username,0);
                    setScore();
                    initAudioFiles();
                    showQuestion();
                } else {
                    UserScoreHelper.setLevel(Integer.parseInt(User.getPronunciationUserLevel()));
                    setScore();
                    initAudioFiles();
                    showQuestion();
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
        giveMessageDependingOnLevel();
        //checking if the user finish all the stages
        isUserFinishAllStages();



    }

    /**
     * Checking if the user press the home button of his/her phone on pronunciation
     */
    private void isUserPressHomeButton() {
       if (Context instanceof PronunciationActivity) {
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

    @Override
    public void onBackPressed() {
        this.initMain();
        return;
    }

    //display information according to user level
    private void giveMessageDependingOnLevel() {
        //get the pronunciation to easily calculate the no of questions that the user need to answer
        this.setScore(); //update the user score
        CurrentPronunciationFolder = "Pronunciation/".concat(UserScoreHelper.getLevel());
        initAudioFiles();


        //check level of the user and give appropriate message
        if (User.getPronunciationUserLevel().equals("1") || User.getPronunciationUserLevel().equals("2")) {
            Message.show("You are in Beginner, you need to answer " + (int) Math.ceil(((this.AudioFiles.size() + 1) * .50) -  Score) + " questions" +
                    " " +
                    "before you can jump to advance", Context);
        } else if (User.getPronunciationUserLevel().equals("3") || User.getPronunciationUserLevel().equals("4")) {
            Message.show("You are in Advance, you need to answer " + (int) Math.ceil(((this.AudioFiles.size() + 1) * 0.9) - Score) + "" +
                    " questions so you can jump to expert", Context);
        } else if (User.getPronunciationUserLevel().equals("5") && this.Score < this.AudioFiles.size()) {
            Message.show("You are in Expert,  you need to answer " + (int) Math.ceil(((this.AudioFiles.size())) - Score) + " questions" +
                    " " +
                    "to finish this level", Context);
        }
    }

    private void isUserFinishAllStages() {
        initAudioFiles();
        showQuestion();

        isFinish = this.checkIsUserDoneAllStage(
                this.Score,
                Integer.parseInt(this.User.getPronunciationUserLevel()),
                this.AudioFilesCount
        );


        // is expert level finish
        if (isFinish) {
            tLevel.setText(R.string.stages_done);
            if (!ConfettiHelper.isConfettiInPronunciationAlreadyDisplay(this,User.Username)) {
                this.generateConfetti();
                SFXHelper.playMusic(getApplicationContext(),R.raw.level_up);
            }
        }
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

    // Shows the audio files in the list view.
    private void showAudioFiles() {
        // You need three parameters 'the context, id of the layout (it will be where the data is shown),
        // and the array that contains the data
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, this.AudioFiles);
        // Here, you set the data in your ListView
        this.lAudio.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    // Shows a modal message.
    private void showMessage(String message) {
        Message.show(message, this.Context);
    }

    // Redirects to the main.
    private void redirectToMain() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra("username", User.Username);
        intent.putExtra("userLevel", User.PronunciationUserLevel);
        startActivity(intent);
        finish();
    }

    // Gets a list of audio files from the storage.
    // Returns a list of string - filename.
    private ArrayList<String> getAudioFiles() {
        ArrayList<String> audioFiles =  new ArrayList<>();
        if (!this.FileHelper.isExternalStorageAvailable() || this.FileHelper.isExternalStorageReadOnly()) {
            this.showMessage(Message.ERROR_OCCURRED);
        } else {
            try {
                String path = Objects.requireNonNull(getExternalFilesDir(this.FileHelper.Filepath + "/" + this.CurrentPronunciationFolder)).toString();
                File directory = new File(path);
                File[] files = directory.listFiles();
                for (File file : files) {
                    String fileName = file.getName();
                    if (fileName.indexOf(".") > 0)
                        fileName = fileName.substring(0, fileName.lastIndexOf("."));

                    audioFiles.add(fileName.trim());
                }
            } catch (Exception e) {
                this.showMessage(Message.ERROR_OCCURRED);
            }
        }

        return audioFiles;
    }

    // Plays the audio file based on the selected item.
    private void playAudio(String item) {
        MediaPlayer mPlayer = new MediaPlayer();
        final String filename = Objects.requireNonNull(getExternalFilesDir(this.FileHelper.Filepath
                + "/" + this.CurrentPronunciationFolder + "/" + item + ".mp3")).toString();
        try {
            mPlayer.setDataSource(this, Uri.parse(filename));
            mPlayer.prepare();
            mPlayer.setOnPreparedListener(MediaPlayer::start);
            mPlayer.setOnCompletionListener(mp -> {
                mp.stop();
                mp.reset();
                mp.release();
            });
        } catch (Exception e) {
            this.showMessage(Message.ERROR_OCCURRED);
        }
    }

    // Initializes the items in the spinner.
    private void initLevels() {
        String[] items = new String[]{"Beginner", "Advance", "Expert"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        this.sMode.setAdapter(adapter);
    }

    // Initializes the audio files.
    private void initAudioFiles() {
        this.AudioFiles = this.getAudioFiles();
        if (!this.AudioFiles.isEmpty()) {
            Collections.sort(this.AudioFiles);
            //this.showAudioFiles();

            //this.showQuestion();
        } else {
            this.showMessage(Message.ERROR_OCCURRED);
        }
    }

    private void showQuestion() {
        // Check if questions object is not empty.
        if (!this.AudioFiles.isEmpty()) {
            this.AudioFilesCount = this.AudioFiles.size();
//            Random rnd = new Random();
            // Generate a random integer between 0 and the length of the audio files.
            // The result will be used as the id of the item.
            int id = RandomHelper.generateRandomNumber(this.AudioFilesCount);
            // Get a single audio based on the unique id.
            final String audio = this.AudioFiles.get(id);
            // Split the filename of the audio.
            // The format of the file is "correct_wrong".
            final String correctAnswer = audio.substring(0, audio.indexOf("_"));
            String textA = correctAnswer;
            String textB = audio.substring(audio.lastIndexOf("_") + 1);
            // Generate a random number between 0 and 11.
            // Choice a = 0 - 5
            // Choice b = 6 - 11
            Random random = new Random();
            int a = random.nextInt(10);
            if (a <= 5) {
                this.bChoiceA.setText(textA);
                this.bChoiceB.setText(textB);
            } else {
                this.bChoiceA.setText(textB);
                this.bChoiceB.setText(textA);
            }

            // Add event listeners to the buttons.
            this.bPlay.setOnClickListener(view -> playAudio(audio));
            this.bChoiceA.setOnClickListener(view -> {
                String text = bChoiceA.getText().toString().trim();
                checkAnswer(text, correctAnswer);
            });
            this.bChoiceB.setOnClickListener(view -> {
                String text = bChoiceB.getText().toString().trim();
                checkAnswer(text, correctAnswer);
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

        this.setScore(); //update the user score

        if (isCorrect) {
            msg = "Very good, that is correct!";
            // Add score.
            this.Score++;
            //music for correct answer
            SFXHelper.playMusic(getApplicationContext(),R.raw.correct);
            //track and set correct answer depending on level
            UserScoreHelper.addCurrentScoreInPronunciation(this,UserScoreHelper.getLevel(),User.Username);
            this.setScore();
        }  else {
            msg = "Sorry, that is incorrect. The correct answer is " + answer + ".";
            this.WrongAnswers++;
            //music for wrong answer
            SFXHelper.playMusic(getApplicationContext(),R.raw.wrong);

            //TODO uncomment this after development mode
            //add mistake to user
            //GameOverHelper.addMistake(this,User.Username,User.getPronunciationUserLevel());
        }

        //checking if the user is game over or not
        if (GameOverHelper.isUserGameOver(this,User.Username,User.getPronunciationUserLevel())) {
            int noOfMistakes = GameOverHelper.getUserMistake(this,User.Username,User.getPronunciationUserLevel());
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
            this.User.setPronunciationUserLevel(newLevel.toString());
            this.setNameLevel(newLevel.toString());
            // Sets the level selection.
            this.setLevelSelection(this.User.PronunciationUserLevel);
       }

        //to avoid double level up I add this condition
        if (isShowLevelUpMsg) {
            //we also need to set a new level for user while answering some questions
            UserScoreHelper.setLevel(Integer.parseInt(User.getPronunciationUserLevel()));

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
        this.showQuestion();
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
                ConfettiHelper.setPronunciationAlreadyDisplayed(this,User.Username);
    }

    // Determines if the user if for level up.
    private boolean isLevelUp() {
        boolean isLevel = false;
        int userLevel = Integer.parseInt(this.User.getPronunciationUserLevel());
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
            double levelTwoRef = (this.AudioFilesCount * 0.25);
            double levelThreeRef = (this.AudioFilesCount * 0.5);
            double levelFourRef = (this.AudioFilesCount * 0.7);
            double levelFiveRef = (this.AudioFilesCount * 0.9);

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

    // Sets the score.
    private void setScore() {
        if (this.Score <= this.AudioFilesCount)
            //checking if the user has a previous score in session
            if (UserScoreHelper.isUserHasPreviousScoreInPronunciation(this,UserScoreHelper.getLevel(),User.Username)) {
                this.Score = UserScoreHelper.getCurrentScoreInPronunciation(this,UserScoreHelper.getLevel(),User.Username);
            } else { // rebase the score to 0
                this.Score = 0;
            }
//          this.tScore.setText(String.format("Score: %s / %s", this.Score.toString(), this.QuestionsCount.toString()));
        this.tScore.setText(String.format("Score: %s", this.Score.toString()));
    }

    // Sets the name and the level of the user.
    private void setNameLevel(String userLevel) {
        String level = "";
        if (Integer.parseInt(userLevel) <= 2) level = this.Beginner;
        if (Integer.parseInt(userLevel) == 3 || Integer.parseInt(userLevel) == 4) level = this.Advance;
        if (Integer.parseInt(userLevel) == 5) level = this.Expert;
        this.tNameLevel.setText(String.format("%s: %s", this.User.Username, level));
    }

    // Level up the user.
    private Integer levelUpUser() {
        Integer newUserLevel = Integer.parseInt(this.User.PronunciationUserLevel);
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

                    Integer userLevel = Integer.parseInt(record.get(6).trim()); // pronunciation user level
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
                    newUserProfile.append(userLevel.toString()); // pronunciation user level
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

    private void displaySetLevel() {

        int currentUserLevel = UserLevelHelper
                .currentLevelOfUser(Integer.parseInt(this.User.getPronunciationUserLevel()));
        tLevel.setText(String.format(Locale.getDefault(),"Level : %d",currentUserLevel));
    }

    private boolean checkIsUserDoneAllStage(int currentScore , int currentLevel , int noOfQuestions) {
        return currentScore >= noOfQuestions && currentLevel == 5;
    }
}
