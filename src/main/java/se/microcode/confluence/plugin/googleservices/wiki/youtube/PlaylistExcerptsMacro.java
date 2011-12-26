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
