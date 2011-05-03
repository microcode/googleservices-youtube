package se.microcode.google.picasa;

import com.google.api.client.util.DataUtil;
import com.google.api.client.util.Key;

import java.util.List;

public class Entry implements Cloneable
{
    @Key("@gd:etag")
    public String etag;

    @Key("atom:link")
    public List<Link> links;

    @Key("atom:title")
    public String title;

    @Override
    public Entry clone()
    {
        return DataUtil.clone(this);
    }
}
