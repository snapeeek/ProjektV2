package projekt;

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
        }
        catch (InterruptedException IntExp)
        {
            IntExp.printStackTrace();
        }
    }





    private int FewSeconds()
    {
        Random x = new Random();
        int ret = x.nextInt(15);
        return ret;
    }

}
