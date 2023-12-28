package Gui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import Gui.Dialogs.CreateChannelDialog;
import Requests.CreateChannel;
import Requests.UserLogin;
import utils.Channel.ChannelResponse;
import utils.Channel.JoinChannel;
import utils.Requests.Request;
import utils.Requests.RequestType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Set;

public class UserMenuFrame extends JFrame {
    private JTextArea channels;
    private JButton joinChannelButton;
    private JButton createChannelButton;
    private JButton sendMessageButton;
    private JButton sendMessageToChannelButton;
    private JTextArea channelMessagetextArea;
    private JComboBox channelSelectorComboBox;
    private JTextArea directMessagesTextArea;
    private JPanel mainPanel;
    private JTabbedPane tabbedPane;
    private JPanel messageScreen;
    private JPanel channelsScreen;
    private JPanel tasksScreen;
    private JButton ValidateButton;
    private JList taskList1;

    // Other Components
    private final Gson jsonHelper;
    private final Client client;
    private UserLogin user;

    public UserMenuFrame(Client client, UserLogin user) {
        $$$setupUI$$$();
        setTitle("User Menu: " + user.getUsername() + " - " + user.getRole());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        add(mainPanel);
        setVisible(true);
        this.jsonHelper = new GsonBuilder().serializeNulls().create();
        this.client = client;
        this.user = user;
        createUIComponents();

        joinChannelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                joinSelectedChannel();
            }
        });
        createChannelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openCreateChannelDialog();
            }
        });


        tabbedPane.addChangeListener(e -> {
            // This method will be called when the selected tab changes
            Component selectedComponent = tabbedPane.getSelectedComponent();

            if (selectedComponent == messageScreen) {
                requestJoinedChannels();
            } else if (selectedComponent == channelsScreen) {
                requestJoinableChannels();
            }
        });

        // Solicitar canais inicialmente quando o UserMenuFrame é criado
        requestJoinableChannels();
        requestJoinedChannels();
    }

    private void openCreateChannelDialog() {
        // Open the "Create Channel" dialog
        CreateChannelDialog createChannelDialog = new CreateChannelDialog(this);
        createChannelDialog.setVisible(true);

        while (createChannelDialog.isVisible()) {
            // Wait until the dialog is closed
        }

        // Retrieve the created channel from the dialog
        CreateChannel createChannel = createChannelDialog.getCreateChannel();

        if (createChannel != null) {
            // If the channel is not null, send the request to the server
            String jsonRequest = jsonHelper.toJson(new Request<>(RequestType.CREATE_CHANNEL, createChannel));
            this.client.sendMessage(jsonRequest);
        } else {
            System.out.println("null");
        }
    }

    private void requestJoinedChannels() {
        // Create a request to get the joined channels
        String jsonRequest = jsonHelper.toJson(new Request<>(RequestType.GET_JOINED_CHANNELS, user.getUuid()));

        // Send the request to the server
        this.client.sendMessage(jsonRequest);
    }

    private void requestJoinableChannels() {
        // Create a request to get the joinable channels
        String jsonRequest = jsonHelper.toJson(new Request<>(RequestType.GET_JOINABLE_CHANNELS, user.getUuid()));

        // Send the request to the server
        this.client.sendMessage(jsonRequest);
    }

    // Add this method to update the UI with the channels received from the server
    public void updateJoinableChannels(Set<ChannelResponse> channelsInfo) {
        String[] channels = new String[channelsInfo.size()];
        int i = 0;

        for (ChannelResponse channel : channelsInfo) {
            channels[i] = channel.getName();
            i++;
        }

        SwingUtilities.invokeLater(() -> {
            // Concatenate the channels with newline separator
            String channelsText = String.join("\n", channels);
            this.channels.setText(channelsText);
        });
    }

    public void updateJoinedChannels(Set<ChannelResponse> channels2) {
        // Update the channel selector combo box
        channelSelectorComboBox.removeAllItems();
        for (ChannelResponse channel : channels2) {
            channelSelectorComboBox.addItem(channel.getName());
        }

    }

    public void joinSelectedChannel() {
        // Retrieve the selected channel from the text area
        String selectedChannel = channels.getSelectedText();

        if (selectedChannel == null) {
            // If no channel is selected, show an error message
            JOptionPane.showMessageDialog(this, "Please select a channel to join", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        JoinChannel joinChannel = new JoinChannel(selectedChannel, user.getUuid());
        // Create a request to join the selected channel
        String jsonRequest = jsonHelper.toJson(new Request<>(RequestType.JOIN_CHANNEL, joinChannel));

        // Send the request to the server
        this.client.sendMessage(jsonRequest);
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane = new JTabbedPane();
        mainPanel.add(tabbedPane, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        messageScreen = new JPanel();
        messageScreen.setLayout(new GridLayoutManager(8, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane.addTab("Messages", messageScreen);
        final JLabel label1 = new JLabel();
        label1.setText("Channel");
        messageScreen.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        messageScreen.add(spacer1, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        channelSelectorComboBox = new JComboBox();
        messageScreen.add(channelSelectorComboBox, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Direct Messages");
        messageScreen.add(label2, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        messageScreen.add(scrollPane1, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        directMessagesTextArea = new JTextArea();
        directMessagesTextArea.setText("");
        scrollPane1.setViewportView(directMessagesTextArea);
        sendMessageButton = new JButton();
        sendMessageButton.setText("Send Message");
        messageScreen.add(sendMessageButton, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sendMessageToChannelButton = new JButton();
        sendMessageToChannelButton.setText("Send Message to Channel");
        messageScreen.add(sendMessageToChannelButton, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        channelMessagetextArea = new JTextArea();
        messageScreen.add(channelMessagetextArea, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        channelsScreen = new JPanel();
        channelsScreen.setLayout(new GridLayoutManager(4, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane.addTab("Channels", channelsScreen);
        final JLabel label3 = new JLabel();
        label3.setText("All Channels");
        channelsScreen.add(label3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        joinChannelButton = new JButton();
        joinChannelButton.setText("Join Channel");
        channelsScreen.add(joinChannelButton, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        createChannelButton = new JButton();
        createChannelButton.setText("Create Channel");
        channelsScreen.add(createChannelButton, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        channels = new JTextArea();
        channels.setEditable(false);
        channelsScreen.add(channels, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        tasksScreen = new JPanel();
        tasksScreen.setLayout(new GridLayoutManager(2, 3, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane.addTab("Tasks", tasksScreen);
        final JLabel label4 = new JLabel();
        label4.setText("All Tasks");
        tasksScreen.add(label4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        ValidateButton = new JButton();
        ValidateButton.setText("Validar");
        tasksScreen.add(ValidateButton, new GridConstraints(1, 0, 1, 3, GridConstraints.ANCHOR_SOUTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        taskList1 = new JList();
        final DefaultListModel defaultListModel1 = new DefaultListModel();
        taskList1.setModel(defaultListModel1);
        tasksScreen.add(taskList1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

    private void createUIComponents() {

        // Adicionar elementos diretamente à JList
        taskList1.setListData(new String[]{"Tarefa 1", "Tarefa 2", "Tarefa 3"});

        // Adicionar ação de clique para a JList
        taskList1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {  // Duplo clique
                    int selectedIndex = taskList1.getSelectedIndex();
                    if (selectedIndex != -1) {
                        String selectedTask = (String) taskList1.getModel().getElementAt(selectedIndex);
                        validarTarefa(selectedTask);
                    }
                }
            }
        });


        ValidateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = taskList1.getSelectedIndex();
                if (selectedIndex != -1) {
                    String selectedTask = (String) taskList1.getModel().getElementAt(selectedIndex);
                    validarTarefa(selectedTask);
                } else {
                    JOptionPane.showMessageDialog(null, "Selecione uma tarefa para validar.");
                }
            }
        });
    }


    private void validarTarefa(String tarefa) {
        // Lógica de validação
        JOptionPane.showMessageDialog(null, "Tarefa validada: " + tarefa);

        // Se você quiser remover a tarefa validada da lista, descomente a linha abaixo
        // ((DefaultListModel<String>) taskList1.getModel()).removeElement(tarefa);
    }


}
