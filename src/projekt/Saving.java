package projekt;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Random;
import java.util.concurrent.Semaphore;

public class Saving implements Runnable
{
    public byte[] filecontent;
    public String filename;
    Integer folder;
    Semaphore sem;
    String username;
    String sauce;

    public Saving(byte[] fc, String fn, Integer f, Semaphore s, String un, String p)
    {
        filecontent = fc;
        filename = fn;
        folder = f;
        sem = s;
        username = un;
        sauce = p;
    }


    @Override
    public void run()
    {
        try
        {
            Thread.sleep(FewSeconds());
            FileOutputStream fos;

            if (folder == null)
            {
                fos = new FileOutputStream(sauce + "\\" + filename);
            }
            else
            {
                fos = new FileOutputStream(sauce +  folder.toString() + "\\" + filename);
            }

            BufferedOutputStream bos = new BufferedOutputStream(fos);
            bos.write(filecontent, 0, filecontent.length);
            bos.close();
            fos.close();

            if (folder != null)
                UpdateCSV();
        }
        catch (InterruptedException IntExp)
        {
            IntExp.printStackTrace();
        }
        catch (IOException IOExp)
        {
            IOExp.printStackTrace();
        }
    }

    private int FewSeconds()
    {
        Random x = new Random();
        int ret = x.nextInt(15);
        return ret;
    }

    public void UpdateCSV()
    {
        while (sem.tryAcquire());
        try
        {
            Files.write(Paths.get("C:\\Users\\mwozn\\Desktop\\FolProj\\Server" + folder.toString() + "\\content.csv"), (filename + "," + username + "\n").getBytes(), StandardOpenOption.APPEND);
        }
        catch (IOException IOExp)
        {
            IOExp.printStackTrace();
        }

        sem.release();
    }
}
