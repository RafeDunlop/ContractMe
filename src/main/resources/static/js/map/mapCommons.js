/**
 * Gets the up-to-date renovations within frame.
 * If the bounds cross the date boundary, two requests are sent partitioned at the date boundary
 * and the union of both requests is yielded
 * @param map The map from which information about the current view screen is retrieved
 * @param showPrivateOnly Whether to include other users' public renovations
 * @returns {Promise<Response>} promise which resolves to a collection of mappings to be exclusively displayed
 */
export async function fetchRenovationMappings(map, showPrivateOnly) {
    let resultMappings;
    const bounds = getCoordinateRectangle(map);
    if (getPhase(bounds.minLon) !== getPhase(bounds.maxLon)) {
        const westBounds = {
            minLat: bounds.minLat,
            minLon: bounds.minLon,
            maxLat: bounds.maxLat,
            maxLon: 180 + 360 * getPhase(bounds.minLon)
        }
        const eastBounds = {
            minLat: bounds.minLat,
            minLon: -180 + 360 * getPhase(bounds.minLon),
            maxLat: bounds.maxLat,
            maxLon: bounds.maxLon
        }
        const westMappings = await singleRequest(westBounds, showPrivateOnly).then(response => response.json())
        const eastMappings = await singleRequest(eastBounds, showPrivateOnly).then(response => response.json())
        if (westMappings)
            resultMappings = westMappings.concat(eastMappings)
        else resultMappings = eastMappings
    } else {
        resultMappings = await singleRequest(bounds, showPrivateOnly).then(response => response.json())
    }
    return addPhases(resultMappings, bounds);
}

/**
 * Sets the longitude phase of the specified mappings to that visible on the map itself, including duplication
 * for multiphase view (when the date lien is on screen)
 * @param mappings The mappings whose phase should be set
 * @param bounds The coordinates of the visible screen; phase is extracted from these coordinates
 * @returns {*} The union of mappings in visible phases
 */
function addPhases(mappings, bounds) {
    const leftPhase = getPhase(bounds.minLon)
    const rightPhase = getPhase(bounds.maxLon)
    const leftCopy = shiftPhase(mappings, leftPhase);
    if (leftPhase !== rightPhase) {
        const rightCopy = shiftPhase(mappings, rightPhase);
        return leftCopy.concat(rightCopy)
    }
    return leftCopy;
}

/**
 * Given a longitude value, gets the phase of that value
 * @param longitude The longitude whose phase is gotten
 */
function getPhase(longitude) {
    const adjusted = longitude + 179.99
    return Math.floor(adjusted / 360)
}

/**
 * returns a (deep) copy of the specified mappings phase shifted to the specified phase
 * @param mappings The mappings to be shifted
 * @param phaseShift The number of revolutions from 0 to shift
 * @returns {any} The shifted mappings
 */
function shiftPhase(mappings, phaseShift) {
    const shifted = JSON.parse(JSON.stringify(mappings));
    if (shifted) shifted.forEach(toShift =>
        toShift.location.longitude = toShift.location.longitude + phaseShift * 360
    )
    return shifted;
}

/**
 * Gets The corners which represent the boundary shown; used to fetch renovations within the frame
 * @param map The map from which to retrieve bounds
 * @returns {{minLat: (HTMLElement|*), minLon: *, maxLat: (HTMLElement|*), maxLon: *}} The json object to be submitted
 */
function getCoordinateRectangle(map) {
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
 * Returns a single asynchronous fetch request for renovation data with the corresponding parameters
 * @param bounds The coordinates within which to fetch renovations
 * @param showPrivateOnly Whether to disclude other users' public renovations
 * @returns {Promise<Response>} The fetch request whose response should be awaited
 */
function singleRequest(bounds, showPrivateOnly) {
    const withPublic = !showPrivateOnly
    const params = new URLSearchParams();
    params.set("withPublic", withPublic.toString());
    params.set("minLat", bounds.minLat);
    params.set("minLon", normalizePhase(bounds.minLon));
    params.set("maxLat", bounds.maxLat);
    params.set("maxLon", normalizePhase(bounds.maxLon));
    return fetch(`map/renovations?${params}`)
}

/**
 * Normalizes a longitude value to -180 to 180 range
 * @param lonToShift The longitude to be shifted
 * @returns {*} The normalized longitude
 */
function normalizePhase(lonToShift) {
    const phase = getPhase(lonToShift);
    return lonToShift - 360 * phase
}