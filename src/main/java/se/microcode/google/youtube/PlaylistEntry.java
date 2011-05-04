package se.microcode.google.youtube;

import com.google.api.client.util.Key;

public class PlaylistEntry extends Entry
{
    @Key("atom:summary")
    public String summary;

    @Key("yt:countHint")
    public int countHint;

    @Key("yt:playlistId")
    public String id;
}
