package org.pytorch.demo.objectdetection;

import com.google.gson.annotations.SerializedName;

import java.io.File;

public class ReqData {
    @SerializedName("text")
    private String text;
    @SerializedName("image")
    private File image;

    public ReqData(String text, File image) {
        this.text = text;
        this.image = image;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public File getImage() {
        return image;
    }

    public void setImage(File image) {
        this.image = image;
    }
}
