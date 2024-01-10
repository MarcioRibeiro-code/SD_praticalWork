package Gui.Dialogs;

import Requests.UserLogin;
import utils.Channel.SendMessageToUser;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Optional;

public class SendMessageDialog extends JDialog {

    private JTextField messageTextField;

    private JComboBox<String> usersComboBox;
    private JButton sendMessageButton;

    private SendMessageToUser sendMessageToUser;

    private String senderUserName;

    public SendMessageDialog(JFrame parent, List<UserLogin> users, String senderUserName) {
        super(parent, "Send Message", true);

        this.senderUserName = senderUserName;

        // Initialize Components
        messageTextField = new JTextField();
        sendMessageButton = new JButton("Send Message");

        usersComboBox = new JComboBox<String>();
        for (UserLogin user : users) {
            usersComboBox.addItem(user.getUsername());
        }

        sendMessageButton.addActionListener(e -> processMessage(users));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(3, 2, 10, 10)); // Adjust as needed

        mainPanel.add(new JLabel("Select User:"));
        mainPanel.add(usersComboBox);

        mainPanel.add(new JLabel("Enter Message:"));
        mainPanel.add(messageTextField);

        mainPanel.add(new JLabel()); // Empty label for spacing
        mainPanel.add(sendMessageButton);

        // Add mainPanel to the dialog's content pane
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(mainPanel, BorderLayout.CENTER);

        // Set frame properties
        pack();
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private void processMessage(List<UserLogin> users) {
        // Retrieve the channel name from the text field
        Object receiver = usersComboBox.getSelectedItem();
        String message = messageTextField.getText();

        if (receiver == null) {
            // If the channel name is empty, show an error message
            JOptionPane.showMessageDialog(this, "Please select a user",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (message.isEmpty()) {
            // If the channel name is empty, show an error message
            JOptionPane.showMessageDialog(this, "Please enter a message",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Optional<UserLogin> foundUser = users.stream()
                .filter(user -> user.getUsername().equals(receiver))
                .findFirst();

        // Create a new CreateChannel object
        if (foundUser.isPresent()) {
            this.sendMessageToUser = new SendMessageToUser(senderUserName,foundUser.get().getUuid(), message);
        }


        // Close the dialog
        dispose();
    }

    public SendMessageToUser getSendMessageToUser() {
        return sendMessageToUser;
    }
}
