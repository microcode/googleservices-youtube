package se.microcode.confluence.plugin.googleservices.base.youtube;

public class PlaylistExcerptsMacroArguments
{
    public PlaylistExcerptsMacroArguments()
    {
        maxEntries = 5;
        randomize = false;
        display = DisplayMode.PLAYLISTS;
    }

    public String user;
    public String playlist;
    public int maxEntries;
    public boolean randomize;
    public String page;
    public DisplayMode display;
}
