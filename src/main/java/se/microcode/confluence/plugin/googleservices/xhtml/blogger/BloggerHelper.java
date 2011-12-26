package se.microcode.confluence.plugin.googleservices.xhtml.blogger;

import com.atlassian.confluence.velocity.htmlsafe.HtmlFragment;

public class BloggerHelper extends se.microcode.confluence.plugin.googleservices.base.blogger.BloggerHelper
{
    public Object buildContent(String content)
    {
        return new HtmlFragment(content);
    }
}
