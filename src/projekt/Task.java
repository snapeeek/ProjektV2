package projekt;

import java.io.File;

public class Task
{
    public String filename;
    public byte[] content;
    public String user;
    public File file;


    public Task(String fn, byte[] fc, String us)
    {
        filename = fn;
        content = fc;
        user = us;
    }

    public Task (String fn, File f)
    {
        filename = fn;
        file = f;
    }
}
