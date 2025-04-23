package gui;

import models.User;
import services.UserService;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class RegistrationScreen extends JFrame {

    private JTextField emailField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField nicknameField;
    private JButton registerButton;
    private JButton uploadPictureButton;
    private JButton backButton;
    private JLabel profilePictureLabel;
    private String profilePicturePath;

    // Professional/Enterprise theme colors
    private static final Color THEME_PRIMARY = new Color(59, 89, 152);  // Dark blue
    private static final Color THEME_SECONDARY = new Color(223, 227, 238);  // Light blue-gray
    private static final Color THEME_BACKGROUND = new Color(240, 242, 245);  // Very light gray
    private static final Color THEME_HEADER = new Color(35, 53, 91);  // Darker blue
    private static final Color THEME_TEXT = new Color(33, 33, 33);  // Dark gray for text

    public RegistrationScreen() {
        setTitle("ChatNest - Register");
        setSize(400, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create professional-style header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(THEME_HEADER);
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        headerPanel.setPreferredSize(new Dimension(400, 60));

        JLabel titleLabel = new JLabel("ChatNest Registration");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        // Create components with professional styling
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        emailLabel.setForeground(THEME_TEXT);

        emailField = new JTextField(20);
        emailField.setFont(new Font("Arial", Font.PLAIN, 14));
        emailField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(THEME_PRIMARY, 1),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)));

        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        usernameLabel.setForeground(THEME_TEXT);

        usernameField = new JTextField(20);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 14));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(THEME_PRIMARY, 1),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)));

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        passwordLabel.setForeground(THEME_TEXT);

        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(THEME_PRIMARY, 1),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)));

        JLabel nicknameLabel = new JLabel("Nickname:");
        nicknameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        nicknameLabel.setForeground(THEME_TEXT);

        nicknameField = new JTextField(20);
        nicknameField.setFont(new Font("Arial", Font.PLAIN, 14));
        nicknameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(THEME_PRIMARY, 1),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)));

        JLabel profilePicLabel = new JLabel("Profile Picture:");
        profilePicLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        profilePicLabel.setForeground(THEME_TEXT);

        profilePictureLabel = new JLabel("No image selected");
        profilePictureLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        uploadPictureButton = new JButton("Upload Picture");
        styleButton(uploadPictureButton);

        registerButton = new JButton("Register");
        styleButton(registerButton);

        backButton = new JButton("Back to Login");
        styleButton(backButton);

        // Create form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBackground(THEME_BACKGROUND);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.gridwidth = 2;

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(emailLabel, gbc);

        gbc.gridy = 1;
        formPanel.add(emailField, gbc);

        gbc.gridy = 2;
        formPanel.add(usernameLabel, gbc);

        gbc.gridy = 3;
        formPanel.add(usernameField, gbc);

        gbc.gridy = 4;
        formPanel.add(passwordLabel, gbc);

        gbc.gridy = 5;
        formPanel.add(passwordField, gbc);

        gbc.gridy = 6;
        formPanel.add(nicknameLabel, gbc);

        gbc.gridy = 7;
        formPanel.add(nicknameField, gbc);

        gbc.gridy = 8;
        formPanel.add(profilePicLabel, gbc);

        // Create picture panel
        JPanel picturePanel = new JPanel();
        picturePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        picturePanel.setBackground(THEME_BACKGROUND);
        picturePanel.add(profilePictureLabel);
        picturePanel.add(uploadPictureButton);

        gbc.gridy = 9;
        formPanel.add(picturePanel, gbc);

        // Create button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 2, 10, 0));
        buttonPanel.setBackground(THEME_BACKGROUND);
        buttonPanel.add(registerButton);
        buttonPanel.add(backButton);

        gbc.gridy = 10;
        gbc.insets = new Insets(20, 8, 8, 8);
        formPanel.add(buttonPanel, gbc);

        // Main panel with BorderLayout
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(THEME_BACKGROUND);
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);

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

                int result = fileChooser.showOpenDialog(RegistrationScreen.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    profilePicturePath = selectedFile.getAbsolutePath();
                    profilePictureLabel.setText(selectedFile.getName());

                    // Optionally, display a preview of the image
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

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String email = emailField.getText();
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                String nickname = nicknameField.getText();

                // Validate input
                if (email.isEmpty() || username.isEmpty() || password.isEmpty() || nickname.isEmpty()) {
                    JOptionPane.showMessageDialog(RegistrationScreen.this, 
                        "Please fill in all required fields (Email, Username, Password, Nickname).",
                        "Registration Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                User user = new User();
                user.setEmail(email);
                user.setUsername(username);
                user.setPassword(password);
                user.setNickname(nickname);
                user.setProfilePicture(profilePicturePath); // Set the profile picture path

                userService.registerUser(user);

                JOptionPane.showMessageDialog(RegistrationScreen.this, "Registration successful!");
                dispose(); // Close registration screen
                // Don't create a new login screen, the original one is still open
                // The user will be returned to the existing login screen
            }
        });

        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // Close registration screen
                // Don't create a new login screen, the original one is still open
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RegistrationScreen().setVisible(true));
    }

    // Helper method to style buttons with professional theme
    private void styleButton(JButton button) {
        button.setBackground(THEME_PRIMARY);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
    }
}
