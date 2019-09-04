import projekt.*;
import projekt.Graphics;

import javax.swing.*;
import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class Server
{
    private static TreeMap<String, Semaphore> usernames = new TreeMap<>;
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
        control = new Control(pool);
        pool.execute(control);
        graphics = new Graphics("Server");
        for (int i = 0; i < 5; i++)
        {
            JTextArea jTextArea = new JTextArea(20,10);
            jTextArea.setEditable(false);
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

        }
    }
}
