package se.microcode.confluence.plugin.xhtml.picasa;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.macro.Macro;

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

    public String execute(Map<String,String> params, String body, ConversionContext conversionContext)
    {
        StringBuilder builder = new StringBuilder();
        return builder.toString();
    }
}
