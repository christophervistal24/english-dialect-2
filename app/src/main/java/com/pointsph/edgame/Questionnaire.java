package com.pointsph.edgame;

public class Questionnaire {
    public String Question;
    public String Choice_A;
    public String Choice_B;
    public String Choice_C;
    public String Choice_D;
    public String Answer;

    public Questionnaire(){}

    public void setQuestion(String question) {
        this.Question = question;
    }
    public String getQuestion() { return this.Question; }

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

    public void setChoice_C(String choice_C) {
        this.Choice_C = choice_C;
    }
    public String getChoice_C() {
        return this.Choice_C;
    }

    public void setChoice_D(String choice_D) { this.Choice_D = choice_D; }
    public String getChoice_D() {
        return this.Choice_D;
    }

    public void setAnswer(String answer) { this.Answer= answer; }
    public String getAnswer() {
        return this.Answer;
    }
}
