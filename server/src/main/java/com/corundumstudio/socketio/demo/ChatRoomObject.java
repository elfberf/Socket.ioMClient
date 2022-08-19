package com.corundumstudio.socketio.demo;

public class ChatRoomObject {

    private String userName;
    private String room;
    private String message;

    public ChatRoomObject() {
    }

    public ChatRoomObject(String userName, String message) {
        super();
        this.userName = userName;
        this.message = message;
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

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

}
