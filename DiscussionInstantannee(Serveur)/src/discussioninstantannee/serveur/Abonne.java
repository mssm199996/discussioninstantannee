package discussioninstantannee.serveur;

public class Abonne{
    private int num;
    private String surnom;
    public Abonne(int num,String surnom){
        this.num = num;
        this.surnom = surnom;
    }
    public int getNumber(){
        return this.num;
    }
    public String getNickname(){
        return this.surnom;
    }
}
