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

package se.microcode.confluence.plugin.wiki.picasa;

import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.renderer.v2.macro.BaseMacro;
import com.atlassian.renderer.v2.macro.MacroException;
import com.atlassian.renderer.v2.RenderMode;

import com.atlassian.renderer.RenderContext;
import com.atlassian.confluence.renderer.PageContext;
import com.atlassian.confluence.pages.Page;
import com.atlassian.cache.Cache;

import com.atlassian.confluence.renderer.radeox.macros.MacroUtils;
import com.atlassian.confluence.util.velocity.VelocityUtils;

import com.opensymphony.webwork.ServletActionContext;
import se.microcode.base.ArgumentParser;
import se.microcode.base.ArgumentResolver;
import se.microcode.confluence.plugin.PluginHelper;
import se.microcode.confluence.plugin.base.picasa.GalleryHelper;
import se.microcode.confluence.plugin.base.picasa.GalleryMacroArguments;
import se.microcode.google.picasa.*;

import java.util.Map;
import java.util.List;
import java.lang.Math;
import java.io.IOException;
import java.lang.Integer;

public class GalleryMacro extends BaseMacro
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
    private WebResourceManager webResourceManager;

    public GalleryMacro(SettingsManager settingsManager, WebResourceManager webResourceManager)
    {
        this.settingsManager = settingsManager;
        this.webResourceManager = webResourceManager;
    }

    public String execute(Map params, String body, RenderContext renderContext) throws MacroException
    {
        GalleryMacroArguments args = (GalleryMacroArguments)ArgumentParser.parse(new GalleryMacroArguments(), params, new ArgumentResolver()
            {
                @Override
                public String get(String s)
                {
                    return ServletActionContext.getRequest().getParameter(s);
                }
            });


        if (args.user == null)
        {
            throw new MacroException("No user specified");
        }

        Cache cache = PluginHelper.getCache("se.microcode.confluence.plugin.picasa");

        UserFeed userFeed;
        try
        {
            userFeed = PicasaHelper.getUserFeed(args.user, cache);
        }
        catch (IOException e)
        {
            throw new MacroException("Failed to retrieve user feed");
        }

        AlbumFeed albumFeed = null;
        AlbumEntry albumEntry = null;
        if (args.album != null && (userFeed.albums != null))
        {
            for (AlbumEntry entry : userFeed.albums)
            {
                if (entry.id.equals(args.album))
                {
                    albumEntry = entry;
                    break;
                }
            }

            if (albumEntry != null)
            {
                try
                {
                    albumFeed = PicasaHelper.getAlbumFeed(args.user, albumEntry.id, cache);
                }
                catch (IOException e)
                {
                    throw new MacroException("Failed to retrieve album feed");
                }
            }
        }

        PhotoEntry photoEntry = null;
        List<PhotoEntry> thumbnails = null;
        int photoIndex = 0;
        if (args.photo != null && albumFeed != null && albumFeed.photos != null)
        {
            for (int i = 0, n = albumFeed.photos.size(); i != n; ++i)
            {
                PhotoEntry photo = albumFeed.photos.get(i);
                if (args.photo.equals(photo.id))
                {
                    int begin = (int)Math.max(0, i - Math.floor(args.thumbnails/2.0f));
                    int end = (int)Math.min(albumFeed.photos.size(), i + Math.ceil(args.thumbnails/2.0f));

                    photoEntry = photo;
                    photoIndex = i;
                    thumbnails = albumFeed.photos.subList(begin, end);
                    break;
                }
            }
        }

        StringBuilder builder = new StringBuilder();
        Map context = MacroUtils.defaultVelocityContext();

        if (args.pageId > 0)
        {
            context.put("baseUrl", "?pageId=" + args.pageId + "&");
        }
        else
        {
            context.put("baseUrl", "?");
        }

        PageContext pageContext = (PageContext)renderContext;
        Page page = (pageContext.getEntity() != null && pageContext.getEntity() instanceof Page) ? (Page)pageContext.getEntity() : null;
        if (page != null)
        {
            context.put("title", page.getTitle());
        }

        if (photoEntry != null)
        {
            context.put("currPage", photoIndex + 1);
            context.put("pageCount", albumFeed.photos.size());

            if (photoIndex > 0)
            {
                context.put("prev", albumFeed.photos.get(photoIndex-1).id);
            }
            if (photoIndex < albumFeed.photos.size()-1)
            {
                context.put("next", albumFeed.photos.get(photoIndex+1).id);
            }

            context.put("photo", GalleryHelper.buildPhotoEntry(photoEntry, args.imageSize));
            context.put("album", GalleryHelper.buildAlbumEntry(albumEntry));
            context.put("pageId", args.pageId);

            if (thumbnails != null)
            {
                context.put("thumbnails", GalleryHelper.buildPhotoList(thumbnails, 0));
            }

            builder.append(VelocityUtils.getRenderedTemplate("/se/microcode/google-plugin/picasa/photo.vm", context));

        }
        else if (albumFeed != null && albumFeed.photos != null)
        {
            int begin = 0;
            int end = albumFeed.photos.size();
            if (args.pageSize > 0)
            {
                context.put("currPage", args.page+1);
                context.put("pageCount", (end+args.pageSize-1) / args.pageSize);

                begin = Math.min(end, args.page * args.pageSize);
                end = Math.min(end, begin + args.pageSize);
            }

            context.put("photos", GalleryHelper.buildPhotoList(albumFeed.photos.subList(begin, end), 1));
            context.put("album", GalleryHelper.buildAlbumEntry(albumEntry));
            context.put("pageId", args.pageId);

            builder.append(VelocityUtils.getRenderedTemplate("/se/microcode/google-plugin/picasa/photos.vm", context));
        }
        else if (userFeed.albums != null)
        {
            int begin = 0;
            int end = userFeed.albums.size();
            if (args.pageSize > 0)
            {
                context.put("currPage", Integer.toString(args.page+1));
                context.put("pageCount", (end+args.pageSize-1) / args.pageSize);

                begin = Math.min(end, args.page * args.pageSize);
                end = Math.min(end, begin + args.pageSize);
            }

            context.put("albums", GalleryHelper.buildAlbumList(userFeed.albums.subList(begin, end)));
            context.put("pageId", args.pageId);

            builder.append(VelocityUtils.getRenderedTemplate("/se/microcode/google-plugin/picasa/albums.vm", context));
        }

//        builder.append(PluginHelper.createCssFix(webResourceManager, "se.microcode.confluence.plugin.google-plugin:picasa-gallery-resources"));

        return builder.toString();
    }
}
