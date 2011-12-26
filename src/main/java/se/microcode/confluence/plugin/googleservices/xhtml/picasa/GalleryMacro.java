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
