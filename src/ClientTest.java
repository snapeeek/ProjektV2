import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import projekt.FolderLocale;
import projekt.NameAlreadyTakenException;
import projekt.Saving;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

import static org.junit.jupiter.api.Assertions.*;

class ClientTest
{

    @Test
    void ClientConstructorTest()
    {
        System.out.println("Constructor test");
        try
        {
            Server server = new Server();
            String[] args = new String[0];
            try {
                new Thread(() -> {
                    try {
                        server.main(args);
                    } catch (Exception Exp) {
                    }
                }).start();
                Thread.sleep(500);
                server.graphics.jFrame.setVisible(false);
            } catch (InterruptedException IntExp) {
            } catch (Exception Exp) {
            }
            Thread.sleep(100);
            Client client = new Client("Test", "C:\\Users\\mwozn\\Desktop\\FolProj\\Test");
            Thread.sleep(500);
            assertEquals("Test", client.username);
            server.TheEnd();
        }
        catch (InterruptedException IntExp) {}
        catch (Exception Exp) {}
    }

    @Test
    void nameVerificationGood()
    {
        System.out.println("Good Name Verification Test");
        Server server = new Server();
        String [] args = new String[0];
        try
        {
            new Thread(() -> {
                try
                {
                    server.main(args);
                }
                catch (Exception Exp)
                {

                }
            }).start();
            Thread.sleep(100);
            server.graphics.jFrame.setVisible(false);
        }
        catch (InterruptedException IntExp)
        {

        }
        catch (Exception Exp)
        {

        }
        Client client = new Client("Test", "C:\\Users\\mwozn\\Desktop\\FolProj\\Test");
        try
        {
            client.NameVerification();
        }
        catch(NameAlreadyTakenException NATExp)
        {

        }
        server.TheEnd();
    }

    @Test
    void nameVerificationBad()
    {
        System.out.println("Bad name verification test");
        Server server=new Server();
        String [] args=new String[0];
        try {
            new Thread(() -> {
                try
                {
                    server.main(args);
                }
                catch (Exception e) { }
            }).start();
            Thread.sleep(50);
            server.graphics.jFrame.setVisible(false);
        }catch (InterruptedException ie){ }
        catch (Exception e){
        }
        Client client = new Client("Test", "C:\\Users\\mwozn\\Desktop\\FolProj\\Test");
        try
        {
            client.NameVerification();
        }
        catch (NameAlreadyTakenException NATExp){}
        Client client1 = new Client("Test", "C:\\Users\\mwozn\\Desktop\\FolProj\\Test");
        Assertions.assertThrows(NameAlreadyTakenException.class,()->
        {
            client1.NameVerification();
            client.graphics.end = true;
            client1.graphics.end = true;
            server.TheEnd();
        });
    }

    @Test
    void SavingTest()
    {
        new Saving("to jest test zapisu".getBytes(), "testzapisu.txt", null, new Semaphore(1), "test", "C:\\Users\\mwozn\\Desktop\\FolProj\\Test");
        try
        {
            Thread.sleep(500);
        }
        catch (InterruptedException IntExp) {}
        File f = new File("C:\\Users\\mwozn\\Desktop\\FolProj\\Test\\testzapisu.txt");
        try
        {
            Scanner scan = new Scanner(f);
            assertEquals("to jest test zapisu", scan.nextLine());
            assertEquals("testzapisu.txt", f.getName());
            scan.close();
        }
        catch (FileNotFoundException FNFExp) {}
    }

    @Test
    void FolderLocaleNewFile()
    {
        FolderLocale test = new FolderLocale("C:\\Users\\mwozn\\Desktop\\FolProj\\Test");
        test.NewFile();
        assertTrue(test.FilesList.size() > 0);
        assertTrue(test.ToSend.size() > 0);
    }

    @Test
    void FolderLocaleGetNames()
    {
        FolderLocale test = new FolderLocale("C:\\Users\\mwozn\\Desktop\\FolProj\\Test");
        //System.out.println(test.GetNames());
        assertTrue(test.GetNames().contains("test.txt"));
    }
}