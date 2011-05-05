package se.microcode.google.picasa;

import com.google.api.client.googleapis.GoogleUrl;

import com.google.api.client.util.Key;

public class Url extends GoogleUrl
{
    public static String ROOT_URL = "https://picasaweb.google.com/data/";

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
