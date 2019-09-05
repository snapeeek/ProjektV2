import projekt.*;
import projekt.Graphics;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import static java.lang.Thread.sleep;

public class Server
{
    private static TreeMap<String, Semaphore> usernames = new TreeMap<>();
    private static ArrayList<Semaphore> content = new ArrayList<>();
    protected static TreeMap<String, DataOutputStream>  clients = new TreeMap<>();
    protected static ArrayList<FolderLocale> folders = new ArrayList<>();
    private static TreeMap<String, ArrayList<Task>> tasks = new TreeMap<>();
    private static ExecutorService pool;
    protected static Graphics graphics;
    private static ArrayList<JTextArea> lists = new ArrayList<>();
    protected static Control control;



    public static void main(String[] args) throws Exception
    {
        System.out.println("Server is currently running");
        pool = Executors.newFixedThreadPool(50);
        for (Integer i = 1; i < 6; i++)
        {
            new File("C:\\Users\\mwozn\\Desktop\\FolServ\\Server" + i.toString()).mkdir();
            new File("C:\\Users\\mwozn\\Desktop\\FolServ\\Server" + i.toString() + "\\content.csv").createNewFile();
            folders.add(new FolderLocale("C:\\Users\\mwozn\\Desktop\\FolServ\\Server" + i.toString()));
            content.add(new Semaphore(1));
        }
        sleep(50);
        control = new Control(pool);
        pool.execute(control);
        graphics = new Graphics("Server");
        for (int i = 0; i < 5; i++)
        {
            JTextArea jTextArea = new JTextArea(20,10);
            jTextArea.setEditable(false);
            lists.add(jTextArea);
            GridBagConstraints grid = new GridBagConstraints();
            grid.gridx = i;
            grid.gridy = 0;
            graphics.jPanel.add(jTextArea, grid);
        }
        graphics.jLabel.setText("Czekam");
        graphics.jFrame.pack();
        graphics.jFrame.setVisible(true);
        for (Integer i = 0; i < 5; i++)
        {
            Integer j = i + 1;
            lists.get(i).append("Folder" + j.toString() + "\n");
            ArrayList<String> k = folders.get(i).GetNames();
            for (String l : k)
            {
                if (l.compareTo("content.csv") != 0)
                {
                    lists.get(i).append(l + "\n");
                }
            }
        }
        try (var listener = new ServerSocket(50005))
        {
            while (true)
            {
                pool.execute(new Handler(listener.accept()));
            }
        }
    }

    protected static void TheEnd(){
        if(graphics.end){
            if(clients.size()==0){
                graphics.jFrame.setVisible(false);
                graphics.jFrame.dispose();
                pool.shutdown();
                System.exit(0);
            }
        }
        else{
            graphics.end=false;
        }
    }

    protected static class Control implements Runnable
    {
        private ExecutorService pool;
        protected ArrayList<Task> queue;

        private Control(ExecutorService executorService)
        {
            pool = executorService;
            queue = new ArrayList<>();
        }

        private int HowManyClients()
        {
            int ret = 0;
            for (Task t : queue)
            {
                if (clients.containsKey(t.user))
                {
                    ret++;
                }
            }
            return ret;
        }

        private Integer WhichFolder()
        {
            for (int i = 0; i < 5; i++)
            {
                folders.get(i).NewFile();
            }
            int size = folders.get(0).FilesList.size();
            int temp;
            Integer ret = 1;
            for (int i = 1; i < 5; i++)
            {
                temp = folders.get(i).FilesList.size();
                if (size > temp)
                {
                    size = temp;
                    ret = i + 1;
                }
            }
            return ret;
        }

        @Override
        public void run()
        {
            int size;
            int counter = 0;
            String lastServed = "";
            int folder;
            Task chosenTask;

            while (true)
            {
                synchronized (queue)
                {
                    size = queue.size();
                    if (size > 0)
                    {
                        folder = WhichFolder();
                        if (HowManyClients() > 1)
                        {
                            while (lastServed.compareTo(queue.get(counter).user) == 0)
                            {
                                counter++;
                            }
                            lastServed = queue.get(counter).user;
                            chosenTask = queue.remove(counter);
                            counter = 0;
                        }
                        else
                        {
                            lastServed = queue.get(0).user;
                            chosenTask = queue.remove(0);
                        }
                        pool.execute(new Saving(chosenTask.content, chosenTask.filename, folder, content.get(folder - 1), chosenTask.user, "C:\\Users\\mwozn\\Desktop\\FolServ\\Server"));
                        lists.get(folder - 1).append(chosenTask.filename + "\n");
                    }
                }
            }
        }
    }

    protected static class Handler implements Runnable
    {
        private String username;
        private ArrayList<Task> filesFromClients = new ArrayList<>();
        private Socket socket;
        final Semaphore mut;
        DataOutputStream dos;
        DataInputStream dis;

        protected Handler(Socket sock)
        {
            this.socket = sock;
            mut = new Semaphore(1);
        }

