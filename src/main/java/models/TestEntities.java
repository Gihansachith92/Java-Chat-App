package models;

import models.User;
import models.Chat;
import models.Subscription;
import org.hibernate.Session;
import org.hibernate.Transaction;
import utils.HibernateUtil;

import java.time.LocalDateTime;

public class TestEntities {

    public static void main(String[] args) {

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {

            Transaction transaction = session.beginTransaction();

            // Create a User object and save it to the database
            User user1 = new User();
            user1.setEmail("testuser03@example.com");
            user1.setUsername("testuser");
            user1.setPassword("password123");
            user1.setNickname("Tester");
            user1.setProfilePicture("profile_pic.jpg");

            session.save(user1);

            // Create a Chat object and save it to the database
            Chat chat1 = new Chat();
            chat1.setStartTime(LocalDateTime.now());
            session.save(chat1);

            // Create a Subscription object and save it to the database
            Subscription subscription1 = new Subscription();
            subscription1.setUser(user1);
            subscription1.setChat(chat1);
            session.save(subscription1);

            transaction.commit();

            System.out.println("Entities saved successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
