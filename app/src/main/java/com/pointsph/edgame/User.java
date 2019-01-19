package com.pointsph.edgame;

public class User {
    public String FirstName;
    public String LastName;
    public String Birthday;
    public String Username;
    public String Password;
    public String GrammarUserLevel;
    public String PronunciationUserLevel;
    public String SpellingUserLevel;

    public User() {}

    public void setFirstName(String firstName) {
        this.FirstName = firstName;
    }
    public String getFirstName() {
        return this.FirstName;
    }

    public void setLastName(String lastName) {
        this.LastName = lastName;
    }
    public String getLastName() {
        return this.LastName;
    }

    public void setBirthday(String birthday) {
        this.Birthday = birthday;
    }
    public String getBirthday() {
        return this.Birthday;
    }

    public void setUsername(String username) {
        this.Username = username;
    }
    public String getUsername() {
        return this.Username;
    }

    public void setPassword(String password) {
        this.Password= password;
    }
    public String getPassword() {
        return this.Password;
    }

    public void setGrammarUserLevel(String userLevel) {
        this.GrammarUserLevel = userLevel;
    }
    public String getGrammarUserLevel() {
        return this.GrammarUserLevel;
    }

    public void setPronunciationUserLevel(String userLevel) {
        this.PronunciationUserLevel = userLevel;
    }
    public String getPronunciationUserLevel() {
        return this.PronunciationUserLevel;
    }

    public void setSpellingUserLevel(String userLevel) {
        this.SpellingUserLevel = userLevel;
    }
    public String getSpellingUserLevel() {
        return this.SpellingUserLevel;
    }
}
