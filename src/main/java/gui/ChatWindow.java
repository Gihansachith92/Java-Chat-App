package gui;

import models.User;
import network.ChatService;
import org.hibernate.Session;
import org.hibernate.query.Query;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ChatWindow extends JFrame {

    private JTextArea messageArea;
    private JTextField messageField;
    private JButton sendButton;
    private JButton leaveChatButton;
    private JButton subscribeButton; // Button for self-subscribing to chats
    private JButton unsubscribeButton; // Button for self-unsubscribing from chats
    private JComboBox<String> recipientComboBox; // Dropdown for selecting message recipients
    private JComboBox<String> chatComboBox; // Dropdown for selecting chats to subscribe/unsubscribe
    private JCheckBox privateMessageCheckBox; // Checkbox to toggle private messaging
    private List<String> connectedUsers; // List of connected users for private messaging

    private User user; // The logged-in user participating in the chat
    private boolean isChatActive = true; // To track if the user is still in the chat
    private ChatService chatService; // RMI chat service
    private services.SubscriptionService subscriptionService; // Subscription service for observer pattern
    private ChatWindowObserver observer; // Observer for subscription events

    public ChatWindow(User user) {
        this.user = user;
        this.connectedUsers = new ArrayList<>();
        this.subscriptionService = new services.SubscriptionService();

        setTitle("Chat Window - Welcome " + user.getNickname());
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create and register the observer
        this.observer = new ChatWindowObserver(this);
        subscriptionService.addObserver(this.observer);

        // Connect to the RMI chat service
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            chatService = (ChatService) registry.lookup("ChatService");

            // Notify that the user has joined
            chatService.notifyUserJoined(user.getNickname());

            // Add the current user to the connected users list
            connectedUsers.add(user.getNickname());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to connect to chat server: " + e.getMessage());
            e.printStackTrace();
        }

        // Add window closing handler to leave chat properly
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                leaveChat();
            }
        });

        // Create components
        messageArea = new JTextArea();
        messageArea.setEditable(false); // Prevent editing of received messages
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);

        messageField = new JTextField(30);
        sendButton = new JButton("Send");
        leaveChatButton = new JButton("Leave Chat");

        // Create subscription components
        subscribeButton = new JButton("Subscribe");
        unsubscribeButton = new JButton("Unsubscribe");
        chatComboBox = new JComboBox<>();

        // Populate chat dropdown with available chats
        populateChatsDropdown();

        // Create private messaging components
        recipientComboBox = new JComboBox<>();
        recipientComboBox.addItem("Everyone"); // Default option for broadcasting

        privateMessageCheckBox = new JCheckBox("Private Message");
        privateMessageCheckBox.addActionListener(e -> {
            recipientComboBox.setEnabled(privateMessageCheckBox.isSelected());
        });

        // Initially disable the recipient dropdown
        recipientComboBox.setEnabled(false);

        // Layout setup
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // Create user profile panel
        JPanel profilePanel = new JPanel();
        profilePanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        // Add user profile picture (placeholder for now)
        JLabel profilePicLabel = new JLabel();
        if (user.getProfilePicture() != null && !user.getProfilePicture().isEmpty()) {
            try {
                ImageIcon profilePic = new ImageIcon(user.getProfilePicture());
                // Resize the image to fit
                Image img = profilePic.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
                profilePicLabel.setIcon(new ImageIcon(img));
            } catch (Exception e) {
                // If image loading fails, use a placeholder
                profilePicLabel.setText("[No Image]");
            }
        } else {
            profilePicLabel.setText("[No Image]");
        }
        profilePanel.add(profilePicLabel);

        // Add user nickname
        JLabel nicknameLabel = new JLabel("Nickname: " + user.getNickname());
        profilePanel.add(nicknameLabel);

        // Add profile panel to the main panel
        panel.add(profilePanel, BorderLayout.NORTH);

        // Add message area (scrollable)
        panel.add(new JScrollPane(messageArea), BorderLayout.CENTER);

        // Add input panel (message field + buttons)
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new FlowLayout());

        // Add private messaging controls
        JPanel privateMessagePanel = new JPanel();
        privateMessagePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        privateMessagePanel.add(privateMessageCheckBox);
        privateMessagePanel.add(recipientComboBox);

        // Add subscription controls
        JPanel subscriptionPanel = new JPanel();
        subscriptionPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        subscriptionPanel.add(new JLabel("Available Chats:"));
        subscriptionPanel.add(chatComboBox);
        subscriptionPanel.add(subscribeButton);
        subscriptionPanel.add(unsubscribeButton);

        // Add all components to the input panel
        inputPanel.add(messageField);
        inputPanel.add(sendButton);
        inputPanel.add(leaveChatButton);

        // Create a panel for the bottom section with private messaging and subscription controls
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());

        // Create a panel for the controls
        JPanel controlsPanel = new JPanel();
        controlsPanel.setLayout(new GridLayout(2, 1));
        controlsPanel.add(privateMessagePanel);
        controlsPanel.add(subscriptionPanel);

        bottomPanel.add(controlsPanel, BorderLayout.NORTH);
        bottomPanel.add(inputPanel, BorderLayout.SOUTH);

        panel.add(bottomPanel, BorderLayout.SOUTH);

        add(panel);

        // Display a welcome message when the user joins
        displayMessage("Chat started at: " + LocalDateTime.now());
        displayMessage(user.getNickname() + " has joined: " + LocalDateTime.now());

        // Button actions
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        leaveChatButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                leaveChat();
            }
        });

        // Add action listeners for subscribe and unsubscribe buttons
        subscribeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                subscribeToChat();
            }
        });

        unsubscribeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                unsubscribeFromChat();
            }
        });

        // Handle "Enter" key press for sending messages
        messageField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
    }

    // Method to display a message in the chat window
    private void displayMessage(String message) {
        messageArea.append(message + "\n");

        // Check if this is a user join notification
        if (message.contains("has joined the chat")) {
            String nickname = message.substring(0, message.indexOf(" has joined"));
            if (!nickname.equals(user.getNickname()) && !connectedUsers.contains(nickname)) {
                connectedUsers.add(nickname);
                recipientComboBox.addItem(nickname);
            }
        }

        // Check if this is a user leave notification
        if (message.contains("has left the chat")) {
            String nickname = message.substring(0, message.indexOf(" has left"));
            if (connectedUsers.contains(nickname)) {
                connectedUsers.remove(nickname);
                recipientComboBox.removeItem(nickname);
            }
        }
    }

    // Method to send a message
    private void sendMessage() {
        if (!isChatActive) {
            JOptionPane.showMessageDialog(this, "You have already left the chat.");
            return;
        }

        String message = messageField.getText().trim();
        if (message.isEmpty()) {
            return; // Do nothing if the input is empty
        }

        if (message.equalsIgnoreCase("Bye")) {
            leaveChat(); // Leave the chat if the user types "Bye"
            return;
        }

        try {
            boolean isPrivate = privateMessageCheckBox.isSelected();
            String recipient = (String) recipientComboBox.getSelectedItem();

            if (isPrivate && recipient != null && !recipient.equals("Everyone")) {
                // Send a private message to the selected recipient
                chatService.sendPrivateMessage(user.getNickname(), recipient, message);

                // Display the private message locally
                displayMessage("[Private to " + recipient + "] " + user.getNickname() + ": " + message);
            } else {
                // Broadcast the message to all users via the chat service
                chatService.broadcastMessage(user.getNickname() + ": " + message);

                // Display the message locally
                displayMessage(user.getNickname() + ": " + message);
            }

            messageField.setText(""); // Clear the input field
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to send message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Method to leave the chat
    private void leaveChat() {
        if (!isChatActive) {
            JOptionPane.showMessageDialog(this, "You have already left the chat.");
            return;
        }

        try {
            // Notify the server that the user has left
            if (chatService != null) {
                chatService.notifyUserLeft(user.getNickname());
            }

            // Display a message indicating that the user has left
            LocalDateTime leaveTime = LocalDateTime.now();
            String leaveMessage = user.getNickname() + " left: " + leaveTime;
            displayMessage(leaveMessage);

            // Check if this is the last user in the chat
            if (connectedUsers.size() <= 1) {
                // This is the last user, so end the chat
                LocalDateTime endTime = LocalDateTime.now();
                String endMessage = "Chat stopped at: " + endTime;
                displayMessage(endMessage);

                // Save the chat to a .txt file
                saveChatToFile(endTime);
            }
        } catch (Exception e) {
            System.err.println("Error notifying server about user leaving: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Unregister the observer
            if (subscriptionService != null && observer != null) {
                subscriptionService.removeObserver(observer);
            }

            isChatActive = false; // Mark chat as inactive
            JOptionPane.showMessageDialog(this, "You have left the chat.");

            // Close this window after leaving
            dispose();
        }
    }

    // Method to save the chat to a .txt file
    private void saveChatToFile(LocalDateTime endTime) {
        try {
            // Get the chat content
            String chatContent = messageArea.getText();

            // Create a new chat in the database
            services.ChatService chatService = new services.ChatService();
            models.Chat chat = chatService.startChat();

            // Set the end time and save the chat log
            chat.setEndTime(endTime);
            chatService.stopChat(chat, chatContent);

            System.out.println("Chat saved to file: " + chat.getFilePath());
        } catch (Exception e) {
            System.err.println("Error saving chat to file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Method called when a user is subscribed to the chat
    public void userSubscribed(User user) {
        if (!user.getNickname().equals(this.user.getNickname())) {
            // Add the user to the connected users list if not already present
            if (!connectedUsers.contains(user.getNickname())) {
                connectedUsers.add(user.getNickname());
                recipientComboBox.addItem(user.getNickname());
            }

            // Display a message indicating that the user has subscribed
            displayMessage(user.getNickname() + " has subscribed to this chat.");
        }
    }

    // Method called when a user is unsubscribed from the chat
    public void userUnsubscribed(User user) {
        // Remove the user from the connected users list
        connectedUsers.remove(user.getNickname());
        recipientComboBox.removeItem(user.getNickname());

        // Display a message indicating that the user has unsubscribed
        displayMessage(user.getNickname() + " has unsubscribed from this chat.");
    }

    // Method to populate the chat dropdown with available chats
    private void populateChatsDropdown() {
        try {
            // Clear existing items
            chatComboBox.removeAllItems();

            // Get all chats from the database
            services.ChatService chatDbService = new services.ChatService();
            List<models.Chat> chats = getAllChats();

            // Add chats to the dropdown
            for (models.Chat chat : chats) {
                chatComboBox.addItem("Chat " + chat.getId());
            }
        } catch (Exception e) {
            System.err.println("Error populating chats dropdown: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Method to get all chats from the database
    private List<models.Chat> getAllChats() {
        try (Session session = utils.HibernateUtil.getSessionFactory().openSession()) {
            Query<models.Chat> query = session.createQuery("FROM Chat", models.Chat.class);
            return query.list();
        } catch (Exception e) {
            System.err.println("Error getting chats: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Method to subscribe to a chat
    private void subscribeToChat() {
        if (chatComboBox.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select a chat to subscribe to.");
            return;
        }

        try {
            // Get the selected chat ID
            String chatItem = (String) chatComboBox.getSelectedItem();
            int chatId = Integer.parseInt(chatItem.replace("Chat ", ""));

            // Get the chat from the database
            models.Chat chat = getChatById(chatId);

            if (chat != null) {
                // Subscribe the user to the chat
                subscriptionService.subscribeUserToChat(user, chat);
                JOptionPane.showMessageDialog(this, "You have subscribed to Chat " + chatId);
            } else {
                JOptionPane.showMessageDialog(this, "Chat not found.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error subscribing to chat: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Method to unsubscribe from a chat
    private void unsubscribeFromChat() {
        if (chatComboBox.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select a chat to unsubscribe from.");
            return;
        }

        try {
            // Get the selected chat ID
            String chatItem = (String) chatComboBox.getSelectedItem();
            int chatId = Integer.parseInt(chatItem.replace("Chat ", ""));

            // Get the chat from the database
            models.Chat chat = getChatById(chatId);

            if (chat != null) {
                // Unsubscribe the user from the chat
                subscriptionService.unsubscribeUserFromChat(user, chat);
                JOptionPane.showMessageDialog(this, "You have unsubscribed from Chat " + chatId);
            } else {
                JOptionPane.showMessageDialog(this, "Chat not found.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error unsubscribing from chat: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Method to get a chat by ID
    private models.Chat getChatById(int chatId) {
        try (Session session = utils.HibernateUtil.getSessionFactory().openSession()) {
            return session.get(models.Chat.class, chatId);
        } catch (Exception e) {
            System.err.println("Error getting chat by ID: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
