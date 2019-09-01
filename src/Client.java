import projekt.*;

import javax.naming.Name;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
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
        }







    }
}
