package se.microcode.confluence.plugin.youtube;

import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheFactory;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.renderer.PageContext;
import com.atlassian.confluence.renderer.radeox.macros.MacroUtils;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.confluence.util.velocity.VelocityUtils;
import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.v2.macro.BaseMacro;
import com.atlassian.renderer.v2.macro.MacroException;
import com.atlassian.spring.container.ContainerManager;
import com.opensymphony.webwork.ServletActionContext;
import se.microcode.google.youtube.*;

import javax.servlet.http.HttpServletRequest;
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

    public PlaylistExcerptsMacro(SettingsManager settingsManager, PageManager pageManager)
    {
        this.settingsManager = settingsManager;
        this.pageManager = pageManager;
    }

    public static final String USER_PARAM = "user";
    public static final String PLAYLIST_PARAM = "playlist";
    public static final String MAXENTRIES_PARAM = "maxEntries";
    public static final String RANDOMIZE_PARAM = "randomize";
    public static final String PAGE_PARAM = "page";
    public static final String DISPLAY_PARAM = "display";

    public String execute(Map params, String body, RenderContext renderContext) throws MacroException
    {
        String user = (String)params.get(USER_PARAM);
        String playlistId = (String)params.get(PLAYLIST_PARAM);
        String pageKey = (String)params.get(PAGE_PARAM);
        String displayType = (String)params.get(DISPLAY_PARAM);

        if (user == null)
        {
            throw new MacroException("No user specified");
        }

        String url = null;
        if (pageKey != null)
        {
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

        boolean randomize = false;
        try
        {
            randomize = Boolean.parseBoolean((String)params.get(RANDOMIZE_PARAM));
        }
        catch (NumberFormatException e)
        {
        }

        HttpServletRequest request = ServletActionContext.getRequest();
        boolean flushCache = false;
        if (request != null)
        {
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

        StringBuilder builder = new StringBuilder();

        if ("playlist".equals(displayType) || displayType == null)
        {
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
            }
            else if ((playlistsFeed.playlists != null) && (playlistsFeed.playlists.size() > 0))
            {
                playlistEntry = playlistsFeed.playlists.get(playlistsFeed.playlists.size()-1);
            }

            VideoFeed videoFeed = null;
            if (playlistEntry != null)
            {
                try
                {
                    videoFeed = YoutubeHelper.getPlaylistFeed(playlistEntry.id, cache);
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
                int end = (int)Math.min(videos.size(), 0 + (maxEntries - count));

                if (randomize)
                {
                    Collections.shuffle(videos);
                }

                context.put("videos", PlaylistHelper.buildVideoList(videos.subList(begin, end)));
                context.put("playlist", PlaylistHelper.buildPlaylist(playlistEntry));
                context.put("url", url);

                builder.append(VelocityUtils.getRenderedTemplate("/se/microcode/google-plugin/youtube/playlist-excerpts.vm", context));
            }
        }
        else if ("playlists".equals(displayType))
        {
            if ((playlistsFeed != null) && (playlistsFeed.playlists != null))
            {
                Map context = MacroUtils.defaultVelocityContext();
                List<PlaylistEntry> playlists = new ArrayList<PlaylistEntry>(playlistsFeed.playlists);
                int begin = 0;
                int count = 0;
                int end = (int)Math.min(playlists.size(), 0 + (maxEntries - count));

                Collections.reverse(playlists);
                if (randomize)
                {
                    Collections.shuffle(playlists);
                }

                context.put("playlists", PlaylistHelper.buildPlaylists(playlists.subList(begin, end)));
                context.put("url", url);

                builder.append(VelocityUtils.getRenderedTemplate("/se/microcode/google-plugin/youtube/playlists-excerpts.vm", context));
            }
        }

        return builder.toString();
    }
}
