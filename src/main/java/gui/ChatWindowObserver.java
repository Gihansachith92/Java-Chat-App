package gui;

import models.Chat;
import models.User;
import services.ChatObserver;

/**
 * Concrete implementation of ChatObserver for the ChatWindow
 */
public class ChatWindowObserver implements ChatObserver {
    
    private ChatWindow chatWindow;
    
    public ChatWindowObserver(ChatWindow chatWindow) {
        this.chatWindow = chatWindow;
    }
    
    @Override
    public void onSubscribe(User user, Chat chat) {
        // Notify the chat window that a user has subscribed
        chatWindow.userSubscribed(user);
    }
    
    @Override
    public void onUnsubscribe(User user, Chat chat) {
        // Notify the chat window that a user has unsubscribed
        chatWindow.userUnsubscribed(user);
    }
}