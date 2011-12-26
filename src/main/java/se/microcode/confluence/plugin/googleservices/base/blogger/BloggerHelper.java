/*

Copyright (c) 2011, Jesper Svennevid
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice,
   this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution.
 * Neither the name of microcode.se nor the names of its contributors may be
   used to endorse or promote products derived from this software without
   specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.

 */

package se.microcode.confluence.plugin.googleservices.base.blogger;

import com.atlassian.cache.Cache;
import com.atlassian.confluence.renderer.radeox.macros.MacroUtils;
import com.atlassian.confluence.util.velocity.VelocityUtils;
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

abstract public class BloggerHelper extends GoogleHelper
{
    public List<Map> buildPostFeed(List<Post> posts, ImageStyle imageStyle, int widthParam)
    {
        ArrayList<Map> output = new ArrayList<Map>();

        for (Post post : posts)
        {
            output.add(buildPost(post, imageStyle, widthParam));
        }

        return output;
    }

    public Map buildPost(Post post, ImageStyle imageStyle, int widthParam)
    {
        HashMap<String,Object> entry = new HashMap<String,Object>();

        String content = post.content;
        List<Map> images = null;

        Pattern imagePattern = Pattern.compile("(.*)/s640/(.*)");

        if (imageStyle != ImageStyle.ON)
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
        entry.put("content", buildContent(content));
        if (imageStyle != ImageStyle.OFF)
        {
            entry.put("images", images);
        }

        return entry;
    }

    abstract public Object buildContent(String content);

    public PostFeed getBlogPosts(String id, String labels[], Cache cache, int timeout) throws IOException
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

    public String renderPostFeed(PostFeed postFeed, BloggerMacroArguments args)
    {
        Map context = MacroUtils.defaultVelocityContext();

        context.put("width", args.width);
        context.put("timestamp", args.timestamp);
        context.put("header", args.header.toString().toLowerCase());

        List<Post> posts = new ArrayList<Post>(postFeed.posts);

        if (args.count > 0)
        {
            posts = posts.subList(0, args.count);
        }

        if (args.reverse)
        {
            Collections.reverse(posts);
        }

        context.put("posts", buildPostFeed(posts, args.images, args.width));
        return VelocityUtils.getRenderedTemplate("/se/microcode/confluence/plugin/googleservices/blogger/posts.vm", context);
    }
}
