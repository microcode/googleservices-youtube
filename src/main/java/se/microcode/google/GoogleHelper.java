package se.microcode.google;

import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.googleapis.xml.atom.GoogleAtom;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.util.DateTime;
import com.google.api.client.xml.atom.AtomParser;

import com.atlassian.cache.Cache;
import com.google.api.client.googleapis.GoogleUrl;

import java.io.IOException;

public class GoogleHelper
{
    public static Feed getFeed(GoogleUrl url, Cache cache, Class<? extends Feed> feedClass) throws IOException
    {
        return getFeed(url, cache, feedClass, true, 3600);
    }

    public static Feed getFeed(GoogleUrl url, Cache cache, Class<? extends Feed> feedClass, boolean useFields, long timeout) throws IOException
    {
        HttpTransport transport = createTransport();
        String key = url.toString();
        DateTime now = new DateTime();

        Feed feed = null;

        if (cache != null)
        {
            try
            {
                feed = (Feed)cache.get(key);
                if (feed != null)
                {
                    if (feed.timeout.value < now.value)
                    {
                        feed = null;
                    }
                }
            }
            catch (ClassCastException e)
            {
                feed = null;
            }
        }

        if (feed == null)
        {
            feed = executeGet(transport, url, feedClass, useFields);
            if ((feed != null) && (cache != null))
            {
                feed.timeout = new DateTime(now.value + 3600);
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

    static Feed executeGet(HttpTransport transport, GoogleUrl url, Class<? extends Feed> feedClass, boolean useFields) throws IOException
    {
        if (useFields)
        {
            url.fields = GoogleAtom.getFieldsFor(feedClass);
        }
        HttpRequest request = transport.buildGetRequest();
        request.url = url;

        HttpResponse response = request.execute();
        return response.parseAs(feedClass);
    }
}
