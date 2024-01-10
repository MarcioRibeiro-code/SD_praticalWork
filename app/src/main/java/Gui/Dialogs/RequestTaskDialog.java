package Gui.Dialogs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import utils.Channel.SendMessageToChannel;
import utils.Channel.SendMessageToUser;
import utils.Requests.RequestType;

public class RequestTaskDialog extends JDialog {

    private JTextField taskTextField;
    private JComboBox<String> taskComboBox;
    private JButton sendTaskButton;

    private SendMessageToChannel sendMessageToChannel;


    public RequestTaskDialog(JFrame parent, String channelName, String senderUserName) {
        super(parent, "Request Military Task", true);


        // Initialize Components
        taskTextField = new JTextField();
        sendTaskButton = new JButton("Send Task");

        // Populate taskComboBox with military task types
        taskComboBox = new JComboBox<>(new String[]{"Launch Missile", "Move Personnel", "Secure Area", "Reconnaissance"});
        
        sendTaskButton.addActionListener(e -> processTask(channelName, senderUserName));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(3, 2, 10, 10)); // Adjust as needed

        mainPanel.add(new JLabel("Select Military Task:"));
        mainPanel.add(taskComboBox);

        mainPanel.add(new JLabel("Task Details:"));
        mainPanel.add(taskTextField);

        mainPanel.add(new JLabel()); // Empty label for spacing
        mainPanel.add(sendTaskButton);

        // Add mainPanel to the dialog's content pane
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(mainPanel, BorderLayout.CENTER);

        // Set frame properties
        pack();
    }

    private void processTask(String channelName, String senderUserName) {
        // Add your logic to process the military task here
        String selectedTaskType = (String) taskComboBox.getSelectedItem();
        String taskDetails = taskTextField.getText();



//check if all fields are filled
        if (taskDetails.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (selectedTaskType == null) {
            // If the channel name is empty, show an error message
            JOptionPane.showMessageDialog(this, "Please select a task type",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

     this.sendMessageToChannel = new SendMessageToChannel(RequestType.REQUEST_TASK,
               channelName, senderUserName, selectedTaskType+ " - " +taskDetails);

        // Perform actions based on the selected military task type and entered details
        // You can use sendMessageToChannel or any other appropriate method
        // to send the task information to the channel.

        // Close the dialog after processing
        dispose();
    }

    public SendMessageToChannel getSendMessageToChannel() {
        return sendMessageToChannel;
    }

}
