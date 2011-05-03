package se.microcode.google.picasa;

import com.google.api.client.util.Key;

public class AlbumEntry extends Entry
{
    @Key("gphoto:access")
    public String access;

    @Key("gphoto:id")
    public String id;

    @Key("gphoto:name")
    public String name;

    @Key("gphoto:numphotos")
    public int numPhotos;

    @Key("media:group")
    public Group group;

    @Key
    public Category category = Category.newKind("album");

    public AlbumEntry clone()
    {
        return (AlbumEntry) super.clone();
    }
}
