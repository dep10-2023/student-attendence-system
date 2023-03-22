package lk.ijse.dep10.jdbc.model;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.Serializable;
import java.sql.Blob;

public class Student implements Serializable {
    String id;
    String name;
    ImageView picture;

    public Student() {
    }

    public Student(String id, String name, ImageView picture) {
        this.id = id;
        this.name = name;
        this.picture = picture;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ImageView getPicture() {
        picture.setFitWidth(100);
        picture.setFitHeight(100);
        picture.setPreserveRatio(true);
        return picture;
    }

    public void setPicture(ImageView picture) {
        this.picture = picture;
    }
}
