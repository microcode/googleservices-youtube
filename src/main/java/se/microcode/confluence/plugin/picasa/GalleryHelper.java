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

package se.microcode.confluence.plugin.picasa;

import com.atlassian.confluence.plugin.webresource.ConfluenceWebResourceManager;
import com.atlassian.confluence.renderer.radeox.macros.MacroUtils;
import com.atlassian.confluence.util.velocity.VelocityUtils;
import com.atlassian.plugin.webresource.WebResourceManager;
import se.microcode.google.picasa.AlbumEntry;
import se.microcode.google.picasa.PhotoEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GalleryHelper
{
    public static List<Map> buildPhotoList(List<PhotoEntry> photos, int maxSize)
    {
        ArrayList<Map> output = new ArrayList<Map>();

        for(PhotoEntry photo : photos)
        {
            output.add(buildPhotoEntry(photo,maxSize));
        }
        return output;
    }

    public static HashMap<String,String> buildPhotoEntry(PhotoEntry photo, int maxSize)
    {
        HashMap<String,String> entry = new HashMap<String,String>();
        if (maxSize > photo.group.thumbnail.size())
        {
            entry.put("image", photo.group.content.url + "?imgmax=" + maxSize);
            entry.put("size", Integer.toString(maxSize));
        }
        else
        {
            entry.put("image", photo.group.thumbnail.get(maxSize).url);
            entry.put("size", Integer.toString(Math.max(photo.group.thumbnail.get(maxSize).width,photo.group.thumbnail.get(maxSize).height)));
        }
        entry.put("title", photo.title);
        entry.put("id", photo.id);
        return entry;
    }

    public static List<Map> buildAlbumList(List<AlbumEntry> albums)
    {
        ArrayList<Map> output = new ArrayList<Map>();

        for (AlbumEntry album : albums)
        {
            output.add(buildAlbumEntry(album));
        }
        return output;
    }

    public static HashMap<String,String> buildAlbumEntry(AlbumEntry album)
    {
        HashMap<String,String> entry = new HashMap<String,String>();
        entry.put("image", album.group.thumbnail.get(0).url);
        entry.put("title", album.title);
        entry.put("id", album.id);
        entry.put("count", Integer.toString(album.numPhotos));
        return entry;
    }

}
