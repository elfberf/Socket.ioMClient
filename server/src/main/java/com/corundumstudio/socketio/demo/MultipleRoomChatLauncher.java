package com.corundumstudio.socketio.demo;

import com.corundumstudio.socketio.*;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;

import java.util.*;

public class MultipleRoomChatLauncher {

    public static final String ROOM_STAJYERLER = "stajyerler";

    private HashMap<String, HashSet<SocketIOClient>> rooms = new HashMap<String, HashSet<SocketIOClient>>();

    public static void main(String[] args) throws InterruptedException {
        MultipleRoomChatLauncher launcher = new MultipleRoomChatLauncher();
        launcher.start();
    }

    private void start() throws InterruptedException {
        Configuration config = new Configuration();
        config.setHostname("localhost");//192.168.254.105
        config.setPort(9092);

        final SocketIOServer server = new SocketIOServer(config);

        BroadcastOperations stajyerler = server.getRoomOperations(ROOM_STAJYERLER);
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
                            server.getRoomOperations(ROOM_STAJYERLER).sendEvent("chatevent", single);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
                thread.start();
            }
        });

        server.addEventListener("create", Object.class, new DataListener<Object>() {
            @Override
            public void onData(SocketIOClient socketIOClient, Object o, AckRequest ackRequest) throws Exception {
                System.out.println();
            }
        });

        server.addEventListener("create-room", Object.class, new DataListener<Object>() {
            @Override
            public void onData(SocketIOClient socketIOClient, Object o, AckRequest ackRequest) throws Exception {
                System.out.println();
            }
        });

        server.addEventListener("chatevent", ChatRoomObject.class, new DataListener<ChatRoomObject>() {
            @Override
            public void onData(SocketIOClient client, ChatRoomObject data, AckRequest ackRequest) {
                // broadcast messages to all clients - tüm istemcilere mesaj yayınla
                server.getBroadcastOperations().sendEvent("chatevent", data);
                String room = data.getRoom();
                System.out.println("data.getRoom() = " + room);
                if (room != null)
                {
                    HashSet<SocketIOClient> socketIOClients = rooms.get(room);
                    if (socketIOClients == null) {
                        socketIOClients = new HashSet<SocketIOClient>();
                        rooms.put(room, socketIOClients);
                    }
                    socketIOClients.add(client);
                    System.out.println();
                    ChatRoomObject single = new ChatRoomObject(data.getUserName(), "[" + room + "] " + data.getMessage() + " - - ");
                    for (SocketIOClient c : socketIOClients) {
                        if (!client.equals(c))
                            c.sendEvent("chatevent", single);
                    }
                }

              /*  Collection<SocketIOClient> allClients = server.getAllClients();
                ChatRoomObject single = new ChatRoomObject(data.getUserName(), "[TÜM] " + data.getMessage());
                for (SocketIOClient c : allClients) {
                    if (!client.equals(c))
                        c.sendEvent("chatevent", single);
                }*/
            }
        });



















        server.start();

        Thread.sleep(Integer.MAX_VALUE);

        server.stop();
    }

}
