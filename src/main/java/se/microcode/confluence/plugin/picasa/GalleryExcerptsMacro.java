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
import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheFactory;
import com.atlassian.confluence.renderer.radeox.macros.MacroUtils;
import com.atlassian.confluence.util.velocity.VelocityUtils;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.renderer.PageContext;
import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.v2.macro.MacroException;
import com.atlassian.spring.container.ContainerManager;
import com.opensymphony.webwork.ServletActionContext;
import se.microcode.confluence.plugin.PluginHelper;
import se.microcode.google.picasa.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.lang.Math;
import java.io.IOException;
import java.lang.NumberFormatException;
import java.lang.Integer;

public class GalleryExcerptsMacro extends BaseMacro
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

    public static final String PICASAUSER_PARAM = "user";
    public static final String ALBUM_PARAM = "album";
    public static final String MAXENTRIES_PARAM = "maxEntries";
    public static final String THUMBSIZE_PARAM = "thumbSize";
    public static final String PHOTO_PARAM = "photo";
    public static final String RANDOMIZE_PARAM = "randomize";
    public static final String PAGE_PARAM = "page";
    public static final String DISPLAY_PARAM = "display";

    public GalleryExcerptsMacro(SettingsManager settingsManager, WebResourceManager webResourceManager)
    {
        this.settingsManager = settingsManager;
        this.webResourceManager = webResourceManager;
    }

    public String execute(Map params, String body, RenderContext renderContext) throws MacroException
    {
        String picasaUser = (String)params.get(PICASAUSER_PARAM);
        String albumId = (String)params.get(ALBUM_PARAM);
        String photoId = (String)params.get(PHOTO_PARAM);
        String displayType = (String)params.get(DISPLAY_PARAM);
        String url = null;
        String pageKey = (String)params.get(PAGE_PARAM);
        if (pageKey != null)
        {
            PageManager pageManager = (PageManager) ContainerManager.getComponent("pageManager");
            Page page = null;

            if (pageKey.indexOf(':') >= 0)
            {
                page = pageManager.getPage(pageKey.substring(0, pageKey.indexOf(':')), pageKey.substring(pageKey.indexOf(':')+1));
            }
            else
            {
                PageContext pageContext = (PageContext)renderContext;
                page = pageManager.getPage(pageContext.getSpaceKey(), pageKey);
            }

            if (page != null)
            {
                url = settingsManager.getGlobalSettings().getBaseUrl() + page.getUrlPath();
            }
        }

        int maxEntries = 5;
        try
        {
            maxEntries = Integer.parseInt((String)params.get(MAXENTRIES_PARAM));
        }
        catch (NumberFormatException e)
        {
        }
        if (maxEntries == -1)
        {
            throw new MacroException("A maximum number of photos must be provided (maxEntries)");
        }

        int thumbSize = 0;
        try
        {
            thumbSize = Integer.parseInt((String)params.get(THUMBSIZE_PARAM));
        }
        catch (NumberFormatException e)
        {
        }

        boolean randomize = false;
        try
        {
            randomize = Boolean.parseBoolean((String)params.get(RANDOMIZE_PARAM));
        }
        catch (NumberFormatException e)
        {
        }

        if (picasaUser == null)
        {
            throw new MacroException("No user specified");
        }

        HttpServletRequest request = ServletActionContext.getRequest();
        boolean flushCache = false;
        if (request != null)
        {
            try
            {
                flushCache = Boolean.parseBoolean((String)request.getParameter("flush-cache"));
            }
            catch (NumberFormatException e)
            {
            }
        }

        CacheFactory cacheFactory = (CacheFactory) ContainerManager.getComponent("cacheManager");
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

        StringBuilder builder = new StringBuilder();


        if ("photos".equals(displayType) || displayType == null)
        {
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
            }
            else if (userFeed.albums != null)
            {
                albumEntry = userFeed.albums.get(0);
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

            int photoIndex = 0;
            if ((photoId != null) && (albumFeed != null))
            {
                for (int i = 0, n = albumFeed.photos.size(); i != n; ++i)
                {
                    PhotoEntry photo = albumFeed.photos.get(i);
                    if (photoId.equals(photo.id))
                    {
                        photoIndex = i;
                        break;
                    }
                }
            }

            if ((albumFeed != null) && albumFeed.photos != null)
            {
                Map context = MacroUtils.defaultVelocityContext();
                List<PhotoEntry> photos = new ArrayList<PhotoEntry>(albumFeed.photos);
                int begin = (int)Math.max(0, photoIndex - Math.floor(maxEntries/2.0f));
                int count = photoIndex - begin;
                int end = (int)Math.min(photos.size(), photoIndex + (maxEntries - count));

                if (randomize)
                {
                    Collections.shuffle(photos);
                }

                List<Map> photoList = GalleryHelper.buildPhotoList(photos.subList(begin,end), thumbSize);

                context.put("photos", photoList);
                context.put("album", GalleryHelper.buildAlbumEntry(albumEntry));
                if (url != null)
                {
                    context.put("url", url);
                }

                builder.append(VelocityUtils.getRenderedTemplate("/se/microcode/google-plugin/picasa/photos-excerpts.vm", context));

            }
        }
        else if ("albums".equals(displayType))
        {
            int albumIndex = 0;
            if ((albumId != null) && (userFeed != null))
            {
                for (int i = 0, n = userFeed.albums.size(); i != n; ++i)
                {
                    AlbumEntry album = userFeed.albums.get(i);
                    if (albumId.equals(album.id))
                    {
                        albumIndex = i;
                        break;
                    }
                }
            }

            if ((userFeed != null) && (userFeed.albums != null))
            {
                Map context = MacroUtils.defaultVelocityContext();
                List<AlbumEntry> albums = new ArrayList<AlbumEntry>(userFeed.albums);
                int begin = (int)Math.max(0, albumIndex - Math.floor(maxEntries / 2.0f));
                int count = albumIndex - begin;
                int end = (int)Math.min(albums.size(), albumIndex + (maxEntries - count));

                if (randomize)
                {
                    Collections.shuffle(albums);
                }

                List<Map> albumList = GalleryHelper.buildAlbumList(albums.subList(begin, end));

                context.put("albums", albumList);
                if (url != null)
                {
                    context.put("url", url);
                }

                builder.append(VelocityUtils.getRenderedTemplate("/se/microcode/google-plugin/picasa/albums-excerpts.vm", context));
            }
        }

        builder.append(PluginHelper.createCssFix(webResourceManager, "se.microcode.confluence.plugin.google-plugin:picasa-gallery-resources"));

        return builder.toString();
    }
}
