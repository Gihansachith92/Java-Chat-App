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
    private JButton backButton;
    private JLabel profilePictureLabel;
    private String profilePicturePath;
    private User user;

    public ProfileUpdateScreen(User user) {
        this.user = user;
        this.profilePicturePath = user.getProfilePicture();

        setTitle("Update Profile - " + user.getNickname());
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create components
        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField(user.getUsername(), 20);

        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField(user.getPassword(), 20);

        JLabel nicknameLabel = new JLabel("Nickname:");
        nicknameField = new JTextField(user.getNickname(), 20);

        JLabel profilePicLabel = new JLabel("Profile Picture:");
        profilePictureLabel = new JLabel(user.getProfilePicture() != null ? new File(user.getProfilePicture()).getName() : "No image selected");
        
        // Display current profile picture if available
        if (user.getProfilePicture() != null && !user.getProfilePicture().isEmpty()) {
            try {
                ImageIcon icon = new ImageIcon(user.getProfilePicture());
                Image img = icon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
                profilePictureLabel.setIcon(new ImageIcon(img));
            } catch (Exception ex) {
                profilePictureLabel.setIcon(null);
                profilePictureLabel.setText("Error loading image");
            }
        }
        
        uploadPictureButton = new JButton("Change Picture");
        updateButton = new JButton("Update Profile");
        backButton = new JButton("Back to Chat");

        // Layout setup
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(5, 2, 10, 10));
        formPanel.add(usernameLabel);
        formPanel.add(usernameField);
        formPanel.add(passwordLabel);
        formPanel.add(passwordField);
        formPanel.add(nicknameLabel);
        formPanel.add(nicknameField);
        formPanel.add(profilePicLabel);
        
        JPanel picturePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        picturePanel.add(profilePictureLabel);
        picturePanel.add(uploadPictureButton);
        formPanel.add(picturePanel);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(updateButton);
        buttonPanel.add(backButton);
        
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
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
}