package projekt;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Random;
import java.util.concurrent.Semaphore;

/**
 * klasa zapisujaca zawartosc do pliku
 */
public class Saving implements Runnable
{
    /**zawartosc pliku*/
    public byte[] filecontent;
    /**nazwa pliku*/
    public String filename;
    /**numer folderu do ktorego plik bedzie zapisany*/
    Integer folder;
    /** semafor dzieki ktoremu mozna zapisac zawartosc*/
    Semaphore sem;
    /** wlasciciel pliku*/
    String username;
    /**sciezka zapisu*/
    String sauce;

    /**
     * @param fc zawartosc pliku
     * @param fn nazwa pliku
     * @param f numer folderu
     * @param s semafor
     * @param un wlasciciel pliku
     * @param p sciezka zapisu
     */
    public Saving(byte[] fc, String fn, Integer f, Semaphore s, String un, String p)
    {
        filecontent = fc;
        filename = fn;
        folder = f;
        sem = s;
        username = un;
        sauce = p;
    }

    /**
     * glowna funkcja zapisujaca
     * zeby zwizualizowac symulacje na niewielkiej liczbie uzytkownikow watek jest usypiany na losowa liczbe sekund
     * nastepnie zapisuje plik w podanej lokalizacji oraz uaktualnia plik csv
     */
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

    /**
     * funkcja losujaca ilosc sekund ktore bedzie czekac watek
     * @return ilosc sekund
     */
    private int FewSeconds()
    {
        Random x = new Random();
        int ret = x.nextInt(15);
        return ret;
    }

    /**
     *funkcja uakualniajaca plik csv
     */
    public void UpdateCSV()
    {
        while (sem.tryAcquire());
        try
        {
            Files.write(Paths.get("C:\\Users\\mwozn\\Desktop\\FolServ\\Server" + folder.toString() + "\\content.csv"), (filename + "," + username + "\n").getBytes(), StandardOpenOption.APPEND);
        }
        catch (IOException IOExp)
        {
            IOExp.printStackTrace();
        }

        sem.release();
    }
}
