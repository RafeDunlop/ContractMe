export function fetchRenovationMappings(bounds, showPrivateOnly) {

    const withPublic = !showPrivateOnly
    const params = new URLSearchParams();
    params.set("withPublic", withPublic.toString());
    params.set("minLat", bounds.minLat);
    params.set("minLon", computeLongitude(bounds.minLon).toString());
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

/**
 * Computes a valid longitude value (ie between -180 and 180)
 * @param longitude the original longitude value
 * @returns {number}
 */
export function computeLongitude(longitude) {
    let updatedLong = parseFloat(longitude) % 360
    if (updatedLong > 180) {
        updatedLong -= 360
    }

    return updatedLong
}
