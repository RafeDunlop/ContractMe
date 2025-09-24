/**
 * Gets the up-to-date renovations within frame.
 * If the bounds cross the date boundary, two requests are sent partitioned at the date boundary
 * and the union of both requests is yielded
 * @param bounds The up-to-date renovations in frame
 * @param showPrivateOnly Whether to include other users' public renovations
 * @returns {Promise<Response>} promise which resolves to a collection of mappings to be exclusively displayed
 */
export async function fetchRenovationMappings(bounds, showPrivateOnly) {
    if (bounds.minLon >= 0 && bounds.maxLon <= 0) {
        const westBounds = {
            minLat: bounds.minLat,
            minLon: bounds.minLon,
            maxLat: bounds.maxLat,
            maxLon: 180
        }
        const eastBounds = {
            minLat: bounds.minLat,
            minLon: -180,
            maxLat: bounds.maxLat,
            maxLon: bounds.maxLon
        }
        const westMappings = await singleRequest(westBounds, showPrivateOnly).then(response => response.json())
        const eastMappings = await singleRequest(eastBounds, showPrivateOnly).then(response => response.json())
        return westMappings.addAll(eastMappings)
    }
    return await singleRequest(bounds, showPrivateOnly).then(response => response.json())
}

/**
 * Gets The corners which represent the boundary shown; used to fetch renovations within the frame
 * @param map The map from which to retrieve bounds
 * @returns {{minLat: (HTMLElement|*), minLon: *, maxLat: (HTMLElement|*), maxLon: *}} The json object to be submitted
 */
export function getCoordinateRectangle(map) {
    const bounds = map.getBounds();
    const southwest = bounds.getSouthWest();
    const northeast = bounds.getNorthEast();
    return {
        minLat: southwest.lat,
        minLon: southwest.lng,
        maxLat: northeast.lat,
        maxLon: northeast.lng
    }
}

/**
 * Delays all UI updates by the specified duration; <code>wait</code> is the maximum update frequency
 * @param func the function that triggers the UI update
 * @param wait the duration of delay
 * @returns {(function(...[*]): void)|*} The debounced function
 */
export function debounce(func, wait) {
    let timeout;
    return function(...args) {
        clearTimeout(timeout);
        timeout = setTimeout(() => func.apply(this, args), wait);
    };
}

/**
 * Returns a single asynchronous fetch request for renovation data with teh corresponding parameters
 * @param bounds The coordinates within which to fetch renovations
 * @param showPrivateOnly Whether to disclude other users' public renovations
 * @returns {Promise<Response>} The fetch request whose response should be awaited
 */
function singleRequest(bounds, showPrivateOnly) {
    const withPublic = !showPrivateOnly
    const params = new URLSearchParams();
    params.set("withPublic", withPublic.toString());
    params.set("minLat", bounds.minLat);
    params.set("minLon", bounds.minLon);
    params.set("maxLat", bounds.maxLat);
    params.set("maxLon", bounds.maxLon);
    return fetch(`map/renovations?${params}`)
}