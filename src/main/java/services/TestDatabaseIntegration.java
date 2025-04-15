package services;

import models.User;
import models.Chat;
import services.UserService;
import services.ChatService;
import services.SubscriptionService;

public class TestDatabaseIntegration {

    public static void main(String[] args) {

        // Initialize services
        UserService userService = new UserService();
        ChatService chatService = new ChatService();
        SubscriptionService subscriptionService = new SubscriptionService();

        // Create and save test users
        User user1 = new User();
        user1.setEmail("u3@example.com");
        user1.setUsername("u3");
        user1.setPassword("password12");
        user1.setNickname("User On");
        userService.registerUser(user1);

        User user2 = new User();
        user2.setEmail("u4@example.com");
        user2.setUsername("u4");
        user2.setPassword("password13");
        user2.setNickname("Use Two");
        userService.registerUser(user2);

        // Start a test chat
        Chat chat1 = chatService.startChat();

        // Subscribe users to the chat
        subscriptionService.subscribeUserToChat(user1, chat1);
        subscriptionService.subscribeUserToChat(user2, chat1);

        // Get subscriptions for the chat
        System.out.println("\nSubscriptions for Chat ID " + chat1.getId() + ":");
        subscriptionService.getSubscriptionsForChat(chat1).forEach(subscription -> {
            System.out.println(subscription.getUser().getNickname());
        });

        // Unsubscribe a user from the chat
        subscriptionService.unsubscribeUserFromChat(user1, chat1);

        // Get subscriptions again after unsubscription
        System.out.println("\nSubscriptions after unsubscription:");
        subscriptionService.getSubscriptionsForChat(chat1).forEach(subscription -> {
            System.out.println(subscription.getUser().getNickname());
        });

        // Stop the chat and save its log
        String logContent = "This is a sample log for the test chat.";
        chatService.stopChat(chat1, logContent);
    }
}
