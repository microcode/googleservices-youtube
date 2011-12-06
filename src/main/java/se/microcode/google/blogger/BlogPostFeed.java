package se.microcode.google.blogger;

import com.google.api.client.util.Key;
import se.microcode.google.Feed;

import java.util.List;

public class BlogPostFeed extends Feed
{
    @Key("atom:entry")
    public List<BlogPost> posts;
}
