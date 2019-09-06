package projekt;

import java.io.*;
import java.util.concurrent.Semaphore;

/**
 * klasa umozliwajaca wysylanie pliku przez strumien wyjscia
 */
public class Sending implements Runnable
{
    /**plik*/
    private File file;
    /**semafor umozliwiajacy dostep do strumienia*/
    private Semaphore sem;
    /**strumien wyjscia*/
    private DataOutputStream output;

    /**
     * @param f plik
     * @param s semafor
     * @param dos strumien wyjsciowy
     */
    public Sending(File f, Semaphore s, DataOutputStream dos)
    {
        file = f;
        sem = s;
        output = dos;
    }

    /**
     * glowna funkcja wykonwcza
     * najpierw pobiera wielkosc pliku, nastepnie tworzy bufor o takiej samej wielkosci
     * uzyskuje dostep do semafora, wczytuje plik do buforowanego strumienia
     * przesyla plik poprzez strumien
     */
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