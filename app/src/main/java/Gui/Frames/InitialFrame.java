package Gui.Frames;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.password4j.Hash;
import com.password4j.Password;

import Entity.MilitarType;
import Entity.User;
import Gui.Client;
import Requests.Login;
import Requests.UserLogin;
import Requests.UserRegister;
import utils.Requests.Request;
import utils.Requests.RequestType;
import Gui.UserMenuFrame;

import javax.swing.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;

/**
 * My idea is to have first a welcome Screen with a login and register button
 * When the user clicks on register, a new frame will appear with the register
 * form When the user clicks on login, a new frame will appear with the login
 * form
 */
public class InitialFrame extends JFrame {
    private JPanel welcomePanel;
    private JPanel loginPanel;
    private JPanel registerPanel;
    private JPanel currentPanel;

    // Welcome components
    private JButton loginButtonWelcome;
    private JButton registerButtonWelcome;

    // Login components
    private JTextField usernameLoginTextField;
    private JPasswordField passwordLoginTextField;
    private JButton loginButton;
    private JButton backButtonLogin;

    // Register components
    private JTextField usernameRegisterTextField;
    private JPasswordField passwordRegisterTextField;
    private JPasswordField confirmPasswordRegisterTextField;
    private JTextField nameRegisterTextField;
    private JComboBox<MilitarType> militaryTypeComboBox;
    private JButton registerButton;
    private JButton backButtonRegister;

    // Other Components
    private final Gson jsonHelper;
    private final Client client;

    public InitialFrame(Client client) {
        this.jsonHelper = new GsonBuilder().serializeNulls().create();
        this.client = client;
        // Create UI components
        createUIComponents();
        // Initialize UI after components are created
        initializeUI();
    }

    private void createUIComponents() {
        // Initialize your own components

        backButtonLogin = new JButton("Back");
        backButtonRegister = new JButton("Back");

        // Welcome panel
        welcomePanel = new JPanel(new BorderLayout(10, 10));
        JPanel welcomeButtonsPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        loginButtonWelcome = new JButton("Login");
        registerButtonWelcome = new JButton("Register");
        welcomeButtonsPanel.add(loginButtonWelcome);
        welcomeButtonsPanel.add(registerButtonWelcome);
        welcomePanel.add(new JLabel("Welcome! Choose an option:"), BorderLayout.NORTH);
        welcomePanel.add(welcomeButtonsPanel, BorderLayout.CENTER);

        // Login panel
        loginPanel = new JPanel(new BorderLayout(10, 10));
        JPanel loginInnerPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        usernameLoginTextField = new JTextField();
        passwordLoginTextField = new JPasswordField();
        loginButton = new JButton("Login");
        loginInnerPanel.add(new JLabel("Username:"));
        loginInnerPanel.add(usernameLoginTextField);
        loginInnerPanel.add(new JLabel("Password:"));
        loginInnerPanel.add(passwordLoginTextField);
        loginPanel.add(loginInnerPanel, BorderLayout.CENTER);
        loginPanel.add(loginButton, BorderLayout.SOUTH);
        loginPanel.add(backButtonLogin, BorderLayout.NORTH);

        // Register panel
        registerPanel = new JPanel(new BorderLayout(10, 10));
        JPanel registerInnerPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        usernameRegisterTextField = new JTextField();
        passwordRegisterTextField = new JPasswordField();
        confirmPasswordRegisterTextField = new JPasswordField();
        nameRegisterTextField = new JTextField();
        militaryTypeComboBox = new JComboBox<>(MilitarType.values());
        registerInnerPanel.add(new JLabel("Username:"));
        registerInnerPanel.add(usernameRegisterTextField);
        registerInnerPanel.add(new JLabel("Password:"));
        registerInnerPanel.add(passwordRegisterTextField);
        registerInnerPanel.add(new JLabel("Confirm Password:"));
        registerInnerPanel.add(confirmPasswordRegisterTextField);
        registerInnerPanel.add(new JLabel("Full Name:"));
        registerInnerPanel.add(nameRegisterTextField);
        registerInnerPanel.add(new JLabel("Military Type:"));
        registerInnerPanel.add(militaryTypeComboBox);
        registerPanel.add(registerInnerPanel, BorderLayout.CENTER);
        registerButton = new JButton("Register");
        registerPanel.add(registerButton, BorderLayout.SOUTH);
        registerPanel.add(backButtonRegister, BorderLayout.NORTH);

        // Set the current panel to the welcome panel initially
        currentPanel = welcomePanel;
    }

