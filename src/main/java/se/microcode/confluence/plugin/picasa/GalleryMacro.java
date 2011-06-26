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

package se.microcode.confluence.plugin.picasa;

import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.renderer.v2.macro.BaseMacro;
import com.atlassian.renderer.v2.macro.MacroException;
import com.atlassian.renderer.v2.RenderMode;

import com.atlassian.renderer.RenderContext;
import com.atlassian.confluence.renderer.PageContext;
import com.atlassian.confluence.pages.Page;
import com.atlassian.cache.CacheFactory;
import com.atlassian.cache.Cache;

import com.atlassian.confluence.renderer.radeox.macros.MacroUtils;
import com.atlassian.confluence.util.velocity.VelocityUtils;

import com.atlassian.spring.container.ContainerManager;

import com.opensymphony.webwork.ServletActionContext;
import javax.servlet.http.HttpServletRequest;

import se.microcode.confluence.plugin.PluginHelper;
import se.microcode.google.picasa.*;

import java.util.Map;
import java.util.List;
import java.lang.Math;
import java.io.IOException;
import java.lang.NumberFormatException;
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

    public static final String PICASAUSER_PARAM = "user";
    public static final String MAXENTRIES_PARAM = "pageSize";
    public static final String IMAGESIZE_PARAM = "imageSize";
    public static final String THUMBNAILS_PARAM = "thumbnails";

    private SettingsManager settingsManager;
    private WebResourceManager webResourceManager;

    public GalleryMacro(SettingsManager settingsManager, WebResourceManager webResourceManager)
    {
        this.settingsManager = settingsManager;
        this.webResourceManager = webResourceManager;
    }

    public String execute(Map params, String body, RenderContext renderContext) throws MacroException
    {
        String picasaUser = (String)params.get(PICASAUSER_PARAM);
        int maxEntries = -1;
        try
        {
            maxEntries = Integer.parseInt((String)params.get(MAXENTRIES_PARAM));
        }
        catch (NumberFormatException e)
        {
        }

        int thumbnailCount = 5;
        try
        {
            thumbnailCount = Integer.parseInt((String)params.get(THUMBNAILS_PARAM));
        }
        catch (NumberFormatException e)
        {
        }

        int imageSize = 640;
        try
        {
            imageSize = Integer.parseInt((String)params.get(IMAGESIZE_PARAM));
        }
        catch (NumberFormatException e)
        {
        }

        if (picasaUser == null)
        {
            throw new MacroException("No user specified");
        }

        HttpServletRequest request = ServletActionContext.getRequest();
        String albumId = null;
        String photoId = null;
        int pageIndex = 0;
        int pageId = 0;
        boolean flushCache = false;
        if (request != null)
        {
            albumId = request.getParameter("album");
            photoId = request.getParameter("photo");

            try
            {
                pageIndex = Integer.parseInt(request.getParameter("page"))-1;
            }
            catch (NumberFormatException e)
            {
            }

            try
            {
                pageId = Integer.parseInt(request.getParameter("pageId"));
            }
            catch (NumberFormatException e)
            {
            }

            try
            {
                flushCache = Boolean.parseBoolean(request.getParameter("flush-cache"));
            }
            catch (NumberFormatException e)
            {
            }
        }

        CacheFactory cacheFactory = (CacheFactory)ContainerManager.getComponent("cacheManager");
        Cache cache = cacheFactory.getCache("se.microcode.confluence.plugin.picasa");
        if (flushCache)
        {
            cache.removeAll();
        }

        UserFeed userFeed;
        try
        {
            userFeed = PicasaHelper.getUserFeed(picasaUser, cache);
        }
        catch (IOException e)
        {
            throw new MacroException("Failed to retrieve user feed");
        }

        AlbumFeed albumFeed = null;
        AlbumEntry albumEntry = null;
        if (albumId != null && (userFeed.albums != null))
        {
            for (AlbumEntry entry : userFeed.albums)
            {
                if (entry.id.equals(albumId))
                {
                    albumEntry = entry;
                    break;
                }
            }

            if (albumEntry != null)
            {
                try
                {
                    albumFeed = PicasaHelper.getAlbumFeed(picasaUser, albumEntry.id, cache);
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
        if (photoId != null && albumFeed != null && albumFeed.photos != null)
        {
            for (int i = 0, n = albumFeed.photos.size(); i != n; ++i)
            {
                PhotoEntry photo = albumFeed.photos.get(i);
                if (photoId.equals(photo.id))
                {
                    int begin = (int)Math.max(0, i - Math.floor(thumbnailCount/2.0f));
                    int end = (int)Math.min(albumFeed.photos.size(), i + Math.ceil(thumbnailCount/2.0f));

                    photoEntry = photo;
                    photoIndex = i;
                    thumbnails = albumFeed.photos.subList(begin, end);
                    break;
                }
            }
        }

        StringBuilder builder = new StringBuilder();
        Map context = MacroUtils.defaultVelocityContext();

        if (pageId > 0)
        {
            context.put("baseUrl", "?pageId=" + pageId + "&");
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

            context.put("photo", GalleryHelper.buildPhotoEntry(photoEntry, imageSize));
            context.put("album", GalleryHelper.buildAlbumEntry(albumEntry));
            context.put("pageId", pageId);

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
            if (maxEntries > 0)
            {
                context.put("currPage", pageIndex+1);
                context.put("pageCount", (end+maxEntries-1) / maxEntries);

                begin = Math.min(end, pageIndex * maxEntries);
                end = Math.min(end, begin + maxEntries);
            }

            context.put("photos", GalleryHelper.buildPhotoList(albumFeed.photos.subList(begin, end), 1));
            context.put("album", GalleryHelper.buildAlbumEntry(albumEntry));
            context.put("pageId", pageId);

            builder.append(VelocityUtils.getRenderedTemplate("/se/microcode/google-plugin/picasa/photos.vm", context));
        }
        else if (userFeed.albums != null)
        {
            int begin = 0;
            int end = userFeed.albums.size();
            if (maxEntries > 0)
            {
                context.put("currPage", Integer.toString(pageIndex+1));
                context.put("pageCount", (end+maxEntries-1) / maxEntries);

                begin = Math.min(end, pageIndex * maxEntries);
                end = Math.min(end, begin + maxEntries);
            }

            context.put("albums", GalleryHelper.buildAlbumList(userFeed.albums.subList(begin ,end)));
            context.put("pageId", pageId);

            builder.append(VelocityUtils.getRenderedTemplate("/se/microcode/google-plugin/picasa/albums.vm", context));
        }

        builder.append(PluginHelper.createCssFix(webResourceManager, "se.microcode.confluence.plugin.google-plugin:picasa-gallery-resources"));

        return builder.toString();
    }
}
