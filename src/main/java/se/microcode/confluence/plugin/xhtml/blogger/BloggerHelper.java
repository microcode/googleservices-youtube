package se.microcode.confluence.plugin.xhtml.blogger;

import com.atlassian.confluence.velocity.htmlsafe.HtmlFragment;

public class BloggerHelper extends se.microcode.confluence.plugin.base.blogger.BloggerHelper
{
    public Object buildContent(String content)
    {
        return new HtmlFragment(content);
    }
}
