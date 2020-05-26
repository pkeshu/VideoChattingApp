package com.pickndrop.keshartestappforvediocalling.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Contacts implements Parcelable {
    private String name,image,status,uid,mobileNumber;

    public Contacts() {
    }

    public Contacts(String name, String image, String status, String uid,String mobileNumber) {
        this.name = name;
        this.image = image;
        this.status = status;
        this.uid = uid;
        this.mobileNumber=mobileNumber;
    }

    protected Contacts(Parcel in) {
        name = in.readString();
        image = in.readString();
        status = in.readString();
        uid = in.readString();
        mobileNumber = in.readString();
    }

    public static final Creator<Contacts> CREATOR = new Creator<Contacts>() {
        @Override
        public Contacts createFromParcel(Parcel in) {
            return new Contacts(in);
        }

        @Override
        public Contacts[] newArray(int size) {
            return new Contacts[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(image);
        parcel.writeString(status);
        parcel.writeString(uid);
        parcel.writeString(mobileNumber);
    }
}
