package se.microcode.confluence.plugin.blogger;

import com.atlassian.cache.Cache;

import com.google.api.client.util.DateTime;
import se.microcode.google.GoogleHelper;
import se.microcode.google.blogger.Post;
import se.microcode.google.blogger.PostFeed;
import se.microcode.google.blogger.Url;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BloggerHelper extends GoogleHelper
{
    public static List<Map> buildPostFeed(List<Post> posts, boolean extractImages, int widthParam)
    {
        ArrayList<Map> output = new ArrayList<Map>();

        for (Post post : posts)
        {
            output.add(buildPost(post, extractImages, widthParam));
        }

        return output;
    }

    public static Map buildPost(Post post, boolean extractImages, int widthParam)
    {
        HashMap<String,Object> entry = new HashMap<String,Object>();

        String content = post.content;
        List<Map> images = null;

        Pattern imagePattern = Pattern.compile("(.*)/s640/(.*)");

        if (extractImages)
        {
            images = new ArrayList<Map>();

            Pattern pattern = Pattern.compile("<img.+?src=\"(.+?)\".*?>");
            Matcher matcher = pattern.matcher(content);

            while (matcher.find())
            {
                HashMap<String,String> image = new HashMap<String,String>();
                String url = matcher.group(1);

                Matcher m = imagePattern.matcher(url);
                String thumb = url;
                if (m.find())
                {
                    thumb = m.group(1) + "/s" + Integer.toString(widthParam) + "-c/" + m.group(2);
                }

                image.put("thumb", thumb);
                image.put("url", url);

                images.add(image);
            }

            matcher.reset();

            try
            {
                content = matcher.replaceAll("");
            }
            catch (IllegalStateException e)
            {
            }
        }

        content = content.replaceAll("<a [^>]+>< ?\\/a>", ""); // empty links
        content = content.replaceAll("<div [^>]*class=\"separator\"[^>]*>< ?\\/div>", ""); // empty separator divs
        content = content.replaceAll("<div [^>]*class=\"blogger-post-footer\"[^>]*>.*< ?\\/div>", ""); // footer tracking link

        DateTime time;
        try
        {
            time = DateTime.parseRfc3339(post.published);
        }
        catch (NumberFormatException e)
        {
            time = new DateTime(new Date());
        }

        SimpleDateFormat format = new SimpleDateFormat("HH:mm");


        entry.put("title", post.title);
        entry.put("timestamp", format.format(new Date(time.value)));
        entry.put("content", content);
        entry.put("images", images);

        return entry;
    }

    public static PostFeed getBlogPosts(String id, String labels[], Cache cache, int timeout) throws IOException
    {
        String query = "";
        if (labels.length > 0)
        {
            query = "/-";
            for (String label : labels)
            {
                query += "/" + label;
            }
        }

        Url url = Url.relativeToRoot("feeds/" + id + "/posts/default" + query);

        return (PostFeed)getFeed(url, cache, PostFeed.class, false, timeout);
    }
}
