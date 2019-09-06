package projekt;

import java.io.File;

/**
 * klasa zadania przechowywujaca dane na jego temat
 */
public class Task
{
    /**nazwa pliku*/
    public String filename;
    /**zawartos pliku*/
    public byte[] content;
    /**zadaniodawca*/
    public String user;
    /**plik*/
    public File file;

    /**
     * @param fn nazwa pliku
     * @param fc zawartosc pliku
     * @param us zleceniodawca
     */
    public Task(String fn, byte[] fc, String us)
    {
        filename = fn;
        content = fc;
        user = us;
    }

    /**
     * @param fn nazwa pliku
     * @param f plik
     */
    public Task (String fn, File f)
    {
        filename = fn;
        file = f;
    }
}
