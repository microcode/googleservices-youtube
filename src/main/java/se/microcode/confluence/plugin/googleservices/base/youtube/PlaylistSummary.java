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

import se.microcode.google.youtube.VideoEntry;

import java.util.HashMap;

import java.util.List;
import java.util.regex.*;

import se.microcode.google.youtube.*;

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

    public PlaylistSummary(PlaylistEntry entry, int thumbSize)
    {
        videos = new HashMap<String,VideoInfo>();

        String summary = entry.summary;
        if (summary != null)
        {
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
        }

        Thumbnail activeThumbnail = null;
        if (entry.group != null && entry.group.thumbnail != null)
        {
            for (Thumbnail thumbnail : entry.group.thumbnail)
            {
                if (thumbnail.height == thumbSize)
                {
                    activeThumbnail = thumbnail;
                    break;
                }
            }

            if (activeThumbnail == null)
            {
                if (entry.group.thumbnail.size() > 0)
                {
                    activeThumbnail = entry.group.thumbnail.get(0);
                }
            }
        }

        if (activeThumbnail != null)
        {
            cover = activeThumbnail.url;
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
