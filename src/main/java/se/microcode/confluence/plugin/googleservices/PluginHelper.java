package se.microcode.confluence.plugin.googleservices;

import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheFactory;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.plugin.webresource.ConfluenceWebResourceManager;
import com.atlassian.confluence.renderer.PageContext;
import com.atlassian.confluence.renderer.radeox.macros.MacroUtils;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.confluence.util.velocity.VelocityUtils;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.spring.container.ContainerManager;
import com.opensymphony.webwork.ServletActionContext;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PluginHelper
{
    public static String getPageUrl(String pageName, SettingsManager settingsManager, PageContext pageContext)
    {
        PageManager pageManager = (PageManager) ContainerManager.getComponent("pageManager");
        Page page = null;
        String url = null;

        if (pageName.indexOf(':') >= 0)
        {
            page = pageManager.getPage(pageName.substring(0, pageName.indexOf(':')), pageName.substring(pageName.indexOf(':')+1));
        }
        else
        {
            page = pageManager.getPage(pageContext.getSpaceKey(), pageName);
        }

        if (page != null)
        {
            url = settingsManager.getGlobalSettings().getBaseUrl() + page.getUrlPath();
        }

        return url;
    }

    public static Cache getCache(String name)
    {
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
        Cache cache = cacheFactory.getCache(name);
        if (flushCache)
        {
            cache.removeAll();
        }

        return cache;
    }
}
