package gui;

import models.User;
import services.UserService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RegistrationScreen extends JFrame {

    private JTextField emailField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField nicknameField;
    private JButton registerButton;

    public RegistrationScreen() {
        setTitle("Chat Application - Register");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create components
        JLabel emailLabel = new JLabel("Email:");
        emailField = new JTextField(20);

        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField(20);

        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField(20);

        JLabel nicknameLabel = new JLabel("Nickname:");
        nicknameField = new JTextField(20);

        registerButton = new JButton("Register");

        // Layout setup
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(5, 2, 10, 10));
        panel.add(emailLabel);
        panel.add(emailField);
        panel.add(usernameLabel);
        panel.add(usernameField);
        panel.add(passwordLabel);
        panel.add(passwordField);
        panel.add(nicknameLabel);
        panel.add(nicknameField);
        panel.add(registerButton);

        add(panel);

        // Button actions
        UserService userService = new UserService();

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String email = emailField.getText();
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                String nickname = nicknameField.getText();

                User user = new User();
                user.setEmail(email);
                user.setUsername(username);
                user.setPassword(password);
                user.setNickname(nickname);

                userService.registerUser(user);

                JOptionPane.showMessageDialog(RegistrationScreen.this, "Registration successful!");
                dispose(); // Close registration screen
                new LoginScreen().setVisible(true); // Open login screen
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RegistrationScreen().setVisible(true));
    }
}
