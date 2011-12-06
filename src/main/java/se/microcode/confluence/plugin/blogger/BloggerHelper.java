package se.microcode.confluence.plugin.blogger;

import com.atlassian.cache.Cache;

import se.microcode.google.GoogleHelper;
import se.microcode.google.blogger.BlogPost;
import se.microcode.google.blogger.BlogPostFeed;
import se.microcode.google.blogger.Url;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BloggerHelper extends GoogleHelper
{
    public static List<Map> buildBlogPosts(List<BlogPost> blogPosts, boolean extractImages, int widthParam)
    {
        ArrayList<Map> output = new ArrayList<Map>();

        for (BlogPost post : blogPosts)
        {
            output.add(buildBlogPost(post, extractImages, widthParam));
        }

        return output;
    }

    public static Map buildBlogPost(BlogPost post, boolean extractImages, int widthParam)
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

        content = content.replaceAll("<a.+?>< ?\\/a>", ""); // empty links
        content = content.replaceAll("<div.+?class=\"separator\".+?>< ?\\/div>", ""); // empty separator divs
        content = content.replaceAll("<div.+?class=\"blogger-post-footer\".+?>.*</div>", ""); // footer tracking link

        entry.put("title", post.title);
        entry.put("timestamp", post.published);
        entry.put("content", content);
        entry.put("images", images);

        return entry;
    }

    public static BlogPostFeed getBlogPosts(String id, String labels[], Cache cache) throws IOException
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

        return (BlogPostFeed)getFeed(url, cache, BlogPostFeed.class, false, 600
        );
    }
}
