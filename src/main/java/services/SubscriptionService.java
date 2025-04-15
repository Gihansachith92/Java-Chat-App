package services;

import models.Subscription;
import models.User;
import models.Chat;
import org.hibernate.Session;
import org.hibernate.Transaction;
import utils.HibernateUtil;

import java.util.List;

public class SubscriptionService {

    // Subscribe a user to a chat
    public void subscribeUserToChat(User user, Chat chat) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();

            Subscription subscription = new Subscription();
            subscription.setUser(user);
            subscription.setChat(chat);

            session.save(subscription);
            transaction.commit();

            System.out.println("User " + user.getNickname() + " subscribed to chat " + chat.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Unsubscribe a user from a chat
    public void unsubscribeUserFromChat(User user, Chat chat) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();

            String hql = "FROM Subscription WHERE user.id = :userId AND chat.id = :chatId";
            Subscription subscription = session.createQuery(hql, Subscription.class)
                    .setParameter("userId", user.getId())
                    .setParameter("chatId", chat.getId())
                    .uniqueResult();

            if (subscription != null) {
                session.delete(subscription);
                System.out.println("User " + user.getNickname() + " unsubscribed from chat " + chat.getId());
            } else {
                System.out.println("Subscription not found.");
            }

            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Get all subscriptions for a specific chat
    public List<Subscription> getSubscriptionsForChat(Chat chat) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Subscription WHERE chat.id = :chatId";
            return session.createQuery(hql, Subscription.class)
                    .setParameter("chatId", chat.getId())
                    .list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Get all subscriptions for a specific user
    public List<Subscription> getSubscriptionsForUser(User user) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Subscription WHERE user.id = :userId";
            return session.createQuery(hql, Subscription.class)
                    .setParameter("userId", user.getId())
                    .list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
