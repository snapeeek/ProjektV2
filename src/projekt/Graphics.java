package projekt;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class Graphics
{
    public JFrame jFrame;

    public JPanel jPanel;

    public JLabel jLabel;

    private String WindowName;

    public boolean end;

    public Graphics(String name)
    {
        WindowName = name;
        jFrame = new JFrame(WindowName);
        jFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        jFrame.addWindowFocusListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (JOptionPane.showConfirmDialog(jFrame, "Are you sure you want to close?", "Closing",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
                    end = true;
            }
        });
        jLabel = new JLabel();
        jFrame.getContentPane().add(jLabel, BorderLayout.NORTH);
        jPanel = new JPanel(new GridBagLayout());
        Border border = BorderFactory.createEmptyBorder(10,10,10,10);
        jPanel.setBorder(border);
        jFrame.add(jPanel, BorderLayout.CENTER);
        jFrame.setSize(300,300);
    }
}
