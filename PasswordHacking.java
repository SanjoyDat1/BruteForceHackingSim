import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class PasswordHacking extends JFrame {

    private static final int  TIME_BETWEEN_ATTEMPTS = 1;  // Time between attempts in milliseconds

    private JTextArea bruteForceA;
    private JTextArea bruteForceZ;
    private JTextArea bruteForceRandom;
    private JTextArea randomGuessing;
    private JButton startButton;
    private JTextField passwordField;
    private JLabel statusLabel;
    private ExecutorService executorService = Executors.newFixedThreadPool(4);
    private AtomicBoolean running = new AtomicBoolean(false);
    private AtomicBoolean passwordFound = new AtomicBoolean(false);

    public PasswordHacking() {
        setTitle("Hacking Simulator");
        setSize(1200, 600);  // Adjusted size for better layout
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);  // Center the window

        // Create components
        bruteForceA = createTextArea();
        bruteForceZ = createTextArea();
        bruteForceRandom = createTextArea();
        randomGuessing = createTextArea();

        passwordField = new JTextField(20);
        startButton = new JButton("Start Attack");
        statusLabel = new JLabel("Status: Waiting to start");

        // Style components
        styleComponents();

        // Add components to the frame
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(150, 150, 150));
        topPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        topPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
        topPanel.add(new JLabel("Enter Password (Only letters and number):"));
        topPanel.add(passwordField);
        topPanel.add(startButton);
        topPanel.add(statusLabel);

        add(topPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(1, 4, 10, 10));  // Adjusted for 4 columns
        centerPanel.setBackground(new Color(40, 40, 40));
        centerPanel.add(createScrollPane("Brute Force (From A)", bruteForceA));
        centerPanel.add(createScrollPane("Brute Force (From 9)", bruteForceZ));
        centerPanel.add(createScrollPane("Brute Force (Random)", bruteForceRandom));
        centerPanel.add(createScrollPane("Brute Force (Random)", randomGuessing));

        add(centerPanel, BorderLayout.CENTER);

        // Button action
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String password = passwordField.getText().toUpperCase();
                if (isValidPassword(password)) {
                    if (!running.get()) {
                        clearTextAreas();
                        statusLabel.setText("Status: Attacking...");
                        running.set(true);
                        passwordFound.set(false);
                        startAttacks(password);
                    }
                } else {
                    JOptionPane.showMessageDialog(null,
                            "Password must contain only uppercase letters and numbers.",
                            "Invalid Password",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    private boolean isValidPassword(String password) {
        return password.matches("[A-Z0-9]+");
    }

    private JTextArea createTextArea() {
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setBackground(new Color(20, 20, 20));
        textArea.setForeground(new Color(50, 200, 0));
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        return textArea;
    }

    private JScrollPane createScrollPane(String title, JTextArea textArea) {
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(255, 100, 0)),
                title, TitledBorder.LEFT, TitledBorder.TOP, new Font("Monospaced", Font.BOLD, 14), new Color(255, 100, 0)));
        return scrollPane;
    }

    private void styleComponents() {
        // Style the start button
        startButton.setBackground(new Color(200, 100, 0));
        startButton.setForeground(Color.WHITE);
        startButton.setFont(new Font("Sans Serif", Font.BOLD, 14));
        startButton.setFocusPainted(false);
        startButton.setBorder(BorderFactory.createRaisedBevelBorder());

        // Style the status label
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setFont(new Font("Sans Serif", Font.PLAIN, 14));
    }

    private void clearTextAreas() {
        bruteForceA.setText("");
        bruteForceZ.setText("");
        bruteForceRandom.setText("");
        randomGuessing.setText("");
    }

    private void startAttacks(String password) {
        textAreaAppend("Starting attacks...\n", bruteForceA);
        textAreaAppend("Starting attacks...\n", bruteForceZ);
        textAreaAppend("Starting attacks...\n", bruteForceRandom);
        textAreaAppend("Starting attacks...\n", randomGuessing);

        Future<?> bruteForceAFuture = executorService.submit(() -> bruteForceFromA(password));
        Future<?> bruteForceZFuture = executorService.submit(() -> bruteForceFromZ(password));
        Future<?> bruteForceRandomFuture = executorService.submit(() -> bruteForceFromRandom(password));
        Future<?> randomGuessingFuture = executorService.submit(() -> randomGuessing(password));

        executorService.submit(() -> {
            try {
                bruteForceAFuture.get();
                bruteForceZFuture.get();
                bruteForceRandomFuture.get();
                randomGuessingFuture.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            } finally {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Status: Completed");
                    running.set(false);
                });
            }
        });
    }

    private void bruteForceFromA(String password) {
        final String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        char[] attempt = new char[password.length()];
        for (int i = 0; i < attempt.length; i++) {
            attempt[i] = characters.charAt(0);
        }

        long startTime = System.currentTimeMillis();
        int attemptCount = 0;
        while (running.get() && !passwordFound.get()) {
            String currentAttempt = new String(attempt);
            attemptCount++;
            if (currentAttempt.equals(password)) {
                long endTime = System.currentTimeMillis();
                textAreaAppend("Password guessed: " + currentAttempt + "\nTime taken: " + (endTime - startTime) + " milliseconds", bruteForceA);
                passwordFound.set(true);
                break;
            }

            if (attemptCount % 10 == 0) {
                textAreaAppend("Trying: " + currentAttempt, bruteForceA);
            }

            attempt = incrementAttempt(attempt, characters);
            if (attempt == null) {
                textAreaAppend("Failed to guess the password.", bruteForceA);
                break;
            }

            try {
                Thread.sleep(TIME_BETWEEN_ATTEMPTS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void bruteForceFromZ(String password) {
        final String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        char[] attempt = new char[password.length()];
        for (int i = 0; i < attempt.length; i++) {
            attempt[i] = characters.charAt(characters.length() - 1);
        }

        long startTime = System.currentTimeMillis();
        int attemptCount = 0;
        while (running.get() && !passwordFound.get()) {
            String currentAttempt = new String(attempt);
            attemptCount++;
            if (currentAttempt.equals(password)) {
                long endTime = System.currentTimeMillis();
                textAreaAppend("Password guessed: " + currentAttempt + "\nTime taken: " + (endTime - startTime) + " milliseconds", bruteForceZ);
                passwordFound.set(true);
                break;
            }

            if (attemptCount % 10 == 0) {
                textAreaAppend("Trying: " + currentAttempt, bruteForceZ);
            }

            attempt = decrementAttempt(attempt, characters);
            if (attempt == null) {
                textAreaAppend("Failed to guess the password.", bruteForceZ);
                break;
            }

            try {
                Thread.sleep(TIME_BETWEEN_ATTEMPTS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void bruteForceFromRandom(String password) {
        final String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        char[] attempt = new char[password.length()];
        int attemptCount = 0;

        // Initialize attempt with a random start
        for (int i = 0; i < attempt.length; i++) {
            attempt[i] = characters.charAt(random.nextInt(characters.length()));
        }

        long startTime = System.currentTimeMillis();
        while (running.get() && !passwordFound.get()) {
            String currentAttempt = new String(attempt);
            attemptCount++;
            if (currentAttempt.equals(password)) {
                long endTime = System.currentTimeMillis();
                textAreaAppend("Password guessed: " + currentAttempt + "\nTime taken: " + (endTime - startTime) + " milliseconds", bruteForceRandom);
                passwordFound.set(true);
                break;
            }

            if (attemptCount % 10 == 0) {
                textAreaAppend("Trying: " + currentAttempt, bruteForceRandom);
            }

            // Randomly generate a new attempt
            for (int i = 0; i < attempt.length; i++) {
                attempt[i] = characters.charAt(random.nextInt(characters.length()));
            }

            try {
                Thread.sleep(TIME_BETWEEN_ATTEMPTS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void randomGuessing(String password) {
        final String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        long startTime = System.currentTimeMillis();
        int attemptCount = 0;

        while (running.get() && !passwordFound.get()) {
            StringBuilder attempt = new StringBuilder();
            for (int i = 0; i < password.length(); i++) {
                attempt.append(characters.charAt(random.nextInt(characters.length())));
            }

            attemptCount++;
            if (attempt.toString().equals(password)) {
                long endTime = System.currentTimeMillis();
                textAreaAppend("Password guessed: " + attempt.toString() + "\nTime taken: " + (endTime - startTime) + " milliseconds", randomGuessing);
                passwordFound.set(true);
                break;
            }

            if (attemptCount % 10 == 0) {
                textAreaAppend("Trying: " + attempt.toString(), randomGuessing);
            }

            try {
                Thread.sleep(TIME_BETWEEN_ATTEMPTS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private char[] incrementAttempt(char[] attempt, String characters) {
        for (int i = attempt.length - 1; i >= 0; i--) {
            int index = characters.indexOf(attempt[i]);
            if (index < characters.length() - 1) {
                attempt[i] = characters.charAt(index + 1);
                return attempt;
            } else {
                attempt[i] = characters.charAt(0);
            }
        }
        return null;
    }

    private char[] decrementAttempt(char[] attempt, String characters) {
        for (int i = attempt.length - 1; i >= 0; i--) {
            int index = characters.indexOf(attempt[i]);
            if (index > 0) {
                attempt[i] = characters.charAt(index - 1);
                return attempt;
            } else {
                attempt[i] = characters.charAt(characters.length() - 1);
            }
        }
        return null;
    }

    private void textAreaAppend(String text, JTextArea textArea) {
        SwingUtilities.invokeLater(() -> {
            textArea.append(text + "\n");
            textArea.setCaretPosition(textArea.getDocument().getLength());
            writeToFile(text);
        });
    }

    private void writeToFile(String content) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("output.txt", true))) {
            writer.write(content);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PasswordHacking frame = new PasswordHacking();
            frame.setVisible(true);
        });
    }
}
