package com.corundumstudio.socketio.demo;

import com.corundumstudio.socketio.listener.*;
import com.corundumstudio.socketio.*;
import com.corundumstudio.socketio.protocol.Packet;

import java.util.Collection;

public class ChatLauncher {

    public static final String ROOM_STAJYERLER = "stajyerler";

    public static void main(String[] args) throws InterruptedException {

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
                            ChatObject single = new ChatObject("server", "message to room");
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

        /*server.addEventListener("create-room", Object.class, new DataListener<Object>() {
            @Override
            public void onData(SocketIOClient socketIOClient, Object o, AckRequest ackRequest) throws Exception {
                System.out.println();
            }
        });*/

        server.addEventListener("chatevent", ChatObject.class, new DataListener<ChatObject>() {
            @Override
            public void onData(SocketIOClient client, ChatObject data, AckRequest ackRequest) {
                // broadcast messages to all clients - tüm istemcilere mesaj yayınla
                server.getBroadcastOperations().sendEvent("chatevent", data);
                /*Collection<SocketIOClient> allClients = server.getAllClients();
                ChatObject single = new ChatObject(data.getUserName(), data.getMessage() + " - - ");
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
