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
import java.io.File;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.text.*;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

public class ChatWindow extends JFrame {

    private JTextArea messageArea;
    private JTextField messageField;
    private JButton sendButton;
    private JButton leaveChatButton;
    private JButton subscribeButton; // Button for self-subscribing to chats
    private JButton unsubscribeButton; // Button for self-unsubscribing from chats
    private JButton updateProfileButton; // Button for updating user profile
    private JButton backButton; // Button for navigating back to login screen
    private JComboBox<String> chatComboBox; // Dropdown for selecting chats to subscribe/unsubscribe
    private List<String> connectedUsers; // List of connected users in the chat

    private User user; // The logged-in user participating in the chat
    private boolean isChatActive = true; // To track if the user is still in the chat
    private ChatService chatService; // RMI chat service
    private services.SubscriptionService subscriptionService; // Subscription service for observer pattern
    private ChatWindowObserver observer; // Observer for subscription events
    private network.ChatClientCallback callback; // Callback for receiving messages from the server
    private String callbackId; // ID of the registered callback
    private services.UserService userService; // User service for getting user information
    private models.Chat currentChat; // The current chat the user is viewing

    // Professional/Enterprise theme colors
    private static final Color THEME_PRIMARY = new Color(59, 89, 152);  // Dark blue
    private static final Color THEME_SECONDARY = new Color(223, 227, 238);  // Light blue-gray
    private static final Color THEME_BACKGROUND = new Color(240, 242, 245);  // Very light gray
    private static final Color THEME_HEADER = new Color(35, 53, 91);  // Darker blue
    private static final Color THEME_TEXT = new Color(33, 33, 33);  // Dark gray for text

