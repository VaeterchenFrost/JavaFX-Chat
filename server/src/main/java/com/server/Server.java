package com.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.exception.DuplicateUsernameException;
import com.messages.Message;
import com.messages.MessageType;
import com.messages.Status;
import com.messages.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server {

    /* Setting up variables */
    private static final int PORT = 9001;
    private static final HashMap<String, User> names = new HashMap<>();
    private static HashSet<ObjectOutputStream> writers = new HashSet<>();
    private static ArrayList<User> users = new ArrayList<>();
    static Logger logger = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) throws Exception {
        logger.info("The chat server is running.");
        ServerSocket listener = new ServerSocket(PORT);

        try {
            while (true) {
                new Handler(listener.accept()).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            listener.close();
        }
    }

    private static class Handler extends Thread {
        private String name;
        private Socket socket;
        private Logger logger = LoggerFactory.getLogger(Handler.class);
        private User user;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            logger.info("Attempting to connect a user...");
            try (ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                    ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream())) {
                Message firstMessage = (Message) input.readObject();
                checkDuplicateUsername(firstMessage);
                writers.add(output);
                sendNotification(firstMessage);
                addToList();

                while (socket.isConnected()) {
                    Message inputmsg = (Message) input.readObject();
                    if (inputmsg != null) {
                        logger.info("{} - {}: {}",
                                new Object[] { inputmsg.getType(), inputmsg.getName(), inputmsg.getMsg() });
                        switch (inputmsg.getType()) {
                            case USER:
                                write(inputmsg);
                                break;
                            case VOICE:
                                write(inputmsg);
                                break;
                            case CONNECTED:
                                addToList();
                                break;
                            case STATUS:
                                changeStatus(inputmsg);
                                break;
                        }
                    }
                }
            } catch (SocketException socketException) {
                logger.error("Socket Exception for user {}", name);
            } catch (DuplicateUsernameException duplicateException) {
                logger.error("Duplicate Username : {}", name);
            } catch (IOException e) {
                logger.error(String.format("%s in run() method for user: %s", e.getClass().getSimpleName(), name), e);
            } catch (ClassNotFoundException e) {
                logger.error("Encountered a problem communicating with user {}: {}", name, e.getMessage());
            } finally {
                closeConnections();
            }
        }

        private Message changeStatus(Message inputmsg) throws IOException {
            logger.debug("{} has changed status to {}", inputmsg.getName(), inputmsg.getStatus());
            Message msg = new Message();
            msg.setName(user.getName());
            msg.setType(MessageType.STATUS);
            msg.setMsg("");
            User userObj = names.get(name);
            userObj.setStatus(inputmsg.getStatus());
            write(msg);
            return msg;
        }

        private synchronized void checkDuplicateUsername(Message firstMessage) throws DuplicateUsernameException {
            logger.info("{} is trying to connect", firstMessage.getName());
            if (!names.containsKey(firstMessage.getName())) {
                this.name = firstMessage.getName();
                user = new User();
                user.setName(firstMessage.getName());
                user.setStatus(Status.ONLINE);
                user.setPicture(firstMessage.getPicture());

                users.add(user);
                names.put(name, user);

                logger.info("{} has been added to the list", name);
            } else {
                logger.error("{} is already connected", firstMessage.getName());
                throw new DuplicateUsernameException(firstMessage.getName() + " is already connected");
            }
        }

        private Message sendNotification(Message firstMessage) throws IOException {
            Message msg = new Message();
            msg.setMsg("has joined the chat.");
            msg.setType(MessageType.NOTIFICATION);
            msg.setName(firstMessage.getName());
            msg.setPicture(firstMessage.getPicture());
            write(msg);
            return msg;
        }

        private Message removeFromList() throws IOException {
            logger.debug("removeFromList() method Enter");
            Message msg = new Message();
            msg.setMsg("has left the chat.");
            msg.setType(MessageType.DISCONNECTED);
            msg.setName("SERVER");
            msg.setUserlist(names);
            write(msg);
            logger.debug("removeFromList() method Exit");
            return msg;
        }

        /*
         * For displaying that a user has joined the server
         */
        private Message addToList() throws IOException {
            Message msg = new Message();
            msg.setMsg("Welcome, You have now joined the server! Enjoy chatting!");
            msg.setType(MessageType.CONNECTED);
            msg.setName("SERVER");
            write(msg);
            return msg;
        }

        /*
         * Creates and sends a Message type to the listeners.
         */
        private void write(Message msg) throws IOException {
            for (ObjectOutputStream writer : writers) {
                msg.setUserlist(names);
                msg.setUsers(users);
                msg.setOnlineCount(names.size());
                writer.writeObject(msg);
                writer.reset();
            }
        }

        /*
         * Once a user has been disconnected, we close the open connections and remove
         * the writers
         */
        private synchronized void closeConnections() {
            logger.debug("closeConnections() method Enter");
            if (logger.isInfoEnabled())
                logger.info("HashMap names: {} writers: {} usersList size: {}",
                        new String[] { Integer.toString(names.size()), Integer.toString(writers.size()),
                                Integer.toString(users.size()) });
            if (name != null) {
                names.remove(name);
                logger.info("User: {} has been removed!", name);
            }
            if (user != null) {
                users.remove(user);
                logger.info("User object: {} has been removed!", user);
            }
            try {
                removeFromList();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (logger.isInfoEnabled())
                logger.info("HashMap names: {} writers: {} usersList size: {}",
                        new String[] { Integer.toString(names.size()), Integer.toString(writers.size()),
                                Integer.toString(users.size()) });
            logger.debug("closeConnections() method Exit");
        }
    }
}
