package se.microcode.confluence.plugin.googleservices.base.youtube;


import se.microcode.google.youtube.PlaylistEntry;

import java.util.Comparator;

public class PlaylistSorter
{
    static Comparator<PlaylistEntry> createSorter(SortMode mode)
    {
        switch (mode)
        {
            case OFF: return null;
            case NAME: return new PlaylistNameSorter();
        }

        return null;
    }
}

class PlaylistNameSorter implements Comparator<PlaylistEntry>
{
    public int compare(PlaylistEntry o1, PlaylistEntry o2)
    {
        return o1.title.compareTo(o2.title);
    }
}
