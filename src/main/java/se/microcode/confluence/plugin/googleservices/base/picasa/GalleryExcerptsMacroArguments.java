package se.microcode.confluence.plugin.googleservices.base.picasa;

public class GalleryExcerptsMacroArguments
{
    public GalleryExcerptsMacroArguments()
    {
        maxEntries = 5;
        imageSize = "144u";
        randomize = false;
        display = DisplayMode.PHOTOS;
    }

    public String user;
    public String album;
    public int maxEntries;
    public String imageSize;
    public String photo;
    public boolean randomize;
    public String page;
    public DisplayMode display;
}
