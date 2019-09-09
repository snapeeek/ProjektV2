import org.junit.jupiter.api.Test;
import projekt.Task;

import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;

class ServerTest
{

    @Test
    void ConstructorTest()
    {
        Server test = new Server();
        new Thread(() -> {
            try
            {
                test.main(new String[0]);
            }
            catch (Exception Exp) {}
        }).start();
        try
        {
            Thread.sleep(500);
        }
        catch (InterruptedException IntExp) {}
        assertEquals(5, test.folders.size());
        test.TheEnd();
    }

    @Test
    void TaskTest()
    {
        byte[] test = "to jest test".getBytes();
        Task TaskTest = new Task("test", test, "Test");
        assertEquals(TaskTest.user, "Test");
    }

    @Test
    void SendingAtStartTest()
    {
        Server.Handler handler = new Server.Handler(new Socket());
        assertTrue(handler.SendingAtStart("Test2").size() > 0);
    }

    @Test
    void FindFileTest()
    {
        Server.Handler handler = new Server.Handler(new Socket());
        assertEquals(handler.FindFile("test2.txt", "Test2").getName(), "test2.txt");
    }
}