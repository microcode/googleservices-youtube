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

package se.microcode.confluence.plugin.googleservices.base.youtube;

import com.atlassian.cache.Cache;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.renderer.PageContext;
import com.atlassian.confluence.renderer.radeox.macros.MacroUtils;
import com.atlassian.confluence.util.velocity.VelocityUtils;
import com.atlassian.renderer.v2.macro.MacroException;
import se.microcode.confluence.plugin.googleservices.PluginHelper;
import se.microcode.google.youtube.*;

import java.io.IOException;
import java.util.*;

public class PlaylistHelper
{
    public static List<Map> buildPlaylists(List<PlaylistEntry> playlists)
    {
        ArrayList<Map> output = new ArrayList<Map>();

        for (PlaylistEntry playlist : playlists)
        {
            output.add(buildPlaylist(playlist));
        }

        return output;
    }

    public static Map buildPlaylist(PlaylistEntry playlist)
    {
        HashMap<String,String> entry = new HashMap<String,String>();
        PlaylistSummary summary = new PlaylistSummary(playlist.summary);

        entry.put("id", playlist.id);
        entry.put("title", playlist.title);
        entry.put("count", Integer.toString(playlist.countHint));
        entry.put("image", summary.cover);
        entry.put("desc", summary.text);

        return entry;
    }

    public static List buildVideoList(List<VideoEntry> videos)
    {
        ArrayList<Map> output = new ArrayList<Map>();

        for (VideoEntry video : videos)
        {
            output.add(buildVideoEntry(video));
        }

        return output;
    }

    public static Map buildVideoEntry(VideoEntry video)
    {
        HashMap<String,String> entry = new HashMap<String,String>();

        entry.put("id", video.group.id);
        entry.put("title", video.title);
        entry.put("image", "http://i.ytimg.com/vi/" + video.group.id + "/default.jpg");
        entry.put("desc", video.group.description);
        entry.put("credit", video.group.credit);

        return entry;
    }

    public static void retrievePlaylists(PlaylistMacroArguments args, PlaylistContext playlistContext) throws IOException
    {
        Cache cache = PluginHelper.getCache("se.microcode.confluence.plugin.googleservices.youtube");
        playlistContext.playlistFeed = YoutubeHelper.getPlaylistFeed(args.user, cache);

        if ((args.playlist != null) && (playlistContext.playlistFeed.playlists != null))
        {
            for (PlaylistEntry entry : playlistContext.playlistFeed.playlists)
            {
                if (entry.id.equals(args.playlist))
                {
                    playlistContext.playlistEntry = entry;
                    break;
                }
            }

            if (playlistContext.playlistEntry != null)
            {
                playlistContext.videoFeed = YoutubeHelper.getVideoFeed(playlistContext.playlistEntry.id, cache);

                PlaylistSummary summary = new PlaylistSummary(playlistContext.playlistEntry.summary);
                summary.patchVideos(playlistContext.videoFeed.videos);
            }
        }

        if (args.video != null && (playlistContext.videoFeed != null) && (playlistContext.videoFeed.videos != null))
        {
            for (int i = 0, n = playlistContext.videoFeed.videos.size(); i != n; ++i)
            {
                VideoEntry video = playlistContext.videoFeed.videos.get(i);
                if (args.video.equals(video.group.id))
                {
                    int begin = (int)Math.max(0, i - Math.floor(args.thumbnails/2.0f));
                    int end = (int)Math.min(playlistContext.videoFeed.videos.size(), i + Math.ceil(args.thumbnails/2.0f));

                    playlistContext.videoEntry = video;
                    playlistContext.videoIndex = i;
                    playlistContext.thumbnails = playlistContext.videoFeed.videos.subList(begin, end);
                    break;
                }
            }
        }
    }

    public static void retrievePlaylistExcerpts(PlaylistExcerptsMacroArguments args, PlaylistContext playlistContext) throws IOException
    {
        Cache cache = PluginHelper.getCache("se.microcode.confluence.plugin.googleservices.youtube");

        playlistContext.playlistFeed = YoutubeHelper.getPlaylistFeed(args.user, cache);

        switch (args.display)
        {
            case VIDEOS:
            {
                if ((args.playlist != null) && (playlistContext.playlistFeed.playlists != null))
                {
                    for (PlaylistEntry entry : playlistContext.playlistFeed.playlists)
                    {
                        if (entry.id.equals(args.playlist))
                        {
                            playlistContext.playlistEntry = entry;
                            break;
                        }
                    }
                }
                else if ((playlistContext.playlistFeed.playlists != null) && (playlistContext.playlistFeed.playlists.size() > 0))
                {
                    playlistContext.playlistEntry = playlistContext.playlistFeed.playlists.get(playlistContext.playlistFeed.playlists.size()-1);
                }

                if (playlistContext.playlistEntry != null)
                {
                    playlistContext.videoFeed = YoutubeHelper.getVideoFeed(playlistContext.playlistEntry.id, cache);
                }
            }
            break;

            case PLAYLISTS:
            {

            }
            break;
        }
    }

