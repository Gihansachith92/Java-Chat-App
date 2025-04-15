package gui;

import models.User;

import javax.swing.*;

public class TestChatWindow {

    public static void main(String[] args) {

        // Create a dummy user for testing purposes
        User testUser=new User();testUser.setNickname ("Test User");
        SwingUtilities.invokeLater (() ->new ChatWindow(testUser).setVisible(true));}}
