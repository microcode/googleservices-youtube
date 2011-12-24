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

package se.microcode.confluence.plugin.wiki.blogger;

import com.atlassian.cache.Cache;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.v2.macro.BaseMacro;
import com.atlassian.renderer.v2.macro.MacroException;
import com.opensymphony.webwork.ServletActionContext;
import se.microcode.base.ArgumentParser;
import se.microcode.base.ArgumentResolver;
import se.microcode.confluence.plugin.PluginHelper;
import se.microcode.confluence.plugin.base.blogger.BloggerMacroArguments;
import se.microcode.google.blogger.PostFeed;

import java.io.IOException;
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

    private WebResourceManager webResourceManager;
    private BloggerHelper bloggerHelper;

    public BloggerMacro(WebResourceManager webResourceManager)
    {
        this.webResourceManager = webResourceManager;
        this.bloggerHelper = new BloggerHelper();
    }

    public String execute(Map params, String body, RenderContext renderContext) throws MacroException
    {
        BloggerMacroArguments args = (BloggerMacroArguments)ArgumentParser.parse(new BloggerMacroArguments(), params, new ArgumentResolver()
            {
                @Override
                public String get(String s)
                {
                    return ServletActionContext.getRequest().getParameter(s);
                }
            });


        if (args.id == null)
        {
            throw new MacroException("Blog id not specified");
        }

        Cache cache = PluginHelper.getCache("se.microcode.confluence.plugin.blogger");

        PostFeed blogFeed;
        try
        {
            blogFeed = bloggerHelper.getBlogPosts(args.id, args.labels, cache, args.timeout);
        }
        catch (IOException e)
        {
            throw new MacroException(e.toString());
        }

        StringBuilder builder = new StringBuilder();

        if(blogFeed != null && blogFeed.posts != null)
        {
            builder.append(bloggerHelper.renderPostFeed(blogFeed, args));
        }

        return builder.toString();
    }
}
