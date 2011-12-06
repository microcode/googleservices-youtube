package se.microcode.google;

import com.google.api.client.util.Key;

public class Author
{
    @Key("atom:name")
    public String name;

    @Key("atom:uri")
    public String uri;

    @Key("atom:email")
    public String email;
}
