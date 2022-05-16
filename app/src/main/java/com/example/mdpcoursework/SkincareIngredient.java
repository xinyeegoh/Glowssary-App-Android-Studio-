package com.example.mdpcoursework;

import android.os.Parcel;
import android.os.Parcelable;

public class SkincareIngredient implements Parcelable {

    String name, other_names,type, rating, usage, featured;

    public SkincareIngredient(){

    }

    public SkincareIngredient(String name, String other_names, String type, String rating, String usage, String featured) {
        this.name = name;
        this.other_names = other_names;
        this.type = type;
        this.rating = rating;
        this.usage = usage;
        this.featured = featured;
    }

    protected SkincareIngredient(Parcel in) {
        name = in.readString();
        other_names = in.readString();
        type = in.readString();
        rating = in.readString();
        usage = in.readString();
        featured = in.readString();
    }

    public static final Creator<SkincareIngredient> CREATOR = new Creator<SkincareIngredient>() {
        @Override
        public SkincareIngredient createFromParcel(Parcel in) {
            return new SkincareIngredient(in);
        }

        @Override
        public SkincareIngredient[] newArray(int size) {
            return new SkincareIngredient[size];
        }
    };

    public String getOther_names() {
        return other_names;
    }

    public void setOther_names(String other_names) {
        this.other_names = other_names;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getUsage() {
        return usage;
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }

    public String getFeatured() { return featured; }

    public void setFeatured(String featured) { this.featured = featured; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(other_names);
        parcel.writeString(type);
        parcel.writeString(rating);
        parcel.writeString(usage);
        parcel.writeString(featured);
    }
}