    public static String renderPlaylists(PlaylistMacroArguments args, PageContext pageContext, PlaylistContext playlistContext)
    {
        Map context = MacroUtils.defaultVelocityContext();

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

        if (playlistContext.videoEntry != null)
        {
            context.put("currPage", playlistContext.videoIndex + 1);
            context.put("pageCount", playlistContext.videoFeed.videos.size());

            if (playlistContext.videoIndex > 0)
            {
                context.put("prev", playlistContext.videoFeed.videos.get(playlistContext.videoIndex-1).group.id);
            }
            if (playlistContext.videoIndex < playlistContext.videoFeed.videos.size()-1)
            {
                context.put("next", playlistContext.videoFeed.videos.get(playlistContext.videoIndex+1).group.id);
            }

            context.put("video", PlaylistHelper.buildVideoEntry(playlistContext.videoEntry));
            context.put("playlist", PlaylistHelper.buildPlaylist(playlistContext.playlistEntry));

            if (playlistContext.thumbnails != null)
            {
                context.put("thumbnails", PlaylistHelper.buildVideoList(playlistContext.thumbnails));
            }

            return VelocityUtils.getRenderedTemplate("/se/microcode/confluence/plugin/googleservices/youtube/video.vm", context);
        }
        else if ((playlistContext.videoFeed != null) && (playlistContext.videoFeed.videos != null))
        {
            int begin = 0;
            int end = playlistContext.videoFeed.videos.size();
            if (args.pageSize > 0)
            {
                context.put("currPage", args.pageIndex +1);
                context.put("pageCount", (end+args.pageSize-1) / args.pageSize);

                begin = Math.min(end, args.pageIndex * args.pageSize);
                end = Math.min(end, begin + args.pageSize);
            }

            context.put("videos", PlaylistHelper.buildVideoList(playlistContext.videoFeed.videos.subList(begin, end)));
            context.put("playlist", PlaylistHelper.buildPlaylist(playlistContext.playlistEntry));

            return VelocityUtils.getRenderedTemplate("/se/microcode/confluence/plugin/googleservices/youtube/playlist.vm", context);
        }
        else if (playlistContext.playlistFeed.playlists != null)
        {
            ArrayList<PlaylistEntry> playlists = new ArrayList<PlaylistEntry>(playlistContext.playlistFeed.playlists);

            int begin = 0;
            int end = playlists.size();

            Comparator<PlaylistEntry> comparator = PlaylistSorter.createSorter(args.sort);
            if (comparator != null)
            {
                Collections.sort(playlists, comparator);
            }

            if (args.reverse)
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

            return VelocityUtils.getRenderedTemplate("/se/microcode/confluence/plugin/googleservices/youtube/playlists.vm", context);
        }

        return "";
    }

    public static String renderPlaylistExcerpts(PlaylistExcerptsMacroArguments args, String url, PageContext pageContext, PlaylistContext playlistContext)
    {
        switch (args.display)
        {
            case VIDEOS:
            {
                if ((playlistContext.videoFeed != null) && (playlistContext.videoFeed.videos != null))
                {
                    Map context = MacroUtils.defaultVelocityContext();
                    List<VideoEntry> videos = new ArrayList<VideoEntry>(playlistContext.videoFeed.videos);
                    int begin = 0;
                    int count = 0;
                    int end = (int)Math.min(videos.size(), 0 + (args.maxEntries - count));

                    if (args.reverse)
                    {
                        Collections.reverse(videos);
                    }
                    
                    if (args.randomize)
                    {
                        Collections.shuffle(videos);
                    }

                    context.put("videos", PlaylistHelper.buildVideoList(videos.subList(begin, end)));
                    context.put("playlist", PlaylistHelper.buildPlaylist(playlistContext.playlistEntry));
                    context.put("url", url);

                    return VelocityUtils.getRenderedTemplate("/se/microcode/confluence/plugin/googleservices/youtube/playlist-excerpts.vm", context);
                }
            }
            break;

            case PLAYLISTS:
            {
                if ((playlistContext.playlistFeed != null) && (playlistContext.playlistFeed.playlists != null))
                {
                    Map context = MacroUtils.defaultVelocityContext();
                    List<PlaylistEntry> playlists = new ArrayList<PlaylistEntry>(playlistContext.playlistFeed.playlists);
                    int begin = 0;
                    int count = 0;
                    int end = (int)Math.min(playlists.size(), 0 + (args.maxEntries - count));

                    Comparator<PlaylistEntry> comparator = PlaylistSorter.createSorter(args.sort);
                    if (comparator != null)
                    {
                        Collections.sort(playlists, comparator);
                    }
                    
                    if (args.reverse)
                    {
                        Collections.reverse(playlists);
                    }

                    if (args.randomize)
                    {
                        Collections.shuffle(playlists);
                    }

                    context.put("playlists", PlaylistHelper.buildPlaylists(playlists.subList(begin, end)));
                    context.put("url", url);

                    return VelocityUtils.getRenderedTemplate("/se/microcode/confluence/plugin/googleservices/youtube/playlists-excerpts.vm", context);
                }
            }
            break;
        }

        return "";
    }
}
