package network;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class ChatServerImpl extends UnicastRemoteObject implements ChatService {

    private List<String> connectedUsers; // List of connected user nicknames

    public ChatServerImpl() throws RemoteException {
        connectedUsers = new ArrayList<>();
    }

    @Override
    public void broadcastMessage(String message) throws RemoteException {
        System.out.println("[Broadcast] " + message);
        for (String user : connectedUsers) {
            System.out.println("Message sent to " + user + ": " + message);
        }
    }

    @Override
    public void notifyUserJoined(String nickname) throws RemoteException {
        connectedUsers.add(nickname);
        System.out.println(nickname + " has joined the chat.");
        broadcastMessage(nickname + " has joined the chat.");
    }

    @Override
    public void notifyUserLeft(String nickname) throws RemoteException {
        connectedUsers.remove(nickname);
        System.out.println(nickname + " has left the chat.");
        broadcastMessage(nickname + " has left the chat.");
    }
}
