package se.microcode.google.picasa;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.Key;

import java.io.IOException;
import java.util.List;

public class UserFeed extends Feed
{
    @Key("atom:entry")
    public List<AlbumEntry> albums;

    public static UserFeed executeGet(HttpTransport transport, Url url) throws IOException
    {
        url.kinds = "album";
        return (UserFeed) Feed.executeGet(transport, url, UserFeed.class);
    }
}
