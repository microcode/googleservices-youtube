package se.microcode.google.youtube;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.Key;

import se.microcode.google.Feed;

import java.io.IOException;
import java.util.List;

public class PlaylistsFeed extends Feed
{
    @Key("atom:entry")
    public List<PlaylistEntry> playlists;
}
