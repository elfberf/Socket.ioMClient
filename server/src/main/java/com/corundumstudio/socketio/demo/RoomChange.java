package com.corundumstudio.socketio.demo;

public class RoomChange {

    private String userName;
    private String room;

    public RoomChange() {
    }

    public RoomChange(String userName) {
        super();
        this.userName = userName;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
}