    public ChatWindow(User user) {
        this.user = user;
        this.connectedUsers = new ArrayList<>();
        this.subscriptionService = new services.SubscriptionService();
        this.userService = new services.UserService();

        setTitle("ChatNest - " + user.getNickname());
        setSize(400, 650); // More phone-like dimensions
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create components first to avoid NullPointerException in displayMessage
        messageArea = new JTextArea();
        messageArea.setEditable(false); // Prevent editing of received messages
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setBackground(THEME_BACKGROUND);
        messageArea.setFont(new Font("Arial", Font.PLAIN, 14));

        // Add mouse listener to detect clicks on profile picture indicators
        messageArea.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                try {
                    // Get the position of the click
                    int pos = messageArea.viewToModel(e.getPoint());

                    // Get the line of the click
                    int line = messageArea.getLineOfOffset(pos);
                    int start = messageArea.getLineStartOffset(line);
                    int end = messageArea.getLineEndOffset(line);
                    String text = messageArea.getText(start, end - start);

                    // Check if the line contains a profile picture indicator
                    if (text.contains("👤")) {
                        // Extract the nickname from the line
                        String nickname;
                        if (text.contains("You")) {
                            nickname = user.getNickname();
                        } else {
                            // Extract nickname from format "👤 nickname - timestamp"
                            nickname = text.substring(text.indexOf("👤") + 2, text.lastIndexOf(" - ")).trim();
                        }

                        // Show the profile picture
                        showProfilePicture(nickname);
                    }
                } catch (Exception ex) {
                    System.err.println("Error handling mouse click: " + ex.getMessage());
                }
            }
        });

        messageField = new JTextField(30);
        messageField.setBackground(Color.WHITE);
        messageField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(THEME_PRIMARY, 1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        messageField.setFont(new Font("Arial", Font.PLAIN, 14));

        sendButton = new JButton("Send");
        sendButton.setBackground(THEME_PRIMARY);
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.setBorderPainted(false);

        leaveChatButton = new JButton("Leave");
        leaveChatButton.setBackground(THEME_PRIMARY);
        leaveChatButton.setForeground(Color.WHITE);
        leaveChatButton.setFocusPainted(false);
        leaveChatButton.setBorderPainted(false);

        updateProfileButton = new JButton("Profile");
        updateProfileButton.setBackground(THEME_PRIMARY);
        updateProfileButton.setForeground(Color.WHITE);
        updateProfileButton.setFocusPainted(false);
        updateProfileButton.setBorderPainted(false);

        backButton = new JButton("Back to Login");
        backButton.setBackground(THEME_PRIMARY);
        backButton.setForeground(Color.WHITE);
        backButton.setFocusPainted(false);
        backButton.setBorderPainted(false);

        // Create and register the observer
        this.observer = new ChatWindowObserver(this);
        subscriptionService.addObserver(this.observer);

        // Connect to the RMI chat service
        boolean isConnected = false;
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            chatService = (ChatService) registry.lookup("ChatService");

            // Create and register the callback
            callback = new network.ChatClientCallbackImpl(this);
            callbackId = chatService.registerCallbackWithId(user.getNickname(), callback);

            // Notify that the user has joined
            chatService.notifyUserJoined(user.getNickname());

            // Add the current user to the connected users list
            connectedUsers.add(user.getNickname());

            // Inform the user that they are connected to the chat server
            System.out.println("Successfully connected to chat server");
            isConnected = true;
        } catch (Exception e) {
            // Inform the user about the connection failure but allow them to continue in local mode
            String errorMsg = "Failed to connect to chat server: " + e.getMessage() + 
                "\n\nYou can still use the chat window in local mode, but messages will not be sent to other users.";
            JOptionPane.showMessageDialog(this, errorMsg, "Connection Error", JOptionPane.WARNING_MESSAGE);

            // Add the current user to the connected users list for local operation
            connectedUsers.add(user.getNickname());

            // Log the error for debugging
            System.err.println("Failed to connect to chat server: " + e.getMessage());
            e.printStackTrace();
        }

        // Add window closing handler to leave chat properly
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                leaveChat();
            }
        });

        // Create subscription components
        subscribeButton = new JButton("Subscribe");
        subscribeButton.setBackground(THEME_PRIMARY);
        subscribeButton.setForeground(Color.WHITE);
        subscribeButton.setFocusPainted(false);
        subscribeButton.setBorderPainted(false);

        unsubscribeButton = new JButton("Unsubscribe");
        unsubscribeButton.setBackground(THEME_PRIMARY);
        unsubscribeButton.setForeground(Color.WHITE);
        unsubscribeButton.setFocusPainted(false);
        unsubscribeButton.setBorderPainted(false);

        chatComboBox = new JComboBox<>();
        chatComboBox.setBackground(Color.WHITE);
        chatComboBox.setBorder(BorderFactory.createLineBorder(THEME_PRIMARY, 1));

        // Populate chat dropdown with available chats
        populateChatsDropdown();

        // Add action listener to chat combo box
        chatComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateCurrentChatFromComboBox();
            }
        });

        // Initialize current chat if user is already subscribed to any chat
        if (chatComboBox.getItemCount() > 0) {
            chatComboBox.setSelectedIndex(0);
            updateCurrentChatFromComboBox();
        }

        // No private messaging components needed for group chat

        // Layout setup
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(THEME_BACKGROUND);

        // Create professional-style header panel
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBackground(THEME_HEADER);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        headerPanel.setPreferredSize(new Dimension(400, 60));

        // Create user profile panel for the header
        JPanel profilePanel = new JPanel();
        profilePanel.setLayout(new BorderLayout());
        profilePanel.setBackground(THEME_HEADER);

        // Add user profile picture (placeholder for now)
        JLabel profilePicLabel = new JLabel();
        if (user.getProfilePicture() != null && !user.getProfilePicture().isEmpty()) {
            try {
                ImageIcon profilePic = new ImageIcon(user.getProfilePicture());
                // Resize the image to fit
                Image img = profilePic.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
                profilePicLabel.setIcon(new ImageIcon(img));
            } catch (Exception e) {
                // If image loading fails, use a placeholder
                profilePicLabel.setText("👤");
                profilePicLabel.setFont(new Font("Arial", Font.PLAIN, 24));
                profilePicLabel.setForeground(Color.WHITE);
            }
        } else {
            profilePicLabel.setText("👤");
            profilePicLabel.setFont(new Font("Arial", Font.PLAIN, 24));
            profilePicLabel.setForeground(Color.WHITE);
        }

        // Create a panel for the profile picture
        JPanel picPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        picPanel.setBackground(THEME_HEADER);
        picPanel.add(profilePicLabel);

        // Add user nickname with professional styling
        JLabel nicknameLabel = new JLabel(user.getNickname());
        nicknameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        nicknameLabel.setForeground(Color.WHITE);

        // Add components to profile panel
        profilePanel.add(picPanel, BorderLayout.WEST);
        profilePanel.add(nicknameLabel, BorderLayout.CENTER);

        // Add profile panel to the header
        headerPanel.add(profilePanel, BorderLayout.CENTER);

        // Add header panel to the main panel
        panel.add(headerPanel, BorderLayout.NORTH);

        // Add message area (scrollable)
        panel.add(new JScrollPane(messageArea), BorderLayout.CENTER);

        // Create professional-style message input area
        JPanel messageInputPanel = new JPanel();
        messageInputPanel.setLayout(new BorderLayout());
        messageInputPanel.setBackground(THEME_BACKGROUND);
        messageInputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create a rounded panel for the message field and send button
        JPanel inputFieldPanel = new JPanel();
        inputFieldPanel.setLayout(new BorderLayout());
        inputFieldPanel.setBackground(Color.WHITE);
        inputFieldPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(THEME_PRIMARY, 1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        // Add message field to the input field panel
        inputFieldPanel.add(messageField, BorderLayout.CENTER);

        // Create a panel for the send button
        JPanel sendButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        sendButtonPanel.setBackground(Color.WHITE);
        sendButtonPanel.add(sendButton);

        // Add send button panel to the input field panel
        inputFieldPanel.add(sendButtonPanel, BorderLayout.EAST);

        // Add input field panel to the message input panel
        messageInputPanel.add(inputFieldPanel, BorderLayout.CENTER);

        // Create a panel for the action buttons
        JPanel actionButtonsPanel = new JPanel();
        actionButtonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        actionButtonsPanel.setBackground(THEME_BACKGROUND);

        // Create new window button
        JButton newWindowButton = new JButton("New Window");
        newWindowButton.setBackground(THEME_PRIMARY);
        newWindowButton.setForeground(Color.WHITE);
        newWindowButton.setFocusPainted(false);
        newWindowButton.setBorderPainted(false);
        newWindowButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openNewChatWindow();
            }
        });

        actionButtonsPanel.add(backButton);
        actionButtonsPanel.add(leaveChatButton);
        actionButtonsPanel.add(updateProfileButton);
        actionButtonsPanel.add(newWindowButton);

        // Add action buttons panel to the message input panel
        messageInputPanel.add(actionButtonsPanel, BorderLayout.SOUTH);

        // Create a panel for the controls (subscription only)
        JPanel controlsPanel = new JPanel();
        controlsPanel.setLayout(new BorderLayout());
        controlsPanel.setBackground(THEME_BACKGROUND);

        // Create a panel for subscription controls
        JPanel subscriptionPanel = new JPanel();
        subscriptionPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        subscriptionPanel.setBackground(THEME_BACKGROUND);
        JLabel chatsLabel = new JLabel("Available Chats:");
        chatsLabel.setForeground(THEME_TEXT);
        subscriptionPanel.add(chatsLabel);
        subscriptionPanel.add(chatComboBox);
        subscriptionPanel.add(subscribeButton);
        subscriptionPanel.add(unsubscribeButton);

        // Add subscription panel to the controls panel
        controlsPanel.add(subscriptionPanel, BorderLayout.CENTER);

        // Create a panel for the bottom section
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.setBackground(THEME_BACKGROUND);

        // Add controls panel and message input panel to the bottom panel
        bottomPanel.add(controlsPanel, BorderLayout.NORTH);
        bottomPanel.add(messageInputPanel, BorderLayout.SOUTH);

        // Add bottom panel to the main panel
        panel.add(bottomPanel, BorderLayout.SOUTH);

        add(panel);

        // Display a welcome message when the user joins
        displayMessage("Chat started at: " + formatDateTime(LocalDateTime.now()));
        displayMessage(user.getNickname() + " has joined: " + formatDateTime(LocalDateTime.now()));

        // Display connection status message
        if (chatService != null) {
            displayMessage("Connected to chat server successfully.");
        } else {
            displayMessage("WARNING: Not connected to chat server. Messages will only be displayed locally.");
        }

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

        // Add action listener for update profile button
        updateProfileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // Close chat window
                new ProfileUpdateScreen(user).setVisible(true); // Open profile update screen
            }
        });

        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // Close chat window
                new LoginScreen().setVisible(true); // Open login screen
            }
        });
    }

    // Helper method to get a user's profile picture as an icon
    private ImageIcon getUserProfileIcon(String nickname, int size) {
        try {
            System.out.println("Getting profile icon for: " + nickname);
            User messageUser = userService.getUserByNickname(nickname);
            if (messageUser != null) {
                System.out.println("User found: " + messageUser.getNickname());
                if (messageUser.getProfilePicture() != null && !messageUser.getProfilePicture().isEmpty()) {
                    System.out.println("Profile picture path: " + messageUser.getProfilePicture());

                    // Check if the file exists
                    File profilePicFile = new File(messageUser.getProfilePicture());
                    if (!profilePicFile.exists()) {
                        System.out.println("Profile picture file does not exist: " + profilePicFile.getAbsolutePath());
                        return null;
                    }

                    try {
                        // Create the profile picture with a direct path
                        ImageIcon profilePic = new ImageIcon(profilePicFile.getAbsolutePath());
                        System.out.println("Profile picture loaded, width: " + profilePic.getIconWidth() + ", height: " + profilePic.getIconHeight());

                        // Check if the image was loaded successfully
                        if (profilePic.getIconWidth() <= 0 || profilePic.getIconHeight() <= 0) {
                            System.out.println("Failed to load profile picture");
                            return null;
                        }

                        // Resize the image to fit
                        Image img = profilePic.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
                        return new ImageIcon(img);
                    } catch (Exception e) {
                        System.err.println("Error loading profile picture: " + e.getMessage());
                        e.printStackTrace();
                        return null;
                    }
                } else {
                    System.out.println("User has no profile picture");
                }
            } else {
                System.out.println("User not found: " + nickname);
            }
        } catch (Exception e) {
            System.err.println("Error getting user profile icon: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // Method to display a user's profile picture in a popup window
    private void showProfilePicture(String nickname) {
        try {
            System.out.println("Showing profile picture for: " + nickname);
            User messageUser = userService.getUserByNickname(nickname);
            if (messageUser != null) {
                System.out.println("User found: " + messageUser.getNickname());
                if (messageUser.getProfilePicture() != null && !messageUser.getProfilePicture().isEmpty()) {
                    System.out.println("Profile picture path: " + messageUser.getProfilePicture());

                    // Check if the file exists
                    File profilePicFile = new File(messageUser.getProfilePicture());
                    if (!profilePicFile.exists()) {
                        System.out.println("Profile picture file does not exist: " + profilePicFile.getAbsolutePath());
                        JOptionPane.showMessageDialog(this, "Profile picture file not found: " + profilePicFile.getAbsolutePath(), "File Not Found", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    try {
                        // Create the profile picture with a direct path
                        ImageIcon profilePic = new ImageIcon(profilePicFile.getAbsolutePath());
                        System.out.println("Profile picture loaded, width: " + profilePic.getIconWidth() + ", height: " + profilePic.getIconHeight());

                        // Check if the image was loaded successfully
                        if (profilePic.getIconWidth() <= 0 || profilePic.getIconHeight() <= 0) {
                            System.out.println("Failed to load profile picture");
                            JOptionPane.showMessageDialog(this, "Failed to load profile picture for " + nickname, "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        // Create a new frame to display the profile picture
                        JFrame picFrame = new JFrame(nickname + "'s Profile Picture");
                        picFrame.setSize(300, 300);
                        picFrame.setLocationRelativeTo(this);

                        // Create a label to display the profile picture
                        JLabel picLabel = new JLabel();
                        picLabel.setHorizontalAlignment(JLabel.CENTER);

                        // Resize the image to fit the frame
                        Image img = profilePic.getImage().getScaledInstance(250, 250, Image.SCALE_SMOOTH);
                        picLabel.setIcon(new ImageIcon(img));

                        // Add the label to the frame
                        picFrame.add(picLabel);

                        // Show the frame
                        picFrame.setVisible(true);
                        System.out.println("Profile picture displayed in popup window");
                    } catch (Exception e) {
                        System.err.println("Error loading profile picture: " + e.getMessage());
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(this, "Error loading profile picture: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    System.out.println("User has no profile picture");
                    JOptionPane.showMessageDialog(this, "No profile picture available for " + nickname, "No Picture", JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                System.out.println("User not found: " + nickname);
                JOptionPane.showMessageDialog(this, "User not found: " + nickname, "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            System.err.println("Error showing profile picture: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error showing profile picture: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Method to create a small, fixed JLabel with the profile picture
    private void addProfilePictureToChat(String nickname, int x, int y) {
        try {
            System.out.println("Adding profile picture for: " + nickname);
            User messageUser = userService.getUserByNickname(nickname);
            if (messageUser != null) {
                System.out.println("User found: " + messageUser.getNickname());
                if (messageUser.getProfilePicture() != null && !messageUser.getProfilePicture().isEmpty()) {
                    System.out.println("Profile picture path: " + messageUser.getProfilePicture());

                    // Check if the file exists
                    File profilePicFile = new File(messageUser.getProfilePicture());
                    if (!profilePicFile.exists()) {
                        System.out.println("Profile picture file does not exist: " + profilePicFile.getAbsolutePath());

                        // Create a default profile picture
                        JLabel picLabel = new JLabel("👤");
                        picLabel.setFont(new Font("Arial", Font.PLAIN, 16));
                        picLabel.setForeground(THEME_PRIMARY);
                        picLabel.setHorizontalAlignment(JLabel.CENTER);
                        picLabel.setBorder(BorderFactory.createLineBorder(THEME_PRIMARY, 1));
                        picLabel.setSize(20, 20);
                        picLabel.setLocation(x, y);
                        picLabel.setOpaque(true);
                        picLabel.setBackground(Color.WHITE);

                        // Add a mouse listener to show a larger profile picture when clicked
                        picLabel.addMouseListener(new java.awt.event.MouseAdapter() {
                            @Override
                            public void mouseClicked(java.awt.event.MouseEvent evt) {
                                JOptionPane.showMessageDialog(ChatWindow.this, 
                                    "No profile picture available for " + nickname, 
                                    "No Picture", JOptionPane.INFORMATION_MESSAGE);
                            }
                        });

                        // Get the layered pane
                        JLayeredPane layeredPane = getLayeredPane();
                        System.out.println("Layered pane obtained: " + layeredPane);

                        // Add the label to the layered pane at a high layer
                        layeredPane.add(picLabel, JLayeredPane.POPUP_LAYER);
                        layeredPane.setComponentZOrder(picLabel, 0);
                        layeredPane.repaint();
                        System.out.println("Default profile picture added to layered pane");
                        return;
                    }

                    try {
                        // Create the profile picture with a direct path
                        ImageIcon profilePic = new ImageIcon(profilePicFile.getAbsolutePath());
                        System.out.println("Profile picture loaded, width: " + profilePic.getIconWidth() + ", height: " + profilePic.getIconHeight());

                        // Check if the image was loaded successfully
                        if (profilePic.getIconWidth() <= 0 || profilePic.getIconHeight() <= 0) {
                            System.out.println("Failed to load profile picture, using default");

                            // Create a default profile picture
                            JLabel picLabel = new JLabel("👤");
                            picLabel.setFont(new Font("Arial", Font.PLAIN, 16));
                            picLabel.setForeground(THEME_PRIMARY);
                            picLabel.setHorizontalAlignment(JLabel.CENTER);
                            picLabel.setBorder(BorderFactory.createLineBorder(THEME_PRIMARY, 1));
                            picLabel.setSize(20, 20);
                            picLabel.setLocation(x, y);
                            picLabel.setOpaque(true);
                            picLabel.setBackground(Color.WHITE);

                            // Add a mouse listener to show a larger profile picture when clicked
                            picLabel.addMouseListener(new java.awt.event.MouseAdapter() {
                                @Override
                                public void mouseClicked(java.awt.event.MouseEvent evt) {
                                    JOptionPane.showMessageDialog(ChatWindow.this, 
                                        "Failed to load profile picture for " + nickname, 
                                        "Error", JOptionPane.ERROR_MESSAGE);
                                }
                            });

                            // Get the layered pane
                            JLayeredPane layeredPane = getLayeredPane();
                            System.out.println("Layered pane obtained: " + layeredPane);

                            // Add the label to the layered pane at a high layer
                            layeredPane.add(picLabel, JLayeredPane.POPUP_LAYER);
                            layeredPane.setComponentZOrder(picLabel, 0);
                            layeredPane.repaint();
                            System.out.println("Default profile picture added to layered pane");
                            return;
                        }

                        // Create a label to display the profile picture
                        JLabel picLabel = new JLabel();
                        picLabel.setHorizontalAlignment(JLabel.CENTER);
                        picLabel.setBorder(BorderFactory.createLineBorder(THEME_PRIMARY, 1));
                        picLabel.setSize(20, 20);
                        picLabel.setLocation(x, y);
                        picLabel.setOpaque(true);
                        picLabel.setBackground(Color.WHITE);
                        System.out.println("Label created at position: " + x + ", " + y);

                        // Resize the image to fit
                        Image img = profilePic.getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH);
                        picLabel.setIcon(new ImageIcon(img));

                        // Add a mouse listener to show a larger profile picture when clicked
                        picLabel.addMouseListener(new java.awt.event.MouseAdapter() {
                            @Override
                            public void mouseClicked(java.awt.event.MouseEvent e) {
                                showProfilePicture(nickname);
                            }
                        });

                        // Get the layered pane
                        JLayeredPane layeredPane = getLayeredPane();
                        System.out.println("Layered pane obtained: " + layeredPane);

                        // Add the label to the layered pane at a high layer
                        layeredPane.add(picLabel, JLayeredPane.POPUP_LAYER);
                        layeredPane.setComponentZOrder(picLabel, 0);
                        layeredPane.repaint();
                        System.out.println("Profile picture added to layered pane");
                    } catch (Exception e) {
                        System.err.println("Error adding profile picture to chat: " + e.getMessage());
                        e.printStackTrace();

                        // Create a default profile picture
                        JLabel picLabel = new JLabel("👤");
                        picLabel.setFont(new Font("Arial", Font.PLAIN, 16));
                        picLabel.setForeground(THEME_PRIMARY);
                        picLabel.setHorizontalAlignment(JLabel.CENTER);
                        picLabel.setBorder(BorderFactory.createLineBorder(THEME_PRIMARY, 1));
                        picLabel.setSize(20, 20);
                        picLabel.setLocation(x, y);
                        picLabel.setOpaque(true);
                        picLabel.setBackground(Color.WHITE);

                        // Store the error message in a final variable
                        final String errorMessage = e.getMessage();

                        // Add a mouse listener to show a larger profile picture when clicked
                        picLabel.addMouseListener(new java.awt.event.MouseAdapter() {
                            @Override
                            public void mouseClicked(java.awt.event.MouseEvent evt) {
                                JOptionPane.showMessageDialog(ChatWindow.this, 
                                    "Error loading profile picture: " + errorMessage, 
                                    "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        });

                        // Get the layered pane
                        JLayeredPane layeredPane = getLayeredPane();
                        System.out.println("Layered pane obtained: " + layeredPane);

                        // Add the label to the layered pane at a high layer
                        layeredPane.add(picLabel, JLayeredPane.POPUP_LAYER);
                        layeredPane.setComponentZOrder(picLabel, 0);
                        layeredPane.repaint();
                        System.out.println("Default profile picture added to layered pane");
                    }
                } else {
                    System.out.println("User has no profile picture, using default");

                    // Create a default profile picture
                    JLabel picLabel = new JLabel("👤");
                    picLabel.setFont(new Font("Arial", Font.PLAIN, 16));
                    picLabel.setForeground(THEME_PRIMARY);
                    picLabel.setHorizontalAlignment(JLabel.CENTER);
                    picLabel.setBorder(BorderFactory.createLineBorder(THEME_PRIMARY, 1));
                    picLabel.setSize(20, 20);
                    picLabel.setLocation(x, y);
                    picLabel.setOpaque(true);
                    picLabel.setBackground(Color.WHITE);

                    // Add a mouse listener to show a larger profile picture when clicked
                    picLabel.addMouseListener(new java.awt.event.MouseAdapter() {
                        @Override
                        public void mouseClicked(java.awt.event.MouseEvent evt) {
                            JOptionPane.showMessageDialog(ChatWindow.this, 
                                "No profile picture available for " + nickname, 
                                "No Picture", JOptionPane.INFORMATION_MESSAGE);
                        }
                    });

                    // Get the layered pane
                    JLayeredPane layeredPane = getLayeredPane();
                    System.out.println("Layered pane obtained: " + layeredPane);

                    // Add the label to the layered pane at a high layer
                    layeredPane.add(picLabel, JLayeredPane.POPUP_LAYER);
                    layeredPane.setComponentZOrder(picLabel, 0);
                    layeredPane.repaint();
                    System.out.println("Default profile picture added to layered pane");
                }
            } else {
                System.out.println("User not found: " + nickname);
            }
        } catch (Exception e) {
            System.err.println("Error getting user profile: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Method to display a message in the chat window with professional formatting
    public void displayMessage(String message) {
        // Format system messages differently
        if (message.contains("Chat started at:") || 
            message.contains("has joined:") || 
            message.contains("has joined the chat") || 
            message.contains("has left the chat") ||
            message.contains("Connected to chat server") ||
            message.contains("WARNING:")) {

            // System message - centered, gray
            messageArea.append("\n  *** " + message + " ***\n");
        } 
        // Format user messages as chat bubbles
        else if (message.contains(":")) {
            // Try to extract the sender's nickname
            String sender;
            String content;
            try {
                sender = message.substring(0, message.indexOf(":")).trim();
                content = message.substring(message.indexOf(":") + 1).trim();
            } catch (Exception e) {
                // If there's an error extracting sender and content, use defaults
                sender = user.getNickname();
                content = message;
            }

            // Get current time for timestamp
            LocalDateTime now = LocalDateTime.now();
            String timestamp = now.getHour() + ":" + String.format("%02d", now.getMinute());

            // Format based on who sent the message
            if (sender.equals(user.getNickname())) {
                // Message from current user - right-aligned
                // Add user profile info and timestamp with emoji
                String profileInfo = "👤 You - " + timestamp;
                messageArea.append("\n" + getSpaces(50) + profileInfo + "\n");

                // Split long messages into multiple lines
                String[] lines = splitMessage(content, 30);

                for (String line : lines) {
                    // Right-align the message with a green indicator
                    messageArea.append(getSpaces(50 - line.length()) + line + "\n");
                }
            } else {
                // Message from other user - left-aligned
                // Add sender's profile picture indicator, nickname and timestamp at the top
                String profileInfo = "👤 " + sender + " - " + timestamp;
                messageArea.append("\n" + profileInfo + "\n");

                // Split long messages into multiple lines
                String[] lines = splitMessage(content, 30);
                for (String line : lines) {
                    // Left-align the message
                    messageArea.append(line + "\n");
                }
            }
        } else {
            // Regular message without sender info - treat it as a message from the current user
            String sender = user.getNickname();
            String content = message;

            // Get current time for timestamp
            LocalDateTime now = LocalDateTime.now();
            String timestamp = now.getHour() + ":" + String.format("%02d", now.getMinute());

            // Add user profile info and timestamp with emoji
            String profileInfo = "👤 You - " + timestamp;
            messageArea.append("\n" + getSpaces(50) + profileInfo + "\n");

            // Split long messages into multiple lines
            String[] lines = splitMessage(content, 30);
            for (String line : lines) {
                // Right-align the message
                messageArea.append(getSpaces(50 - line.length()) + line + "\n");
            }
        }

        // Check if this is a user join notification
        if (message.contains("has joined the chat")) {
            String nickname = message.substring(0, message.indexOf(" has joined"));
            if (!nickname.equals(user.getNickname()) && !connectedUsers.contains(nickname)) {
                connectedUsers.add(nickname);
            }
        }

        // Check if this is a user leave notification
        if (message.contains("has left the chat")) {
            String nickname = message.substring(0, message.indexOf(" has left"));
            if (connectedUsers.contains(nickname)) {
                connectedUsers.remove(nickname);
            }
        }

        // Scroll to the bottom to show the latest message
        messageArea.setCaretPosition(messageArea.getDocument().getLength());
    }

    // Helper method to create spaces for alignment
    private String getSpaces(int count) {
        StringBuilder spaces = new StringBuilder();
        for (int i = 0; i < count; i++) {
            spaces.append(" ");
        }
        return spaces.toString();
    }

    // Helper method to split long messages into multiple lines
    private String[] splitMessage(String message, int maxLength) {
        if (message.length() <= maxLength) {
            return new String[] { message };
        }

        int lines = (message.length() + maxLength - 1) / maxLength;
        String[] result = new String[lines];

        for (int i = 0; i < lines; i++) {
            int start = i * maxLength;
            int end = Math.min(start + maxLength, message.length());
            result[i] = message.substring(start, end);
        }

        return result;
    }

    // Helper method to format LocalDateTime to a user-friendly format
    private String formatDateTime(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");
        return dateTime.format(formatter);
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

        // Check if a chat is selected
        if (currentChat == null) {
            JOptionPane.showMessageDialog(this, 
                "Please subscribe to a chat first before sending messages.", 
                "No Chat Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Check if the user is subscribed to the current chat
        if (!subscriptionService.isUserSubscribedToChat(user, currentChat)) {
            JOptionPane.showMessageDialog(this, 
                "You are not subscribed to this chat. Please subscribe first.", 
                "Not Subscribed", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Check if chatService is available
            if (chatService == null) {
                // If chatService is not available, just display the message locally
                displayMessage(user.getNickname() + ": " + message);

                // Show a warning that the message was not sent to the server
                JOptionPane.showMessageDialog(this, 
                    "Message displayed locally only. Not connected to chat server.", 
                    "Warning", JOptionPane.WARNING_MESSAGE);
            } else {
                // If chatService is available, broadcast the message to all users via the chat service
                // The message will be displayed via the callback, so we don't need to display it locally
                chatService.broadcastMessage(user.getNickname() + ": " + message);
            }

            messageField.setText(""); // Clear the input field
        } catch (Exception e) {
            // Display the message locally even if sending to server fails
            displayMessage(user.getNickname() + ": " + message);

            JOptionPane.showMessageDialog(this, 
                "Failed to send message to server: " + e.getMessage() + "\nMessage displayed locally only.", 
                "Error", JOptionPane.ERROR_MESSAGE);
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
            // Notify the server that the user has left (if connected)
            if (chatService != null) {
                try {
                    // Unregister the callback before notifying that the user has left
                    if (callback != null && callbackId != null) {
                        try {
                            chatService.unregisterCallbackById(callbackId);
                        } catch (Exception ex) {
                            System.err.println("Error unregistering callback: " + ex.getMessage());
                        }
                    }

                    chatService.notifyUserLeft(user.getNickname());
                } catch (Exception e) {
                    System.err.println("Error notifying server about user leaving: " + e.getMessage());
                    // Continue with local processing even if server notification fails
                }
            } else {
                System.out.println("Chat server not connected. User leaving handled locally only.");
            }

            // Display a message indicating that the user has left
            LocalDateTime leaveTime = LocalDateTime.now();
            String leaveMessage = user.getNickname() + " left: " + formatDateTime(leaveTime);
            displayMessage(leaveMessage);

            // Check if this is the last user in the chat
            if (connectedUsers.size() <= 1) {
                // This is the last user, so end the chat
                LocalDateTime endTime = LocalDateTime.now();
                String endMessage = "Chat stopped at: " + formatDateTime(endTime);
                displayMessage(endMessage);

                // Save the chat to a .txt file
                saveChatToFile(endTime);
            }
        } catch (Exception e) {
            System.err.println("Error during chat leaving process: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Unregister the observer
            if (subscriptionService != null && observer != null) {
                try {
                    subscriptionService.removeObserver(observer);
                } catch (Exception e) {
                    System.err.println("Error removing observer: " + e.getMessage());
                }
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
            }

            // Display a message indicating that the user has subscribed
            displayMessage(user.getNickname() + " has subscribed to this chat.");
        }
    }

    // Method called when a user is unsubscribed from the chat
    public void userUnsubscribed(User user) {
        // Remove the user from the connected users list
        connectedUsers.remove(user.getNickname());

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
                // Check if the user is already subscribed to this chat
                boolean alreadySubscribed = subscriptionService.isUserSubscribedToChat(user, chat);

                // Check if this is already the current chat
                boolean isCurrentChat = (currentChat != null && currentChat.getId() == chat.getId());

                if (!alreadySubscribed) {
                    // Subscribe the user to the chat
                    subscriptionService.subscribeUserToChat(user, chat);
                    JOptionPane.showMessageDialog(this, "You have subscribed to Chat " + chatId);
                    // Display a message in the chat window
                    displayMessage("You are now viewing Chat " + chatId + ". Only subscribers can send messages to this chat.");
                } else {
                    // User is already subscribed, just switch to this chat
                    JOptionPane.showMessageDialog(this, "You are already subscribed to Chat " + chatId);

                    // Only display the message if this is not already the current chat
                    if (!isCurrentChat) {
                        displayMessage("You are now viewing Chat " + chatId + ".");
                    }
                }

                // Set this as the current chat
                this.currentChat = chat;
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

    // Method to update the current chat from the combo box selection
    private void updateCurrentChatFromComboBox() {
        if (chatComboBox.getSelectedItem() == null) {
            return;
        }

        try {
            // Get the selected chat ID
            String chatItem = (String) chatComboBox.getSelectedItem();
            int chatId = Integer.parseInt(chatItem.replace("Chat ", ""));

            // Get the chat from the database
            models.Chat chat = getChatById(chatId);

            if (chat != null) {
                // Check if the user is already subscribed to this chat
                boolean alreadySubscribed = subscriptionService.isUserSubscribedToChat(user, chat);

                // Check if this is already the current chat
                boolean isCurrentChat = (currentChat != null && currentChat.getId() == chat.getId());

                if (alreadySubscribed) {
                    // User is already subscribed, set this as the current chat
                    this.currentChat = chat;

                    // Only display the message if this is not already the current chat
                    if (!isCurrentChat) {
                        displayMessage("You are now viewing Chat " + chatId + ".");
                    }
                } else {
                    // User is not subscribed, don't update the current chat
                    // Only display the message if this is not already the current chat
                    if (!isCurrentChat) {
                        displayMessage("You need to subscribe to Chat " + chatId + " before you can send messages.");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error updating current chat: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Method to open a new chat window for the same user and chat
    private void openNewChatWindow() {
        try {
            // Create a new chat window for the same user
            ChatWindow newWindow = new ChatWindow(user);

            // If there's a current chat, select it in the new window
            if (currentChat != null) {
                // Find the index of the current chat in the combo box
                for (int i = 0; i < newWindow.chatComboBox.getItemCount(); i++) {
                    String item = newWindow.chatComboBox.getItemAt(i).toString();
                    int chatId = Integer.parseInt(item.replace("Chat ", ""));
                    if (chatId == currentChat.getId()) {
                        newWindow.chatComboBox.setSelectedIndex(i);
                        newWindow.subscribeToChat(); // Subscribe to the chat
                        break;
                    }
                }
            }

            // Show the new window
            newWindow.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Failed to open new chat window: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}
