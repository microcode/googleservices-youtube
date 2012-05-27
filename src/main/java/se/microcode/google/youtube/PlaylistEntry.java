package se.microcode.google.youtube;

import com.google.api.client.util.Key;

import se.microcode.google.Entry;

public class PlaylistEntry extends Entry
{
    @Key("atom:summary")
    public String summary;

    @Key("yt:countHint")
    public int countHint;

    @Key("media:group")
    public PlaylistGroup group;

    @Key("yt:playlistId")
    public String id;
}
