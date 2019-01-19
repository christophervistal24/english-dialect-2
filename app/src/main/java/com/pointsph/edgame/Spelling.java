package com.pointsph.edgame;

public class Spelling {
    public String Description;
    public String Choice_A;
    public String Choice_B;
    public String Answer;

    public Spelling(){}

    public void setQuestion(String description) {
        this.Description = description;
    }
    public String getQuestion() {
        return this.Description;
    }

    public void setChoice_A(String choice_A) {
        this.Choice_A = choice_A;
    }
    public String getChoice_A() {
        return this.Choice_A;
    }

    public void setChoice_B(String choice_B) {
        this.Choice_B = choice_B;
    }
    public String getChoice_B() {
        return this.Choice_B;
    }

    public void setAnswer(String answer) { this.Answer= answer; }
    public String getAnswer() {
        return this.Answer;
    }
}
