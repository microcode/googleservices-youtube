package se.microcode.google.youtube;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.Key;

import java.io.IOException;
import java.util.List;

public class VideoFeed extends Feed
{
    @Key("atom:entry")
    public List<VideoEntry> videos;

    public static VideoFeed executeGet(HttpTransport transport, Url url) throws IOException
    {
        url.kinds = "videos";
        return (VideoFeed) Feed.executeGet(transport, url, VideoFeed.class);
    }
}
