package Gui.Dialogs;

import java.awt.*;
import java.util.List;
import java.util.Optional;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import Requests.UserLogin;
import utils.Channel.SendMessageToChannel;
import utils.Channel.SendMessageToUser;
import utils.Requests.RequestType;

public class SendMessageToChannelDialog extends JDialog {
    private JTextField messageTextField;

    private JButton sendMessageButton;

    private SendMessageToChannel sendMessageToChannel;

    private String senderUserName;

    public SendMessageToChannelDialog(JFrame parent, String channelName, String senderUserName) {
        super(parent, "Send Message to Channel", true);

        this.senderUserName = senderUserName;

        // Initialize Components
        messageTextField = new JTextField();
        sendMessageButton = new JButton("Send Message to Channel");


        sendMessageButton.addActionListener(e -> processMessage(channelName,senderUserName));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(3, 2, 10, 10)); // Adjust as needed


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

    private void processMessage(String chanString, String senderUserName) {
        String message = messageTextField.getText();

        if (chanString == null) {
            // If the channel name is empty, show an error message
            JOptionPane.showMessageDialog(this, "Please select a channel in UserMenuFrame",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (message.isEmpty()) {
            // If the channel name is empty, show an error message
            JOptionPane.showMessageDialog(this, "Please enter a message",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        this.sendMessageToChannel = new SendMessageToChannel(RequestType.SEND_MESSAGE_TO_CHANNEL,
                chanString, senderUserName, message);

        // Close the dialog
        dispose();
    }

    public SendMessageToChannel getSendMessageToChannel() {
        return sendMessageToChannel;
    }
}

