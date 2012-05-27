package se.microcode.google.youtube;

import com.google.api.client.util.Key;

import java.util.List;

public class Group
{
    @Key("media:credit")
    public String credit;

    @Key("media:description")
    public String description;

    @Key("media:thumbnail")
    public List<Thumbnail> thumbnail;
}
