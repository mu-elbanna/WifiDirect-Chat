package com.example.wifidirectchat.connection;


/**
 * This interface includes client and server classes which are threads
 * And ransh is read from the socket and sent with send
 */
public abstract class IMessenger extends Thread {
    public abstract void send(String text, boolean isMessage);

    public abstract void DestroySocket();
}
