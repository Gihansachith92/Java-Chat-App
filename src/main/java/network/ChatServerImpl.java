package network;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatServerImpl extends UnicastRemoteObject implements ChatService {

    private List<String> connectedUsers; // List of connected user nicknames
    private Map<String, ChatClientCallback> clientCallbacks; // Map of nickname to client callback

    public ChatServerImpl() throws RemoteException {
        connectedUsers = new ArrayList<>();
        clientCallbacks = new HashMap<>();
    }

    @Override
    public void registerCallback(String nickname, ChatClientCallback callback) throws RemoteException {
        clientCallbacks.put(nickname, callback);
        System.out.println("Registered callback for " + nickname);
    }

    @Override
    public void unregisterCallback(String nickname) throws RemoteException {
        clientCallbacks.remove(nickname);
        System.out.println("Unregistered callback for " + nickname);
    }

    @Override
    public void broadcastMessage(String message) throws RemoteException {
        System.out.println("[Broadcast] " + message);

        // Deliver the message to all connected clients
        for (Map.Entry<String, ChatClientCallback> entry : clientCallbacks.entrySet()) {
            try {
                entry.getValue().receiveMessage(message);
                System.out.println("Message sent to " + entry.getKey() + ": " + message);
            } catch (RemoteException e) {
                System.err.println("Error sending message to " + entry.getKey() + ": " + e.getMessage());
            }
        }
    }

    @Override
    public void notifyUserJoined(String nickname) throws RemoteException {
        connectedUsers.add(nickname);
        System.out.println(nickname + " has joined the chat.");

        // Notify all connected clients that a user has joined
        String joinMessage = nickname + " has joined the chat.";
        for (Map.Entry<String, ChatClientCallback> entry : clientCallbacks.entrySet()) {
            try {
                if (!entry.getKey().equals(nickname)) { // Don't notify the user who joined
                    entry.getValue().userJoined(nickname);
                    entry.getValue().receiveMessage(joinMessage);
                }
            } catch (RemoteException e) {
                System.err.println("Error notifying " + entry.getKey() + " about user join: " + e.getMessage());
            }
        }
    }

    @Override
    public void notifyUserLeft(String nickname) throws RemoteException {
        connectedUsers.remove(nickname);
        System.out.println(nickname + " has left the chat.");

        // Notify all connected clients that a user has left
        String leaveMessage = nickname + " has left the chat.";
        for (Map.Entry<String, ChatClientCallback> entry : clientCallbacks.entrySet()) {
            try {
                if (!entry.getKey().equals(nickname)) { // Don't notify the user who left
                    entry.getValue().userLeft(nickname);
                    entry.getValue().receiveMessage(leaveMessage);
                }
            } catch (RemoteException e) {
                System.err.println("Error notifying " + entry.getKey() + " about user leave: " + e.getMessage());
            }
        }

        // Unregister the callback for the user who left
        unregisterCallback(nickname);
    }

    @Override
    public void notifyNewChat(int chatId) throws RemoteException {
        System.out.println("New chat created with ID: " + chatId);

        // Notify all connected clients about the new chat
        String newChatMessage = "A new chat (ID: " + chatId + ") has been created. Subscribe to join the conversation!";
        for (Map.Entry<String, ChatClientCallback> entry : clientCallbacks.entrySet()) {
            try {
                entry.getValue().receiveMessage(newChatMessage);
                System.out.println("New chat notification sent to " + entry.getKey());
            } catch (RemoteException e) {
                System.err.println("Error notifying " + entry.getKey() + " about new chat: " + e.getMessage());
            }
        }
    }
}
