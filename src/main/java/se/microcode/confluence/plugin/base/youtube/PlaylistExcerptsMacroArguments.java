package se.microcode.confluence.plugin.base.youtube;

import se.microcode.confluence.plugin.base.youtube.DisplayMode;

public class PlaylistExcerptsMacroArguments
{
    public PlaylistExcerptsMacroArguments()
    {
        maxEntries = 5;
        randomize = false;
    }

    public String user;
    public String playlist;
    public int maxEntries;
    public boolean randomize;
    public String page;
    public DisplayMode display;
}
