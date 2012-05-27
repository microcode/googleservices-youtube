package se.microcode.google.youtube;

import com.google.api.client.util.Key;

import se.microcode.google.Entry;

public class VideoEntry extends Entry
{
    @Key("media:group")
    public VideoGroup group;
}
