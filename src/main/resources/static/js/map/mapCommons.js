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
    const centre = map.getCenter();
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
        if (westMappings)
            resultMappings = westMappings.concat(eastMappings)
        else resultMappings = eastMappings
    } else {
        resultMappings = await singleRequest(bounds, showPrivateOnly).then(response => response.json())
    }
    return addPhases(resultMappings, centre);
}

/**
 * Sets the longitude phase of the specified mappings to that visible on the map itself, including duplication
 * for multiphase view (when the date lien is on screen)
 * @param mappings The mappings whose phase should be set
 * @param centre The coordinates of the centre of the visible screen; current phase is extracted from these
 * @returns {*} The union of mappings in visible phases
 */
function addPhases(mappings, centre) {
    const phase = Math.floor(centre.lng / 360);
    const leftCopy = shiftPhase(mappings, phase - 1);
    const middleCopy = shiftPhase(mappings, phase);
    const rightCopy = shiftPhase(mappings, phase + 1);
    if (leftCopy) return leftCopy.concat(middleCopy).concat(rightCopy);
    return leftCopy;
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
    );
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
        minLon: computeLongitude(southwest.lng),
        maxLat: northeast.lat,
        maxLon: computeLongitude(northeast.lng)
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
    params.set("minLon", computeLongitude(bounds.minLon).toString());
    params.set("maxLat", bounds.maxLat);
    params.set("maxLon", computeLongitude(bounds.maxLon).toString());
    return fetch(`map/renovations?${params}`)
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