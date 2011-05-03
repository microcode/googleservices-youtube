This is a small plugin for Confluence (tested with 3.4.9) that embeds galleries from Picasa.

Example usage:

{picasa-gallery:user=mypicasauser|}

Arguments:

user        - User to gather photo feed from
pageSize    - Max entries per page (default unlimited)
imageSize   - Image size for full-sized photos (default: 640)
thumbnails  - Number of thumbnails to display when viewing a photo full-size (default: 5)

There is also a specialized version for inlining a certain set of photos:

{picasa-gallery-excerpts:user=mypicasauser|album=0123456789}

user        - User to gather photo feed from
album       - What album to gather the feed from
maxEntries  - Total number of entries to display
thumbSize   - Size of thumbnail (either index into thumbnail list or picasa-listed size)
photo       - Photo to focus thumbnails around
randomize   - true if feed should be randomized before picking photos to display
page        - Page containing a {picasa-gallery} macro (enables linking)
