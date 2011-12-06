package se.microcode.google.blogger;

import com.google.api.client.util.Key;
import se.microcode.google.Author;
import se.microcode.google.Entry;

import java.util.List;

public class BlogPost extends Entry
{
    @Key("atom:id")
    public String id;

    @Key("atom:published")
    public String published;

    @Key("atom:updated")
    public String updated;

    @Key("atom:category")
    public List<String> categories;

    @Key("atom:content")
    public String content;

    @Key("atom:author")
    public Author author;
}
