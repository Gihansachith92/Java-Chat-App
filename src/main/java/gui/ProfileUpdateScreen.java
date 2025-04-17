package gui;

import models.User;
import services.UserService;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * Screen for users to update their profile details
 */
public class ProfileUpdateScreen extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField nicknameField;
    private JButton updateButton;
    private JButton uploadPictureButton;
    protected JButton backButton;
    private JLabel profilePictureLabel;
    private String profilePicturePath;
    private User user;

    // WhatsApp colors
    private static final Color WHATSAPP_GREEN = new Color(0, 168, 132);
    private static final Color WHATSAPP_LIGHT_GREEN = new Color(220, 248, 198);
    private static final Color WHATSAPP_BACKGROUND = new Color(230, 230, 230);
    private static final Color WHATSAPP_HEADER = new Color(32, 44, 51);
    private static final Color WHATSAPP_MESSAGE_TEXT = new Color(0, 0, 0);

    public ProfileUpdateScreen(User user) {
        this.user = user;
        this.profilePicturePath = user.getProfilePicture();

        setTitle("WhatsApp - Profile Update");
        setSize(400, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create WhatsApp-style header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(WHATSAPP_HEADER);
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        headerPanel.setPreferredSize(new Dimension(400, 60));

        JLabel titleLabel = new JLabel("Profile: " + user.getNickname());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        // Create components with WhatsApp styling
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        usernameLabel.setForeground(WHATSAPP_MESSAGE_TEXT);

        usernameField = new JTextField(user.getUsername(), 20);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 14));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(WHATSAPP_GREEN, 1),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)));

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        passwordLabel.setForeground(WHATSAPP_MESSAGE_TEXT);

        passwordField = new JPasswordField(user.getPassword(), 20);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(WHATSAPP_GREEN, 1),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)));

        JLabel nicknameLabel = new JLabel("Nickname:");
        nicknameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        nicknameLabel.setForeground(WHATSAPP_MESSAGE_TEXT);

        nicknameField = new JTextField(user.getNickname(), 20);
        nicknameField.setFont(new Font("Arial", Font.PLAIN, 14));
        nicknameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(WHATSAPP_GREEN, 1),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)));

        JLabel profilePicLabel = new JLabel("Profile Picture:");
        profilePicLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        profilePicLabel.setForeground(WHATSAPP_MESSAGE_TEXT);

        profilePictureLabel = new JLabel(user.getProfilePicture() != null ? new File(user.getProfilePicture()).getName() : "No image selected");
        profilePictureLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        // Display current profile picture if available
        if (user.getProfilePicture() != null && !user.getProfilePicture().isEmpty()) {
            try {
                ImageIcon icon = new ImageIcon(user.getProfilePicture());
                Image img = icon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
                profilePictureLabel.setIcon(new ImageIcon(img));
            } catch (Exception ex) {
                profilePictureLabel.setIcon(null);
                profilePictureLabel.setText("Error loading image");
            }
        }

        uploadPictureButton = new JButton("Change Picture");
        styleButton(uploadPictureButton);

        updateButton = new JButton("Update Profile");
        styleButton(updateButton);

        backButton = new JButton("Back to Chat");
        styleButton(backButton);

        // Create profile picture panel
        JPanel profilePanel = new JPanel();
        profilePanel.setBackground(WHATSAPP_BACKGROUND);
        profilePanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        // Add user profile picture
        JLabel userPicLabel = new JLabel();
        if (user.getProfilePicture() != null && !user.getProfilePicture().isEmpty()) {
            try {
                ImageIcon icon = new ImageIcon(user.getProfilePicture());
                Image img = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                userPicLabel.setIcon(new ImageIcon(img));
            } catch (Exception ex) {
                userPicLabel.setText("👤");
                userPicLabel.setFont(new Font("Arial", Font.PLAIN, 48));
                userPicLabel.setForeground(WHATSAPP_GREEN);
            }
        } else {
            userPicLabel.setText("👤");
            userPicLabel.setFont(new Font("Arial", Font.PLAIN, 48));
            userPicLabel.setForeground(WHATSAPP_GREEN);
        }
        profilePanel.add(userPicLabel);

        // Create form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBackground(WHATSAPP_BACKGROUND);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.gridwidth = 2;

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(usernameLabel, gbc);

        gbc.gridy = 1;
        formPanel.add(usernameField, gbc);

        gbc.gridy = 2;
        formPanel.add(passwordLabel, gbc);

        gbc.gridy = 3;
        formPanel.add(passwordField, gbc);

        gbc.gridy = 4;
        formPanel.add(nicknameLabel, gbc);

        gbc.gridy = 5;
        formPanel.add(nicknameField, gbc);

        gbc.gridy = 6;
        formPanel.add(profilePicLabel, gbc);

        // Create picture panel
        JPanel picturePanel = new JPanel();
        picturePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        picturePanel.setBackground(WHATSAPP_BACKGROUND);
        picturePanel.add(profilePictureLabel);
        picturePanel.add(uploadPictureButton);

        gbc.gridy = 7;
        formPanel.add(picturePanel, gbc);

        // Create button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 2, 10, 0));
        buttonPanel.setBackground(WHATSAPP_BACKGROUND);
        buttonPanel.add(updateButton);
        buttonPanel.add(backButton);

        gbc.gridy = 8;
        gbc.insets = new Insets(20, 8, 8, 8);
        formPanel.add(buttonPanel, gbc);

        // Main panel with BorderLayout
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(WHATSAPP_BACKGROUND);
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(profilePanel, BorderLayout.CENTER);
        mainPanel.add(formPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // Button actions
        UserService userService = new UserService();

        // Add action listener for upload button
        uploadPictureButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Select Profile Picture");
                fileChooser.setFileFilter(new FileNameExtensionFilter("Image files", "jpg", "jpeg", "png", "gif"));

                int result = fileChooser.showOpenDialog(ProfileUpdateScreen.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    profilePicturePath = selectedFile.getAbsolutePath();
                    profilePictureLabel.setText(selectedFile.getName());

                    // Display a preview of the image
                    try {
                        ImageIcon icon = new ImageIcon(profilePicturePath);
                        Image img = icon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
                        profilePictureLabel.setIcon(new ImageIcon(img));
                    } catch (Exception ex) {
                        profilePictureLabel.setIcon(null);
                        profilePictureLabel.setText("Error loading image: " + selectedFile.getName());
                    }
                }
            }
        });

        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                String nickname = nicknameField.getText();

                // Validate input
                if (username.isEmpty() || password.isEmpty() || nickname.isEmpty()) {
                    JOptionPane.showMessageDialog(ProfileUpdateScreen.this, 
                        "Please fill in all required fields (Username, Password, Nickname).",
                        "Update Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Update user details
                user.setUsername(username);
                user.setPassword(password);
                user.setNickname(nickname);
                user.setProfilePicture(profilePicturePath);

                userService.updateUser(user);

                JOptionPane.showMessageDialog(ProfileUpdateScreen.this, "Profile updated successfully!");
                dispose(); // Close profile update screen
                new ChatWindow(user).setVisible(true); // Return to chat window
            }
        });

        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // Close profile update screen
                new ChatWindow(user).setVisible(true); // Return to chat window
            }
        });
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
