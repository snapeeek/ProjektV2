import projekt.*;
import projekt.Graphics;

import javax.naming.Name;
import javax.swing.*;


import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class Client
{
    private String serverIP;
    private Semaphore mut;
    protected String username;
    protected String path;
    protected Socket sock;
    protected FolderLocale folder;
    protected DataOutputStream dos;
    protected DataInputStream dis;
    protected Graphics graphics;
    protected JTextArea files = new JTextArea(20,50);
    protected boolean ListNeeded = false;
    protected boolean ChooseClient = false;
    protected String SendingUser;
    protected boolean ChooseFile = false;
    protected String SendingFile;

    public Client (String name, String sauce)
    {
        this.serverIP = "127.0.0.1";
        this.username = name;
        this.path = sauce;
        mut = new Semaphore(1);

        try
        {
            sock = new Socket(serverIP, 50005);
            dos = new DataOutputStream(sock.getOutputStream());
            dis = new DataInputStream(sock.getInputStream());
        }
        catch (IOException IOExp)
        {
            IOExp.printStackTrace();
        }

        graphics=new Graphics("Klient"+username);
        graphics.jPanel.add(files);
        graphics.jLabel.setText("czekam");
        files.setEditable(false);
        JButton jButton=new JButton("Lista klientow");
        jButton.addActionListener(e -> ListNeeded = true);
        graphics.jFrame.add(jButton, BorderLayout.SOUTH);
        graphics.jFrame.pack();
    }

    protected void NameVerification() throws NameAlreadyTakenException
    {
        try
        {
            dos.writeUTF(username);
            String ret = dis.readUTF();
            System.out.println(ret);

            if (ret.compareTo("Name is already taken") == 0)
                throw new NameAlreadyTakenException();
        }
        catch (IOException IOExp)
        {
            IOExp.printStackTrace();
        }
    }

    protected void run()
    {
        ExecutorService pool = Executors.newFixedThreadPool(10);
        try
        {
            folder = new FolderLocale(path);
            try
            {
                NameVerification();
            }
            catch (NameAlreadyTakenException NATExp)
            {
                System.out.println(NATExp.GetWarning());
            }

            String ServerMessage;
            ArrayList<String> clients = new ArrayList<>();
            String filename;
            int HowManyFiles;

            ArrayList<String> ClientFiles = folder.GetNames();

            HowManyFiles = dis.readInt();
            graphics.jLabel.setText("Odbieram");

            for (int i = 0; i < HowManyFiles; i++)
            {
                while (mut.tryAcquire()) {}
                mut.release();
                filename = dis.readUTF();
                if (ClientFiles.contains(filename))
                {
                    dos.writeUTF("No");
                }
                else
                {
                    dos.writeUTF("Yes");
                    pool.execute(new Receiving((path + "\\" + filename), mut, dis));
                    Thread.sleep(150);
                }
            }

            while (true)
            {
                folder.NewFile();

                if (folder.ToSend.size() > 0)
                {
                    while (mut.tryAcquire());
                    dos.writeUTF("New file");
                    File temp = folder.ToSend.remove(0);
                    files.append(temp.getName());
                    dos.writeUTF(temp.getName());
                    ServerMessage = dis.readUTF();
                    if (ServerMessage.compareTo("Send") == 0)
                    {
                        mut.release();
                        pool.execute(new Sending(temp, mut, dos));
                        graphics.jLabel.setText("Wysylam");
                        Thread.sleep(300);
                    }
                }
                else if (graphics.end)
                {
                    while (mut.tryAcquire());
                    dos.writeUTF("End");
                    break;
                }
                else if (ListNeeded) {
                    while (mut.tryAcquire()) ;
                    dos.writeUTF("I need a list of clients");
                    int number;
                    number = dis.readInt();
                    if (number > 0) {
                        clients.clear();
                        for (int i = 0; i < number; i++) {
                            clients.add(dis.readUTF());
                        }

                        ListNeeded = false;
                        graphics.jFrame.setVisible(false);
                        Graphics ToWhom = new Graphics("Komu wyslac?");
                        int pos = 0;
                        ToWhom.jFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                        for (String x : clients) {
                            JButton button = new JButton(x);
                            button.addActionListener(e -> {
                                SendingUser = button.getText();
                                ChooseClient = true;
                                ToWhom.jFrame.setVisible(false);
                                ToWhom.jFrame.dispose();
                            });
                            GridBagConstraints grid = new GridBagConstraints();
                            grid.gridx = pos;
                            grid.gridy = 0;
                            pos++;
                            ToWhom.jPanel.add(button, grid);
                        }

                        ToWhom.jFrame.pack();
                        ToWhom.jFrame.setVisible(true);

                        while (!ChooseClient) {
                            Thread.sleep(500);
                        }
                        ChooseClient = false;
                        Graphics WhatToSend = new Graphics("Co wyslac?");
                        WhatToSend.jFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                        pos = 0;
                        for (String x : folder.GetNames()) {
                            JButton butt = new JButton(x);
                            butt.addActionListener(e -> {
                                SendingFile = butt.getText();
                                ChooseFile = true;
                                WhatToSend.jFrame.setVisible(false);
                                WhatToSend.jFrame.dispose();
                            });
                            GridBagConstraints grid = new GridBagConstraints();
                            grid.gridx = pos;
                            grid.gridy = 0;
                            pos++;
                            WhatToSend.jPanel.add(butt, grid);
                        }
                        WhatToSend.jFrame.pack();
                        WhatToSend.jFrame.setVisible(true);

                        while (!ChooseFile) {
                            Thread.sleep(500);
                        }


                        ChooseFile = false;
                        dos.writeUTF("File to client");
                        dos.writeUTF(SendingFile);
                        dos.writeUTF(SendingUser);
                        graphics.jFrame.setVisible(true);
                        mut.release();
                    }
                }
                else
                {
                    while (mut.tryAcquire());
                    dos.writeUTF("Something for me");
                    ServerMessage = dis.readUTF();
                    if (ServerMessage.compareTo("Yes") == 0)
                    {
                        SendingFile = dis.readUTF();
                        if (folder.GetNames().contains(SendingFile))
                        {
                            dos.writeUTF("I have this one");
                        }
                        else
                        {
                            mut.release();
                            dos.writeUTF("Send");
                            pool.execute(new Receiving((path + "\\" + SendingFile), mut, dis));
                            graphics.jLabel.setText("Odbieram");
                            Thread.sleep(500);
                            while (mut.tryAcquire());
                        }
                    }
                    mut.release();
                }
                graphics.jLabel.setText("Czekam");
            }

        }
        catch (Exception Exp)
        {
            Exp.printStackTrace();
        }
        finally
        {
            graphics.jFrame.setVisible(false);
            graphics.jFrame.dispose();
            pool.shutdown();
        }
    }

    public static void main(String[] args)
    {
        if (args.length != 2)
        {
            System.err.println("Two arguments are needed to run this program");
            return;
        }

        String name = args[0];
        String path = args[1];

        var client = new Client(name, path);

        client.graphics.jFrame.setVisible(true);
        client.run();
    }
}
