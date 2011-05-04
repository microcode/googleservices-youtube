package se.microcode.google.youtube;

import com.google.api.client.googleapis.GoogleUrl;

import com.google.api.client.util.Key;

public class Url extends GoogleUrl
{
    public static String ROOT_URL = "http://gdata.youtube.com/";

    @Key
    public String kinds;

    public Url(String encodedUrl)
    {
        super(encodedUrl);
    }

    public static Url relativeToRoot(String relativePath)
    {
        return new Url(ROOT_URL + relativePath);
    }
}
