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

/**
 * klasa odpowiedzialna za klienta i komunikacje z serwerem
 */
public class Client
{
    /**adres na ktory klient sie laczy*/
    private String serverIP;
    /**semafor kontrolujacy strumienie*/
    private Semaphore mut;
    /**nazwa uzytkownika*/
    protected String username;
    /**sciezka do folderu klienta*/
    protected String path;
    /**gniazdo do polaczenia z serwerem*/
    protected Socket sock;
    /**folder lokalny klienta*/
    protected FolderLocale folder;
    /**strumien wyjsciowy*/
    protected DataOutputStream dos;
    /**strumien wejsciowy*/
    protected DataInputStream dis;
    /**okno graficzne*/
    protected Graphics graphics;
    /**pole tekstowe*/
    protected JTextArea files = new JTextArea(20,50);
    /**zmienia wartosc kiedy uzytkownik wysyla wiadomosc o potrzebie list*/
    protected boolean ListNeeded = false;
    /**zmienia wartosc gdy uzytkownik chce wybrac uzytkownika do przeslania mu pliku*/
    protected boolean ChooseClient = false;
    /**nazwa klienta do ktorego chcemy wyslac plik*/
    protected String SendingUser;
    /**zmienia wartosc gdy uzytkownik chce wybrac plik do wyuslania*/
    protected boolean ChooseFile = false;
    /**sciezka pliku ktory bedzie wysylany*/
    protected String SendingFile;

    /**
     * konstruktor tworzacy okno graficzne i inicjalizujacy zmienne
     * @param name nazwa uzytkownika
     * @param sauce sciezka do folderu
     */
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

    /**
     * funkcja wysylajaca imie klienta do serwera i oczekujaca jego akceptacji
     * @throws NameAlreadyTakenException wyjatek rzucany gdy imie jest juz zajete
     */
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

    /**
     * glowma funkcja odpowiedzialna za obsluge klienta i komunikacje z serwerem
     */
    protected void run()
    {
        /**tworzenie puli watkow i weryfikacja imienia*/
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

            /**zmienna do zapisu komunikatow z serwera*/
            String ServerMessage;
            /**lista klientow polaczonych z serwerem*/
            ArrayList<String> clients = new ArrayList<>();
            /**nazwa pliku (do wyslania/odebrania)*/
            String filename;
            /**zmienna przechowujaca liczbe plikow ktore beda przeslane z serwera*/
            int HowManyFiles;
            /**lista plikow w folderze*/
            ArrayList<String> ClientFiles = folder.GetNames();

            /**pobieranie liczby plikow z serwera. Jesli klient posiada taki plik to wysyla komunikat "No"
             * jesli go nie posiada odsyla "Yes" i pobiera pliki
             */
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

            /**
             * glowna petla do komunikacji z serwerem
             */
            while (true)
            {
                /**sprawdzanie czy w folderze klienta jest nowy plik*/
                folder.NewFile();

                /**jesli pojawil sie nowy plik i nie ma go na serwerze to jest on wysylany*/
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
                /**jezeli okno graficzne zostalo zamnkiete program konczy sie*/
                else if (graphics.end)
                {
                    while (mut.tryAcquire());
                    dos.writeUTF("End");
                    break;
                }
                /**jesli klient zazada listy, do serwera jest odpowiedni komunikat.
                 * nastepnie serwer przesyla liste klientow (o ile jest ich wiecej niz 1) i dla niej tworzone jest nowe okno graficzne
                 * gdy uzytkownik wybierze uzytkownika do ktorego chce wyslac plik inicjowane jest kolejne okno graficzne
                 * w nowym oknie graficznym uzytkownik musi dokonac wyboru pliku ktory chce wyslac
                 * po wyborze plik jest wysylany do innego klienta
                 * wysylka pliku jest poprzedzona odpowiednim komunikatem dla serwera
                 */
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
                /**jezeli nic sie nie wydarzylo, klient wysyla zapytanie o nowe pliki dla niego*/
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
            /**program konczy sie poprzez wylaczenie puli watkow oraz usuniecie okna*/
            graphics.jFrame.setVisible(false);
            graphics.jFrame.dispose();
            pool.shutdown();
        }
    }

    /**funkcja main sprawdza czy klient ma odpowiednia ilosc argumentow oraz uruchamia funkcje run
     *
     * @param args pierwszy to nazwa klienta, a drugi to sciezka do jego folderu
     */
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
