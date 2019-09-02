import projekt.*;

import javax.naming.Name;
import java.io.DataInputStream;
import java.io.DataOutputStream;
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

        //miejsce na grafike????
    }

    protected void NameVerification() throws NameAlreadyTakenException
    {
        try
        {
            dos.writeUTF(username);
            String ret = dis.readUTF();
            System.out.println(ret);

            if (ret.compareTo("chuj") != 0)
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

                }
            }

        }
        catch (Exception Exp)
        {
            Exp.printStackTrace();
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

        client.run();
    }
}
