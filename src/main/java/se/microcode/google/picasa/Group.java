package se.microcode.google.picasa;

import com.google.api.client.util.Key;

import java.util.List;

public class Group
{
    @Key("media:content")
    public Content content;

    @Key("media:description")
    public String description;

    @Key("media:thumbnail")
    public List<Thumbnail> thumbnail;
}
