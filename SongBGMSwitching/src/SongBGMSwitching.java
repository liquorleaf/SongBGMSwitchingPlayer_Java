import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;

import javax.media.*;
import javax.media.Player;

public class SongBGMSwitching
{
    public static void main(String[] args) throws NoPlayerException, MalformedURLException, IOException
    {
        SwitchingPlayerFrame switchingPlayer = new SwitchingPlayerFrame();
        switchingPlayer.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        switchingPlayer.setVisible(true);

        ActionListener listener1 = new TimePrinter1();

        //Timer t1 = new Timer(1000, listener1);
        //t1.start();
    }
}

class TimePrinter1 implements ActionListener
{
    static Date startDate = new Date();

    public void actionPerformed(ActionEvent event)
    {
        Date now = new Date();
        long runTime = now.getTime() - startDate.getTime();
        System.out.println("The program has run for " + runTime + "ms.");
    }
}

