package se.microcode.confluence.plugin.googleservices.wiki.youtube;

import com.atlassian.cache.Cache;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.renderer.PageContext;
import com.atlassian.confluence.renderer.radeox.macros.MacroUtils;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.confluence.util.velocity.VelocityUtils;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.v2.macro.BaseMacro;
import com.atlassian.renderer.v2.macro.MacroException;
import com.opensymphony.webwork.ServletActionContext;
import se.microcode.base.ArgumentParser;
import se.microcode.base.ArgumentResolver;
import se.microcode.confluence.plugin.googleservices.PluginHelper;
import se.microcode.confluence.plugin.googleservices.base.youtube.PlaylistContext;
import se.microcode.confluence.plugin.googleservices.base.youtube.PlaylistExcerptsMacroArguments;
import se.microcode.confluence.plugin.googleservices.base.youtube.PlaylistHelper;
import se.microcode.google.youtube.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PlaylistExcerptsMacro extends BaseMacro
{
    public RenderMode getBodyRenderMode()
    {
        return RenderMode.NO_RENDER;
    }

    public boolean hasBody()
    {
        return false;
    }

    private SettingsManager settingsManager;

    public PlaylistExcerptsMacro(SettingsManager settingsManager)
    {
        this.settingsManager = settingsManager;
    }

    public String execute(Map params, String body, RenderContext renderContext) throws MacroException
    {
        PlaylistExcerptsMacroArguments args = (PlaylistExcerptsMacroArguments) ArgumentParser.parse(new PlaylistExcerptsMacroArguments(), params, new ArgumentResolver()
            {
                @Override
                public String get(String s)
                {
                    HttpServletRequest request = ServletActionContext.getRequest();
                    return request != null ? request.getParameter(s) : null;
                }
            });

        if (args.user == null)
        {
            throw new MacroException("No user specified");
        }

        String url = null;
        if (args.page != null)
        {
            url = PluginHelper.getPageUrl(args.page, settingsManager,  (PageContext)renderContext);
        }

        PlaylistContext playlistContext = new PlaylistContext();

        try
        {
            PlaylistHelper.retrievePlaylistExcerpts(args, playlistContext);
        }
        catch (IOException e)
        {
            throw new MacroException("Could not retrieve playlist feed");
        }

        return PlaylistHelper.renderPlaylistExcerpts(args, url, (PageContext)renderContext, playlistContext);
    }
}
