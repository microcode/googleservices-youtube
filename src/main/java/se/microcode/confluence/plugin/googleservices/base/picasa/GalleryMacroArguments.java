package se.microcode.confluence.plugin.googleservices.base.picasa;

import se.microcode.base.Argument;
import se.microcode.base.ArgumentSource;

public class GalleryMacroArguments
{
    public GalleryMacroArguments()
    {
        pageSize = -1;
        imageSize = 640;
        thumbnails = 5;
        albumSize = "144u";
        thumbSize = "72u";
    }

    public String user;
    public int pageSize;
    public int imageSize;
    public String albumSize;
    public int thumbnails;
    public String thumbSize;

    @Argument(name = "album", source = ArgumentSource.EXTERNAL)
    public String album;
    @Argument(name = "photo", source = ArgumentSource.EXTERNAL)
    public String photo;
    @Argument(name = "page", source = ArgumentSource.EXTERNAL)
    public int page;
    @Argument(name = "pageId", source = ArgumentSource.EXTERNAL)
    public int pageId;
}
