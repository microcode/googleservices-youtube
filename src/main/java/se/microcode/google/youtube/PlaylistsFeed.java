package se.microcode.google.youtube;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.Key;

import java.io.IOException;
import java.util.List;

public class PlaylistsFeed extends Feed
{
    @Key("atom:entry")
    public List<PlaylistEntry> playlists;

    public static PlaylistsFeed executeGet(HttpTransport transport, Url url) throws IOException
    {
        url.kinds = "playlists";
        return (PlaylistsFeed) Feed.executeGet(transport, url, PlaylistsFeed.class);
    }
}
