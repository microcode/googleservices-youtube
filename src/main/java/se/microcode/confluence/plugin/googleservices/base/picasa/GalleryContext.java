package se.microcode.confluence.plugin.googleservices.base.picasa;

import se.microcode.google.picasa.AlbumEntry;
import se.microcode.google.picasa.AlbumFeed;
import se.microcode.google.picasa.PhotoEntry;
import se.microcode.google.picasa.UserFeed;

import java.util.List;

public class GalleryContext
{
    public UserFeed userFeed;

    public AlbumFeed albumFeed;
    public AlbumEntry albumEntry;

    public PhotoEntry photoEntry;

    public int photoIndex;
    public int albumIndex;

    public List<PhotoEntry> thumbnails;
}
