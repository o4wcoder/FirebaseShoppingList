package com.fourthwardmobile.android.firebaseshoppinglist.model;

import java.util.HashMap;

/**
 * Created by Chris Hare on 8/10/2016.
 */
public class User {

    String name;
    String email;

    private HashMap<String, Object> timestampJoined;

    public User() {}

    public User(String name, String email, HashMap<String,Object> timestampJoined) {

        this.name = name;
        this.email = email;
        this.timestampJoined = timestampJoined;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public HashMap<String, Object> getTimestampCreated() {
        return timestampJoined;
    }

    public void setTimestampCreated(HashMap<String, Object> timestampCreated) {
        this.timestampJoined = timestampCreated;
    }
}
