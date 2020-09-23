package com.messages;

import java.io.Serializable;

/**
 * Created by Dominic on 2016-May-01.
 */
public class User implements Serializable {

    private static final long serialVersionUID = 2742144767101018291L;

    String name;
    String picture;
    Status status;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(final String picture) {
        this.picture = picture;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(final Status status) {
        this.status = status;
    }

}
