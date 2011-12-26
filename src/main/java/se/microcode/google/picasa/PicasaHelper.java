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

package se.microcode.google.picasa;

import com.atlassian.cache.Cache;
import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.xml.atom.AtomParser;

import se.microcode.google.Util;
import se.microcode.google.GoogleHelper;

import java.io.IOException;
import java.lang.ClassCastException;

public class PicasaHelper extends GoogleHelper
{
    public static UserFeed getUserFeed(String user, Cache cache) throws IOException
    {
        Url url = Url.relativeToRoot("feed/api/user/" + user);
        url.kinds = "album";

        return (UserFeed)getFeed(url, cache, UserFeed.class);
    }

    public static AlbumFeed getAlbumFeed(String user, String albumId, String imageSize, String thumbSize, Cache cache) throws IOException
    {
        Url url = Url.relativeToRoot("feed/api/user/" + user + "/albumid/" + albumId);
        url.kinds = "photo";
        url.imgmax = imageSize;
        url.thumbsize = thumbSize;

        return (AlbumFeed)getFeed(url, cache, AlbumFeed.class);
    }
}
