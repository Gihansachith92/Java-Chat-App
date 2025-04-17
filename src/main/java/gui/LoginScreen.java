package gui;

import models.User;
import network.ChatServerImpl;
import services.UserService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class LoginScreen extends JFrame {

    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;

    private static boolean isServerStarted = false;

    // Method to start the RMI chat server
    private void startChatServer() {
        if (!isServerStarted) {
            try {
                // Create an instance of the server implementation
                network.ChatService chatService = new ChatServerImpl();

                // Start the RMI registry on port 1099
                Registry registry = LocateRegistry.createRegistry(1099);

                // Bind the service to the registry
                registry.rebind("ChatService", chatService);

                System.out.println("Chat server is running...");
                isServerStarted = true;
            } catch (Exception e) {
                System.err.println("Failed to start chat server: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public LoginScreen() {
        // Start the chat server when the login screen is created
        startChatServer();

        setTitle("Chat Application - Login");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create components
        JLabel emailLabel = new JLabel("Email:");
        emailField = new JTextField(20);

        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField(20);

        loginButton = new JButton("Login");
        registerButton = new JButton("Register");

        // Layout setup
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 2, 10, 10));
        panel.add(emailLabel);
        panel.add(emailField);
        panel.add(passwordLabel);
        panel.add(passwordField);
        panel.add(loginButton);
        panel.add(registerButton);

        add(panel);

        // Button actions
        UserService userService = new UserService();

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String email = emailField.getText();
                String password = new String(passwordField.getPassword());

                User user = userService.loginUser(email, password);
                if (user != null) {
                    JOptionPane.showMessageDialog(LoginScreen.this, "Login successful!");
                    dispose(); // Close login screen
                    if (email.equalsIgnoreCase("admin@example.com")) {
                        new AdminPanel().setVisible(true); // Open admin panel for admin
                    } else {
                        new ChatWindow(user).setVisible(true); // Open chat window for regular users
                    }
                } else {
                    JOptionPane.showMessageDialog(LoginScreen.this, "Invalid email or password.");
                }
            }
        });

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // Close login screen
                new RegistrationScreen().setVisible(true); // Open registration screen
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginScreen().setVisible(true));
    }
}
