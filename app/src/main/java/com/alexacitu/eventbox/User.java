package com.alexacitu.eventbox;

import java.io.Serializable;
import java.util.ArrayList;

public class User implements Serializable {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String number;
    private ArrayList<Event> events;
    private UserType userType;
    private ArrayList<ToDo> toDos;
    private String event;

    public User() {
        this.events = new ArrayList<>();
        if(this.userType == UserType.EVENT_PLANNER)
            this.toDos = new ArrayList<>();
    }

    public User(String id, String firstName, String lastName, String number, UserType userType, String event) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.number = number;
        this.events = new ArrayList<>();
        this.userType = userType;
        if(this.userType == UserType.EVENT_PLANNER)
            this.toDos = new ArrayList<>();
        this.event = event;
    }

    public User(String id, String firstName, String lastName, String email, String password, String number, UserType userType, ArrayList<ToDo> toDos) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.number = number;
        this.events = new ArrayList<>();
        this.userType = userType;
        if(this.userType == UserType.EVENT_PLANNER){
            this.toDos = new ArrayList<>();
            this.toDos = toDos;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public ArrayList<Event> getEvents() {
        return events;
    }

    public void setEvents(ArrayList<Event> events) {
        this.events = events;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public ArrayList<ToDo> getToDos() {
        return toDos;
    }

    public void setToDos(ArrayList<ToDo> toDos) {
        this.toDos = toDos;
    }

    public void addToDo(ToDo toDo){
        this.toDos.add(toDo);
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }
}
