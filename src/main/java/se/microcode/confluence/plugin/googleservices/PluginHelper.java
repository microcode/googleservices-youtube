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
