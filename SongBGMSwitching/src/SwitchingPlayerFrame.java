import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import javax.media.*;
import javax.media.Player;
import javax.sound.sampled.*;
import javax.swing.filechooser.FileFilter;

// 窗口框架
class SwitchingPlayerFrame extends JFrame
{
    public SwitchingPlayerFrame()
    {
        int DEFAULT_WIDTH = 1024;
        int DEFAULT_HEIGHT = 768;
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);

        // 标题栏
        final String title = "歌曲和BGM交替播放器";
        setTitle(title);
        // 设置观感为 Windows Classic
        try
        {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel");
        }
        catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e)
        {
            e.printStackTrace();
        }

        JPanel switchingPlayerPanel = new SwitchingPlayerPanel();
        add(switchingPlayerPanel);
        pack();
    }
}

// 面板
class SwitchingPlayerPanel extends JPanel
{
    private File SongFile;
    private File BGMFile;
    private SongBGMSwitcher songBGMSwitcher;

    private SwitchButton switchButton = null;
    private SongOrBGMNowMarker SongMarker;
    private SongOrBGMNowMarker BGMMarker;
    private PlayingTimeMarkerLabel playingTimeMarkerLabel;
    private PauseButton pauseButton;

    public SwitchingPlayerPanel()
    {
        setLayout(new BorderLayout());
        //播放控制面板：下侧
        JPanel playingControlPanel = new JPanel();
        add(playingControlPanel, BorderLayout.SOUTH);
        playingControlPanel.setLayout(new GridLayout(1, 4));
        //播放列表面板：上侧
        JPanel playingListPanel = new JPanel();
        add(playingListPanel, BorderLayout.NORTH);
        playingListPanel.setLayout(new GridLayout(2, 1));
        //播放状态面板：中间
        JPanel playingStatePanel = new JPanel();
        add(playingStatePanel, BorderLayout.CENTER);
        playingStatePanel.setLayout(new GridLayout(2, 1));
        //播放状态面板显示当前是歌曲还是BGM的子面板
        JPanel SongOrBGMPanel = new JPanel();
        playingStatePanel.add(SongOrBGMPanel);
        SongOrBGMPanel.setLayout(new GridLayout(1, 2));
        //播放状态面板显示当前播放进度的子面板
        JPanel playingTimePanel = new JPanel();
        playingStatePanel.add(playingTimePanel);

        //添加播放列表面板的组件
        JButton SongFileSelectorButton = new FileSelectorButton("Song");
        playingListPanel.add(SongFileSelectorButton);
        JButton BGMFileSelectorButton = new FileSelectorButton("BGM");
        playingListPanel.add(BGMFileSelectorButton);

        //添加播放控制面板的组件
        JButton PlayFromSongButton = new PlayButton("Song");
        playingControlPanel.add(PlayFromSongButton);
        JButton PlayFromBGMButton = new PlayButton("BGM");
        playingControlPanel.add(PlayFromBGMButton);

        switchButton = new SwitchButton();
        playingControlPanel.add(switchButton);
        switchButton.setEnabled(false);

        pauseButton = new PauseButton();
        playingControlPanel.add(pauseButton);
        pauseButton.setEnabled(false);

        //添加播放状态面板的组件（其余部分在playButton的ActionListener中实现）
        SongMarker = new SongOrBGMNowMarker("Song");
        BGMMarker = new SongOrBGMNowMarker("BGM");
        SongOrBGMPanel.add(SongMarker);
        SongOrBGMPanel.add(BGMMarker);

        playingTimeMarkerLabel = new PlayingTimeMarkerLabel();
        playingTimePanel.add(playingTimeMarkerLabel);
        playingTimeMarkerLabel.setVisible(false);

        // 开始播放后，选择文件、开始播放按钮都不可用
        PlayFromSongButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                if (SongFile != null && BGMFile != null)
                {
                    SongFileSelectorButton.setEnabled(false);
                    BGMFileSelectorButton.setEnabled(false);
                    PlayFromSongButton.setEnabled(false);
                    PlayFromBGMButton.setEnabled(false);
                    PlayFromSongButton.setBackground(new Color(0, 255, 0));
                    PlayFromBGMButton.setBackground(null);
                }
            }
        });
        PlayFromBGMButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                if (SongFile != null && BGMFile != null)
                {
                    SongFileSelectorButton.setEnabled(false);
                    BGMFileSelectorButton.setEnabled(false);
                    PlayFromSongButton.setEnabled(false);
                    PlayFromBGMButton.setEnabled(false);
                    PlayFromBGMButton.setBackground(new Color(0, 255, 0));
                    PlayFromSongButton.setBackground(null);
                }
            }
        });

    }

    // 播放状态面板：当前播放Song/BGM
    private static class SongOrBGMNowMarker extends JButton
    {
        public SongOrBGMNowMarker(String SongOrBGM)
        {
            setPreferredSize(new Dimension(200, 25));
            setText(SongOrBGM);
            setEnabled(false);
        }
    }

    // 播放状态面板：播放进度
    private class PlayingTimeMarkerLabel extends JLabel
    {
        private long playingTimeNow = 0;
        private String playingTimeNowString;
        private long playingDuration;
        private String playingDurationString;


        public PlayingTimeMarkerLabel()
        {
        }

        public void setPlayingMarkerFor(SongBGMSwitcher switcher)
        {
            Clip clip = null;
            try
            {
                clip = AudioSystem.getClip();
            }
            catch (LineUnavailableException e)
            {
                e.printStackTrace();
            }
            AudioInputStream ais = null;
            try
            {
                ais = AudioSystem.getAudioInputStream(SongFile);
            }
            catch (UnsupportedAudioFileException | IOException e)
            {
                e.printStackTrace();
            }
            try
            {
                if (clip != null)
                {
                    clip.open(ais);
                }
            }
            catch (LineUnavailableException | IOException e)
            {
                e.printStackTrace();
            }
            if (clip != null)
            {
                playingDuration = (long) (clip.getMicrosecondLength() / 1E3); // milliseconds
                playingDurationString = new SimpleDateFormat("mm:ss").format(new Date(playingDuration));
                playingTimeNowString = new SimpleDateFormat("mm:ss").format(new Date(playingTimeNow));
                setText(playingTimeNowString + "/" + playingDurationString);
            }
            setAlignmentX(JLabel.CENTER);
            setAlignmentY(JLabel.CENTER);

            Timer t = new Timer(200, new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    if (switcher.isSongOrBGMNow())
                        playingTimeNow = (long) (switcher.getSongTime().getSeconds() * 1E3); // milliseconds
                    else
                        playingTimeNow = (long) (switcher.getBGMTime().getSeconds() * 1E3); // milliseconds
                    playingTimeNowString = new SimpleDateFormat("mm:ss").format(new Date(playingTimeNow));
                    setText(playingTimeNowString + "/" + playingDurationString);
                    // 播放结束后未循环时使暂停按钮不可用
                    pauseButton.setEnabled(playingTimeNow < playingDuration);
                }
            });
            t.start();
        }
    }

    // 播放列表面板：文件选择按钮、歌曲名显示
    private class FileSelectorButton extends JButton
    {
        private JFileChooser chooser;
        private boolean selectedFlag = false;

        public FileSelectorButton(String selectWhat)
        {
            // 默认按钮大小
            setPreferredSize(new Dimension(200, 25));
            // 按钮文本
            setText("Select " + selectWhat + " file");
            // 建立文件选择器
            chooser = new JFileChooser();
            chooser.setFileFilter(new wavFilter());
            chooser.setCurrentDirectory(new File(".")); // 初始目录为Project路径
            // 动作：选择文件
            // 匿名内部类，文件选择按钮的动作
            this.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    // 选择文件
                    int result = chooser.showOpenDialog(null);
                    // 文件选择成功
                    if (result == JFileChooser.APPROVE_OPTION)
                    {
                        selectedFlag = true;
                        setBackground(new Color(0, 255, 0));
                        //根据selectWhat改外围类的private File成员
                        if (Objects.equals(selectWhat, "BGM"))
                            BGMFile = chooser.getSelectedFile();
                        else if (Objects.equals(selectWhat, "Song"))
                            SongFile = chooser.getSelectedFile();
                        setText(chooser.getSelectedFile().getName());
                    }
                }
            });
        }

        public boolean selectedOrNot()  // 是否已用这个按钮选择文件
        {
            return this.selectedFlag;
        }
    }

    // 播放控制面板：从Song或BGM开始的按钮
    private class PlayButton extends JButton
    {
        public PlayButton(String playFromWhat)
        {
            // 按钮文本
            setText("Play from " + playFromWhat);

            // 动作：开始播放
            // 匿名内部类，开始播放按钮的动作
            this.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    // 有一个没选好则无法播放
                    if (SongFile == null || BGMFile == null)
                        setBackground(new Color(255, 0, 0));
                    else
                    {
                        songBGMSwitcher = new SongBGMSwitcher(SongFile, BGMFile);
                        if (Objects.equals(playFromWhat, "BGM"))
                        {
                            songBGMSwitcher.startFromBGM();
                            BGMMarker.setBackground(new Color(0, 255, 255));
                        }
                        else if (Objects.equals(playFromWhat, "Song"))
                        {
                            songBGMSwitcher.startFromSong();
                            SongMarker.setBackground(new Color(0, 255, 255));
                        }

                        // 开始播放后，切换按钮、播放进度标签、暂停按钮可用
                        switchButton.addActionListener(songBGMSwitcher);
                        switchButton.setEnabled(true);

                        playingTimeMarkerLabel.setPlayingMarkerFor(songBGMSwitcher);
                        playingTimeMarkerLabel.setVisible(true);

                        pauseButton.setPauseButtonFor(songBGMSwitcher);
                        pauseButton.setEnabled(true);
                    }
                }
            });
        }
    }

    // 播放控制面板：切换/循环按钮
    private class SwitchButton extends JButton
    {
        public SwitchButton()
        {
            // 按钮文本
            setText("Switch/Loop");
            // 动作：切换标签的颜色来显示当前
            this.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    if (SongMarker.getBackground().equals(new Color(0, 255, 255)))
                    {
                        SongMarker.setBackground(null);
                        BGMMarker.setBackground(new Color(0, 255, 255));
                    }
                    else
                    {
                        BGMMarker.setBackground(null);
                        SongMarker.setBackground(new Color(0, 255, 255));
                    }
                }
            });
        }
    }

    // 播放控制面板：暂停/开始按钮
    private class PauseButton extends JButton
    {
        public PauseButton()
        {
            // 按钮初始文本
            setText("Pause");
        }

        public void setPauseButtonFor(SongBGMSwitcher switcher)
        {
            this.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    if(!playingNow)
                    {
                        switcher.play();
                        setText("Pause");
                        playingNow = true;
                    }
                    else
                    {
                        switcher.pause();
                        setText("Play");
                        playingNow = false;
                    }
                }
            });
        }

        private boolean playingNow = true;
    }
}