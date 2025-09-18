let timeoutId
export function fetchRenovationMappings(bounds, includePublic) {
    const params = new URLSearchParams();
    params.set("withPublic", includePublic);
    params.set("minLat", bounds.minLat);
    params.set("minLon", bounds.minLon);
    params.set("maxLat", bounds.maxLat);
    params.set("maxLon", bounds.maxLon);
    return fetch(`map/renovations?${params}`)
}

/**
 * Debounced fetcher for updating the renovations shown on screen.
 * todo call this function when the map is resized. Consider not calling when map is zoomed out a lot
 * @param delayMs The delay before execution. If called multiple times, existing scheduled invocations will be disregarded
 * @param bounds The boundaries the map has been resized to
 * @param includePublic Whether to fetch public records in addition to owned records
 * @param next next callable function which accepts the mappings, e.g. next = <code>mappings => displayRenovations(mappings)</code>
 */
export function debouncedRenovationMappingsFetcher(delayMs = 200, bounds, includePublic, next) {
    clearTimeout(timeoutId);
    timeoutId = setTimeout(
        () => fetchRenovationMappings(bounds, includePublic).then(mappings => next(mappings)),
        delayMs
    );
}