    private void initializeUI() {
        setTitle("Welcome!");

        setContentPane(currentPanel); // Set the current panel to welcomePanel

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set preferred size before packing
        setPreferredSize(new Dimension(700, 500));

        pack();
        setLocationRelativeTo(null);

        // Register button action listener in welcomePanel
        registerButtonWelcome.addActionListener(e -> showRegisterPanel());

        // Login button action listener in welcomePanel
        loginButtonWelcome.addActionListener(e -> showLoginPanel());

        // Register button action listener in registerPanel
        registerButton.addActionListener(e -> registerUser());

        // Login button action listener in loginPanel
        loginButton.addActionListener(e -> loginUser());

        // Back button action listener in registerPanel and loginPanel
        backButtonRegister.addActionListener(e -> showWelcomePanel());
        backButtonLogin.addActionListener(e -> showWelcomePanel());

        // Set the frame visible after packing and setting location
        setVisible(true);
    }

    private void showWelcomePanel() {
        setContentPane(welcomePanel);
        pack();
        setLocationRelativeTo(null);
        currentPanel = welcomePanel;
    }

    private void showRegisterPanel() {
        setContentPane(registerPanel);
        pack();
        setLocationRelativeTo(null);
        currentPanel = registerPanel;
    }

    private void showLoginPanel() {
        setContentPane(loginPanel);
        pack();
        setLocationRelativeTo(null);
        currentPanel = loginPanel;
    }

    private void registerUser() {
        String name = nameRegisterTextField.getText();
        String username = usernameRegisterTextField.getText();
        String password = new String(passwordRegisterTextField.getPassword());
        String confirmPassword = new String(confirmPasswordRegisterTextField.getPassword());
        MilitarType militaryType = (MilitarType) militaryTypeComboBox.getSelectedItem();

        if (name.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()
                || militaryType == null) {
            JOptionPane.showMessageDialog(this, "Please fill all the fields", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords don't match", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Hash hash = Password.hash(password).withArgon2();
        // Create a login request
        UserRegister userRegister = new UserRegister(
                new User(name, militaryType.getTypeString(), hash.getResult(), username));
        // Convert the login request to a json string
        String jsonRequest = jsonHelper.toJson(new Request<>(RequestType.REGISTER, userRegister));

        // Send the request to the server
        this.client.sendMessage(jsonRequest);
    }

    private void loginUser() {
        // TODO: MISSING IMPLEMENTATION
        // Retrieve value from the login form
        String username = usernameLoginTextField.getText();
        String password = new String(passwordLoginTextField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            // Handle empty fields, shown an error message
            JOptionPane.showMessageDialog(this, "Please fill all the fields", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Hash hash = Password.hash(password).withArgon2();

        // Create a login request
        Login Login = new Login(username, hash.getResult());
        // Convert the login request to a json string
        String jsonRequest = jsonHelper.toJson(new Request<>(RequestType.LOGIN, Login));

        // Send the request to the server
        this.client.sendMessage(jsonRequest);
    }

    public void showUserMenuFrame(UserLogin user) {
        UserMenuFrame userMenuFrame = new UserMenuFrame(client, user);
        this.client.setUserMenuFrame(userMenuFrame);
        this.dispose();
        userMenuFrame.setVisible(true);
    }
}
