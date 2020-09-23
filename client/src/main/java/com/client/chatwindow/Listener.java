package com.client.chatwindow;

import com.client.login.LoginController;
import com.messages.Message;
import com.messages.MessageType;
import com.messages.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;

import static com.messages.MessageType.CONNECTED;

public class Listener implements Runnable {
    public String hostname;
    public int port;
    public static String username;
    public ChatController controller;

    private static final String HASCONNECTED = "has connected";
    Logger logger = LoggerFactory.getLogger(Listener.class);

    private static String picture;
    private static ObjectOutputStream objectOS;
    private Socket socket;
    private ObjectInputStream objectIS;

    public Listener(String hostname, int port, String username, String picture, ChatController controller) {
        this.hostname = hostname;
        this.port = port;
        Listener.username = username;
        Listener.picture = picture;
        this.controller = controller;
    }

    public void run() {
        try {
            socket = new Socket(hostname, port);
            LoginController.getInstance().showScene();
            objectOS = new ObjectOutputStream(socket.getOutputStream());
            objectIS = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            LoginController.getInstance().showErrorDialog("Could not connect to server");
            logger.error("Could not Connect");
            closeResources();
            return;
        }
        logger.info("Connection accepted {}:{}", socket.getInetAddress(), socket.getPort());

        try {
            sendConnectedMessage();
            logger.info("Sockets in and out ready!");
            while (socket.isConnected()) {
                Message message = null;
                message = (Message) objectIS.readObject();

                if (message != null) {
                    logger.debug("Message recieved:{} MessageType:{} Name:{}",
                            new String[] { message.getMsg(), message.getType().toString(), message.getName() });
                    selectActionForMessage(message);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            controller.logoutScene();
        }
        closeResources();
    }

    private void closeResources() {
        try {
            if (socket != null)
                socket.close();
            if (objectOS != null)
                objectOS.close();
            if (objectIS != null)
                objectIS.close();
        } catch (IOException closeException) {
            logger.error("Problem closing resources.", closeException);
        }
    }

    /**
     * Selects the appropriate action for the message type provided.
     * 
     * @param message the {@link Message} to process
     */
    private void selectActionForMessage(Message message) {
        switch (message.getType()) {
            case USER:
                controller.addToChat(message);
                break;
            case VOICE:
                controller.addToChat(message);
                break;
            case NOTIFICATION:
                controller.newUserNotification(message);
                break;
            case SERVER:
                controller.addAsServer(message);
                break;
            case CONNECTED:
                controller.setUserList(message);
                break;
            case DISCONNECTED:
                controller.setUserList(message);
                break;
            case STATUS:
                controller.setUserList(message);
                break;
        }
    }

    /*
     * This method is used for sending a normal Message
     * 
     * @param msg - The message which the user generates
     */
    public static void send(String msg) throws IOException {
        Message createMessage = new Message();
        createMessage.setName(username);
        createMessage.setType(MessageType.USER);
        createMessage.setStatus(Status.AWAY);
        createMessage.setMsg(msg);
        createMessage.setPicture(picture);
        objectOS.writeObject(createMessage);
        objectOS.flush();
    }

    /*
     * This method is used for sending a voice Message
     * 
     * @param msg - The message which the user generates
     */
    public static void sendVoiceMessage(byte[] audio) throws IOException {
        Message createMessage = new Message();
        createMessage.setName(username);
        createMessage.setType(MessageType.VOICE);
        createMessage.setStatus(Status.AWAY);
        createMessage.setVoiceMsg(audio);
        createMessage.setPicture(picture);
        objectOS.writeObject(createMessage);
        objectOS.flush();
    }

    /*
     * This method is used for sending a normal Message
     * 
     * @param msg - The message which the user generates
     */
    public static void sendStatusUpdate(Status status) throws IOException {
        Message createMessage = new Message();
        createMessage.setName(username);
        createMessage.setType(MessageType.STATUS);
        createMessage.setStatus(status);
        createMessage.setPicture(picture);
        objectOS.writeObject(createMessage);
        objectOS.flush();
    }

    /* This method is used to send a connecting message */
    public static void sendConnectedMessage() throws IOException {
        Message createMessage = new Message();
        createMessage.setName(username);
        createMessage.setType(CONNECTED);
        createMessage.setMsg(HASCONNECTED);
        createMessage.setPicture(picture);
        objectOS.writeObject(createMessage);
    }

}
