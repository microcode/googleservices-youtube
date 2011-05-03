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

import java.io.IOException;
import java.lang.ClassCastException;

public class PicasaHelper
{
    public static UserFeed getUserFeed(String user, Cache cache) throws IOException
    {
        HttpTransport transport = createTransport();
        Url url = Url.relativeToRoot("feed/api/user/" + user);
        String key = url.toString();

        UserFeed userFeed = null;

        if (cache != null)
        {
            try
            {
                userFeed = (UserFeed)cache.get(key);
            }
            catch (ClassCastException e)
            {
                userFeed = null;
            }
        }

        if (userFeed == null)
        {
            userFeed = UserFeed.executeGet(transport, url);
            if ((userFeed != null) && (cache != null))
            {
                cache.put(key, userFeed);
            }
        }
        return userFeed;
    }

    public static AlbumFeed getAlbumFeed(String user, String albumId, Cache cache) throws IOException
    {
        HttpTransport transport = createTransport();
        Url url = Url.relativeToRoot("feed/api/user/" + user + "/albumid/" + albumId);
        String key = url.toString();

        AlbumFeed albumFeed = null;
        if (cache != null)
        {
            try
            {
                albumFeed = (AlbumFeed)cache.get(key);
            }
            catch (ClassCastException e)
            {
                albumFeed = null;
            }
        }

        if (albumFeed == null)
        {
            albumFeed = AlbumFeed.executeGet(transport, url);
            if ((albumFeed != null) && (cache != null))
            {
                cache.put(key, albumFeed);
            }
        }
        return albumFeed;
    }

    public static HttpTransport createTransport()
    {
        GoogleHeaders headers = new GoogleHeaders();
        headers.setApplicationName("se.microcode.picasa-gallery-plugin/1.0");
        headers.gdataVersion = "2";

        AtomParser parser = new AtomParser();
        parser.namespaceDictionary = Util.NAMESPACE_DICTIONARY;

        HttpTransport transport = new NetHttpTransport();
        transport.defaultHeaders = headers;
        transport.addParser(parser);
        return transport;
    }
}
