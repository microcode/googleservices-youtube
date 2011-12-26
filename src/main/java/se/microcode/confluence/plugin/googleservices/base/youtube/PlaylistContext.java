package se.microcode.confluence.plugin.googleservices.base.youtube;

import se.microcode.google.youtube.PlaylistEntry;
import se.microcode.google.youtube.PlaylistFeed;
import se.microcode.google.youtube.VideoEntry;
import se.microcode.google.youtube.VideoFeed;

import java.util.List;

public class PlaylistContext
{
    PlaylistFeed playlistFeed;
    VideoFeed videoFeed;
    PlaylistEntry playlistEntry;
    VideoEntry videoEntry;
    List<VideoEntry> thumbnails;
    int videoIndex;
}
