package projekt;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/** klasa odpowiedzalna za oprawe graficzna*/
public class Graphics
{
    /**glowna rama okna*/
    public JFrame jFrame;
    /**panel do ktorego mozna dodawac elementy*/
    public JPanel jPanel;
    /**etykieta wyswietlajaca komunikaty*/
    public JLabel jLabel;
    /**String przechowujacy nazwe okna*/
    private String WindowName;
    /**zmienna zero-jedynkowa odpowiadajaca za zamykanie okna*/
    public boolean end;

    /**
     * konstruktor tworzacy okno o podanym tytule oraz ustalajacy etykiete
     * w konstruktorze zawarte jest takze postepowanie przy zamykaniu okna
     * @param name tytul okna
     */
    public Graphics(String name)
    {
        WindowName = name;
        jFrame = new JFrame(WindowName);
        jFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        jFrame.addWindowListener(new java.awt.event.WindowAdapter()
        {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent)
            {
                if (JOptionPane.showConfirmDialog(jFrame, "Are you sure you want to close?", "Closing",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
                {
                    end = true;
                }
            }
        });
        jLabel = new JLabel();
        jFrame.getContentPane().add(jLabel, BorderLayout.NORTH);
        jPanel = new JPanel(new GridBagLayout());
        Border border = BorderFactory.createEmptyBorder(20,20,20,20);
        jPanel.setBorder(border);
        jFrame.add(jPanel, BorderLayout.CENTER);
        jFrame.setSize(300,300);
    }
}
