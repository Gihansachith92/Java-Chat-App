package services;

import models.Chat;
import models.User;

/**
 * Interface for the Observer pattern to handle chat subscriptions
 */
public interface ChatObserver {
    /**
     * Called when a user is subscribed to a chat
     * @param user The user who was subscribed
     * @param chat The chat the user was subscribed to
     */
    void onSubscribe(User user, Chat chat);
    
    /**
     * Called when a user is unsubscribed from a chat
     * @param user The user who was unsubscribed
     * @param chat The chat the user was unsubscribed from
     */
    void onUnsubscribe(User user, Chat chat);
}