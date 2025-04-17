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
        setTitle("WhatsApp Admin - Edit User Profile");

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
    }
}
