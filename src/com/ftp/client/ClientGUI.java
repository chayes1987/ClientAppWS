/*
    @author Conor Hayes
 */
package com.ftp.client;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ClientGUI extends JFrame implements ActionListener {
    private JTextField host_name, username;
    private JTextArea client_output;
    private JButton connect, upload, download, logout;
    private FTPClient client;
    private JLabel user;
    private JPanel client_login, client_actions, output;

    /*
        No-arg Constructor
     */
    public ClientGUI(){
        buildAppWindow();
        createLoginGUI();
        createActionsGUI();
    }

    /*
        CreateActionsGUI Method
        Builds the actions interface for the Client
     */
    private void createActionsGUI() {
        client_actions = new JPanel();
        client_actions.setVisible(false);
        client_actions.setPreferredSize(new Dimension(300, 150));
        client_actions.setLayout(new GridLayout(4, 1));
        client_actions.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Choose Action"));
        user = new JLabel();
        client_actions.add(user);
        upload = new JButton("Upload File");
        upload.addActionListener(this);
        client_actions.add(upload);
        download = new JButton("Download File");
        download.addActionListener(this);
        client_actions.add(download);
        logout = new JButton("Logout");
        logout.addActionListener(this);
        client_actions.add(logout);
        add(client_actions);
        output = new JPanel();
        output.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Client Output"));
        output.setLayout(new GridLayout(1, 1));
        output.setPreferredSize(new Dimension(300, 150));
        client_output = new JTextArea();
        DefaultCaret caret = (DefaultCaret)client_output.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        client_output.setAutoscrolls(true);
        client_output.setLineWrap(true);
        client_output.setEditable(false);
        output.add(new JScrollPane (client_output, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
        output.setVisible(false);
        add(output);
    }

    /*
        CreateLoginGUI Method
        Builds the login interface for the Client
     */
    private void createLoginGUI() {
        client_login = new JPanel();
        client_login.setPreferredSize(new Dimension(300, 300));
        client_login.setLayout(new GridLayout(8, 1));
        client_login.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Client Login"));
        JLabel l = new JLabel("Server");
        client_login.add(l);
        host_name = new JTextField("localhost");
        client_login.add(host_name);
        l = new JLabel("Username");
        client_login.add(l);
        username = new JTextField();
        client_login.add(username);
        l = new JLabel("");
        client_login.add(l);
        connect = new JButton("Login");
        connect.addActionListener(this);
        client_login.add(connect);
        add(client_login);
    }

    /*
        BuildAppWindow Method
        Creates the interface for the Server
     */
    private void buildAppWindow() {
        setSize(350, 350);
        setResizable(false);
        setLayout(new FlowLayout());
        Calendar sysDate = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        setTitle("Client App" + " - " + sdf.format(sysDate.getTime()));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    /*
        ActionPerformed Method
        Handles GUI events
        @param e The event
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("Login")){
            if (host_name.getText().isEmpty()){
                JOptionPane.showMessageDialog(null, "Please enter the Service URL.");
            }else if (username.getText().isEmpty()){
                JOptionPane.showMessageDialog(null, "Please enter your Username.");
            }else{
                client = new FTPClient(host_name.getText());
                if (client.getFtpService() != null){
                    if (client.login(username.getText())) {
                        client_output.setText("Login Successful...\nConnection to FTP Server Established...\nServer Ready for FTP...");
                        client_login.setVisible(false);
                        client_actions.setVisible(true);
                        output.setVisible(true);
                        user.setText("Logged in as " + username.getText());
                    }else{
                        client_output.setText("Connection to FTP Server Failed...");
                    }
                }else{
                    JOptionPane.showMessageDialog(null, "Could not connect to Web Service, make sure server is up...");
                }
            }
        }else if (e.getActionCommand().equals("Upload File")){
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                if (client.uploadFile(file, username.getText()))
                    client_output.append("\n'"+ file.getName() + "' Uploaded Successfully...");
                else
                    client_output.append("\nFile Upload Failed...check server is running");
            }
        }else if (e.getActionCommand().equals("Download File")){
            Object[] availableFiles = client.getListFiles(username.getText());
            if(availableFiles == null){
                JOptionPane.showMessageDialog(null, "Unable to connect, check server is running...");
            }else if (availableFiles.length == 0){
                JOptionPane.showMessageDialog(null, "No files to download.");
            }else{
                String file = (String)JOptionPane.showInputDialog(null, "Choose a file to download.", "File Download", JOptionPane.PLAIN_MESSAGE, null, availableFiles, availableFiles[0]);
                if (file != null){
                    byte[] fileContents = client.downloadFile(username.getText(), file);
                    if(fileContents != null){
                        client_output.append("\n" + file + " Downloaded Successfully...");

                        JFileChooser fileChooser = new JFileChooser();
                        fileChooser.setSelectedFile(new File(file));
                        if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                            String path = fileChooser.getSelectedFile().getPath();
                            String targetDirectory = path.replace(file, "");
                            if (client.saveFile(path, fileContents)){
                                client_output.append("\nFile '" + file + "' Saved to '" + targetDirectory + "'.");
                            }
                        }
                    }else{
                        JOptionPane.showMessageDialog(null, "File was not downloaded...");
                    }
                }
            }
        }else if (e.getActionCommand().equals("Logout")){
            if(client.logout(username.getText())){
                client_actions.setVisible(false);
                client_login.setVisible(true);
                output.setVisible(false);
                username.setText("");
            }else{
                JOptionPane.showMessageDialog(null, "Logout failed...check server is running");
            }
        }
    }
}
