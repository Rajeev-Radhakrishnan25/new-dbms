package com.lightweightDbms;

import com.lightweightDbms.auth.*;
import com.lightweightDbms.console.ConsoleShell;
import com.lightweightDbms.audit.AuditLogger;
import com.lightweightDbms.audit.CsvAuditLogger;
import com.lightweightDbms.audit.IpProvider;
import com.lightweightDbms.audit.SimpleIpProvider;
import com.lightweightDbms.storage.StorageConfig;
import com.lightweightDbms.storage.QueryLogger;
import com.lightweightDbms.repository.FileUserRepository;
import com.lightweightDbms.db.DatabaseEngine;
import com.lightweightDbms.db.InMemoryDatabaseEngine;
import com.lightweightDbms.sql.SqlParser;
import com.lightweightDbms.exception.PasswordHashException;
import com.lightweightDbms.exception.UserAlreadyExistsException;
import com.lightweightDbms.exception.UserNotFoundException;
import com.lightweightDbms.model.User;
import com.lightweightDbms.repository.InMemoryUserRepository;
import com.lightweightDbms.repository.UserRepository;

import java.util.Scanner;
/**
 * Main class for the lightweight DBMS.
 * This class serves as the entry point and handles the console-based user interaction with application.
 *
 */
public class DBMSApplication {
    private final Scanner scanner;
    private final AuthenticationService authService;
    private final StorageConfig storage;
    /**
     * Constructs a new DBMSApplication.
     * Initializes the authentication service with all required dependencies.
     */
    public DBMSApplication() {
        storage = new StorageConfig("data", '|', "\\");
        UserRepository userRepository = new FileUserRepository(storage);
        PasswordHasher passwordHasher = new SHA256PasswordHasher();
        CaptchaGenerator captchaGenerator = new SimpleCaptchaGenerator();
        SecurityQuestions securityQuestions = new SimpleSecurityQuestions();
        TokenManager tokenManager = new TokenManager();
        PasswordRecovery passwordRecovery = new PasswordRecoveryService(userRepository, passwordHasher, tokenManager);
        AuditLogger auditLogger = new CsvAuditLogger(storage.auditFile().getPath());
        IpProvider ipProvider = new SimpleIpProvider();

        this.authService = new AuthenticationService(userRepository, passwordHasher, captchaGenerator, securityQuestions, passwordRecovery, auditLogger, ipProvider);
        this.scanner = new Scanner(System.in);
    }

    /**
     * Starts the DBMS application.
     * Displays the main menu and handles user interactions.
     */
    public void start() {
        printWelcomeBanner();

        while (true) {
            try {
                displayMainMenu();
                String choice = scanner.nextLine().trim();

                switch (choice) {
                    case "1" -> handleRegistration();
                    case "2" -> handleLogin();
                    case "3" -> handlePasswordRecovery();
                    case "4" -> {
                        System.out.println("\nThank you for using DBMS. Goodbye!");
                        return;
                    }
                    default -> System.out.println("\n[ERROR] Invalid choice. Please try again.");
                }
            } catch (Exception e) {
                System.out.println("\nERROR - An unexpected error occurred: " + e.getMessage());
            }
        }
    }

