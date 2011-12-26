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

package se.microcode.confluence.plugin.googleservices.base.picasa;

import com.atlassian.cache.Cache;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.renderer.PageContext;
import com.atlassian.confluence.renderer.radeox.macros.MacroUtils;
import com.atlassian.confluence.util.velocity.VelocityUtils;
import se.microcode.confluence.plugin.googleservices.PluginHelper;
import se.microcode.google.picasa.*;

import java.io.IOException;
import java.util.*;

public class GalleryHelper
{
    public static List<Map> buildPhotoList(List<PhotoEntry> photos, boolean useThumbnail, int thumbIndex)
    {
        ArrayList<Map> output = new ArrayList<Map>();

        for(PhotoEntry photo : photos)
        {
            output.add(buildPhotoEntry(photo, useThumbnail, thumbIndex));
        }
        return output;
    }

    public static HashMap<String,String> buildPhotoEntry(PhotoEntry photo, boolean useThumbnail, int thumbIndex)
    {
        HashMap<String,String> entry = new HashMap<String,String>();

        if (useThumbnail)
        {
            Thumbnail thumb = photo.group.thumbnail.get(thumbIndex);
            entry.put("image", thumb.url);
            entry.put("width", Integer.toString(thumb.width));
            entry.put("height", Integer.toString(thumb.height));
        }
        else
        {
            entry.put("image", photo.group.content.url);
            entry.put("width", Integer.toString(photo.group.content.width));
            entry.put("height", Integer.toString(photo.group.content.height));
        }

        entry.put("title", photo.title);
        entry.put("id", photo.id);
        return entry;
    }

    public static List<Map> buildAlbumList(List<AlbumEntry> albums)
    {
        ArrayList<Map> output = new ArrayList<Map>();

        for (AlbumEntry album : albums)
        {
            output.add(buildAlbumEntry(album));
        }
        return output;
    }

    public static HashMap<String,String> buildAlbumEntry(AlbumEntry album)
    {
        HashMap<String,String> entry = new HashMap<String,String>();
        entry.put("image", album.group.thumbnail.get(0).url);
        entry.put("title", album.title);
        entry.put("id", album.id);
        entry.put("count", Integer.toString(album.numPhotos));
        return entry;
    }

    public static void retrieveGallery(GalleryMacroArguments args, GalleryContext galleryContext) throws IOException
    {
        Cache cache = PluginHelper.getCache("se.microcode.confluence.plugin.googleservices.picasa");
        galleryContext.userFeed = PicasaHelper.getUserFeed(args.user, cache);

        if (args.album != null && (galleryContext.userFeed.albums != null))
         {
             for (AlbumEntry entry : galleryContext.userFeed.albums)
             {
                 if (entry.id.equals(args.album))
                 {
                     galleryContext.albumEntry = entry;
                     break;
                 }
             }

             if (galleryContext.albumEntry != null)
             {
                 galleryContext.albumFeed = PicasaHelper.getAlbumFeed(args.user, galleryContext.albumEntry.id, Integer.toString(args.imageSize), args.albumSize + "," + args.thumbSize, cache);
             }
         }

        if (args.photo != null && galleryContext.albumFeed != null && galleryContext.albumFeed.photos != null)
        {
            for (int i = 0, n = galleryContext.albumFeed.photos.size(); i != n; ++i)
            {
                PhotoEntry photo = galleryContext.albumFeed.photos.get(i);
                if (args.photo.equals(photo.id))
                {
                    int begin = (int)Math.max(0, i - Math.floor(args.thumbnails/2.0f));
                    int end = (int)Math.min(galleryContext.albumFeed.photos.size(), i + Math.ceil(args.thumbnails/2.0f));

                    galleryContext.photoEntry = photo;
                    galleryContext.photoIndex = i;
                    galleryContext.thumbnails = galleryContext.albumFeed.photos.subList(begin, end);
                    break;
                }
            }
        }
    }

