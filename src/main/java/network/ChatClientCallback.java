package network;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface for client callbacks to receive messages from the chat server.
 * This allows the server to push messages to connected clients.
 */
public interface ChatClientCallback extends Remote {
    /**
     * Called by the server to deliver a message to the client.
     * 
     * @param message The message to be displayed
     * @throws RemoteException If a remote communication error occurs
     */
    void receiveMessage(String message) throws RemoteException;

    /**
     * Called by the server to notify the client that a user has joined the chat.
     * 
     * @param nickname The nickname of the user who joined
     * @throws RemoteException If a remote communication error occurs
     */
    void userJoined(String nickname) throws RemoteException;

    /**
     * Called by the server to notify the client that a user has left the chat.
     * 
     * @param nickname The nickname of the user who left
     * @throws RemoteException If a remote communication error occurs
     */
    void userLeft(String nickname) throws RemoteException;
}
