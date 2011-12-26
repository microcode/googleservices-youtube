package se.microcode.confluence.plugin.googleservices.xhtml.youtube;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.macro.Macro;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.opensymphony.webwork.ServletActionContext;
import se.microcode.base.ArgumentParser;
import se.microcode.base.ArgumentResolver;
import se.microcode.confluence.plugin.googleservices.base.youtube.PlaylistContext;
import se.microcode.confluence.plugin.googleservices.base.youtube.PlaylistHelper;
import se.microcode.confluence.plugin.googleservices.base.youtube.PlaylistMacroArguments;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

public class PlaylistMacro implements Macro
{
    public BodyType getBodyType()
    {
        return BodyType.NONE;
    }

    public OutputType getOutputType()
    {
        return OutputType.BLOCK;
    }

    public String execute(Map<String,String> params, String body, ConversionContext conversionContext) throws MacroExecutionException
    {
        PlaylistMacroArguments args = (PlaylistMacroArguments) ArgumentParser.parse(new PlaylistMacroArguments(), params, new ArgumentResolver() {
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

        PlaylistContext playlistContext = new PlaylistContext();

        try
        {
            PlaylistHelper.retrievePlaylists(args, playlistContext);
        }
        catch (IOException e)
        {
            throw new MacroExecutionException("Could not retrieve playlists");
        }

        return PlaylistHelper.renderPlaylists(args, conversionContext.getPageContext(), playlistContext);
    }
}
