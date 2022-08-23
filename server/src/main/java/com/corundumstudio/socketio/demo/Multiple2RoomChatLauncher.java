package com.corundumstudio.socketio.demo;

import com.corundumstudio.socketio.*;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;

import java.util.HashMap;
import java.util.HashSet;

public class Multiple2RoomChatLauncher {

    public static final String ROOM_DİGERLERİ = "digerleri";

    ////defining the map strings
    private HashMap<String, HashSet<SocketIOClient>> mapRooms = new HashMap<String, HashSet<SocketIOClient>>();
    private HashMap<SocketIOClient, String> mapClientRoom = new HashMap<SocketIOClient, String>();

    public static void main(String[] args) throws InterruptedException {
        Multiple2RoomChatLauncher launcher = new Multiple2RoomChatLauncher();
        launcher.start();
    }

    private void start() throws InterruptedException {
        Configuration config = new Configuration();
        config.setHostname("localhost");
        config.setPort(9092);

         //adding socket
        final SocketIOServer server = new SocketIOServer(config);

         //adding broadcast to server
        BroadcastOperations digerleri = server.getRoomOperations(ROOM_DİGERLERİ);
        System.out.println();

        server.addConnectListener(new ConnectListener() {
            @Override
            public void onConnect(SocketIOClient socketIOClient) {
                System.out.println("socketIOClient = " + socketIOClient.getSessionId());
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(5);
                            ChatRoomObject single = new ChatRoomObject("server", "message to room");
                            server.getRoomOperations(ROOM_DİGERLERİ).sendEvent("chatevent", single);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
                thread.start();
            }
        });

        //create function
        server.addEventListener("create", Object.class, new DataListener<Object>() {
            @Override
            public void onData(SocketIOClient socketIOClient, Object o, AckRequest ackRequest) throws Exception {
                System.out.println();
            }
        });
        
        //creating room
        server.addEventListener("create-room", Object.class, new DataListener<Object>() {
            @Override
            public void onData(SocketIOClient socketIOClient, Object o, AckRequest ackRequest) throws Exception {
                System.out.println();
            }
        });

        //room change function
        //add user list to here
        server.addEventListener("roomChange", RoomChange.class, new DataListener<RoomChange>() {
            @Override
            public void onData(SocketIOClient client, RoomChange roomChange, AckRequest ackRequest) throws Exception {
                // creating active room which holds current room
                String activeRoom = mapClientRoom.get(client);
                //printing the active room and changed room to terminal
                System.out.println(roomChange.getUserName() + ": activeRoom = " + activeRoom);
                System.out.println(roomChange.getUserName() + ": new room = " + roomChange.getRoom());
                // removing the client from the room which currently in
                mapClientRoom.remove(client);
                // adding client to data in roomChange data
                mapClientRoom.put(client, roomChange.getRoom());

               // removing client from the previous room
                // by this we are making the room data private for that room
                HashSet<SocketIOClient> socketIOClients = null;
                if (activeRoom != null) {
                    socketIOClients = mapRooms.get(activeRoom);
                    if (socketIOClients != null) {
                        socketIOClients.remove(client);
                    }
                }

                // adding client to new room
                String roomNew = roomChange.getRoom();
                socketIOClients = mapRooms.get(roomNew);
               // if there is no list create a new list
                if (socketIOClients == null) {
                    socketIOClients = new HashSet<SocketIOClient>();
                    mapRooms.put(roomNew, socketIOClients);
                }
                socketIOClients.add(client);
            }

        });

        //chat event
        server.addEventListener("chatevent", ChatRoomObject.class, new DataListener<ChatRoomObject>() {
            @Override
            public void onData(SocketIOClient client, ChatRoomObject data, AckRequest ackRequest) {
                // broadcast messages to all clients 
                String room = data.getRoom();
                System.out.println("data.getRoom() = " + room);
                {
                   
                    HashSet<SocketIOClient> socketIOClients = mapRooms.get(room);
                    if (socketIOClients != null) {
                        ChatRoomObject single = new ChatRoomObject(data.getUserName(), data.getMessage());
                        for (SocketIOClient c : socketIOClients) {
                            if (!client.equals(c))
                                c.sendEvent("chatevent", single);
                        }
                    }
                }
            }
        });


        server.start();

        Thread.sleep(Integer.MAX_VALUE);

        server.stop();
    }

}
