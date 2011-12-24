package se.microcode.confluence.plugin.base.picasa;

import se.microcode.confluence.plugin.base.picasa.DisplayMode;

public class GalleryExcerptsMacroArguments
{
    public GalleryExcerptsMacroArguments()
    {
        maxEntries = 5;
        thumbSize = 0;
        randomize = false;
        display = DisplayMode.PHOTOS;
    }

    public String user;
    public String album;
    public int maxEntries;
    public int thumbSize;
    public String photo;
    public boolean randomize;
    public String page;
    public DisplayMode display;
}
