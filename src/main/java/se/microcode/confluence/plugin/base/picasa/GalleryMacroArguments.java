package se.microcode.confluence.plugin.base.picasa;

import se.microcode.base.Argument;
import se.microcode.base.ArgumentSource;

public class GalleryMacroArguments
{
    public GalleryMacroArguments()
    {
        pageSize = -1;
        imageSize = 640;
        thumbnails = 5;
    }

    public String user;
    public int pageSize;
    public int imageSize;
    public int thumbnails;

    @Argument(name = "album", source = ArgumentSource.SERVLET_REQUEST)
    public String album;
    @Argument(name = "photo", source = ArgumentSource.SERVLET_REQUEST)
    public String photo;
    @Argument(name = "page", source = ArgumentSource.SERVLET_REQUEST)
    public int page;
    @Argument(name = "pageId", source = ArgumentSource.SERVLET_REQUEST)
    public int pageId;
}
