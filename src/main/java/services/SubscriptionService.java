package services;

import models.Subscription;
import models.User;
import models.Chat;
import org.hibernate.Session;
import org.hibernate.Transaction;
import utils.HibernateUtil;

import java.util.ArrayList;
import java.util.List;

public class SubscriptionService {

    private List<ChatObserver> observers = new ArrayList<>();

    // Add an observer
    public void addObserver(ChatObserver observer) {
        observers.add(observer);
    }

    // Remove an observer
    public void removeObserver(ChatObserver observer) {
        observers.remove(observer);
    }

    // Notify observers when a user is subscribed to a chat
    private void notifySubscribe(User user, Chat chat) {
        for (ChatObserver observer : observers) {
            observer.onSubscribe(user, chat);
        }
    }

    // Notify observers when a user is unsubscribed from a chat
    private void notifyUnsubscribe(User user, Chat chat) {
        for (ChatObserver observer : observers) {
            observer.onUnsubscribe(user, chat);
        }
    }

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

            // Notify observers
            notifySubscribe(user, chat);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Unsubscribe a user from a chat
    public void unsubscribeUserFromChat(User user, Chat chat) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();

            String hql = "FROM Subscription WHERE user.id = :userId AND chat.id = :chatId";
            List<Subscription> subscriptions = session.createQuery(hql, Subscription.class)
                    .setParameter("userId", user.getId())
                    .setParameter("chatId", chat.getId())
                    .list();

            if (!subscriptions.isEmpty()) {
                for (Subscription subscription : subscriptions) {
                    session.delete(subscription);
                }
                System.out.println("User " + user.getNickname() + " unsubscribed from chat " + chat.getId());

                // Notify observers
                notifyUnsubscribe(user, chat);
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

    // Check if a user is subscribed to a specific chat
    public boolean isUserSubscribedToChat(User user, Chat chat) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Subscription WHERE user.id = :userId AND chat.id = :chatId";
            List<Subscription> subscriptions = session.createQuery(hql, Subscription.class)
                    .setParameter("userId", user.getId())
                    .setParameter("chatId", chat.getId())
                    .list();
            return !subscriptions.isEmpty();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get all users subscribed to a specific chat
    public List<User> getUsersSubscribedToChat(Chat chat) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT s.user FROM Subscription s WHERE s.chat.id = :chatId";
            return session.createQuery(hql, User.class)
                    .setParameter("chatId", chat.getId())
                    .list();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
