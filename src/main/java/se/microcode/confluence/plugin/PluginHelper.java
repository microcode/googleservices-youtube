package se.microcode.confluence.plugin;

import com.atlassian.confluence.plugin.webresource.ConfluenceWebResourceManager;
import com.atlassian.confluence.renderer.radeox.macros.MacroUtils;
import com.atlassian.confluence.util.velocity.VelocityUtils;
import com.atlassian.plugin.webresource.WebResourceManager;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PluginHelper
{
    public static String createCssFix(WebResourceManager webResourceManager, String key)
    {
        StringBuilder builder = new StringBuilder();

        // workaround for zen filtering resources and "forgetting" to include the ones we use
        ConfluenceWebResourceManager confluenceWebResourceManager = (ConfluenceWebResourceManager)webResourceManager;
        if (confluenceWebResourceManager != null)
        {
            String resources = confluenceWebResourceManager.getResources();
            Pattern pattern = Pattern.compile("<link type=\"text/css\" rel=\"stylesheet\" href=\"(.*/" + key + "/.*)\" media=\"all\">");
            Matcher matcher = pattern.matcher(resources);
            if (matcher.find())
            {
                Map context = MacroUtils.defaultVelocityContext();

                context.put("resource", matcher.group(1));
                context.put("key", key);

                builder.append(VelocityUtils.getRenderedTemplate("/se/microcode/google-plugin/css-fix.vm", context));
            }
        }
        return builder.toString();
    }
}
