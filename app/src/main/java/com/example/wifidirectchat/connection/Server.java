package com.example.wifidirectchat.connection;

import android.arch.lifecycle.MutableLiveData;

import com.example.wifidirectchat.LocalDevice;
import com.example.wifidirectchat.db.MessageRepository;
import com.example.wifidirectchat.model.MessageEntity;
import com.example.wifidirectchat.viewmodel.ChatPageViewModel;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Calendar;
import java.util.Date;

public class Server extends IMessenger {

    private Socket socket;
    private ServerSocket serverSocket;
    private String peerName;
    private MutableLiveData<Boolean> isConnected;
    private ChatPageViewModel model;

    public Server(ChatPageViewModel model, MutableLiveData<Boolean> isConnected) {
        this.model = model;
        this.isConnected = isConnected;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(8888);
            socket = serverSocket.accept();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // After connecting, we send the name of our device as the first message
        send(LocalDevice.getInstance().getDevice().deviceName, false);

        // Has the peer's name already been read?
        boolean isAddresseeSet = false;

        while (socket != null) {
            try {
                ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
                String messageText = (String) inputStream.readObject();
                if (messageText != null) {
                    if (isAddresseeSet) {
                        // If a new message arrives, we store it directly in the database
                        // It is this base object that the activity observes, corresponding to the read object
                        // We no longer have to send for active
                        Date c = Calendar.getInstance().getTime();
                        MessageEntity message = new MessageEntity(messageText, c, peerName, false);
                        MessageRepository.getInstance().insert(message);
                    } else {
                        // As the first message, we read the peer's name and then turn on the chat
                        isAddresseeSet = true;
                        peerName = messageText;
                        model.setAddressee(messageText);
                        isConnected.postValue(true);
                    }
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                // If the socket is closed from the other side, we also close the chat window
                model.closeChat();
            }
        }

    }

    @Override
    public void send(final String text, final boolean isMessage) {

        new Thread() {
            @Override
            public void run() {
                if (socket == null) return;
                try {
                    ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                    outputStream.writeObject(text);
                    outputStream.flush();
                    if (isMessage) {
                        // If it is not the first message, we do not send the name
                        // Then we have to save it in the database
                        Date c = Calendar.getInstance().getTime();
                        MessageEntity message = new MessageEntity(text, c, peerName, true);
                        MessageRepository.getInstance().insert(message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    @Override
    public void DestroySocket() {
        if (socket != null) {
            try {
                socket.close();
                socket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
