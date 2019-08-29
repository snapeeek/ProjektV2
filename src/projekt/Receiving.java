package projekt;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.Semaphore;

public class Receiving implements Runnable
{
    private String sauce;

    private Semaphore sem;

    private DataInputStream input;

    public Receiving(String pth, Semaphore s, DataInputStream dis)
    {
        sauce = pth;
        sem = s;
        input = dis;
    }

    @Override
    public void run()
    {
        try
        {
            sem.acquire();
            Long size=input.readLong();
            byte[] bytes = new byte[size.intValue()];
            FileOutputStream fos = new FileOutputStream(sauce);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            input.readFully(bytes);
            bos.write(bytes,0,size.intValue());
            bos.close();
            fos.close();
        }
        catch (InterruptedException IntExp)
        {
            IntExp.printStackTrace();
        }
        catch (IOException IOExp)
        {
            IOExp.printStackTrace();
        }
        finally
        {
            sem.release();
        }
    }
}
