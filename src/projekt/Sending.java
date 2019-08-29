package projekt;

import java.io.*;
import java.util.concurrent.Semaphore;

public class Sending implements Runnable
{
    private File file;

    private Semaphore sem;

    private DataOutputStream output;

    public Sending(File f, Semaphore s, DataOutputStream dos)
    {
        file = f;
        sem = s;
        output = dos;
    }

    @Override
    public void run()
    {
        Long size=file.length();
        byte[] buf = new byte[size.intValue()];
        try
        {
            sem.acquire();
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            bis.read(buf, 0, size.intValue());
            output.writeLong(size);
            output.write(buf,0,size.intValue());
            output.flush();
            bis.close();
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