    public static void retrieveGalleryExcerpts(GalleryExcerptsMacroArguments args, GalleryContext galleryContext) throws IOException
    {
        Cache cache = PluginHelper.getCache("se.microcode.confluence.plugin.googleservices.picasa");

        galleryContext.userFeed = PicasaHelper.getUserFeed(args.user, cache);

        switch (args.display)
        {
            case PHOTOS:
            {
                if (args.album != null && (galleryContext.userFeed.albums != null))
                {
                    for (AlbumEntry entry : galleryContext.userFeed.albums)
                    {
                        if (entry.id.equals(args.album))
                        {
                            galleryContext.albumEntry = entry;
                            break;
                        }
                    }
                }
                else if (galleryContext.userFeed.albums != null)
                {
                    galleryContext.albumEntry = galleryContext.userFeed.albums.get(0);
                }

                if (galleryContext.albumEntry != null)
                {
                    galleryContext.albumFeed = PicasaHelper.getAlbumFeed(args.user, galleryContext.albumEntry.id, args.imageSize, null, cache);
                }

                if ((args.photo != null) && (galleryContext.albumFeed != null))
                {
                    for (int i = 0, n = galleryContext.albumFeed.photos.size(); i != n; ++i)
                    {
                        PhotoEntry photo = galleryContext.albumFeed.photos.get(i);
                        if (args.photo.equals(photo.id))
                        {
                            galleryContext.photoIndex = i;
                            break;
                        }
                    }
                }
            }
            break;

            case ALBUMS:
            {
                if ((args.album != null) && (galleryContext.userFeed != null))
                {
                    for (int i = 0, n = galleryContext.userFeed.albums.size(); i != n; ++i)
                    {
                        AlbumEntry album = galleryContext.userFeed.albums.get(i);
                        if (args.album.equals(album.id))
                        {
                            galleryContext.albumIndex = i;
                            break;
                        }
                    }
                }
            }
            break;
        }
    }

    private static void fillVelocityContext(GalleryMacroArguments args, PageContext pageContext, Map velocityContext)
    {
        if (args.pageId > 0)
        {
            velocityContext.put("baseUrl", "?pageId=" + args.pageId + "&");
        }
        else
        {
            velocityContext.put("baseUrl", "?");
        }

        Page page = (pageContext.getEntity() != null && pageContext.getEntity() instanceof Page) ? (Page)pageContext.getEntity() : null;
        if (page != null)
        {
            velocityContext.put("title", page.getTitle());
        }
    }

    public static String renderGallery(GalleryMacroArguments args, PageContext pageContext, GalleryContext galleryContext)
    {
        if (galleryContext.photoEntry != null)
        {
            return GalleryHelper.renderPhoto(args, pageContext, galleryContext);
        }
        else if (galleryContext.albumFeed != null && galleryContext.albumFeed.photos != null)
        {
            return GalleryHelper.renderAlbum(args, pageContext, galleryContext);
        }
        else if (galleryContext.userFeed != null && galleryContext.userFeed.albums != null)
        {
            return GalleryHelper.renderAlbums(args, pageContext, galleryContext);
        }
        return "";
    }

    public static String renderGalleryExcerpts(GalleryExcerptsMacroArguments args, String url, PageContext pageContext, GalleryContext galleryContext)
    {
        switch (args.display)
        {
            case PHOTOS:
            {
                if ((galleryContext.albumFeed != null) && galleryContext.albumFeed.photos != null)
                {
                    Map context = MacroUtils.defaultVelocityContext();
                    List<PhotoEntry> photos = new ArrayList<PhotoEntry>(galleryContext.albumFeed.photos);
                    int begin = (int)Math.max(0, galleryContext.photoIndex - Math.floor(args.maxEntries/2.0f));
                    int count = galleryContext.photoIndex - begin;
                    int end = (int)Math.min(photos.size(), galleryContext.photoIndex + (args.maxEntries - count));

                    if (args.randomize)
                    {
                        Collections.shuffle(photos);
                    }

                    List<Map> photoList = GalleryHelper.buildPhotoList(photos.subList(begin,end), false, 0);

                    context.put("photos", photoList);
                    context.put("album", GalleryHelper.buildAlbumEntry(galleryContext.albumEntry));
                    if (url != null)
                    {
                        context.put("url", url);
                    }

                    return VelocityUtils.getRenderedTemplate("/se/microcode/confluence/plugin/googleservices/picasa/photos-excerpts.vm", context);

                }
            }
            break;

            case ALBUMS:
            {
                if ((galleryContext.userFeed != null) && (galleryContext.userFeed.albums != null))
                {
                    Map context = MacroUtils.defaultVelocityContext();
                    List<AlbumEntry> albums = new ArrayList<AlbumEntry>(galleryContext.userFeed.albums);
                    int begin = (int)Math.max(0, galleryContext.albumIndex - Math.floor(args.maxEntries / 2.0f));
                    int count = galleryContext.albumIndex - begin;
                    int end = (int)Math.min(albums.size(), galleryContext.albumIndex + (args.maxEntries - count));

                    if (args.randomize)
                    {
                        Collections.shuffle(albums);
                    }

                    List<Map> albumList = GalleryHelper.buildAlbumList(albums.subList(begin, end));

                    context.put("albums", albumList);
                    if (url != null)
                    {
                        context.put("url", url);
                    }

                    return VelocityUtils.getRenderedTemplate("/se/microcode/confluence/plugin/googleservices/picasa/albums-excerpts.vm", context);
                }

            }
            break;
        }

        return "";
    }

