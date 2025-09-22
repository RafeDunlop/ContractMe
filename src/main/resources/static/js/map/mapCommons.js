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
 * Delays all UI updates by the specified duration
 * @param func the function that triggers the UI update
 * @param wait the duration of delay
 * @returns {(function(...[*]): void)|*}
 */
export function debounce(func, wait) {
    let timeout;
    return function(...args) {
        clearTimeout(timeout);
        timeout = setTimeout(() => func.apply(this, args), wait);
    };
}