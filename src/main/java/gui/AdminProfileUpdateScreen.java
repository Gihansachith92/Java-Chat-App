package gui;

import models.User;
import services.UserService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Screen for admins to update user profile details
 * This is a specialized version of ProfileUpdateScreen that returns to AdminPanel
 */
public class AdminProfileUpdateScreen extends ProfileUpdateScreen {

    public AdminProfileUpdateScreen(User user) {
        super(user);

        // Change the title to indicate admin mode
        setTitle("ChatNest Admin - Edit User Profile");

        // Override the back button action to return to AdminPanel
        for (ActionListener al : backButton.getActionListeners()) {
            backButton.removeActionListener(al);
        }

        backButton.setText("Back to Admin Panel");

        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // Close profile update screen
                new AdminPanel().setVisible(true); // Return to admin panel
            }
        });

        // Override the update button action to return to AdminPanel after successful update
        for (ActionListener al : updateButton.getActionListeners()) {
            updateButton.removeActionListener(al);
        }

        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                String nickname = nicknameField.getText();

                // Validate input
                if (username.isEmpty() || password.isEmpty() || nickname.isEmpty()) {
                    JOptionPane.showMessageDialog(AdminProfileUpdateScreen.this, 
                        "Please fill in all required fields (Username, Password, Nickname).",
                        "Update Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Update user details
                user.setUsername(username);
                user.setPassword(password);
                user.setNickname(nickname);
                user.setProfilePicture(profilePicturePath);

                UserService userService = new UserService();
                userService.updateUser(user);

                JOptionPane.showMessageDialog(AdminProfileUpdateScreen.this, "Profile updated successfully!");
                dispose(); // Close profile update screen
                new AdminPanel().setVisible(true); // Return to admin panel
            }
        });
    }
}
