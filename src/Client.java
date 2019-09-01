import projekt.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
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

    }
}
