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

package se.microcode.confluence.plugin.wiki.youtube;

import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.renderer.PageContext;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.renderer.v2.macro.BaseMacro;
import com.atlassian.renderer.v2.macro.MacroException;
import com.atlassian.renderer.v2.RenderMode;

import com.atlassian.renderer.RenderContext;
import com.atlassian.cache.Cache;

import com.atlassian.confluence.renderer.radeox.macros.MacroUtils;
import com.atlassian.confluence.util.velocity.VelocityUtils;

import com.opensymphony.webwork.ServletActionContext;
import se.microcode.base.ArgumentParser;
import se.microcode.base.ArgumentResolver;
import se.microcode.confluence.plugin.PluginHelper;
import se.microcode.confluence.plugin.base.youtube.PlaylistHelper;
import se.microcode.confluence.plugin.base.youtube.PlaylistMacroArguments;
import se.microcode.confluence.plugin.base.youtube.PlaylistSummary;
import se.microcode.google.youtube.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.List;
import java.lang.Math;
import java.io.IOException;
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

    private WebResourceManager webResourceManager;

    public PlaylistMacro(WebResourceManager webResourceManager)
    {
        this.webResourceManager = webResourceManager;
    }

    public String execute(Map params, String body, RenderContext renderContext) throws MacroException
    {
        PlaylistMacroArguments args = (PlaylistMacroArguments) ArgumentParser.parse(new PlaylistMacroArguments(), params, new ArgumentResolver()
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

        Cache cache = PluginHelper.getCache("se.microcode.confluence.plugin.youtube");

        PlaylistFeed playlistFeed;
        try
        {
            playlistFeed = YoutubeHelper.getPlaylistFeed(args.user, cache);
        }
        catch (IOException e)
        {
            throw new MacroException("Failed to retrieve playlists");
        }

        VideoFeed videoFeed = null;
        PlaylistEntry playlistEntry = null;
        if ((args.playlist != null) && (playlistFeed.playlists != null))
        {
            for (PlaylistEntry entry : playlistFeed.playlists)
            {
                if (entry.id.equals(args.playlist))
                {
                    playlistEntry = entry;
                    break;
                }
            }

            if (playlistEntry != null)
            {
                try
                {
                    videoFeed = YoutubeHelper.getVideoFeed(playlistEntry.id, cache);

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
        if (args.video != null && (videoFeed != null) && (videoFeed.videos != null))
        {
            for (int i = 0, n = videoFeed.videos.size(); i != n; ++i)
            {
                VideoEntry video = videoFeed.videos.get(i);
                if (args.video.equals(video.group.id))
                {
                    int begin = (int)Math.max(0, i - Math.floor(args.thumbnails/2.0f));
                    int end = (int)Math.min(videoFeed.videos.size(), i + Math.ceil(args.thumbnails/2.0f));

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

        if (args.pageId > 0)
        {
            context.put("baseUrl", "?pageId=" + args.pageId + "&");
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
            if (args.pageSize > 0)
            {
                context.put("currPage", args.pageIndex +1);
                context.put("pageCount", (end+args.pageSize-1) / args.pageSize);

                begin = Math.min(end, args.pageIndex * args.pageSize);
                end = Math.min(end, begin + args.pageSize);
            }

            context.put("videos", PlaylistHelper.buildVideoList(videoFeed.videos.subList(begin, end)));
            context.put("playlist", PlaylistHelper.buildPlaylist(playlistEntry));

            builder.append(VelocityUtils.getRenderedTemplate("/se/microcode/google-plugin/youtube/playlist.vm", context));
        }
        else if (playlistFeed.playlists != null)
        {
            ArrayList<PlaylistEntry> playlists = new ArrayList<PlaylistEntry>(playlistFeed.playlists);

            int begin = 0;
            int end = playlists.size();

            if (!args.reverse)
            {
                Collections.reverse(playlists);
            }

            if (args.pageSize > 0)
            {
                context.put("currPage", Integer.toString(args.pageIndex +1));
                context.put("pageCount", (end+args.pageSize-1) / args.pageSize);

                begin = Math.min(end, args.pageIndex * args.pageSize);
                end = Math.min(end, begin + args.pageSize);
            }

            context.put("playlists", PlaylistHelper.buildPlaylists(playlists.subList(begin, end)));

            builder.append(VelocityUtils.getRenderedTemplate("/se/microcode/google-plugin/youtube/playlists.vm", context));
        }

//        builder.append(PluginHelper.createCssFix(webResourceManager, "se.microcode.confluence.plugin.google-plugin:youtube-playlist-resources"));

        return builder.toString();
    }
}
