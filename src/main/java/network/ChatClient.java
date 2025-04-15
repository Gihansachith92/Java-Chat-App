package network;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class ChatClient {
    public static void main(String[] args) {
        try {
            // Connect to the RMI registry on localhost and port 1099
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);

            // Lookup the ChatService from the registry
            ChatService chatService = (ChatService) registry.lookup("ChatService");

            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter your nickname: ");
            String nickname = scanner.nextLine();

            // Notify that the user has joined
            chatService.notifyUserJoined(nickname);

            while (true) {
                System.out.print("Enter message (type 'Bye' to leave): ");
                String message = scanner.nextLine();

                if (message.equalsIgnoreCase("Bye")) {
                    chatService.notifyUserLeft(nickname);
                    break;
                }

                // Broadcast the message
                chatService.broadcastMessage(nickname + ": " + message);
            }

            System.out.println("You have left the chat.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
