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

package se.microcode.confluence.plugin.youtube;

import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.renderer.PageContext;
import com.atlassian.renderer.v2.macro.BaseMacro;
import com.atlassian.renderer.v2.macro.MacroException;
import com.atlassian.renderer.v2.RenderMode;

import com.atlassian.renderer.RenderContext;
import com.atlassian.cache.CacheFactory;
import com.atlassian.cache.Cache;

import com.atlassian.confluence.renderer.radeox.macros.MacroUtils;
import com.atlassian.confluence.util.velocity.VelocityUtils;

import com.atlassian.spring.container.ContainerManager;

import com.opensymphony.webwork.ServletActionContext;
import javax.servlet.http.HttpServletRequest;

import se.microcode.google.youtube.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.List;
import java.lang.Math;
import java.io.IOException;
import java.lang.NumberFormatException;
import java.lang.Integer;

public class PlaylistMacro extends BaseMacro
{
    public RenderMode getBodyRenderMode()
    {
        return RenderMode.NO_RENDER;
    }

    public boolean hasBody()
    {
        return false;
    }

    public static final String USER_PARAM = "user";
    public static final String MAXENTRIES_PARAM = "maxEntries";
    public static final String THUMBNAILS_PARAM = "thumbnails";
    public static final String REVERSE_PARAM = "reverse";

    public String execute(Map params, String body, RenderContext renderContext) throws MacroException
    {
        String user = (String)params.get(USER_PARAM);
        if (user == null)
        {
            throw new MacroException("No user specified");
        }

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

        boolean reverse = false;
        try
        {
            reverse = Boolean.parseBoolean((String)params.get(REVERSE_PARAM));
        }
        catch (NumberFormatException e)
        {
        }

        HttpServletRequest request = ServletActionContext.getRequest();
        String playlistId = null;
        String videoId = null;
        int pageIndex = 0;
        int pageId = 0;
        boolean flushCache = false;
        if (request != null)
        {
            playlistId = request.getParameter("playlist");
            videoId = request.getParameter("video");

            try
            {
                pageIndex = Integer.parseInt(request.getParameter("pageIndex"))-1;
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
        Cache cache = cacheFactory.getCache("se.microcode.confluence.plugin.youtube");
        if (flushCache)
        {
            cache.removeAll();
        }

        PlaylistsFeed playlistsFeed;
        try
        {
            playlistsFeed = YoutubeHelper.getPlaylistsFeed(user, cache);
        }
        catch (IOException e)
        {
            throw new MacroException("Failed to retrieve playlists");
        }

        VideoFeed videoFeed = null;
        PlaylistEntry playlistEntry = null;
        if ((playlistId != null) && (playlistsFeed.playlists != null))
        {
            for (PlaylistEntry entry : playlistsFeed.playlists)
            {
                if (entry.id.equals(playlistId))
                {
                    playlistEntry = entry;
                    break;
                }
            }

            if (playlistEntry != null)
            {
                try
                {
                    videoFeed = YoutubeHelper.getPlaylistFeed(playlistEntry.id, cache);

                    PlaylistSummary summary = new PlaylistSummary(playlistEntry.summary);
                    summary.patchVideos(videoFeed.videos);
                }
                catch (IOException e)
                {
                    throw new MacroException("Failed to retrieve video feed");
                }
            }
        }

        VideoEntry videoEntry = null;
        List<VideoEntry> thumbnails = null;
        int videoIndex = 0;
        if (videoId != null && (videoFeed != null) && (videoFeed.videos != null))
        {
            for (int i = 0, n = videoFeed.videos.size(); i != n; ++i)
            {
                VideoEntry video = videoFeed.videos.get(i);
                if (videoId.equals(video.group.id))
                {
                    int begin = (int)Math.max(0, i - Math.floor(thumbnailCount/2.0f));
                    int end = (int)Math.min(videoFeed.videos.size(), i + Math.ceil(thumbnailCount/2.0f));

                    videoEntry = video;
                    videoIndex = i;
                    thumbnails = videoFeed.videos.subList(begin, end);
                    break;
                }
            }
        }

        StringBuilder builder = new StringBuilder();
        Map context = MacroUtils.defaultVelocityContext();
        PageContext pageContext = (PageContext)renderContext;

        Page page = (pageContext.getEntity() != null && pageContext.getEntity() instanceof Page) ? (Page)pageContext.getEntity() : null;
        if (page != null)
        {
            context.put("title", page.getTitle());
        }

        if (pageId > 0)
        {
            context.put("baseUrl", "?pageId=" + pageId + "&");
        }
        else
        {
            context.put("baseUrl", "?");
        }

        if (videoEntry != null)
        {
            context.put("currPage", videoIndex + 1);
            context.put("pageCount", videoFeed.videos.size());

            if (videoIndex > 0)
            {
                context.put("prev", videoFeed.videos.get(videoIndex-1).group.id);
            }
            if (videoIndex < videoFeed.videos.size()-1)
            {
                context.put("next", videoFeed.videos.get(videoIndex+1).group.id);
            }

            context.put("video", PlaylistHelper.buildVideoEntry(videoEntry));
            context.put("playlist", PlaylistHelper.buildPlaylist(playlistEntry));

            if (thumbnails != null)
            {
                context.put("thumbnails", PlaylistHelper.buildVideoList(thumbnails));
            }

            builder.append(VelocityUtils.getRenderedTemplate("/se/microcode/google-plugin/youtube/video.vm", context));
        }
        else if ((videoFeed != null) && (videoFeed.videos != null))
        {
            int begin = 0;
            int end = videoFeed.videos.size();
            if (maxEntries > 0)
            {
                context.put("currPage", pageIndex +1);
                context.put("pageCount", (end+maxEntries-1) / maxEntries);

                begin = Math.min(end, pageIndex * maxEntries);
                end = Math.min(end, begin + maxEntries);
            }

            context.put("videos", PlaylistHelper.buildVideoList(videoFeed.videos.subList(begin, end)));
            context.put("playlist", PlaylistHelper.buildPlaylist(playlistEntry));

            builder.append(VelocityUtils.getRenderedTemplate("/se/microcode/google-plugin/youtube/playlist.vm", context));
        }
        else if (playlistsFeed.playlists != null)
        {
            ArrayList<PlaylistEntry> playlists = new ArrayList<PlaylistEntry>(playlistsFeed.playlists);

            int begin = 0;
            int end = playlists.size();

            if (!reverse)
            {
                Collections.reverse(playlists);
            }

            if (maxEntries > 0)
            {
                context.put("currPage", Integer.toString(pageIndex +1));
                context.put("pageCount", (end+maxEntries-1) / maxEntries);

                begin = Math.min(end, pageIndex * maxEntries);
                end = Math.min(end, begin + maxEntries);
            }

            context.put("playlists", PlaylistHelper.buildPlaylists(playlists.subList(begin ,end)));

            builder.append(VelocityUtils.getRenderedTemplate("/se/microcode/google-plugin/youtube/playlists.vm", context));
        }

        return builder.toString();
    }
}
