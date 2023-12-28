package Gui.Dialogs;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import Entity.MilitarType;
import Requests.CreateChannel;

import java.awt.BorderLayout;
import java.awt.GridLayout;

public class CreateChannelDialog extends JDialog {
    private JTextField channelNameField;
    private JComboBox<MilitarType> roleComboBox;
    private JRadioButton isPrivateRadioButton;
    private JButton createChannelButton;

    // Other Components
    private CreateChannel createChannel;

    public CreateChannelDialog(JFrame parent) {
        super(parent, "Create Channel", true);

        // Initialize components
        channelNameField = new JTextField();
        createChannelButton = new JButton("Create Channel");

        // Initialize combo box and radio button
        roleComboBox = new JComboBox<>(MilitarType.values());
        isPrivateRadioButton = new JRadioButton("Private");

        // Set layout
        setLayout(new BorderLayout(10, 10));

        // Add components to the dialog
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(3, 2, 5, 5));
        inputPanel.add(new JLabel("Channel Name:"));
        inputPanel.add(channelNameField);
        inputPanel.add(new JLabel("Role to access channel:"));
        inputPanel.add(roleComboBox);
        inputPanel.add(new JLabel("Private:"));
        inputPanel.add(isPrivateRadioButton);

        // Initially hide the combo box
        roleComboBox.setVisible(false);

        add(inputPanel, BorderLayout.CENTER);
        add(createChannelButton, BorderLayout.SOUTH);

        // Add action listeners
        createChannelButton.addActionListener(e -> createChannel());
        isPrivateRadioButton.addActionListener(e -> {
            // Show/hide the combo box depending on the radio button's state
            roleComboBox.setVisible(isPrivateRadioButton.isSelected());
        });

        // Set the size of the dialog
        setSize(300, 200);
        setLocationRelativeTo(parent);
    }

    private void createChannel() {
        // Retrieve the channel name from the text field
        String channelName = channelNameField.getText();

        if (channelName.isEmpty()) {
            // If the channel name is empty, show an error message
            JOptionPane.showMessageDialog(this, "Please enter a channel name",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Retrieve the selected channel type
        boolean isPrivate = isPrivateRadioButton.isSelected();

        // Retrieve the selected role (default to null if private is false)
        MilitarType selectedRole = isPrivate ? (MilitarType) roleComboBox.getSelectedItem() : null;

        // Create a new CreateChannel object
        this.createChannel = new CreateChannel(channelName, isPrivate, selectedRole);

        // Close the dialog
        dispose();
    }

    public CreateChannel getCreateChannel() {
        return createChannel;
    }
}
