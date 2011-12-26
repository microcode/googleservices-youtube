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

package se.microcode.google;

import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.googleapis.xml.atom.GoogleAtom;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.util.DateTime;
import com.google.api.client.xml.atom.AtomParser;

import com.atlassian.cache.Cache;
import com.google.api.client.googleapis.GoogleUrl;

import java.io.IOException;
import java.util.Date;

public class GoogleHelper
{
    public static Feed getFeed(GoogleUrl url, Cache cache, Class<? extends Feed> feedClass) throws IOException
    {
        return getFeed(url, cache, feedClass, true, 3600 * 1000);
    }

    public static Feed getFeed(GoogleUrl url, Cache cache, Class<? extends Feed> feedClass, boolean useFields, long timeoutMillis) throws IOException
    {
        HttpTransport transport = createTransport();
        String key = url.toString();
        DateTime now = new DateTime(new Date());

        Feed feed = null;

        if (cache != null)
        {
            try
            {
                feed = (Feed)cache.get(key);
                if (feed != null)
                {
                    if (feed.timeout.value < now.value)
                    {
                        feed = null;
                    }
                }
            }
            catch (ClassCastException e)
            {
                feed = null;
            }
        }

        if (feed == null)
        {
            feed = executeGet(transport, url, feedClass, useFields);
            if ((feed != null) && (cache != null))
            {
                feed.timeout = new DateTime(now.value + timeoutMillis);
                cache.put(key, feed);
            }
        }
        return feed;
    }

    static HttpTransport createTransport()
    {
        GoogleHeaders headers = new GoogleHeaders();
        headers.setApplicationName("se.microcode.confluence.plugin.googleservices/1.0");
        headers.gdataVersion = "2";

        AtomParser parser = new AtomParser();
        parser.namespaceDictionary = Util.NAMESPACE_DICTIONARY;

        HttpTransport transport = new NetHttpTransport();
        transport.defaultHeaders = headers;
        transport.addParser(parser);
        return transport;
    }

    static Feed executeGet(HttpTransport transport, GoogleUrl url, Class<? extends Feed> feedClass, boolean useFields) throws IOException
    {
        if (useFields)
        {
            url.fields = GoogleAtom.getFieldsFor(feedClass);
        }
        HttpRequest request = transport.buildGetRequest();
        request.url = url;

        HttpResponse response = request.execute();
        return response.parseAs(feedClass);
    }
}
