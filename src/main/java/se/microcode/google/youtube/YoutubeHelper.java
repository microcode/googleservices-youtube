package se.microcode.google.youtube;

import com.atlassian.cache.Cache;
import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.xml.atom.AtomParser;

import java.io.IOException;
import java.lang.ClassCastException;

public class YoutubeHelper
{
    public static PlaylistsFeed getPlaylistsFeed(String user, Cache cache) throws IOException
    {
        HttpTransport transport = createTransport();
        Url url = Url.relativeToRoot("feeds/api/users/" + user + "/playlists");
        String key = url.toString();

        PlaylistsFeed feed = null;

        if (cache != null)
        {
            try
            {
                feed = (PlaylistsFeed)cache.get(key);
            }
            catch (ClassCastException e)
            {
                feed = null;
            }
        }

        if (feed == null)
        {
            feed = PlaylistsFeed.executeGet(transport, url);
            if ((feed != null) && (cache != null))
            {
                cache.put(key, feed);
            }
        }

        return feed;
    }

    public static VideoFeed getPlaylistFeed(String playlist, Cache cache) throws IOException
    {
        HttpTransport transport = createTransport();
        Url url = Url.relativeToRoot("feeds/api/playlists/" + playlist);
        String key = url.toString();

        VideoFeed feed = null;

        if (cache != null)
        {
            try
            {
                feed = (VideoFeed)cache.get(key);
            }
            catch (ClassCastException e)
            {
                feed = null;
            }
        }

        if (feed == null)
        {
            feed = VideoFeed.executeGet(transport, url);
            if ((feed != null) && (cache != null))
            {
                cache.put(key, feed);
            }
        }

        return feed;
    }

    public static HttpTransport createTransport()
    {
        GoogleHeaders headers = new GoogleHeaders();
        headers.setApplicationName("se.microcode.youtube-playlist-plugin/1.0");
        headers.gdataVersion = "2";

        AtomParser parser = new AtomParser();
        parser.namespaceDictionary = Util.NAMESPACE_DICTIONARY;

        HttpTransport transport = new NetHttpTransport();
        transport.defaultHeaders = headers;
        transport.addParser(parser);
        return transport;
    }
}
