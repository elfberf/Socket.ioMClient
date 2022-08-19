package com.corundumstudio.socketio.demo;

import com.corundumstudio.socketio.*;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;

import java.util.HashMap;
import java.util.HashSet;

public class Multiple2RoomChatLauncher {

    public static final String ROOM_DİGERLERİ = "digerleri";

    private HashMap<String, HashSet<SocketIOClient>> mapRooms = new HashMap<String, HashSet<SocketIOClient>>();
    private HashMap<SocketIOClient, String> mapClientRoom = new HashMap<SocketIOClient, String>();

    public static void main(String[] args) throws InterruptedException {
        Multiple2RoomChatLauncher launcher = new Multiple2RoomChatLauncher();
        launcher.start();
    }

    private void start() throws InterruptedException {
        Configuration config = new Configuration();
        config.setHostname("localhost");//192.168.254.105
        config.setPort(9092);

        final SocketIOServer server = new SocketIOServer(config);


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

        server.addEventListener("roomChange", RoomChange.class, new DataListener<RoomChange>() {
            @Override
            public void onData(SocketIOClient client, RoomChange roomChange, AckRequest ackRequest) throws Exception {
                String activeRoom = mapClientRoom.get(client);
                System.out.println(roomChange.getUserName() + ": activeRoom = " + activeRoom);
                System.out.println(roomChange.getUserName() + ": new room = " + roomChange.getRoom());
                // User'ı şu anda bulunduğu odadan çıkartacağız
                mapClientRoom.remove(client);
                // User'ı room change data'sındaki odaya dahil edeceğiz
                mapClientRoom.put(client, roomChange.getRoom());

                // Önceden olduğu odadan kişiyi/client'ı çıkartma
                HashSet<SocketIOClient> socketIOClients = null;
                if (activeRoom != null) {
                    socketIOClients = mapRooms.get(activeRoom);
                    if (socketIOClients != null) {
                        socketIOClients.remove(client);
                    }
                }

                // Yeni odaya ilgili kişiyi/client'ı ekle
                String roomNew = roomChange.getRoom();
                socketIOClients = mapRooms.get(roomNew);
                // Liste hiç yoksa yeni liste yarat
                if (socketIOClients == null) {
                    socketIOClients = new HashSet<SocketIOClient>();
                    mapRooms.put(roomNew, socketIOClients);
                }
                socketIOClients.add(client);

/*
                Oda değişikliğini diğer üyelere haber verme
                RoomChange change = new RoomChange(roomChange.getRoom());
                for (SocketIOClient c : socketIOClients) {
                    if (!client.equals(c))
                        c.sendEvent("roomChange", change);
                }
*/
            }

        });

        server.addEventListener("chatevent", ChatRoomObject.class, new DataListener<ChatRoomObject>() {
            @Override
            public void onData(SocketIOClient client, ChatRoomObject data, AckRequest ackRequest) {
                // broadcast messages to all clients - tüm istemcilere mesaj yayınla
                String room = data.getRoom();
                System.out.println("data.getRoom() = " + room);
                // if (room != null)
                {
                    // server.getBroadcastOperations().sendEvent("chatevent", data);

                    HashSet<SocketIOClient> socketIOClients = mapRooms.get(room);
                    if (socketIOClients != null) {
                        ChatRoomObject single = new ChatRoomObject(data.getUserName(), data.getMessage());
                        for (SocketIOClient c : socketIOClients) {
                            if (!client.equals(c))
                                c.sendEvent("chatevent", single);
                        }
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
