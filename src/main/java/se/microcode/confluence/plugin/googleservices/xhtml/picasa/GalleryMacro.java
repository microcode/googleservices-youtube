package se.microcode.confluence.plugin.googleservices.xhtml.picasa;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.macro.Macro;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.opensymphony.webwork.ServletActionContext;
import se.microcode.base.ArgumentParser;
import se.microcode.base.ArgumentResolver;
import se.microcode.confluence.plugin.googleservices.base.picasa.GalleryContext;
import se.microcode.confluence.plugin.googleservices.base.picasa.GalleryHelper;
import se.microcode.confluence.plugin.googleservices.base.picasa.GalleryMacroArguments;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

public class GalleryMacro implements Macro
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
        GalleryMacroArguments args = (GalleryMacroArguments) ArgumentParser.parse(new GalleryMacroArguments(), params, new ArgumentResolver() {
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

        GalleryContext galleryContext = new GalleryContext();
        try
        {
            GalleryHelper.retrieveGallery(args, galleryContext);
        }
        catch (IOException e)
        {
            throw new MacroExecutionException("Failed to retrieve gallery");
        }

        return GalleryHelper.renderGallery(args, conversionContext.getPageContext(), galleryContext);
    }
}
