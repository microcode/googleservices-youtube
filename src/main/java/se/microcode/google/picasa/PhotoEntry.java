package se.microcode.google.picasa;

import com.google.api.client.util.Key;

public class PhotoEntry extends Entry
{
    @Key
    public Category category = Category.newKind("photo");

    @Key("gphoto:id")
    public String id;

    @Key("media:group")
    public Group group;
}
