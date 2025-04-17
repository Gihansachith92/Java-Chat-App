package network;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ChatService extends Remote {
    // Method to broadcast a message to all subscribed users
    void broadcastMessage(String message) throws RemoteException;

    // Method to notify when a user joins a chat
    void notifyUserJoined(String nickname) throws RemoteException;

    // Method to notify when a user leaves a chat
    void notifyUserLeft(String nickname) throws RemoteException;

    // Method to register a client callback
    void registerCallback(String nickname, ChatClientCallback callback) throws RemoteException;

    // Method to unregister a client callback
    void unregisterCallback(String nickname) throws RemoteException;

    // Method to notify all subscribers about a new chat
    void notifyNewChat(int chatId) throws RemoteException;
}
