package gui;

import models.Chat;
import models.User;
import services.ChatService;
import services.SubscriptionService;
import services.UserService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AdminPanel extends JFrame {

    private JButton createChatButton;
    private JButton viewUsersButton;
    private JButton subscribeUserButton;
    private JButton unsubscribeUserButton;
    private JButton removeUserButton;

    public AdminPanel() {
        setTitle("Admin Panel");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create components
        createChatButton = new JButton("Create Chat");
        viewUsersButton = new JButton("View Users");
        subscribeUserButton = new JButton("Subscribe User");
        unsubscribeUserButton = new JButton("Unsubscribe User");
        removeUserButton = new JButton("Remove User");

        // Layout setup
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(5, 1, 10, 10));
        panel.add(createChatButton);
        panel.add(viewUsersButton);
        panel.add(subscribeUserButton);
        panel.add(unsubscribeUserButton);
        panel.add(removeUserButton);

        add(panel);

        // Button actions
        ChatService chatService = new ChatService();
        UserService userService = new UserService();
        SubscriptionService subscriptionService = new SubscriptionService();

        createChatButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Chat chat = chatService.startChat();
                if (chat != null) {
                    JOptionPane.showMessageDialog(AdminPanel.this, "Chat created successfully!");
                } else {
                    JOptionPane.showMessageDialog(AdminPanel.this, "Failed to create chat.");
                }
            }
        });

        viewUsersButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                StringBuilder userList = new StringBuilder("Registered Users:\n");
                for (User user : userService.getAllUsers()) {
                    userList.append(user.getNickname()).append("\n");
                }
                JOptionPane.showMessageDialog(AdminPanel.this, userList.toString());
            }
        });

        subscribeUserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String userIdInput = JOptionPane.showInputDialog("Enter User ID:");
                String chatIdInput = JOptionPane.showInputDialog("Enter Chat ID:");
                try {
                    int userId = Integer.parseInt(userIdInput);
                    int chatId = Integer.parseInt(chatIdInput);
                    User user = userService.getAllUsers().stream().filter(u -> u.getId() == userId).findFirst().orElse(null);
                    Chat chat = chatService.startChat(); // Replace with actual chat retrieval logic
                    if (user != null && chat != null) {
                        subscriptionService.subscribeUserToChat(user, chat);
                        JOptionPane.showMessageDialog(AdminPanel.this, "User subscribed successfully!");
                    } else {
                        JOptionPane.showMessageDialog(AdminPanel.this, "Invalid User or Chat ID.");
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(AdminPanel.this, "Invalid input.");
                }
            }
        });

        unsubscribeUserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String userIdInput = JOptionPane.showInputDialog("Enter User ID:");
                String chatIdInput = JOptionPane.showInputDialog("Enter Chat ID:");
                try {
                    int userId = Integer.parseInt(userIdInput);
                    int chatId = Integer.parseInt(chatIdInput);
                    User user = userService.getAllUsers().stream().filter(u -> u.getId() == userId).findFirst().orElse(null);
                    Chat chat = chatService.startChat(); // Replace with actual chat retrieval logic
                    if (user != null && chat != null) {
                        subscriptionService.unsubscribeUserFromChat(user, chat);
                        JOptionPane.showMessageDialog(AdminPanel.this, "User unsubscribed successfully!");
                    } else {
                        JOptionPane.showMessageDialog(AdminPanel.this, "Invalid User or Chat ID.");
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(AdminPanel.this, "Invalid input.");
                }
            }
        });

        removeUserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String userIdInput = JOptionPane.showInputDialog("Enter User ID:");
                try {
                    int userId = Integer.parseInt(userIdInput);
                    userService.deleteUser(userId);
                    JOptionPane.showMessageDialog(AdminPanel.this, "User removed successfully!");
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(AdminPanel.this, "Invalid input.");
                }
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AdminPanel().setVisible(true));
    }
}
