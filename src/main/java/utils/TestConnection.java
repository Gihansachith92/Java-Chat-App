package utils;

import org.hibernate.Session;
import utils.HibernateUtil;

public class TestConnection {
    public static void main(String[] args) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            System.out.println("Hibernate is connected to the database!");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to connect to the database.");
        }
    }
}
