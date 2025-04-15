package gui;

import models.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;

public class ChatWindow extends JFrame {

    private JTextArea messageArea;
    private JTextField messageField;
    private JButton sendButton;
    private JButton leaveChatButton;

    private User user; // The logged-in user participating in the chat
    private boolean isChatActive = true; // To track if the user is still in the chat

    public ChatWindow(User user) {
        this.user = user;

        setTitle("Chat Window - Welcome " + user.getNickname());
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create components
        messageArea = new JTextArea();
        messageArea.setEditable(false); // Prevent editing of received messages
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);

        messageField = new JTextField(30);
        sendButton = new JButton("Send");
        leaveChatButton = new JButton("Leave Chat");

        // Layout setup
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // Add message area (scrollable)
        panel.add(new JScrollPane(messageArea), BorderLayout.CENTER);

        // Add input panel (message field + buttons)
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new FlowLayout());
        inputPanel.add(messageField);
        inputPanel.add(sendButton);
        inputPanel.add(leaveChatButton);

        panel.add(inputPanel, BorderLayout.SOUTH);

        add(panel);

        // Display a welcome message when the user joins
        displayMessage("You have joined the chat at " + LocalDateTime.now());

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

        displayMessage(user.getNickname() + ": " + message); // Display user's message
        messageField.setText(""); // Clear the input field

        // Simulate broadcasting the message to other users (replace with actual server logic)
        simulateBroadcastMessage(user.getNickname(), message);
    }

    // Method to simulate broadcasting a message to other users (for testing purposes)
    private void simulateBroadcastMessage(String senderNickname, String message) {
        displayMessage("[Broadcast] " + senderNickname + ": " + message);
    }

    // Method to leave the chat
    private void leaveChat() {
        if (!isChatActive) {
            JOptionPane.showMessageDialog(this, "You have already left the chat.");
            return;
        }

        isChatActive = false; // Mark chat as inactive
        displayMessage("You have left the chat at " + LocalDateTime.now());

        JOptionPane.showMessageDialog(this, "You have left the chat.");

        // Close this window after leaving
        dispose();}
}
