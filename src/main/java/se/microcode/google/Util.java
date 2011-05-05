package se.microcode.google;

import com.google.api.client.xml.XmlNamespaceDictionary;

public class Util
{
    public static final XmlNamespaceDictionary NAMESPACE_DICTIONARY =
    new XmlNamespaceDictionary()
    .set("", "http://www.w3.org/2005/Atom")
    .set("atom", "http://www.w3.org/2005/Atom")
    .set("exif", "http://schemas.google.com/photos/exif/2007")
    .set("gd", "http://schemas.google.com/g/2005")
    .set("geo", "http://www.w3.org/2003/01/geo/wgs84_pos#")
    .set("georss", "http://www.georss.org/georss")
    .set("gml", "http://www.opengis.net/gml")
    .set("gphoto", "http://schemas.google.com/photos/2007")
    .set("media", "http://search.yahoo.com/mrss/")
    .set("openSearch", "http://a9.com/-/spec/opensearch/1.1/")
    .set("xml", "http://www.w3.org/XML/1998/namespace");

    private Util()
    {}
}