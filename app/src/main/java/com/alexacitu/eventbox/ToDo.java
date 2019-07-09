package com.alexacitu.eventbox;

import java.io.Serializable;

public class ToDo implements Serializable {
    private String title;
    private boolean done;

    public ToDo(String title) {
        this.title = title;
        this.done = false;
    }

    public ToDo(String title, boolean done){
        this.title = title;
        this.done = done;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    @Override
    public String toString() {
        return title;
    }
}
