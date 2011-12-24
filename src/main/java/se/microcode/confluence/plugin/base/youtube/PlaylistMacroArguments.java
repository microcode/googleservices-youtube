package se.microcode.confluence.plugin.base.youtube;

import se.microcode.base.Argument;
import se.microcode.base.ArgumentSource;

public class PlaylistMacroArguments
{
    public PlaylistMacroArguments()
    {
        pageSize = -1;
        thumbnails = 5;
        reverse = false;
    }

    public String user;
    public int pageSize;
    public int thumbnails;
    public boolean reverse;

    @Argument(name = "playlist", source = ArgumentSource.EXTERNAL)
    public String playlist;
    @Argument(name = "video", source = ArgumentSource.EXTERNAL)
    public String video;
    @Argument(name = "pageIndex", source = ArgumentSource.EXTERNAL)
    public int pageIndex;
    @Argument(name = "pageId", source = ArgumentSource.EXTERNAL)
    public int pageId;
}
