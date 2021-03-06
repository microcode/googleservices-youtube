package se.microcode.google;

import com.google.api.client.googleapis.GoogleUrl;
import com.google.api.client.googleapis.xml.atom.GoogleAtom;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.Key;

import java.util.List;

import java.io.IOException;

public class Feed
{
    @Key("atom:author")
    public Author author;

    @Key("atom:link")
    public List<Link> links;

    public String getPostLink()
    {
        return Link.find(links, "http://schemas.google.com/g/2005#post");
    }

    public String getNextLink()
    {
        return Link.find(links, "next");
    }

    DateTime timeout;
}
