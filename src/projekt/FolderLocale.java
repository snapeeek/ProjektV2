package projekt;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

/** klasa odpowiedzialna za obsluge folderu */
public class FolderLocale
{
    /**lista plikow w folderze*/
    public ArrayList<File> FilesList;
    /**lista plikow, ktore nalezy wyslac*/
    public ArrayList<File> ToSend;
    /**sciezka folderu */
    String path;
    /**folder*/
    File file;

    /**konstruktor inicjujacy zmienne
     *
     * @param p sciezka do folderu
     */
    public FolderLocale(String p)
    {
        FilesList = new ArrayList<>();
        ToSend = new ArrayList<>();
        path = p;
        file = new File(path);
    }

    /**funkcja sprawdzajaca czy pojawil sie nowy plik w folderze - jesli tak to dodaje go do listy ToSend oraz do listy FilesList
     */
    public void NewFile()
    {
        ArrayList<File> temp = new ArrayList<File>(Arrays.asList(file.listFiles()));
        FolderLocale help;
        File HelpFile;

        for (File f:temp)
        {
            if (f.isDirectory())
            {
                help = new FolderLocale(f.getAbsolutePath());
                help.NewFile();
                for (int i = 0; i < help.ToSend.size(); i++)
                {
                    HelpFile = help.ToSend.get(i);
                    ToSend.add(HelpFile);
                    FilesList.add(HelpFile);
                }
            }
            else if (FilesList.contains(f) == false)
            {
                ToSend.add(f);
                FilesList.add(f);
            }
        }
    }

    /** funkcja zwracajaca nazwy plikow w folderze */
    public ArrayList<String> GetNames()
    {
        ArrayList<String> ret = new ArrayList<>(Arrays.asList(file.list()));
        ArrayList<String> temp;
        FolderLocale help;
        int size = ret.size();

        for (int i = 0; i < size; i++)
        {
            if (!ret.get(i).contains("."))
            {
                help = new FolderLocale(path+"\\"+ret.get(i));
                temp = help.GetNames();
                ret.addAll(temp);
                ret.remove(i);
            }
        }
        return ret;
    }

}
