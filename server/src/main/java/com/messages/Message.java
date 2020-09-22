package com.messages;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Message implements Serializable {

    private static final long serialVersionUID = 4635025961938424754L;
    private String name, msg, picture;
    private MessageType type;
    private int count;
    private List<User> list;
    private List<User> users;

    private Status status;
    private byte[] voiceMsg;

    public byte[] getVoiceMsg() {
        return voiceMsg;
    }

    public String getPicture() {
        return picture;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMsg() {

        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public List<User> getUserlist() {
        return list;
    }

    public void setUserlist(Map<String, User> userList) {
        this.list = new ArrayList<>(userList.values());
    }

    public void setOnlineCount(int count) {
        this.count = count;
    }

    public int getOnlineCount() {
        return this.count;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }

    public void setVoiceMsg(byte[] voiceMsg) {
        this.voiceMsg = voiceMsg;
    }
}
