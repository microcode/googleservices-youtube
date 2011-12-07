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

package se.microcode.confluence.plugin.blogger;

import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheFactory;
import com.atlassian.confluence.renderer.radeox.macros.MacroUtils;
import com.atlassian.confluence.util.velocity.VelocityUtils;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.v2.macro.BaseMacro;
import com.atlassian.renderer.v2.macro.MacroException;
import com.atlassian.spring.container.ContainerManager;
import com.opensymphony.webwork.ServletActionContext;
import se.microcode.google.blogger.Post;
import se.microcode.google.blogger.PostFeed;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class BloggerMacro extends BaseMacro
{
    public RenderMode getBodyRenderMode()
    {
        return RenderMode.NO_RENDER;
    }

    public boolean hasBody()
    {
        return false;
    }

    public static final String ID_PARAM = "id";
    public static final String LABEL_PARAM = "label";
    public static final String AUTHORS_PARAM = "authors";
    public static final String TIMESTAMP_PARAM = "timestamp";
    public static final String IMAGES_PARAM = "images";
    public static final String REVERSE_PARAM = "reverse";
    public static final String COUNT_PARAM = "count";
    public static final String REFRESH_PARAM = "refresh";
    public static final String HEADER_PARAM = "header";
    public static final String WIDTH_PARAM = "width";
    public static final String TIMEOUT_PARAM = "timeout";

    private WebResourceManager webResourceManager;

    public BloggerMacro(WebResourceManager webResourceManager)
    {
        this.webResourceManager = webResourceManager;
    }

    public String execute(Map params, String body, RenderContext renderContext) throws MacroException
    {
        String id = (String)params.get(ID_PARAM);
        if (id == null)
        {
            throw new MacroException("Blog id not specified");
        }

        String imagesParam = (String)params.get(IMAGES_PARAM);

        String labelParam = (String)params.get(LABEL_PARAM);
        String labels[] = {};
        if (labelParam != null)
        {
            labels = labelParam.split(",");
        }

        int timeoutParam = 3600;
        try
        {
            timeoutParam = Integer.parseInt((String)params.get(TIMEOUT_PARAM));
        }
        catch (NumberFormatException e)
        {
        }

        HttpServletRequest request = ServletActionContext.getRequest();
        boolean flushCache = false;
        if (request != null)
        {
            try
            {
                flushCache = Boolean.parseBoolean(request.getParameter("flush-cache"));
            }
            catch (NumberFormatException e)
            {
            }
        }

        CacheFactory cacheFactory = (CacheFactory) ContainerManager.getComponent("cacheManager");
        Cache cache = cacheFactory.getCache("se.microcode.confluence.plugin.blogger");
        if (flushCache)
        {
            cache.removeAll();
        }

        PostFeed blogFeed;
        try
        {
            blogFeed = BloggerHelper.getBlogPosts(id, labels, cache, timeoutParam);
        }
        catch (IOException e)
        {
            throw new MacroException(e.toString());
        }

        StringBuilder builder = new StringBuilder();
        Map context = MacroUtils.defaultVelocityContext();

        int widthParam = 320;
        try
        {
            widthParam = Integer.parseInt((String)params.get(WIDTH_PARAM));
        }
        catch (NumberFormatException e)
        {
        }
        context.put("width", widthParam);

        boolean timestampParam = false;
        try
        {
            timestampParam = Boolean.parseBoolean((String)params.get(TIMESTAMP_PARAM));
        }
        catch (NumberFormatException e)
        {
        }
        context.put("timestamp", timestampParam);


        String headerParam = (String)params.get(HEADER_PARAM);
        if (headerParam == null)
        {
            headerParam = "h5";
        }
        context.put("header", headerParam);

        if(blogFeed != null && blogFeed.posts != null)
        {
            List<Post> posts = new ArrayList<Post>(blogFeed.posts);

            int countParam = posts.size();
            try
            {
                countParam = Math.min(countParam, Integer.parseInt((String) params.get(COUNT_PARAM)));
            }
            catch (NumberFormatException e)
            {
            }
            posts = posts.subList(0, countParam);

            boolean reverseParam = false;
            try
            {
                reverseParam = Boolean.parseBoolean((String)params.get(REVERSE_PARAM));
                if (reverseParam)
                {
                    Collections.reverse(posts);
                }
            }
            catch (NumberFormatException e)
            {
            }

            context.put("posts", BloggerHelper.buildPostFeed(posts, "gallery".equalsIgnoreCase(imagesParam), widthParam));
            builder.append(VelocityUtils.getRenderedTemplate("/se/microcode/google-plugin/blogger/posts.vm", context));
        }

//        builder.append(PluginHelper.createCssFix(webResourceManager, "se.microcode.confluence.plugin.google-plugin:blogger-resources"));

        return builder.toString();
    }
}
