package com.client.chatwindow;

import static com.messages.MessageType.CONNECTED;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import com.client.login.LoginController;
import com.messages.Message;
import com.messages.MessageType;
import com.messages.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Listener implements Runnable {

    private static final String COULD_NOT_CONNECT_TO_SERVER = "Could not connect to server";
    private static final String HASCONNECTED = "has connected";
    public String hostname;
    public int port;
    public static String username;
    public ChatController controller;

    Logger logger = LoggerFactory.getLogger(Listener.class);

    private static String picture;
    private static ObjectOutputStream oos;
    private Socket socket;
    private ObjectInputStream input;

    public Listener(String hostname, int port, String username, String picture, ChatController controller) {
        this.hostname = hostname;
        this.port = port;
        Listener.username = username;
        Listener.picture = picture;
        this.controller = controller;
    }

    public void run() {
        // establish connection
        try {
            socket = new Socket(hostname, port);
            input = new ObjectInputStream(socket.getInputStream());
            oos = new ObjectOutputStream(socket.getOutputStream());
            LoginController.getInstance().showScene();
        } catch (IOException e) {
            LoginController.getInstance().showErrorDialog(COULD_NOT_CONNECT_TO_SERVER);
            logger.error("Could not Connect");
        } 
        logger.info("Connection accepted {}:{}", socket.getInetAddress(), socket.getPort());
        // 
        try {
            sendConnectedMessage();
            logger.info("Sockets in and out ready!");
            while (socket.isConnected()) {
                Message message = null;
                message = (Message) input.readObject();

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
        oos.writeObject(createMessage);
        oos.flush();
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
        oos.writeObject(createMessage);
        oos.flush();
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
        oos.writeObject(createMessage);
        oos.flush();
    }

    /* This method is used to send a connecting message */
    public static void sendConnectedMessage() throws IOException {
        Message createMessage = new Message();
        createMessage.setName(username);
        createMessage.setType(CONNECTED);
        createMessage.setMsg(HASCONNECTED);
        createMessage.setPicture(picture);
        oos.writeObject(createMessage);
    }

}
