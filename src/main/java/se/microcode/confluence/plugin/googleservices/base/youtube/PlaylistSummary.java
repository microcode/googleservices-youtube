package se.microcode.confluence.plugin.googleservices.base.youtube;

import se.microcode.google.youtube.VideoEntry;

import java.util.HashMap;

import java.util.List;
import java.util.regex.*;

public class PlaylistSummary
{
    public class VideoInfo
    {
        String title;
        String description;
        String category;
    }

    public String text;
    public String cover;
    public HashMap<String,VideoInfo> videos;

    public PlaylistSummary(String summary)
    {
        videos = new HashMap<String,VideoInfo>();

        String lines[] = summary.split("[\r\n]+");

        Pattern pattern = Pattern.compile("(.*)\\|(.*)\\|(.*)\\|(.*)");

        for (String line : lines)
        {
            Matcher m = pattern.matcher(line);
            if (m.find())
            {
                VideoInfo info = new VideoInfo();
                info.title = m.group(2);
                info.description = m.group(3);
                info.category = m.group(4);

                videos.put(m.group(1), info);
            }

            text += line;
        }

        if (videos.size() > 0)
        {
            cover = "http://i.ytimg.com/vi/" + videos.keySet().toArray()[0] + "/default.jpg";
        }
        else
        {
            cover = "http://i.ytimg.com/vi/QPTALzZ55pM/default.jpg";
        }
    }

    public void patchVideos(List<VideoEntry> videoEntries)
    {
        if (videoEntries == null)
        {
            return;
        }

        for (VideoEntry video : videoEntries)
        {
            VideoInfo info = videos.get(video.group.id);
            if (info == null)
            {
                continue;
            }

            video.title = info.title;
            video.group.description = info.description;
        }
    }
}
