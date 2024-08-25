package com.kafkademo.domain;

public class MusicDB {
    private Integer id;
    private String name;
    private String author;

    public MusicDB(Integer id, String s, String s1) {
        this.id = id;
        this.name = s;
        this.author = s1;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
