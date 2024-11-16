import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {
    // List of quiz questions and their answers
    private final List<String[]> questions = new ArrayList<>();
    // Map to store user scores (username -> score)
    private final Map<String, Integer> scores = new HashMap<>();

    // Constructor: Initialize quiz questions
    public Server() {
        questions.add(new String[]{"1 + 1 = ?", "2"});
        questions.add(new String[]{"3 * 5 = ?", "15"});
        questions.add(new String[]{"10 - 3 = ?", "7"});
        questions.add(new String[]{"5 + 5 = ?", "10"});
        questions.add(new String[]{"2 * 6 = ?", "12"});
    }

    // Start the server
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(6000)) {
            System.out.println("Quiz Server Started");

            // Continuously listen for client connections
            while (true) {
                Socket connectionSocket = serverSocket.accept();
                System.out.println("Client connected.");
                // Create a new thread for each client
                ServerThread thread = new ServerThread(connectionSocket);
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Main method to run the server
    public static void main(String[] args) {
        new Server().start();
    }

    // Thread class to handle each client's requests
    class ServerThread extends Thread {
        Socket socket;
        BufferedReader inFromClient;
        DataOutputStream outToClient;
        int currentQuestionIndex = 0; // Index of the current quiz question

        // Constructor to initialize socket and I/O streams
        ServerThread(Socket socket) {
            this.socket = socket;
            try {
                inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                outToClient = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Main method to handle client requests
        public void run() {
            try {
                String clientName = "";
                String line;

                // Listen for client messages
                while ((line = inFromClient.readLine()) != null) {
                    String[] parts = line.split(" ");
                    String command = parts[0].toUpperCase();

                    // Process commands from the client
                    switch (command) {
                        case "START":
                            // Initialize the client name and score
                            clientName = parts[1];
                            scores.put(clientName, 0);
                            sendQuestion();
                            break;
                        case "ANSWER":
                            // Check the client's answer
                            String answer = parts[1];
                            checkAnswer(clientName, answer);
                            break;
                        case "SCORE":
                            // Return the client's current score
                            int score = scores.getOrDefault(clientName, 0);
                            outToClient.writeBytes("Your Score: " + score + "\n");
                            break;
                        default:
                            // Handle undefined commands
                            outToClient.writeBytes("Error: Undefined command\n");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Send the current quiz question to the client
        private void sendQuestion() throws IOException {
            if (currentQuestionIndex < questions.size()) {
                String question = questions.get(currentQuestionIndex)[0];
                outToClient.writeBytes("Question: " + question + "\n");
            } else {
                // No more questions left
                outToClient.writeBytes("No more questions. Type 'SCORE' to see your score.\n");
            }
        }

        // Check the client's answer and update the score
        private void checkAnswer(String clientName, String answer) throws IOException {
            // If there are no more questions, notify the client
            if (currentQuestionIndex >= questions.size()) {
                outToClient.writeBytes("No more questions. Type 'SCORE' to see your score.\n");
                return;
            }

            // Get the correct answer for the current question
            String correctAnswer = questions.get(currentQuestionIndex)[1];

            // Check if the client's answer is correct
            if (answer.equals(correctAnswer)) {
                // Update the client's score
                int newScore = scores.get(clientName) + 10;
                scores.put(clientName, newScore);
                outToClient.writeBytes("Correct! Your score: " + newScore + "\n");
            } else {
                // Notify the client that the answer is incorrect
                outToClient.writeBytes("Wrong Answer. Try again!\n");
            }

            // Move to the next question
            currentQuestionIndex++;
            if (currentQuestionIndex < questions.size()) {
                sendQuestion();
            } else {
                // No more questions left
                outToClient.writeBytes("No more questions. Type 'SCORE' to see your score.\n");
            }
        }
    }
}
