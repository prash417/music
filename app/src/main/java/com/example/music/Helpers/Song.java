package com.example.music.Helpers;

import android.net.Uri;

public class Song {

    //members
    String title;
    String uri;
    int size;
    int duration;
    Long ID;
    String path;


    public Song(String title, String uri, int size, int duration, Long ID,String path) {
        this.title = title;
        this.uri = uri;
        this.size = size;
        this.duration = duration;
        this.ID = ID;
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public Long getID() {
        return ID;
    }

    public void setID(Long ID) {
        this.ID = ID;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
