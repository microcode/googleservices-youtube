package se.microcode.confluence.plugin.googleservices.xhtml.picasa;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.macro.Macro;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.opensymphony.webwork.ServletActionContext;
import se.microcode.base.ArgumentParser;
import se.microcode.base.ArgumentResolver;
import se.microcode.confluence.plugin.googleservices.PluginHelper;
import se.microcode.confluence.plugin.googleservices.base.picasa.GalleryContext;
import se.microcode.confluence.plugin.googleservices.base.picasa.GalleryExcerptsMacroArguments;
import se.microcode.confluence.plugin.googleservices.base.picasa.GalleryHelper;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

public class GalleryExcerptsMacro implements Macro
{
    public BodyType getBodyType()
    {
        return BodyType.NONE;
    }

    public OutputType getOutputType()
    {
        return OutputType.BLOCK;
    }

    private SettingsManager settingsManager;

    public GalleryExcerptsMacro(SettingsManager settingsManager)
    {
        this.settingsManager = settingsManager;
    }

    public String execute(Map<String,String> params, String body, ConversionContext conversionContext) throws MacroExecutionException
    {
        GalleryExcerptsMacroArguments args = (GalleryExcerptsMacroArguments) ArgumentParser.parse(new GalleryExcerptsMacroArguments(), params, new ArgumentResolver() {
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

        GalleryContext galleryContext = new GalleryContext();
        try
        {
            GalleryHelper.retrieveGalleryExcerpts(args, galleryContext);
        }
        catch (IOException e)
        {
            throw new MacroExecutionException("Failed to retrieve gallery feed");
        }

        return GalleryHelper.renderGalleryExcerpts(args, url, conversionContext.getPageContext(), galleryContext);
    }
}
