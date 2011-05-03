package se.microcode.google.picasa;

import com.google.api.client.util.Key;

public class Content
{
    @Key("@url")
    public String url;

    @Key("@width")
    public int width;

    @Key("@height")
    public int height;
}
