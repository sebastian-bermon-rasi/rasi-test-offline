package com.rasi.med.common.api;

import java.time.OffsetDateTime;
import java.util.List;

public class ChangesResponse<T> {
    public List<T> upserts;      // registros vigentes o modificados
    public List<T> tombstones;   // registros eliminados (solo publicId, deletedAt)
    public OffsetDateTime nextCursor;
    public boolean hasMore;

    public ChangesResponse(List<T> upserts, List<T> tombstones, OffsetDateTime nextCursor, boolean hasMore) {
        this.upserts = upserts;
        this.tombstones = tombstones;
        this.nextCursor = nextCursor;
        this.hasMore = hasMore;
    }
}
