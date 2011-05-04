package se.microcode.google.youtube;

import com.google.api.client.util.Key;

public class VideoEntry extends Entry
{
    @Key("media:group")
    public VideoGroup group;
}
