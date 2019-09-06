package projekt;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.Semaphore;

/**
 * klasa odpowiedzialna za odbir plikow
 */
public class Receiving implements Runnable
{
    /**sciezka do pliku*/
    private String sauce;
    /**semafor ktory kontroluje strumien*/
    private Semaphore sem;
    /**strumien wejsciowy*/
    private DataInputStream input;

    /**
     * konstruktor przypisujacy zmienne
     * @param pth sciezka
     * @param s semafor
     * @param dis strumien
     */
    public Receiving(String pth, Semaphore s, DataInputStream dis)
    {
        sauce = pth;
        sem = s;
        input = dis;
    }

    /**
     * glowna funkcja. Najpierw uzyskuje dostep do semafora, nastepnie pobiera rozmiar pliki
     * tworzy tablice o takim rozmiarze, pobiera plik i zapisuje go w danej lokalizacji
     */
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