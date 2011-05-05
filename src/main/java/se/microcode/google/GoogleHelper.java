package se.microcode.google;

import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.googleapis.xml.atom.GoogleAtom;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.xml.atom.AtomParser;

import com.atlassian.cache.Cache;
import com.google.api.client.googleapis.GoogleUrl;

import java.io.IOException;

public class GoogleHelper
{
    public static Feed getFeed(GoogleUrl url, Cache cache, Class<? extends Feed> feedClass) throws IOException
    {
        HttpTransport transport = createTransport();
        String key = url.toString();

        Feed feed = null;

        if (cache != null)
        {
            try
            {
                feed = (Feed)cache.get(key);
            }
            catch (ClassCastException e)
            {
                feed = null;
            }
        }

        if (feed == null)
        {
            feed = executeGet(transport, url, feedClass);
            if ((feed != null) && (cache != null))
            {
                cache.put(key, feed);
            }
        }
        return feed;
    }

    static HttpTransport createTransport()
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

    static Feed executeGet(HttpTransport transport, GoogleUrl url, Class<? extends Feed> feedClass) throws IOException
    {
        url.fields = GoogleAtom.getFieldsFor(feedClass);
        HttpRequest request = transport.buildGetRequest();
        request.url = url;

        HttpResponse response = request.execute();
        return response.parseAs(feedClass);
    }
}
