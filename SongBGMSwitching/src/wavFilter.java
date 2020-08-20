import javax.swing.filechooser.FileFilter;
import java.io.File;

public class wavFilter extends FileFilter
{
    public boolean accept(File f)
    {
        return f.getName().toLowerCase().endsWith(".wav") || f.isDirectory();
    }
    public String getDescription()
    {
        return "wav Audio (*.wav)";
    }
}