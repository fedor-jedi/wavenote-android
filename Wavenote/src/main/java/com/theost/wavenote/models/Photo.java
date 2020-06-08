package com.theost.wavenote.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

public class Photo implements Serializable {

    String id;
    String name;
    String uri;
    String date;

    public Photo(String id, String name, String uri, String date) {
        this.id = id;
        this.name = name;
        this.uri = uri;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUri() {
        return uri;
    }

    public Bitmap getBitmap(Context context) {
        try {
            return MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.fromFile(new File(uri)));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public String getDate() {
        return date;
    }

}
