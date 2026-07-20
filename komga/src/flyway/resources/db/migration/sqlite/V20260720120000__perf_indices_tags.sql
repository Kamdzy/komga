-- covering indices for the referential tag queries
-- SELECT DISTINCT TAG can be served as an index scan instead of a full table scan
create index if not exists idx__book_metadata_tag__tag
    on BOOK_METADATA_TAG (TAG);
create index if not exists idx__series_metadata_tag__tag
    on SERIES_METADATA_TAG (TAG);

-- covering the tag -> book/series join, avoids fetching table rows
create index if not exists idx__book_metadata_tag__book_id_tag
    on BOOK_METADATA_TAG (BOOK_ID, TAG);
create index if not exists idx__series_metadata_tag__series_id_tag
    on SERIES_METADATA_TAG (SERIES_ID, TAG);

-- missing foreign key indices, these two were skipped in V20220715213721
create index if not exists idx__readlist_book__book_id
    on READLIST_BOOK (BOOK_ID);
create index if not exists idx__collection_series__series_id
    on COLLECTION_SERIES (SERIES_ID);
