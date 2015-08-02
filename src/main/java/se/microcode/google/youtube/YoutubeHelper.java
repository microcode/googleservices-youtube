package se.microcode.google.youtube;

import com.atlassian.cache.Cache;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class YoutubeHelper
{
    static YouTube youtube = new YouTube.Builder(
            new NetHttpTransport(),
            new JacksonFactory(),
            null
        ).setApplicationName("se.microcode.confluence.plugin.googleservices-youtube/1.1.0.0").build();

    static String key = "";

    public static PlaylistFeed getPlaylistFeed(String user, Cache cache) throws IOException
    {
        List<PlaylistEntry> entries = new ArrayList<PlaylistEntry>();

        String pageToken = null;
        do {
            YouTube.Playlists.List request = youtube.playlists().list("snippet,contentDetails");

            request.setKey(key);
            request.setChannelId(user);
            request.setMaxResults(50L);
            if (pageToken != null) {
                request.setPageToken(pageToken);
            }

            PlaylistListResponse response = request.execute();

            for (Playlist item : response.getItems()) {
                PlaylistEntry entry = new PlaylistEntry();
                entry.description = item.getSnippet().getDescription();
                entry.title = item.getSnippet().getTitle();
                entry.count = item.getContentDetails().getItemCount().intValue();
                try {
                    entry.thumbnail = item.getSnippet().getThumbnails().getDefault().getUrl();
                } catch (Exception e) {
                }
                entry.id = item.getId();

                entries.add(entry);
            }

            pageToken = response.getNextPageToken();
        } while (pageToken != null);

        PlaylistFeed feed = new PlaylistFeed();
        feed.playlists = entries;

        return feed;
    }

    public static VideoFeed getVideoFeed(String playlist, Cache cache) throws IOException
    {
        List<VideoEntry> entries = new ArrayList<VideoEntry>();

        String pageToken = null;
        do {
            YouTube.PlaylistItems.List request = youtube.playlistItems().list("snippet,contentDetails");

            request.setKey(key);
            request.setMaxResults(50L);
            request.setPlaylistId(playlist);

            PlaylistItemListResponse response = request.execute();

            for (PlaylistItem item : response.getItems()) {
                VideoEntry entry = new VideoEntry();
                entry.id = item.getContentDetails().getVideoId();
                entry.title = item.getSnippet().getTitle();
                entry.description = item.getSnippet().getDescription();
                try {
                    entry.thumbnail = item.getSnippet().getThumbnails().getDefault().getUrl();
                } catch (Exception e) {
                }

                entries.add(entry);
            }

            pageToken = response.getNextPageToken();
        } while (pageToken != null);

        VideoFeed feed = new VideoFeed();
        feed.videos = entries;

        return feed;
    }
}
