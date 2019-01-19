package com.pointsph.edgame;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.ArrayMap;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

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
import java.util.Objects;
import java.util.Random;

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
    private ListView lAudio;
    private Spinner sMode;
    private Button bPlay;
    private Button bChoiceA;
    private Button bChoiceB;
    private Button bHome;
    private ConstraintLayout mainContainer;

    private ArrayList<String> AudioFiles;
    private String CurrentPronunciationFolder = "";

    private Integer AudioFilesCount = 0;
    private Integer Score = 0;
    private Integer WrongAnswers = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pronunciation);

        this.FileHelper = new FileHelper(this);
        this.User = new User();
        this.Context = this;
        this.AudioFiles = new ArrayList<>();

        this.tNameLevel = findViewById(R.id.lblNameLevel);
        //this.lAudio = findViewById(R.id.audio_list);
        this.tScore = findViewById(R.id.lblScore);
        this.sMode = findViewById(R.id.spinner_mode);
        this.bPlay = findViewById(R.id.button_play);
        this.bChoiceA = findViewById(R.id.button_a);
        this.bChoiceB = findViewById(R.id.button_b);
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

        // Default the pronunciation folder to the beginner folder.
        this.CurrentPronunciationFolder = this.FileHelper.PronunciationBeginnerFolder;
        this.setNameLevel(this.User.PronunciationUserLevel);
        this.initLevels();
        // Sets the level selection.
        this.setLevelSelection(this.User.PronunciationUserLevel);

        /*try {
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
                    CurrentPronunciationFolder = FileHelper.PronunciationBeginnerFolder;
                }

                if (level.equals(Advance)) {
                    if (User.PronunciationUserLevel.equals("1") || User.PronunciationUserLevel.equals("2")) {
                        Message.show("Level not yet reached.", Context);
                        sMode.setSelection(position - 1);
                        CurrentPronunciationFolder = FileHelper.PronunciationBeginnerFolder;
                    } else {
                        CurrentPronunciationFolder = FileHelper.PronunciationAdvanceFolder;
                    }
                }

                if (level.equals(Expert)) {
                    if (User.PronunciationUserLevel.equals("1") || User.PronunciationUserLevel.equals("2")) {
                        Message.show("Level not yet reached.", Context);
                        sMode.setSelection(position - 2);
                        CurrentPronunciationFolder = FileHelper.PronunciationBeginnerFolder;
                    } else if (User.PronunciationUserLevel.equals("3") || User.PronunciationUserLevel.equals("4")) {
                        Message.show("Level not yet reached.", Context);
                        sMode.setSelection(position - 1);
                        CurrentPronunciationFolder = FileHelper.PronunciationAdvanceFolder;
                    } else {
                        CurrentPronunciationFolder = FileHelper.PronunciationExpertFolder;
                    }
                }

                initAudioFiles();
                showQuestion();
                Score = 0;
                setScore();
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
        intent.putExtra("firstName", User.FirstName);
        intent.putExtra("lastName", User.LastName);
        intent.putExtra("birthday", User.Birthday);
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
            Random rnd = new Random();
            // Generate a random integer between 0 and the length of the audio files.
            // The result will be used as the id of the item.
            int id = rnd.nextInt(this.AudioFilesCount);
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
            this.bPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    playAudio(audio);
                }
            });
            this.bChoiceA.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String text = bChoiceA.getText().toString().trim();
                    checkAnswer(text, correctAnswer);
                }
            });
            this.bChoiceB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String text = bChoiceB.getText().toString().trim();
                    checkAnswer(text, correctAnswer);
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
            //music for correct answer
            SFXHelper.playMusic(getApplicationContext(),R.raw.correct);
        }  else {
            msg = "Sorry, that is incorrect. The correct answer is " + answer + ".";
            this.WrongAnswers++;
            //music for wrong answer
            SFXHelper.playMusic(getApplicationContext(),R.raw.wrong);
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

        if (isShowLevelUpMsg) {
            //music for user level up
            SFXHelper.playMusic(getApplicationContext(),R.raw.level_up);
            Message.show("Congratulations! Level-up acquired!", this.Context);
        }

        Message.show(msg, this.Context);

        // Show another question.
        this.showQuestion();
    }

    // Determines if the user if for level up.
    private boolean isLevelUp() {
        boolean isLevel = false;
        int userLevel = Integer.parseInt(this.User.getPronunciationUserLevel());
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
            this.tScore.setText(String.format("Score: %s / %s", this.Score.toString(), this.AudioFilesCount.toString()));
    }

    // Sets the name and the level of the user.
    private void setNameLevel(String userLevel) {
        String level = "";
        if (Integer.parseInt(userLevel) <= 2) level = this.Beginner;
        if (Integer.parseInt(userLevel) == 3 || Integer.parseInt(userLevel) == 4) level = this.Advance;
        if (Integer.parseInt(userLevel) == 5) level = this.Expert;
        this.tNameLevel.setText(String.format("%s: %s", this.User.FirstName, level));
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
}
