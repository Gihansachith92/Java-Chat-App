package network;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServerImpl extends UnicastRemoteObject implements ChatService {

    private List<String> connectedUsers; // List of connected user nicknames
    private Map<String, List<ChatClientCallback>> userCallbacks; // Map of nickname to list of client callbacks
    private Map<String, ChatClientCallback> callbackRegistry; // Map of callback ID to client callback
    private Map<String, String> callbackToUser; // Map of callback ID to nickname
    private ExecutorService threadPool; // Thread pool for handling client requests

    public ChatServerImpl() throws RemoteException {
        connectedUsers = new ArrayList<>();
        userCallbacks = new HashMap<>();
        callbackRegistry = new ConcurrentHashMap<>();
        callbackToUser = new ConcurrentHashMap<>();
        // Create a fixed thread pool with 10 threads
        threadPool = Executors.newFixedThreadPool(10);
        System.out.println("Chat server initialized with thread pool");
    }

    @Override
    public void registerCallback(String nickname, ChatClientCallback callback) throws RemoteException {
        // Legacy method - register with new method and ignore the ID
        registerCallbackWithId(nickname, callback);
    }

    @Override
    public String registerCallbackWithId(String nickname, ChatClientCallback callback) throws RemoteException {
        // Generate a unique ID for this callback
        String callbackId = UUID.randomUUID().toString();

        // Store the callback in the registry
        callbackRegistry.put(callbackId, callback);
        callbackToUser.put(callbackId, nickname);

        // Add the callback to the user's list
        userCallbacks.computeIfAbsent(nickname, k -> new ArrayList<>()).add(callback);

        System.out.println("Registered callback for " + nickname + " with ID " + callbackId);
        return callbackId;
    }

    @Override
    public void unregisterCallback(String nickname) throws RemoteException {
        // Legacy method - remove all callbacks for this user
        List<ChatClientCallback> callbacks = userCallbacks.remove(nickname);
        if (callbacks != null) {
            // Remove all callback IDs for this user
            callbackToUser.entrySet().removeIf(entry -> entry.getValue().equals(nickname));
            System.out.println("Unregistered all callbacks for " + nickname);
        }
    }

    @Override
    public void unregisterCallbackById(String callbackId) throws RemoteException {
        // Get the callback and user
        ChatClientCallback callback = callbackRegistry.remove(callbackId);
        String nickname = callbackToUser.remove(callbackId);

        if (callback != null && nickname != null) {
            // Remove the callback from the user's list
            List<ChatClientCallback> callbacks = userCallbacks.get(nickname);
            if (callbacks != null) {
                callbacks.remove(callback);
                if (callbacks.isEmpty()) {
                    userCallbacks.remove(nickname);
                }
            }
            System.out.println("Unregistered callback " + callbackId + " for " + nickname);
        }
    }

    @Override
    public void broadcastMessage(String message) throws RemoteException {
        System.out.println("[Broadcast] " + message);

        // Deliver the message to all connected clients using the thread pool
        for (Map.Entry<String, List<ChatClientCallback>> entry : userCallbacks.entrySet()) {
            final String nickname = entry.getKey();
            List<ChatClientCallback> callbacks = entry.getValue();

            for (final ChatClientCallback callback : callbacks) {
                // Submit a task to the thread pool for each callback
                threadPool.submit(() -> {
                    try {
                        callback.receiveMessage(message);
                        System.out.println("Message sent to " + nickname + ": " + message);
                    } catch (RemoteException e) {
                        System.err.println("Error sending message to " + nickname + ": " + e.getMessage());
                        // Consider removing the failed callback
                    }
                });
            }
        }
    }

    @Override
    public void notifyUserJoined(String nickname) throws RemoteException {
        connectedUsers.add(nickname);
        System.out.println(nickname + " has joined the chat.");

        // Notify all connected clients that a user has joined using the thread pool
        final String joinMessage = nickname + " has joined the chat.";
        for (Map.Entry<String, List<ChatClientCallback>> entry : userCallbacks.entrySet()) {
            final String userNickname = entry.getKey();
            List<ChatClientCallback> callbacks = entry.getValue();

            // Don't notify the user who joined
            if (!userNickname.equals(nickname)) {
                for (final ChatClientCallback callback : callbacks) {
                    // Submit a task to the thread pool for each callback
                    threadPool.submit(() -> {
                        try {
                            callback.userJoined(nickname);
                            callback.receiveMessage(joinMessage);
                        } catch (RemoteException e) {
                            System.err.println("Error notifying " + userNickname + " about user join: " + e.getMessage());
                        }
                    });
                }
            }
        }
    }

    @Override
    public void notifyUserLeft(String nickname) throws RemoteException {
        connectedUsers.remove(nickname);
        System.out.println(nickname + " has left the chat.");

        // Notify all connected clients that a user has left using the thread pool
        final String leaveMessage = nickname + " has left the chat.";
        for (Map.Entry<String, List<ChatClientCallback>> entry : userCallbacks.entrySet()) {
            final String userNickname = entry.getKey();
            List<ChatClientCallback> callbacks = entry.getValue();

            // Don't notify the user who left
            if (!userNickname.equals(nickname)) {
                for (final ChatClientCallback callback : callbacks) {
                    // Submit a task to the thread pool for each callback
                    threadPool.submit(() -> {
                        try {
                            callback.userLeft(nickname);
                            callback.receiveMessage(leaveMessage);
                        } catch (RemoteException e) {
                            System.err.println("Error notifying " + userNickname + " about user leave: " + e.getMessage());
                        }
                    });
                }
            }
        }

        // Unregister the callback for the user who left
        unregisterCallback(nickname);
    }

    @Override
    public void notifyNewChat(int chatId) throws RemoteException {
        System.out.println("New chat created with ID: " + chatId);

        // Notify all connected clients about the new chat using the thread pool
        final String newChatMessage = "A new chat (ID: " + chatId + ") has been created. Subscribe to join the conversation!";
        for (Map.Entry<String, List<ChatClientCallback>> entry : userCallbacks.entrySet()) {
            final String userNickname = entry.getKey();
            List<ChatClientCallback> callbacks = entry.getValue();

            for (final ChatClientCallback callback : callbacks) {
                // Submit a task to the thread pool for each callback
                threadPool.submit(() -> {
                    try {
                        callback.receiveMessage(newChatMessage);
                        System.out.println("New chat notification sent to " + userNickname);
                    } catch (RemoteException e) {
                        System.err.println("Error notifying " + userNickname + " about new chat: " + e.getMessage());
                    }
                });
            }
        }
    }

    /**
     * Shuts down the thread pool gracefully.
     * This method should be called when the server is shutting down.
     */
    public void shutdown() {
        System.out.println("Shutting down chat server thread pool...");
        if (threadPool != null && !threadPool.isShutdown()) {
            threadPool.shutdown();
        }
    }
}
