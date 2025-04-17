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
    private JButton viewChatsButton;
    private JButton subscribeUserButton;
    private JButton unsubscribeUserButton;
    private JButton removeUserButton;
    private JButton backButton;

    // WhatsApp colors
    private static final Color WHATSAPP_GREEN = new Color(0, 168, 132);
    private static final Color WHATSAPP_LIGHT_GREEN = new Color(220, 248, 198);
    private static final Color WHATSAPP_BACKGROUND = new Color(230, 230, 230);
    private static final Color WHATSAPP_HEADER = new Color(32, 44, 51);
    private static final Color WHATSAPP_MESSAGE_TEXT = new Color(0, 0, 0);

    public AdminPanel() {
        setTitle("WhatsApp Admin Panel");
        setSize(400, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create components with WhatsApp styling
        createChatButton = new JButton("Create Chat");
        styleButton(createChatButton);

        viewUsersButton = new JButton("View Users");
        styleButton(viewUsersButton);

        viewChatsButton = new JButton("View Chats");
        styleButton(viewChatsButton);

        subscribeUserButton = new JButton("Subscribe User");
        styleButton(subscribeUserButton);

        unsubscribeUserButton = new JButton("Unsubscribe User");
        styleButton(unsubscribeUserButton);

        removeUserButton = new JButton("Remove User");
        styleButton(removeUserButton);

        backButton = new JButton("Back to Login");
        styleButton(backButton);

        // Create WhatsApp-style header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(WHATSAPP_HEADER);
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));

        JLabel titleLabel = new JLabel("WhatsApp Admin");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        // Create main content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new GridLayout(7, 1, 10, 10));
        contentPanel.setBackground(WHATSAPP_BACKGROUND);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        contentPanel.add(createChatButton);
        contentPanel.add(viewUsersButton);
        contentPanel.add(viewChatsButton);
        contentPanel.add(subscribeUserButton);
        contentPanel.add(unsubscribeUserButton);
        contentPanel.add(removeUserButton);
        contentPanel.add(backButton);

        // Main panel with BorderLayout
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(WHATSAPP_BACKGROUND);
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);

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
                StringBuilder userList = new StringBuilder("<html><div style='font-family:Arial; font-size:12px; padding:10px;'>");
                userList.append("<h2 style='color:#075E54;'>Registered Users</h2>");
                userList.append("<table style='width:100%; border-collapse:collapse;'>");
                userList.append("<tr style='background-color:#075E54; color:white;'><th style='padding:8px;'>ID</th><th style='padding:8px;'>Nickname</th></tr>");

                boolean alternate = false;
                for (User user : userService.getAllUsers()) {
                    String rowStyle = alternate ? "background-color:#ECE5DD;" : "background-color:#FFFFFF;";
                    userList.append("<tr style='").append(rowStyle).append("'>");
                    userList.append("<td style='padding:8px; text-align:center;'>").append(user.getId()).append("</td>");
                    userList.append("<td style='padding:8px;'>").append(user.getNickname()).append("</td>");
                    userList.append("</tr>");
                    alternate = !alternate;
                }

                userList.append("</table></div></html>");
                JOptionPane.showMessageDialog(AdminPanel.this, userList.toString(), "WhatsApp Users", JOptionPane.PLAIN_MESSAGE);
            }
        });

        viewChatsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                StringBuilder chatList = new StringBuilder("<html><div style='font-family:Arial; font-size:12px; padding:10px;'>");
                chatList.append("<h2 style='color:#075E54;'>Available Chats</h2>");
                chatList.append("<table style='width:100%; border-collapse:collapse;'>");
                chatList.append("<tr style='background-color:#075E54; color:white;'>");
                chatList.append("<th style='padding:8px;'>Chat ID</th>");
                chatList.append("<th style='padding:8px;'>Created</th>");
                chatList.append("<th style='padding:8px;'>Status</th>");
                chatList.append("</tr>");

                boolean alternate = false;
                for (Chat chat : chatService.getAllChats()) {
                    String rowStyle = alternate ? "background-color:#ECE5DD;" : "background-color:#FFFFFF;";
                    chatList.append("<tr style='").append(rowStyle).append("'>");
                    chatList.append("<td style='padding:8px; text-align:center;'>").append(chat.getId()).append("</td>");
                    chatList.append("<td style='padding:8px;'>").append(chat.getStartTime()).append("</td>");

                    // Determine chat status
                    String status = chat.getEndTime() == null ? "Active" : "Ended";
                    String statusColor = chat.getEndTime() == null ? "green" : "red";
                    chatList.append("<td style='padding:8px; color:").append(statusColor).append(";'>").append(status).append("</td>");

                    chatList.append("</tr>");
                    alternate = !alternate;
                }

                chatList.append("</table></div></html>");
                JOptionPane.showMessageDialog(AdminPanel.this, chatList.toString(), "WhatsApp Chats", JOptionPane.PLAIN_MESSAGE);
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
                    User user = userService.getUserById(userId);
                    Chat chat = chatService.getChatById(chatId);
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
                    User user = userService.getUserById(userId);
                    Chat chat = chatService.getChatById(chatId);
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
                    boolean success = userService.deleteUser(userId);
                    if (success) {
                        JOptionPane.showMessageDialog(AdminPanel.this, "User removed successfully!");
                    } else {
                        JOptionPane.showMessageDialog(AdminPanel.this, "User not found or could not be removed.");
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(AdminPanel.this, "Invalid input.");
                }
            }
        });

        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // Close admin panel
                new LoginScreen().setVisible(true); // Open login screen
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AdminPanel().setVisible(true));
    }

    // Helper method to style buttons with WhatsApp theme
    private void styleButton(JButton button) {
        button.setBackground(WHATSAPP_GREEN);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
    }
}
