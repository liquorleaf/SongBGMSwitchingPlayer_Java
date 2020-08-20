import javax.media.Manager;
import javax.media.NoPlayerException;
import javax.media.Player;
import javax.media.Time;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

class SongBGMSwitcher implements ActionListener
{
    public SongBGMSwitcher(File SongFile, File BGMFile)
    {
        try
        {
            SongPlayer = Manager.createPlayer(SongFile.toURI().toURL());
        }
        catch (IOException | NoPlayerException | NullPointerException e)
        {
            e.printStackTrace();
        }
        try
        {
            BGMPlayer = Manager.createPlayer(BGMFile.toURI().toURL());
        }
        catch (IOException | NoPlayerException | NullPointerException e)
        {
            e.printStackTrace();
        }
    }

    public void startFromSong()
    {
        BGMPlayer.realize();
        SongPlayer.realize();
        SongOrBGMNow = true;
        SongPlayer.start();
    }

    public void startFromBGM()
    {
        SongPlayer.realize();
        BGMPlayer.realize();
        SongOrBGMNow = false;
        BGMPlayer.start();
    }

    public void actionPerformed(ActionEvent e)
    {
        long switchingDelay = 0; // fix the gap caused by switching (严格来说应该做成private可以在构造时设置的)

        /* 可以用同步播放、控制音量的方法实现无缝吗？？？？？
         * 预先缓存可以解决吗？
         * 进一步应用中也许可以：给“触发切换的事件”一个特殊音效来掩盖gap？
         */

        //BGMDuration is supposed to be equal to it
        long songDuration;

        if (this.SongOrBGMNow) // switch from Song to BGM
        {
            this.SongPlayer.stop();
            Time BGMTime = this.SongPlayer.getMediaTime();
            Time SongTime = new Time(BGMTime.getNanoseconds() + switchingDelay);
            songDuration = this.SongPlayer.getDuration().getNanoseconds();
            if (this.SongPlayer.getMediaTime().getNanoseconds() >= songDuration)
                this.BGMPlayer.setMediaTime(new Time(0));
            else
                this.BGMPlayer.setMediaTime(SongTime);
            this.BGMPlayer.start();
            this.SongOrBGMNow = false;
        }
        else // switch from BGM to Song
        {
            this.BGMPlayer.stop();
            Time SongTime = this.BGMPlayer.getMediaTime();
            Time BGMTime = new Time(SongTime.getNanoseconds() + switchingDelay);
            songDuration = this.SongPlayer.getDuration().getNanoseconds();
            if (this.BGMPlayer.getMediaTime().getNanoseconds() >= songDuration)
                this.SongPlayer.setMediaTime(new Time(0));
            else
                this.SongPlayer.setMediaTime(BGMTime);
            this.SongPlayer.start();
            this.SongOrBGMNow = true;
        }
    }

    public void pause()
    {
        if(this.SongOrBGMNow)
        {
            this.SongPlayer.stop();
        }
        else
        {
            this.BGMPlayer.stop();
        }
    }
    public void play()
    {
        if(this.SongOrBGMNow)
        {
            this.SongPlayer.start();
        }
        else
        {
            this.BGMPlayer.start();
        }
    }

    public boolean isSongOrBGMNow()
    {
        return this.SongOrBGMNow;
    }
    public Time getSongTime()
    {
        return this.SongPlayer.getMediaTime();
    }
    public Time getBGMTime()
    {
        return this.BGMPlayer.getMediaTime();
    }

    private boolean SongOrBGMNow; // Song:true BGM:false

    private Player BGMPlayer;
    private Player SongPlayer;
}