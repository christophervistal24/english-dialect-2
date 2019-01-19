package com.pointsph.edgame;

import android.app.Activity;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.ArrayMap;

import java.io.File;
import java.util.Objects;

public class FileHelper extends AppCompatActivity {
    private Activity Activity;

    // File references.
    public String Filepath = "EDGame";
    public String UserFile = "Users.edg";
    public String SettingsFile = "Settings.edg";

    // Folder for the grammar module.
    public String GrammarFolder = "Grammar";
    // Files for the grammar folder.
    public String BeginnerFile = "Beginner.edg";
    public String AdvanceFile = "Advance.edg";
    public String ExpertFile = "Expert.edg";

    // Folders for the pronunciation module.
    public String PronunciationBeginnerFolder = "Pronunciation/Beginner";
    public String PronunciationAdvanceFolder = "Pronunciation/Advance";
    public String PronunciationExpertFolder = "Pronunciation/Expert";

    // Folder for the spelling module.
    public String SpellingBeginnerFolder = "Spelling/Beginner";
    public String SpellingAdvanceFolder = "Spelling/Advance";
    public String SpellingExpertFolder = "Spelling/Expert";



    public FileHelper(Activity activity) {
        this.Activity = activity;
    }

    /**
     * Determines whether the external storage is available.
     * @return true or false.
     */
    public boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(extStorageState);
    }

    /**
     * Determines whether the external storage is read only.
     * @return true or false.
     */
    public boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState);
    }

    /**
     * Determines whether the device has a file.
     * @return true or false.
     * @throws Exception, The exception.
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public boolean hasFile(File file) {
        return file.exists();
    }

    /**
     * Finds a key in an array map.
     * @param arrayMap, The array map object.
     * @param keyToFind, The key to find in the array map.
     * @return true or false.
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public boolean findKeyInArrayMap(ArrayMap arrayMap, String keyToFind) {
        for (int i = 0; i < arrayMap.size(); i++) {
            String key = arrayMap.keyAt(i).toString();
            if (Objects.equals(key.trim().toUpperCase(), keyToFind.trim().toUpperCase()))
                return true;
        }

        return false;
    }

}
