package se.microcode.confluence.plugin.wiki.youtube;

import com.atlassian.cache.Cache;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.renderer.PageContext;
import com.atlassian.confluence.renderer.radeox.macros.MacroUtils;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.confluence.util.velocity.VelocityUtils;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.v2.macro.BaseMacro;
import com.atlassian.renderer.v2.macro.MacroException;
import se.microcode.base.ArgumentParser;
import se.microcode.confluence.plugin.PluginHelper;
import se.microcode.confluence.plugin.base.youtube.PlaylistExcerptsMacroArguments;
import se.microcode.confluence.plugin.base.youtube.PlaylistHelper;
import se.microcode.google.youtube.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PlaylistExcerptsMacro extends BaseMacro
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
    private PageManager pageManager;
    private WebResourceManager webResourceManager;

    public PlaylistExcerptsMacro(SettingsManager settingsManager, PageManager pageManager, WebResourceManager webResourceManager)
    {
        this.settingsManager = settingsManager;
        this.pageManager = pageManager;
        this.webResourceManager = webResourceManager;
    }

    public static final String USER_PARAM = "user";
    public static final String PLAYLIST_PARAM = "playlist";
    public static final String MAXENTRIES_PARAM = "maxEntries";
    public static final String RANDOMIZE_PARAM = "randomize";
    public static final String PAGE_PARAM = "page";
    public static final String DISPLAY_PARAM = "display";

    public String execute(Map params, String body, RenderContext renderContext) throws MacroException
    {
        PlaylistExcerptsMacroArguments args = (PlaylistExcerptsMacroArguments) ArgumentParser.parse(new PlaylistExcerptsMacroArguments(), params);

        if (args.user == null)
        {
            throw new MacroException("No user specified");
        }

        String url = null;
        if (args.page != null)
        {
            Page page = null;

            if (args.page.indexOf(':') >= 0)
            {
                page = pageManager.getPage(args.page.substring(0, args.page.indexOf(':')), args.page.substring(args.page.indexOf(':')+1));
            }
            else
            {
                PageContext pageContext = (PageContext)renderContext;
                page = pageManager.getPage(pageContext.getSpaceKey(), args.page);
            }

            if (page != null)
            {
                url = settingsManager.getGlobalSettings().getBaseUrl() + page.getUrlPath();
            }
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

        StringBuilder builder = new StringBuilder();

        switch (args.display)
        {
            case PLAYLIST:
            {
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
                }
                else if ((playlistFeed.playlists != null) && (playlistFeed.playlists.size() > 0))
                {
                    playlistEntry = playlistFeed.playlists.get(playlistFeed.playlists.size()-1);
                }

                VideoFeed videoFeed = null;
                if (playlistEntry != null)
                {
                    try
                    {
                        videoFeed = YoutubeHelper.getVideoFeed(playlistEntry.id, cache);
                    }
                    catch (IOException e)
                    {
                        throw new MacroException("Failed to retrieve playlist feed");
                    }
                }

                if ((videoFeed != null) && (videoFeed.videos != null))
                {
                    Map context = MacroUtils.defaultVelocityContext();
                    List<VideoEntry> videos = new ArrayList<VideoEntry>(videoFeed.videos);
                    int begin = 0;
                    int count = 0;
                    int end = (int)Math.min(videos.size(), 0 + (args.maxEntries - count));

                    if (args.randomize)
                    {
                        Collections.shuffle(videos);
                    }

                    context.put("videos", PlaylistHelper.buildVideoList(videos.subList(begin, end)));
                    context.put("playlist", PlaylistHelper.buildPlaylist(playlistEntry));
                    context.put("url", url);

                    builder.append(VelocityUtils.getRenderedTemplate("/se/microcode/google-plugin/youtube/playlist-excerpts.vm", context));
                }
            }
            break;

            case PLAYLISTS:
            {
                if ((playlistFeed != null) && (playlistFeed.playlists != null))
                {
                    Map context = MacroUtils.defaultVelocityContext();
                    List<PlaylistEntry> playlists = new ArrayList<PlaylistEntry>(playlistFeed.playlists);
                    int begin = 0;
                    int count = 0;
                    int end = (int)Math.min(playlists.size(), 0 + (args.maxEntries - count));

                    Collections.reverse(playlists);
                    if (args.randomize)
                    {
                        Collections.shuffle(playlists);
                    }

                    context.put("playlists", PlaylistHelper.buildPlaylists(playlists.subList(begin, end)));
                    context.put("url", url);

                    builder.append(VelocityUtils.getRenderedTemplate("/se/microcode/google-plugin/youtube/playlists-excerpts.vm", context));
                }
            }
            break;
        }

        builder.append(PluginHelper.createCssFix(webResourceManager, "se.microcode.confluence.plugin.google-plugin:youtube-playlist-resources"));

        return builder.toString();
    }
}
