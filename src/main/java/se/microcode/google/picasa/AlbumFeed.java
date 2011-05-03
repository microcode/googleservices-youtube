package se.microcode.google.picasa;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.Key;

import java.io.IOException;
import java.util.List;

public class AlbumFeed extends Feed
{
    @Key("atom:entry")
    public List<PhotoEntry> photos;

    public static AlbumFeed executeGet(HttpTransport transport, Url url) throws IOException
    {
        url.kinds = "photo";
        return (AlbumFeed) Feed.executeGet(transport, url, AlbumFeed.class);
    }
}
