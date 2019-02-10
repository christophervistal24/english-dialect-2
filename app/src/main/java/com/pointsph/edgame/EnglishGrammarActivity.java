package com.pointsph.edgame;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.ArrayMap;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;


import com.pointsph.edgame.Helpers.ConfettiHelper;
import com.pointsph.edgame.Helpers.GameOverHelper;
import com.pointsph.edgame.Helpers.RandomHelper;
import com.pointsph.edgame.Helpers.SFXHelper;
import com.pointsph.edgame.Helpers.UserLevelHelper;
import com.pointsph.edgame.Helpers.UserScoreHelper;
import com.pointsph.edgame.Services.BackgroundMusic;
import com.pointsph.edgame.SharedPref.SharedPreferenceHelper;
import com.pointsph.edgame.Watcher.HomeWatcher;
import com.shashank.sony.fancydialoglib.Animation;
import com.shashank.sony.fancydialoglib.FancyAlertDialog;
import com.shashank.sony.fancydialoglib.Icon;

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
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;

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
    private TextView lblWrong;
    private TextView correctAnswers;
    private TextView wrongAnswers;
    private TextView tLevel;
    private Button bChoiceA;
    private Button bChoiceB;
    private Button bChoiceC;
    private Button bChoiceD;
    private Button bHome;
    private ToggleButton soundStatus;
    private FrameLayout mainContainer;

    // This will serve as the unique id and sequence number of each question.
    private Integer Id = 0;
    // Key-value pair variable that will store the questionnaires.
    // The key is the unique id.
    // The value is the ArrayList<String> for the question, choices, and answer.
    private ArrayMap<Integer, ArrayList<String>> Questions = new ArrayMap<>();

    private Integer QuestionsCount = 0;
    private Integer Score = 0;
    private Integer WrongAnswers = 0;
    private static int currentLevel = 0;

    private boolean isFinish = false;
    private boolean isBackward = true;
    FancyAlertDialog.Builder messageDialog;


    private boolean mIsBound = false;
    private BackgroundMusic mServ;
    HomeWatcher mHomeWatcher;

    private nl.dionsegijn.konfetti.KonfettiView viewKonfetti;


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
      this.setScore();
      this.setUserMistakes();
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
        this.correctAnswers = findViewById(R.id.correctAnswers);
        this.wrongAnswers = findViewById(R.id.wrongAnswers);
        this.tLevel         = findViewById(R.id.level);
        this.tScore = findViewById(R.id.lblScore);
        this.sMode = findViewById(R.id.spinner_mode);
        this.bChoiceA = findViewById(R.id.button_a);
        this.bChoiceB = findViewById(R.id.button_b);
        this.bChoiceC = findViewById(R.id.button_c);
        this.bChoiceD = findViewById(R.id.button_d);
        this.bHome = findViewById(R.id.button_home);
        this.soundStatus = findViewById(R.id.soundStatus);
        this.mainContainer = findViewById(R.id.main_container);
        this.viewKonfetti = findViewById(R.id.viewKonfetti);
        this.lblWrong = findViewById(R.id.lblWrong);
        messageDialog = new FancyAlertDialog.Builder(this);

        //rebase the array list
        if  (!RandomHelper.arl.isEmpty()) {
            RandomHelper.rebaseListNumber();
        }

        //listener if the user press the home button
        mHomeWatcher = new HomeWatcher(Context);
        mHomeWatcher.setOnHomePressedListener(new HomeWatcher.OnHomePressedListener() {
            @Override
            public void onHomePressed() { isUserPressHomeButton(); }
            @Override
            public void onHomeLongPressed() { isUserPressHomeButton(); }
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



        /*
        * Check the user option for background music ADDED by Vistal
        * */
        this.initLevels();

        // Sets the level of the user.
        this.setNameLevel(this.User.GrammarUserLevel);

        // Sets the level selection.
        this.setLevelSelection(this.User.GrammarUserLevel);

        //checking the state of background music
        this.checkUserSoundOption();

        //logic for on/off background music when switch button pressed
        soundStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferenceHelper.PREF_FILE = "bg_music";
            if (isChecked) {
                //resume background music
                bindAndPlayMusic();
                SharedPreferenceHelper.setSharedPreferenceBoolean(getApplicationContext(),User.Username+"sound",true);
                soundStatus.setBackgroundResource(R.drawable.ic_volume_up_black_24dp);
            } else {
                //stop background music
                doUnbindService();
                if (BackgroundMusic.mPlayer != null) {
                    if (BackgroundMusic.mPlayer.isPlaying()) {
                        BackgroundMusic.mPlayer.pause();
                        SharedPreferenceHelper.setSharedPreferenceBoolean(getApplicationContext(),User.Username+"sound",false);
                        soundStatus.setBackgroundResource(R.drawable.ic_volume_off_black_24dp);
                    }
                }
            }
        });



        // Set current level for a user in UI
        this.displaySetLevel();


        // Sets an event listener for the home button.
        this.bHome.setOnClickListener(view -> initMain());


        //when first open we need to set the level of the user
        //to easily get the previous score of the user base on level
        UserScoreHelper.setLevel(Integer.parseInt(User.getGrammarUserLevel()));

        this.sMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Levels:
                // 1 and 2 = Beginner
                // 3 and 4 = Advance
                // 5 = Expert
                String level = parent.getItemAtPosition(position).toString();

                if (level.equals(Beginner)) {
                    getQuestionnaires(level);
                    showQuestion();
                    Score = 0;
                    setScore();
                    if(User.GrammarUserLevel.equals("5")) {
                        //set UserScoreHelper level to = what the user choose
                        UserScoreHelper.setLevel(UserScoreHelper.convertWordToLevel(level));
                        //rebase the no of mistakes in UI
                        GameOverHelper.rebaseUserMistakesInLevel(Context,User.Username,UserScoreHelper.getLevel(),"grammar");
                        //rebase the user score on that particular level
                        UserScoreHelper.setCurrentScoreInGrammar(Context,UserScoreHelper.getLevel(),User.Username,0);
                        setScore();
                        getQuestionnaires(UserScoreHelper.getLevel());
                        showQuestion();
                    }
                }

                if (level.equals(Advance)) {
                    if (User.GrammarUserLevel.equals("1") || User.GrammarUserLevel.equals("2")) {
                        messageDialog
                                .setTitle("I N F O R M A T I O N")
                                .setBackgroundColor(Color.parseColor("#303F9F"))
                                .setMessage("Level not yet reached. you need to get " + (int) Math.ceil(((QuestionsCount+1) * .50) - Score) + " correct answer" +
                                        " " +
                                        "before you can jump to advance")
                                .setNegativeBtnText("")
                                .setNegativeBtnBackground(Color.parseColor("#00141312"))  //Don't pass R.color.colorvalue
                                .setPositiveBtnBackground(Color.parseColor("#FF4081"))  //Don't pass R.color.colorvalue
                                .setPositiveBtnText("OK")
                                .setAnimation(Animation.POP)
                                .isCancellable(false)
                                .setIcon(R.drawable.ic_chat_black_24dp, Icon.Visible)
                                .build();
                        isBackward = false;
                        sMode.setSelection(position - 1);
                    } else if(User.GrammarUserLevel.equals("5")) {
                        //set UserScoreHelper level to = what the user choose
                        UserScoreHelper.setLevel(UserScoreHelper.convertWordToLevel(level));
                        //rebase the no of mistakes in UI
                        GameOverHelper.rebaseUserMistakesInLevel(Context,User.Username,UserScoreHelper.getLevel(),"grammar");
                        //rebase the user score on that particular level
                        UserScoreHelper.setCurrentScoreInGrammar(Context,UserScoreHelper.getLevel(),User.Username,0);
                        setScore();
                        getQuestionnaires(UserScoreHelper.getLevel());
                        showQuestion();
                    } else {
                        // Get the questionnaires based on the selected mode.
                        getQuestionnaires(level);
                        showQuestion();
                        Score = 0;
                        setScore();
                        isBackward = true;
                    }
                }

                if (level.equals(Expert)) {
                    if (User.GrammarUserLevel.equals("1") || User.GrammarUserLevel.equals("2")) {
                        messageDialog
                                .setTitle("I N F O R M A T I O N")
                                .setBackgroundColor(Color.parseColor("#303F9F"))
                                .setMessage("Level not yet reached. you need to get " + (int) Math.ceil(((QuestionsCount+1) * .50) - Score)  + "" +
                                        " correct answer to proceed in advance then you also need to get " + (int) Math.ceil((QuestionsCount+1) * 0.9) + " correct answer" +
                                        " so you can jump to expert")
                                .setNegativeBtnText("")
                                .setNegativeBtnBackground(Color.parseColor("#00141312"))  //Don't pass R.color.colorvalue
                                .setPositiveBtnBackground(Color.parseColor("#FF4081"))  //Don't pass R.color.colorvalue
                                .setPositiveBtnText("OK")
                                .setAnimation(Animation.POP)
                                .isCancellable(false)
                                .setIcon(R.drawable.ic_chat_black_24dp, Icon.Visible)
                                .build();
                        isBackward = false;
                        sMode.setSelection(position - 2);
                    } else if (User.GrammarUserLevel.equals("3") || User.GrammarUserLevel.equals("4")) {
//                        Message.show("Advance to expert warning message.", Context);
                        messageDialog
                                .setTitle("I N F O R M A T I O N")
                                .setBackgroundColor(Color.parseColor("#303F9F"))
                                .setMessage("Level not yet reached. you need to get " + (int) Math.ceil(((QuestionsCount+1) * 0.9) - Score) + " correct answer" +
                                        " " +
                                        "before you can jump to expert")
                                .setNegativeBtnText("")
                                .setNegativeBtnBackground(Color.parseColor("#00141312"))  //Don't pass R.color.colorvalue
                                .setPositiveBtnBackground(Color.parseColor("#FF4081"))  //Don't pass R.color.colorvalue
                                .setPositiveBtnText("OK")
                                .setAnimation(Animation.POP)
                                .isCancellable(false)
                                .setIcon(R.drawable.ic_chat_black_24dp, Icon.Visible)
                                .build();
                        isBackward = false;
                        sMode.setSelection(position - 1);
                    } else {
                        // Get the questionnaires based on the selected mode.
                        getQuestionnaires(level);
                        showQuestion();
                        Score = 0;
                        setScore();
                        isBackward = true;
                    }
                }

                //get the user choose
                //compare to it's current level
                boolean itIsEqualToCurrentLevel = level.equals(UserScoreHelper.convertLevelToWord(Integer.parseInt(User.getGrammarUserLevel())));
                //if the not equal to current level perform the action
                if  (!itIsEqualToCurrentLevel && isBackward) {
                    //set UserScoreHelper level to = what the user choose
                    UserScoreHelper.setLevel(UserScoreHelper.convertWordToLevel(level));
                    //rebase the no of mistakes in UI
                    GameOverHelper.rebaseUserMistakesInLevel(Context,User.Username,UserScoreHelper.getLevel(),"grammar");
                    //rebase the user score on that particular level
                    UserScoreHelper.setCurrentScoreInGrammar(Context,UserScoreHelper.getLevel(),User.Username,0);
                    setScore();
                    getQuestionnaires(UserScoreHelper.getLevel());
                    showQuestion();
                } else {
                    UserScoreHelper.setLevel(Integer.parseInt(User.getGrammarUserLevel()));
                    setScore();
                    getQuestionnaires(level);
                    showQuestion();
                }

                setUserMistakes();



            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // sometimes you need nothing here
            }
        });








        //at the first the score of user is 0 so we need to call setScore
        //to resume the previous score of the user
        this.setScore();

        //checking if the user finish all the stages
        this.isUserFinishAllStages();

        //give information to the user about question that needs to answer to acquired level up
        this.giveMessageDependingOnLevel(false);


        //initialize and set user mistakes
        this.setUserMistakes();
    }

    private void setUserMistakes() {
        int noOfMistakes = GameOverHelper.getUserMistake(this,User.Username,UserScoreHelper.getLevel(),"grammar");
        lblWrong.setText(String.format("Wrong : %s", String.valueOf(noOfMistakes)));
    }

    /**
     * if the user press the home button of his/her phone
     */
    private void isUserPressHomeButton() {
        if (Context instanceof EnglishGrammarActivity) {
            GameOverHelper.rebaseUserMistakes(getApplicationContext());
            UserScoreHelper.rebaseUserScore(getApplicationContext());
            ConfettiHelper.rebaseGrammarFinishConffeti(getApplicationContext());
            ConfettiHelper.rebaseSpellingFinishConfetti(getApplicationContext());
            ConfettiHelper.rebasePronunFinishConfetti(getApplicationContext());
        }
    }

    //display information according to user level
    private void giveMessageDependingOnLevel(boolean isLevelFinish) {
        String msg = null;
        String title = null;
        String icon = null;
        //first we need to get the level of the user
        String currentUserLevel = UserScoreHelper.convertLevelToWord(Integer.parseInt(User.GrammarUserLevel));

        //get the quesionnaires to easily calculate the no of questions that the user need to answer
        getQuestionnaires(currentUserLevel);

        this.setScore(); //update the user score

        //check level of the user and give appropriate message
        if (User.getGrammarUserLevel().equals("1") || User.getGrammarUserLevel().equals("2")) {
            title = "I N F O R M A T I O N";
            icon = "ic_chat_black_24dp";
            msg ="You are in Beginner, you need to get " + (int) Math.ceil(((Questions.size() + 1) * .50) - Score) + " correctanswer" +
                    " " +
                    "before you can jump to advance \n" +
                    "\n\n"+
                    "Remember: \n" +
                    "If you reached 5 mistakes your score will automatically back to zero.";
        } else if (User.GrammarUserLevel.equals("3") || User.GrammarUserLevel.equals("4")) {
            if  (isLevelFinish) {
                title = "L E V E L U P";
                icon = "ic_star_black_24dp";
                msg ="Very good that is correct! , now you are in Advance, you need to get " + (int) Math.ceil(((Questions.size() + 1) * 0.9) - Score) + "" +
                                " correct answer so you can jump to expert" +
                                "\n\n" +
                                "Remember: \n" +
                                "If you reach 5 mistakes your score will automatically back to zero";
            } else {
                title = "I N F O R M A T I O N";
                icon = "ic_chat_black_24dp";
                msg =
                        "You are in Advance, you need to get " + (int) Math.ceil(((Questions.size() + 1) * 0.9) - Score) + "" +
                                " correct answer so you can jump to expert" +
                                "\n\n" +
                                "Remember: \n" +
                                "If you reach 5 mistakes your score will automatically back to zero";
            }


        } else if (User.GrammarUserLevel.equals("5") && this.Score < Questions.size()) {
            if (isLevelFinish) {
                title = "L E V E L U P";
                icon = "ic_star_black_24dp";
                msg = "Very good, that is correct ! now you are in Expert,  you need to get " + (int) Math.ceil(((this.Questions.size())) - Score)+ " correct answer" +
                        " " +
                        "to finish this level" +
                        "\n\n" +
                        "Remember: \n" +
                        "If you reach 5 mistakes your score will automatically back to zero";
            } else {
                title = "I N F O R M A T I O N";
                icon = "ic_chat_black_24dp";
                msg = "You are in Expert,  you need to get " + (int) Math.ceil(((this.Questions.size())) - Score)+ " correct answer" +
                        " " +
                        "to finish this level" +
                        "\n\n" +
                        "Remember: \n" +
                        "If you reach 5 mistakes your score will automatically back to zero";
            }

        }

        if (!isFinish) {
            int msgIcon = getResources().getIdentifier(icon,"drawable", Objects.requireNonNull(this).getPackageName());
            new FancyAlertDialog.Builder(this)
                    .setTitle(title.toUpperCase())
                    .setBackgroundColor(Color.parseColor("#303F9F"))
                    .setMessage(msg)
                    .setNegativeBtnText("")
                    .setNegativeBtnBackground(Color.parseColor("#00141312"))  //Don't pass R.color.colorvalue
                    .setPositiveBtnBackground(Color.parseColor("#FF4081"))  //Don't pass R.color.colorvalue
                    .setPositiveBtnText("OK")
                    .setAnimation(Animation.POP)
                    .isCancellable(false)
                    .setIcon(msgIcon, Icon.Visible)
                    .build();
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

    // Initializes the items in the spinner.
    private void initLevels() {
        String[] items = new String[]{this.Beginner, this.Advance, this.Expert};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        this.sMode.setAdapter(adapter);
    }

    // Sets the score.
    private void setScore() {
        if (this.Score <= this.QuestionsCount)
            //checking if the user has a previous score in session
            if (UserScoreHelper.isUserHasPreviousScoreInGrammar(this, UserScoreHelper.getLevel(), User.Username)) {
                this.Score = UserScoreHelper.getCurrentScoreInGrammar(this, UserScoreHelper.getLevel(), User.Username);
            } else { // rebase the score
                this.Score = 0;
            }
            this.tScore.setText(String.format("Correct: %s", this.Score.toString()));
    }

    // Sets the name and the level of the user.
    private void setNameLevel(String userLevel) {
        String level = "";
        if (Integer.parseInt(userLevel) <= 2) level = this.Beginner;
        if (Integer.parseInt(userLevel) == 3 || Integer.parseInt(userLevel) == 4) level = this.Advance;
        if (Integer.parseInt(userLevel) == 5) level = this.Expert;
        this.tNameLevel.setText(String.format("%s: %s", this.User.Username, level));
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
            int id = RandomHelper.generateRandomNumber(this.QuestionsCount);
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
                    this.bChoiceA.setOnClickListener(view -> {
                        String text = bChoiceA.getText().toString().trim();
                        checkAnswer(text, Questionnaire.Answer);
                    });

                    this.bChoiceB.setOnClickListener(view -> {
                        String text = bChoiceB.getText().toString().trim();
                        checkAnswer(text, Questionnaire.Answer);
                    });

                    this.bChoiceC.setOnClickListener(view -> {
                        String text = bChoiceC.getText().toString().trim();
                        checkAnswer(text, Questionnaire.Answer);
                    });
                    this.bChoiceD.setOnClickListener(view -> {
                        String text = bChoiceD.getText().toString().trim();
                        checkAnswer(text, Questionnaire.Answer);
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
        boolean isUserGameOver = false;
        boolean isFinishLevel = false;
        String resultTypeImage;
        boolean isCorrect = choice.trim().toUpperCase().equals(answer.trim().toUpperCase());


        if (isCorrect) {
            msg = "Very good, that is correct!";
            resultTypeImage = "ic_check_black_24dp";
            // Add score.
            this.Score++;
            //music for correct
            SFXHelper.playMusic(getApplicationContext(),R.raw.correct);

            //track the score of user base on level
            UserScoreHelper.addCurrentScoreInGrammar(this,UserScoreHelper.getLevel(),User.Username);

            //display or update the ui
            this.setScore();

        }  else {
            msg = "Sorry, that is incorrect. The correct answer is " + answer + ".";
            resultTypeImage = "ic_clear_black_24dp";
            //add mistake to user
            GameOverHelper.addMistake(this,User.Username,UserScoreHelper.getLevel(),"grammar");

            if (!GameOverHelper.isUserGameOver(this,User.Username,UserScoreHelper.getLevel(),"grammar")) {
                SFXHelper.playMusic(getApplicationContext(),R.raw.wrong);
            }

        }

        //checking if the user is game over or not
        if (GameOverHelper.isUserGameOver(this,User.Username,UserScoreHelper.getLevel(),"grammar")) {
            SFXHelper.playMusic(getApplicationContext(),R.raw.game_over);
            isUserGameOver = true;
            new FancyAlertDialog.Builder(this)
                    .setTitle("G A M E O V E R")
                    .setBackgroundColor(Color.parseColor("#303F9F"))
                    .setMessage("Sorry, you have reached 5 mistakes \n" +
                            "The correct answer is " + answer)
                    .setNegativeBtnText("OTHER CATEGORY")
                    .setPositiveBtnBackground(Color.parseColor("#FF4081"))  //Don't pass R.color.colorvalue
                    .setPositiveBtnText("OK")
                    .setNegativeBtnBackground(Color.parseColor("#FFA9A7A8"))  //Don't pass R.color.colorvalue
                    .setAnimation(Animation.POP)
                    .isCancellable(true)
                    .OnNegativeClicked(this::initMain)
                    .setIcon(R.drawable.ic_err_black_24dp, Icon.Visible)
                    .build();

            //rebase the no of mistakes in UI
            GameOverHelper.rebaseUserMistakesInLevel(this,User.Username,UserScoreHelper.getLevel(),"grammar");

            //rebase the current score of the user in shared pref
            UserScoreHelper.setCurrentScoreInGrammar(Context,UserScoreHelper.getLevel(),User.Username,0);

        }
            this.setUserMistakes();




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



        //to avoid double level up I add this condition
        if (isShowLevelUpMsg) {
            //we also need to set a new level for user while answering some questions
            UserScoreHelper.setLevel(Integer.parseInt(User.getGrammarUserLevel()));
            switch(currentLevel) {
                case 2:
                    isFinishLevel = true;
                    this.giveMessageDependingOnLevel(true);
                    this.setUserMistakes();
                    RandomHelper.rebaseListNumber();
                    SFXHelper.playMusic(getApplicationContext(),R.raw.level_up);
                    break;

                case 4:
                    isFinishLevel = true;
                    this.giveMessageDependingOnLevel(true);
                    this.setUserMistakes();
                    RandomHelper.rebaseListNumber();
                    SFXHelper.playMusic(getApplicationContext(),R.raw.level_up);
                    break;

                case 5:
                    isFinishLevel = true;
                    this.giveMessageDependingOnLevel(true);
                    this.setUserMistakes();
                    RandomHelper.rebaseListNumber();
                    SFXHelper.playMusic(getApplicationContext(),R.raw.level_up);
                    break;


            }

        }

        //rebase the current score of the user since promoted to next level
        this.setScore();
//        Message.show(msg, this.Context);

        if (!isUserGameOver) {
           if (!isFinishLevel) {
               int msgIcon = getResources().getIdentifier(resultTypeImage,"drawable", Objects.requireNonNull(this).getPackageName());
               String title = (resultTypeImage.contains("check")) ? "C o r r e c t".toUpperCase() : "W r o n g".toUpperCase();
               new FancyAlertDialog.Builder(this)
                       .setTitle(title)
                       .setBackgroundColor(Color.parseColor("#303F9F"))
                       .setMessage(msg)
                       .setNegativeBtnText("")
                       .setNegativeBtnBackground(Color.parseColor("#00141312"))  //Don't pass R.color.colorvalue
                       .setPositiveBtnBackground(Color.parseColor("#FF4081"))  //Don't pass R.color.colorvalue
                       .setPositiveBtnText("OK")
                       .setAnimation(Animation.POP)
                       .isCancellable(false)
                       .setIcon(msgIcon, Icon.Visible)
                       .build();
           }
        }


        // Set current level for a user in UI
        this.displaySetLevel();
        this.isUserFinishAllStages();

        // Show another question.
        this.showQuestion();

    }

    /**
     * Checking if the user finish all the stages
     */
    private void isUserFinishAllStages() {
        //set level to get the no of questions
        getQuestionnaires(UserScoreHelper.convertLevelToWord(Integer.parseInt(this.User.getGrammarUserLevel())));
        // Check if user done all stages
        isFinish = this.checkIsUserDoneAllStage(
                this.Score,
                Integer.parseInt(this.User.getGrammarUserLevel()),
                this.Questions.size()
        );

        // is expert level finish
        if (isFinish) {
            tLevel.setText(R.string.stages_done);
            if (!ConfettiHelper.isConfettiInGrammarAlreadyDisplay(this,User.Username)) {
                this.generateConfetti();
                SFXHelper.playMusic(getApplicationContext(),R.raw.level_up);
            }
        }
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
                ConfettiHelper.setGrammarConfettiAlreadyDisplayed(this,User.Username);
    }

    private boolean checkIsUserDoneAllStage(int currentScore , int currentLevel , int noOfQuestions) {
        return currentScore >= noOfQuestions && currentLevel == 5;
    }

    private void displaySetLevel() {

        int currentUserLevel = UserLevelHelper
                                    .currentLevelOfUser(Integer.parseInt(this.User.getGrammarUserLevel()));
        tLevel.setText(String.format(Locale.getDefault(),"LEVEL : %d",currentUserLevel));
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
        currentLevel = userLevel;


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
            soundStatus.setBackgroundResource(R.drawable.ic_volume_up_black_24dp);
        } else {
            doUnbindService();
            soundStatus.setBackgroundResource(R.drawable.ic_volume_off_black_24dp);
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
