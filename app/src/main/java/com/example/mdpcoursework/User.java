package com.example.mdpcoursework;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {

     String user_id, username, age, gender, email;

    public User(String user_id, String username, String age, String gender, String email) {
        this.user_id = user_id;
        this.username = username;
        this.age = age;
        this.gender = gender;
        this.email = email;
    }

    protected User(Parcel in) {
        user_id = in.readString();
        username = in.readString();
        age = in.readString();
        gender = in.readString();
        email = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(user_id);
        parcel.writeString(username);
        parcel.writeString(age);
        parcel.writeString(gender);
        parcel.writeString(email);
    }
}
