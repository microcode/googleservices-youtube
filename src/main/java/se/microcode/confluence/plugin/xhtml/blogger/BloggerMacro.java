package se.microcode.confluence.plugin.xhtml.blogger;

import com.atlassian.cache.Cache;
import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.macro.Macro;
import com.atlassian.confluence.macro.MacroExecutionException;
import se.microcode.base.ArgumentParser;
import se.microcode.confluence.plugin.PluginHelper;
import se.microcode.confluence.plugin.base.blogger.BloggerMacroArguments;
import se.microcode.google.blogger.PostFeed;

import java.io.IOException;
import java.util.Map;

public class BloggerMacro implements Macro
{
    public BodyType getBodyType()
    {
        return BodyType.NONE;
    }

    public OutputType getOutputType()
    {
        return OutputType.BLOCK;
    }

    private BloggerHelper bloggerHelper;

    public BloggerMacro()
    {
        this.bloggerHelper = new BloggerHelper();
    }

    public String execute(Map<String,String> params, String body, ConversionContext conversionContext) throws MacroExecutionException
    {
        BloggerMacroArguments args = (BloggerMacroArguments) ArgumentParser.parse(new BloggerMacroArguments(), params);

        if (args.id == null)
        {
            throw new MacroExecutionException("Blog id not specified");
        }

        Cache cache = PluginHelper.getCache("se.microcode.confluence.plugin.blogger");

        PostFeed blogFeed;
        try
        {
            blogFeed = bloggerHelper.getBlogPosts(args.id, args.labels, cache, args.timeout);
        }
        catch (IOException e)
        {
            throw new MacroExecutionException(e.toString());
        }

        StringBuilder builder = new StringBuilder();

        if(blogFeed != null && blogFeed.posts != null)
        {
            builder.append(bloggerHelper.renderPostFeed(blogFeed, args));
        }

        return builder.toString();
    }
}
