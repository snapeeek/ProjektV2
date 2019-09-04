import projekt.FolderLocale;

import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.concurrent.Semaphore;

public class Server
{
    private static TreeMap<String, Semaphore> usernames = new TreeMap<>;
    private static ArrayList<Semaphore> content = new ArrayList<>();
    protected static TreeMap<String, DataOutputStream>  clients = new TreeMap<>();
    protected static ArrayList<FolderLocale> folders = new ArrayList<>();


}
