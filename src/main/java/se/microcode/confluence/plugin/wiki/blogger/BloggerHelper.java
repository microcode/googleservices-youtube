package se.microcode.confluence.plugin.wiki.blogger;

import com.atlassian.cache.Cache;

import com.atlassian.confluence.renderer.radeox.macros.MacroUtils;
import com.atlassian.confluence.util.velocity.VelocityUtils;
import se.microcode.confluence.plugin.base.blogger.BloggerMacroArguments;
import se.microcode.google.blogger.Post;
import se.microcode.google.blogger.PostFeed;
import se.microcode.google.blogger.Url;

import java.io.IOException;
import java.util.*;

public class BloggerHelper extends se.microcode.confluence.plugin.base.blogger.BloggerHelper
{
    public Object buildContent(String content)
    {
        return content;
    }
}
