package se.microcode.google.youtube;

import com.atlassian.cache.Cache;
import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.xml.atom.AtomParser;

import se.microcode.google.GoogleHelper;
import se.microcode.google.picasa.UserFeed;

import java.io.IOException;
import java.lang.ClassCastException;

public class YoutubeHelper extends GoogleHelper
{
    public static PlaylistsFeed getPlaylistsFeed(String user, Cache cache) throws IOException
    {
        Url url = Url.relativeToRoot("feeds/api/users/" + user + "/playlists");
        url.kinds = "playlists";

        return (PlaylistsFeed)getFeed(url, cache, PlaylistsFeed.class);
    }

    public static VideoFeed getPlaylistFeed(String playlist, Cache cache) throws IOException
    {
        Url url = Url.relativeToRoot("feeds/api/playlists/" + playlist);
        url.kinds = "videos";

        return (VideoFeed)getFeed(url, cache, VideoFeed.class);
    }
}
