package com.ibm.trl.funqy.inference;


public class Param {
    private String input;
    private String model;
    private String synset;

    public Param() {}

    public String getInput() { return input; }
    public void setInput(String i) { this.input = i; }

    public String getModel() { return model; }
    public void setModel(String m) { this.model = m; }

    public String getSynset() { return synset; }
    public void setSynset(String s) { this.synset = s; }
}