    public static String renderPhoto(GalleryMacroArguments args, PageContext pageContext, GalleryContext galleryContext)
    {
        Map velocityContext = MacroUtils.defaultVelocityContext();
        fillVelocityContext(args, pageContext, velocityContext);

        velocityContext.put("currPage", galleryContext.photoIndex + 1);
        velocityContext.put("pageCount", galleryContext.albumFeed.photos.size());

        if (galleryContext.photoIndex > 0)
        {
            velocityContext.put("prev", galleryContext.albumFeed.photos.get(galleryContext.photoIndex-1).id);
        }
        if (galleryContext.photoIndex < galleryContext.albumFeed.photos.size()-1)
        {
            velocityContext.put("next", galleryContext.albumFeed.photos.get(galleryContext.photoIndex+1).id);
        }

        velocityContext.put("photo", GalleryHelper.buildPhotoEntry(galleryContext.photoEntry, false, 0));
        velocityContext.put("album", GalleryHelper.buildAlbumEntry(galleryContext.albumEntry));
        velocityContext.put("pageId", args.pageId);

        if (galleryContext.thumbnails != null)
        {
            velocityContext.put("thumbnails", GalleryHelper.buildPhotoList(galleryContext.thumbnails, true, 1));
        }

        return VelocityUtils.getRenderedTemplate("/se/microcode/confluence/plugin/googleservices/picasa/photo.vm", velocityContext);
    }

    public static String renderAlbum(GalleryMacroArguments args, PageContext pageContext, GalleryContext galleryContext)
    {
        Map velocityContext = MacroUtils.defaultVelocityContext();
        fillVelocityContext(args, pageContext, velocityContext);

        int begin = 0;
        int end = galleryContext.albumFeed.photos.size();
        if (args.pageSize > 0)
        {
            velocityContext.put("currPage", args.page+1);
            velocityContext.put("pageCount", (end+args.pageSize-1) / args.pageSize);

            begin = Math.min(end, args.page * args.pageSize);
            end = Math.min(end, begin + args.pageSize);
        }

        velocityContext.put("photos", GalleryHelper.buildPhotoList(galleryContext.albumFeed.photos.subList(begin, end), true, 0));
        velocityContext.put("album", GalleryHelper.buildAlbumEntry(galleryContext.albumEntry));
        velocityContext.put("pageId", args.pageId);

        return VelocityUtils.getRenderedTemplate("/se/microcode/confluence/plugin/googleservices/picasa/photos.vm", velocityContext);
    }

    public static String renderAlbums(GalleryMacroArguments args, PageContext pageContext, GalleryContext galleryContext)
    {
        Map velocityContext = MacroUtils.defaultVelocityContext();
        fillVelocityContext(args, pageContext, velocityContext);

        int begin = 0;
        int end = galleryContext.userFeed.albums.size();
        if (args.pageSize > 0)
        {
          velocityContext.put("currPage", Integer.toString(args.page+1));
          velocityContext.put("pageCount", (end+args.pageSize-1) / args.pageSize);

          begin = Math.min(end, args.page * args.pageSize);
          end = Math.min(end, begin + args.pageSize);
        }

        velocityContext.put("albums", GalleryHelper.buildAlbumList(galleryContext.userFeed.albums.subList(begin, end)));
        velocityContext.put("pageId", args.pageId);

        return VelocityUtils.getRenderedTemplate("/se/microcode/confluence/plugin/googleservices/picasa/albums.vm", velocityContext);
    }

}
