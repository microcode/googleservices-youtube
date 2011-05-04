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

import se.microcode.google.youtube.PlaylistEntry;
import se.microcode.google.youtube.VideoEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        entry.put("id", playlist.id);
        entry.put("title", playlist.title);
        entry.put("count", Integer.toString(playlist.countHint));
        entry.put("image", "http://i.ytimg.com/vi/QPTALzZ55pM/default.jpg");

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

        return entry;
    }
}
