package se.microcode.confluence.plugin.googleservices.base.blogger;

public class BloggerMacroArguments
{
    public BloggerMacroArguments()
    {
        images = ImageStyle.ON;
        labels = new String[0];
        timestamp = false;
        count = 0;
        reverse = false;
        header = HeaderSize.H5;
        width = 320;
        timeout = 3600;
    }

    public String id;
    public String labels[];
    public ImageStyle images;
    public boolean timestamp;
    public int count;
    public boolean reverse;
    public HeaderSize header;
    public int width;
    public int timeout;
}
