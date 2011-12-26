package se.microcode.confluence.plugin.googleservices.xhtml.youtube;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.macro.Macro;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.confluence.renderer.PageContext;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.renderer.v2.macro.MacroException;
import com.opensymphony.webwork.ServletActionContext;
import se.microcode.base.ArgumentParser;
import se.microcode.base.ArgumentResolver;
import se.microcode.confluence.plugin.googleservices.PluginHelper;
import se.microcode.confluence.plugin.googleservices.base.youtube.PlaylistContext;
import se.microcode.confluence.plugin.googleservices.base.youtube.PlaylistExcerptsMacroArguments;
import se.microcode.confluence.plugin.googleservices.base.youtube.PlaylistHelper;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

public class PlaylistExcerptsMacro implements Macro
{
    public BodyType getBodyType()
    {
        return BodyType.NONE;
    }

    public OutputType getOutputType()
    {
        return OutputType.INLINE;
    }

    private SettingsManager settingsManager;

    public PlaylistExcerptsMacro(SettingsManager settingsManager)
    {
        this.settingsManager = settingsManager;
    }

    public String execute(Map<String,String> params, String body, ConversionContext conversionContext) throws MacroExecutionException
    {
        PlaylistExcerptsMacroArguments args = (PlaylistExcerptsMacroArguments) ArgumentParser.parse(new PlaylistExcerptsMacroArguments(), params, new ArgumentResolver() {
            @Override
            public String get(String s) {
                HttpServletRequest request = ServletActionContext.getRequest();
                return request != null ? request.getParameter(s) : null;
            }
        });

        if (args.user == null)
        {
            throw new MacroExecutionException("No user specified");
        }

        String url = null;
        if (args.page != null)
        {
            url = PluginHelper.getPageUrl(args.page, settingsManager, conversionContext.getPageContext());
        }

        PlaylistContext playlistContext = new PlaylistContext();

        try
        {
            PlaylistHelper.retrievePlaylistExcerpts(args, playlistContext);
        }
        catch (IOException e)
        {
            throw new MacroExecutionException("Could not retrieve playlist feed");
        }

        return PlaylistHelper.renderPlaylistExcerpts(args, url, conversionContext.getPageContext(), playlistContext);
    }
}
