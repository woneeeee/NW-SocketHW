import java.io.*;
import java.net.Socket;

public class Client {
    public static void main(String[] args){
        // Define the server IP and port number
        String serverIp = "localhost";
        int port = 6000;

        try {
            // Create a socket connection to the server
            Socket socket = new Socket(serverIp, port);
            System.out.println("Connected to Quiz Server");

            // Start threads for sending and receiving messages
            Thread sender = new Thread(new ClientSender(socket));
            Thread receiver = new Thread(new ClientReceiver(socket));

            sender.start();
            receiver.start();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    // Thread class for sending messages to the server
    static class ClientSender extends Thread{
        Socket socket;
        DataOutputStream outToServer;

        // Constructor to initialize the socket and output stream
        ClientSender(Socket socket){
            this.socket = socket;
            try{
                outToServer = new DataOutputStream(socket.getOutputStream());
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        // Run method for the thread
        public void run(){
            BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
            try{
                // Prompt the user to enter their name to start the quiz
                System.out.println("Enter your name to start the quiz: ");
                String name = inFromUser.readLine();
                outToServer.writeBytes("START " + name + "\n");

                String line;
                while (true){
                    // Read user input
                    line = inFromUser.readLine();

                    // If the user inputs "SCORE", send the SCORE command to the server
                    if (line.equalsIgnoreCase("SCORE")) {
                        outToServer.writeBytes("SCORE\n");
                    } else {
                        // Otherwise, treat the input as an answer to the quiz question
                        outToServer.writeBytes("ANSWER " + line + "\n");
                    }
                }
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    // Thread class for receiving messages from the server
    static class ClientReceiver extends Thread {
        Socket socket;
        BufferedReader inFromServer;

        // Constructor to initialize the socket and input stream
        ClientReceiver(Socket socket){
            this.socket = socket;
            try{
                inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e){
                e.printStackTrace();
            }
        }

        // Run method for the thread
        public void run(){
            try{
                String serverMessage;
                // Continuously read messages from the server and print them
                while ((serverMessage = inFromServer.readLine()) != null){
                    System.out.println(serverMessage);
                }
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}
