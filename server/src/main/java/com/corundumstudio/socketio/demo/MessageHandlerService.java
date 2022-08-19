package com.corundumstudio.socketio.demo;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DataListener;

public class MessageHandlerService implements DataListener<ChatObject> {
    public MessageHandlerService() {

    }

    @Override
    public void onData(SocketIOClient socketIOClient, ChatObject data, AckRequest ackRequest) throws Exception {
        socketIOClient.getNamespace().getBroadcastOperations().sendEvent("message", data);
    }
}
