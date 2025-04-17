package services;

import models.Chat;
import org.hibernate.Session;
import org.hibernate.Transaction;
import utils.HibernateUtil;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

public class ChatService {

    // Get a chat by ID
    public Chat getChatById(int chatId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Chat chat = session.get(Chat.class, chatId);
            return chat;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Start a new chat
    public Chat startChat() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();

            Chat chat = new Chat();
            chat.setStartTime(LocalDateTime.now());
            session.save(chat);

            transaction.commit();
            System.out.println("Chat started successfully at " + chat.getStartTime());
            return chat;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Stop a chat and save its log
    public void stopChat(Chat chat, String logContent) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();

            // Set end time
            chat.setEndTime(LocalDateTime.now());

            // Save log to file
            String filePath = "src\\main\\java\\services\\chat_logs\\chat_" + chat.getId() + ".txt";
            try (FileWriter writer = new FileWriter(filePath)) {
                writer.write(logContent);
                System.out.println("Chat log saved to " + filePath);
                chat.setFilePath(filePath);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Failed to save chat log.");
                return;
            }

            // Update chat in database
            session.update(chat);

            transaction.commit();System.out.println ("Chat stopped successfully at "+chat.getEndTime());}}}
