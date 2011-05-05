package se.microcode.google.youtube;

import com.google.api.client.util.Key;

public class Group
{
    @Key("media:credit")
    public String credit;

    @Key("media:description")
    public String description;

    @Key("yt:videoid")
    public String id;
}