        public void run()
        {
            try
            {
                dos = new DataOutputStream(socket.getOutputStream());
                dis = new DataInputStream(socket.getInputStream());
                username = dis.readUTF();
                graphics.jLabel.setText("Przyjmuje imie");
                synchronized (usernames)
                {
                    if (!username.isBlank() && !usernames.containsKey(username))
                    {
                        usernames.put(username, mut);
                        dos.writeUTF("Name accepted");
                    }
                    else
                    {
                        dos.writeUTF("Name is already taken");
                        return;
                    }
                }

                String mess;
                String toWhom;
                String filename;
                File temp;
                clients.put(username, dos);
                tasks.put(username, filesFromClients);
                System.out.println(clients);
                mut.acquire();
                ArrayList<File> startingFiles = SendingAtStart(username);
                dos.writeInt(startingFiles.size());

                for (File file:startingFiles)
                {
                    mut.release();
                    dos.writeUTF(file.getName());
                    mess = dis.readUTF();
                    if (mess.compareTo("Yes") == 0)
                    {
                        pool.execute(new Sending(file, mut, dos));
                        graphics.jLabel.setText("Wysylam");
                        sleep(100);
                    }
                    while (mut.tryAcquire());
                }

                byte buff[];
                mut.release();

                while (true)
                {
                    mess = dis.readUTF();
                    if (mess.compareTo("New file") == 0)
                    {
                        while (mut.tryAcquire());
                        graphics.jLabel.setText("Odbieram");
                        filename = dis.readUTF();
                        if (FindFile(filename, username).getName().compareTo("content.csv") == 0)
                        {
                            dos.writeUTF("Send");
                            buff = receive(dis);
                            control.queue.add(new Task(filename, buff, username));
                        }
                        else
                        {
                            dos.writeUTF("I have this one");
                        }
                        mut.release();
                    }
                    else if (mess.compareTo("File to client") == 0)
                    {
                        while (mut.tryAcquire());
                        graphics.jLabel.setText("Wysylam");
                        filename = dis.readUTF();
                        toWhom = dis.readUTF();
                        temp = FindFile(filename, username);
                        if (temp.getName().compareTo("content.csv") != 0)
                        {
                            synchronized (tasks)
                            {
                                tasks.get(toWhom).add(new Task(filename, temp));
                            }
                        }
                        mut.release();
                    }
                    else if (mess.compareTo("I need a list of clients") == 0)
                    {
                        while (mut.tryAcquire());
                        dos.writeInt(usernames.size() - 1);
                        if (usernames.size() > 1)
                        {
                            for (String c : usernames.keySet())
                            {
                                if (c.compareTo(username) != 0)
                                {
                                    dos.writeUTF(c);
                                }
                            }
                        }
                        mut.release();
                    }
                    else if (mess.compareTo("End") == 0)
                    {
                        while (mut.tryAcquire());
                        clients.remove(username);
                        usernames.remove(username);
                        System.out.println(username + " opuszcza server");
                        break;
                    }
                    else if (mess.compareTo("Something for me") == 0)
                    {
                        while (mut.tryAcquire());
                        synchronized (filesFromClients)
                        {
                            if (filesFromClients.size() != 0)
                            {
                                dos.writeUTF("Yes");
                                Task task = filesFromClients.remove(0);
                                dos.writeUTF(task.filename);
                                mess=dis.readUTF();
                                if (mess.compareTo("Send") == 0)
                                {
                                    mut.release();
                                    pool.execute(new Sending(task.file, mut, dos));
                                    graphics.jLabel.setText("Wysylam");
                                    Thread.sleep(500);
                                    while(mut.tryAcquire());
                                }
                            }
                            else
                            {
                                dos.writeUTF("No");
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
                try
                {
                    socket.close();
                    TheEnd();
                }
                catch (IOException IOExp)
                {
                    IOExp.printStackTrace();
                }
            }

        }

        protected ArrayList<File> SendingAtStart(String owner)
        {
            Scanner scanner;
            String s;
            String[] b;
            File temp;
            boolean isRunning;
            ArrayList<File> ret = new ArrayList<>();

            try
            {
                for (Integer i = 1; i < 6; i++)
                {
                    File content = new File("C:\\Users\\mwozn\\Desktop\\FolServ\\Server" + i.toString() + "\\content.csv");
                    isRunning = true;
                    while (isRunning)
                    {
                        if (content.canRead())
                        {
                            scanner = new Scanner(content);
                            while (scanner.hasNextLine())
                            {
                                s = scanner.nextLine();
                                b = s.split(",");
                                if (b[1].compareTo(owner) == 0)
                                {
                                    temp = new File("C:\\Users\\mwozn\\Desktop\\FolServ\\Server" + i.toString() + "\\" + b[0]);
                                    ret.add(temp);
                                }
                            }
                            isRunning = false;
                        }
                        else
                        {
                            try
                            {
                                sleep(500);
                            }
                            catch (InterruptedException IntExp)
                            {
                                isRunning = false;
                            }
                        }
                    }
                }
            }
            catch (FileNotFoundException FNFExp)
            {
                FNFExp.printStackTrace();
            }
            return ret;
        }

        protected File FindFile(String name, String owner)
        {
            File temp;
            Scanner scanner;
            String info;
            for (Integer i = 1; i < 6; i++)
            {
                File content = new File("C:\\Users\\mwozn\\Desktop\\FolServ\\Server" + i.toString() + "\\content.csv");
                try
                {
                    scanner = new Scanner(content);
                    while (scanner.hasNextLine())
                    {
                        info = scanner.nextLine();
                        if (info.compareTo(name + "," + owner) == 0)
                        {
                            temp = new File("C:\\Users\\mwozn\\Desktop\\FolServ\\Server" + i.toString() + "\\" + name);
                            return temp;
                        }
                    }
                }
                catch (FileNotFoundException FNFExp)
                {
                    FNFExp.printStackTrace();
                }
            }
            return new File("C:\\Users\\mwozn\\Desktop\\FolServ\\Server1\\content.csv");
        }

        protected byte[] receive(DataInputStream in)
        {
            byte[] b = new byte[0];
            try
            {
                Long size = in.readLong();
                b = new byte[size.intValue()];
                in.readFully(b);
            }
            catch (IOException IOExp)
            {
                IOExp.printStackTrace();
            }
            return b;
        }

    }
}
