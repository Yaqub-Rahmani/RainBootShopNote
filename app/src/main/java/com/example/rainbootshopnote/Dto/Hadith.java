package com.example.rainbootshopnote.Dto;

public class Hadith {

    private int id;
    private String hadith;
    private String translate;
    public Hadith(String translate, String hadith, int id) {
        this.id = id;
        this.hadith = hadith;
        this.translate = translate;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getHadith() {
        return hadith;
    }

    public void setHadith(String hadith) {
        this.hadith = hadith;
    }

    public String getTranslate() {
        return translate;
    }

    public void setTranslate(String translate) {
        this.translate = translate;
    }

    @Override
    public String toString() {
        return "Hadith{" +
                "id=" + id +
                ", hadith='" + hadith + '\'' +
                ", translate='" + translate + '\'' +
                '}';
    }

}