    /**
     * Displays the welcome banner on top.
     */
    private void printWelcomeBanner() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("          LIGHTWEIGHT DBMS APPLICATION ");
        System.out.println("=".repeat(50));
    }

    /**
     * Displays the main menu options to console.
     */
    private void displayMainMenu() {
        System.out.println("\n" + "-".repeat(50));
        System.out.println("MAIN MENU");
        System.out.println("-".repeat(50));
        System.out.println("1. Register New User");
        System.out.println("2. Login");
        System.out.println("3. Forgot Password? Reset Password");
        System.out.println("4. Exit");
        System.out.print("\nEnter your choice: ");
    }

    /**
     * Handles user registration process.
     */
    private void handleRegistration() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("USER REGISTRATION");
        System.out.println("=".repeat(50));

        try {
            System.out.print("Enter User ID (3-50 characters, alphanumeric and underscore only): ");
            String userId = scanner.nextLine().trim();

            if (authService.userExists(userId)) {
                System.out.println("\nERROR User ID already exists in system. Please choose a different ID.");
                return;
            }

            System.out.print("Enter Password (minimum 6 characters): ");
            String password = scanner.nextLine();

            System.out.print("Confirm Password: ");
            String confirmPassword = scanner.nextLine();

            if (!password.equals(confirmPassword)) {
                System.out.println("\n[ERROR] Passwords do not match. Registration failed.");
                return;
            }

            // Step 2: Generate and display Security Questions
            String[] securityQuestions = authService.generateSecurityQuestions();
            System.out.println("\n" + "-".repeat(50));
            System.out.println("SECURITY QUESTION");
            System.out.println("-".repeat(50));
            for (int i = 0; i < securityQuestions.length; ++i) {
                System.out.println(i + 1 + " " + securityQuestions[i]);
            }
            System.out.print("Choose any Security Question, Enter Choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine();
            if (choice <= 0 || choice > securityQuestions.length)
                throw new IllegalArgumentException("Number not correct");
            String chosenQuestion = securityQuestions[choice - 1];
            System.out.println("Enter the Answer for the Following Question:\n" + chosenQuestion);
            System.out.print("Your Answer: ");
            String answer = scanner.nextLine().trim();

            authService.registerUser(userId, password, chosenQuestion, answer);
            System.out.println("\n[SUCCESS] User registered successfully!");
            System.out.println("You can now login with your credentials.");

        } catch (IllegalArgumentException e) {
            System.out.println("\n[ERROR] Invalid input: " + e.getMessage());
        } catch (UserAlreadyExistsException e) {
            System.out.println("\n[ERROR] " + e.getMessage());
        } catch (PasswordHashException e) {
            System.out.println("\n[ERROR] Failed to register user: " + e.getMessage());
        }
    }

    /**
     * Handles user login process with two-factor authentication with Catpcha verification.
     */
    private void handleLogin() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("USER LOGIN - Two-Factor Authentication");
        System.out.println("=".repeat(50));

        try {
            // Step 1: Get credentials
            System.out.print("Enter User ID: ");
            String userId = scanner.nextLine().trim();

            System.out.print("Enter Password: ");
            String password = scanner.nextLine();

            // Step 2: Generate and display CAPTCHA
            Captcha captcha = authService.generateCaptcha();
            System.out.println("\n" + "-".repeat(50));
            System.out.println("CAPTCHA VERIFICATION");
            System.out.println("-".repeat(50));
            System.out.println("Challenge: " + captcha.getChallenge());
            System.out.print("Your Answer: ");
            String captchaResponse = scanner.nextLine().trim();

            // Step 3: Authenticate
            AuthenticationResult result = authService.authenticate(userId, password, captcha, captchaResponse);

            if (result.isSuccess()) {
                System.out.println("\n" + "=".repeat(50));
                System.out.println("[SUCCESS] " + result.getMessage());
                System.out.println("=".repeat(50));
                System.out.println("Welcome, " + userId + "!");
                System.out.println("You are now logged into the DBMS system.");
                if (result.getPreviousSuccessfulLoginAt() > 0) {
                    System.out.println("Last successful login: " + new java.util.Date(result.getPreviousSuccessfulLoginAt()));
                } else {
                    System.out.println("This appears to be your first successful login.");
                }

                // Initialize database engine and SQL shell
                DatabaseEngine engine = new InMemoryDatabaseEngine(storage);
                SqlParser parser = new SqlParser();
                // Pass audit logger and admin flag into shell for SHOW LOGS
                User loggedIn = authService.getUser(userId);
                AuditLogger auditLogger = new CsvAuditLogger(storage.auditFile().getPath());
                ConsoleShell shell = new ConsoleShell(engine, parser, scanner, auditLogger, loggedIn != null && loggedIn.isAdmin());
                shell.startSession();
            } else {
                System.out.println("\n[FAILED] " + result.getMessage());
                System.out.println("Please try again.");
            }

        } catch (PasswordHashException e) {
            System.out.println("\n[ERROR] Authentication failed: " + e.getMessage());
        }
    }

    public void handlePasswordRecovery() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("PASSWORD RECOVERY");
        System.out.println("=".repeat(50));
        // Step 1: Get User ID
        System.out.print("Enter User ID: ");
        String userId = scanner.nextLine().trim();

        User user = authService.getUser(userId);
        if (user == null) {
            System.out.println("\nERROR User not Found in system. Please enter a correct User ID.");
            return;
        }

        // Step 2: Get Answer for Security Question
        System.out.println("Enter the Answer for the Following Security Question:\n" + user.getSecurityQuestion());
        System.out.print("Your Answer: ");
        String answer = scanner.nextLine().trim();

        // Step 3: Verification
        try {
            String token = authService.initiatePasswordRecovery(userId, answer);
            if (token != null) {
                System.out.println("TOKEN SESSION EXPIRATION MINUTES " + authService.getTokenExpirationTime() + "\n");
                System.out.print("Enter New Password (minimum 6 characters): ");
                String password = scanner.nextLine();

                System.out.print("Confirm Password: ");
                String confirmPassword = scanner.nextLine();

                if (!password.equals(confirmPassword)) {
                    System.out.println("\n[ERROR] Passwords do not match. Password Reset failed.");
                    return;
                }

                AuthenticationResult result = authService.handlePasswordRecovery(token, userId, password);
                if (result.isSuccess()) {
                    System.out.println("\n" + "=".repeat(50));
                    System.out.println("[SUCCESS] " + result.getMessage());
                    System.out.println("=".repeat(50));
                    System.out.println("Welcome, " + userId + "!");
                    System.out.println("You can now login into the DBMS system.");
                } else {
                    System.out.println("\n[FAILED] " + result.getMessage());
                    System.out.println("Please try again.");
                }
            }
        } catch (IllegalArgumentException e) {
            System.out.println("\n[ERROR] Invalid input: " + e.getMessage());
        } catch (UserNotFoundException e) {
            System.out.println("\n[ERROR] " + e.getMessage());
        } catch (PasswordHashException e) {
            System.out.println("\n[ERROR] Authentication failed: " + e.getMessage());
        }

    }

    /**
     * Closes the scanner resource input.
     */
    public void close() {
        if (scanner != null) {
            scanner.close();
        }
    }

    /**
     * Main entry point of the application.
     * Driver method main
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        DBMSApplication app = new DBMSApplication();
        try {
            app.start();
        } finally {
            app.close();
        }
    }
}