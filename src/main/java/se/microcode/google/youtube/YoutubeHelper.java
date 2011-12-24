package se.microcode.google.youtube;

import com.atlassian.cache.Cache;

import se.microcode.google.GoogleHelper;

import java.io.IOException;

public class YoutubeHelper extends GoogleHelper
{
    public static PlaylistFeed getPlaylistFeed(String user, Cache cache) throws IOException
    {
        Url url = Url.relativeToRoot("feeds/api/users/" + user + "/playlists");
        url.kinds = "playlists";

        return (PlaylistFeed)getFeed(url, cache, PlaylistFeed.class);
    }

    public static VideoFeed getVideoFeed(String playlist, Cache cache) throws IOException
    {
        Url url = Url.relativeToRoot("feeds/api/playlists/" + playlist);
        url.kinds = "videos";

        return (VideoFeed)getFeed(url, cache, VideoFeed.class);
    }
}
