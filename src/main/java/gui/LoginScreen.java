package gui;

import models.User;
import network.ChatServerImpl;
import services.UserService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
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

    // Professional/Enterprise theme colors
    private static final Color THEME_PRIMARY = new Color(59, 89, 152);  // Dark blue
    private static final Color THEME_SECONDARY = new Color(223, 227, 238);  // Light blue-gray
    private static final Color THEME_BACKGROUND = new Color(240, 242, 245);  // Very light gray
    private static final Color THEME_HEADER = new Color(35, 53, 91);  // Darker blue
    private static final Color THEME_TEXT = new Color(33, 33, 33);  // Dark gray for text

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

        setTitle("ChatNest - Login");
        setSize(400, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create header with professional theme
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(THEME_HEADER);
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        headerPanel.setPreferredSize(new Dimension(400, 60));

        JLabel titleLabel = new JLabel("ChatNest");
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

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        passwordLabel.setForeground(THEME_TEXT);

        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(THEME_PRIMARY, 1),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)));

        loginButton = new JButton("Login");
        styleButton(loginButton);

        registerButton = new JButton("Register");
        styleButton(registerButton);

        // Create logo panel
        JPanel logoPanel = new JPanel();
        logoPanel.setBackground(THEME_BACKGROUND);
        logoPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        // Add ChatNest logo (text as placeholder)
        JLabel logoLabel = new JLabel("ChatNest");
        logoLabel.setFont(new Font("Arial", Font.BOLD, 24));
        logoLabel.setForeground(THEME_PRIMARY);
        logoPanel.add(logoLabel);

        // Create form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBackground(THEME_BACKGROUND);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        formPanel.add(emailLabel, gbc);

        gbc.gridy = 1;
        formPanel.add(emailField, gbc);

        gbc.gridy = 2;
        formPanel.add(passwordLabel, gbc);

        gbc.gridy = 3;
        formPanel.add(passwordField, gbc);

        gbc.gridy = 4;
        gbc.gridwidth = 1;
        formPanel.add(loginButton, gbc);

        gbc.gridx = 1;
        formPanel.add(registerButton, gbc);

        // Main panel with BorderLayout
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(THEME_BACKGROUND);
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(logoPanel, BorderLayout.CENTER);
        mainPanel.add(formPanel, BorderLayout.SOUTH);

        add(mainPanel);

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
