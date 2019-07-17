package com.example.tommy.camerademo;

import android.net.Uri;
import android.provider.MediaStore;

public enum Media {

    PHOTO(1, "photos", ".jpg", MediaStore.ACTION_IMAGE_CAPTURE, MediaStore.Images.Media.EXTERNAL_CONTENT_URI),
    VIDEO(2, "videos", ".mp4", MediaStore.ACTION_VIDEO_CAPTURE, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);

    private int index;
    private String type;
    private String suffix;
    private String action;
    private Uri url;

    Media(int index, String type, String suffix, String action, Uri url) {
        this.index = index;
        this.type = type;
        this.suffix = suffix;
        this.action = action;
        this.url = url;
    }

    public int getIndex() {
        return index;
    }

    public String getType() {
        return type;
    }

    public String getSuffix() {
        return suffix;
    }

    public String getAction() {
        return action;
    }

    public Uri getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return "Media{" +
                "index=" + index +
                ", type='" + type + '\'' +
                ", suffix='" + suffix + '\'' +
                ", action='" + action + '\'' +
                ", url=" + url +
                '}';
    }
}
