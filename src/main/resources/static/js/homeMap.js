const map = L.map('map').setView([-43.52460, 172.57710], 11);
const bounds = map.getBounds();
const southwest = bounds.getSouthWest();
const northeast = bounds.getNorthEast();
const rawCoordinates = [southwest.lat, southwest.lng, northeast.lat, northeast.lng]
const renovationResponse = await fetch(`map/renovations/${encodeURIComponent(rawCoordinates.join(","))}`, {method: "GET"});
let renovationData = await renovationResponse.json();
const waitTime = 200;


const userRenovation = L.icon({
    iconUrl: new URL("images/markers/user-renovation.png", document.baseURI),
    iconSize: [32, 32]
})

const publicRenovation = L.icon({
    iconUrl: new URL("images/markers/public-renovation.png", document.baseURI),
    iconSize: [32, 32]
})

L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    minZoom: 3,
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap contributors</a>'
}).addTo(map);


/**
 * Delays all UI updates by the specified duration
 * @param func the function that triggers the UI update
 * @param wait the duration of delay
 * @returns {(function(...[*]): void)|*}
 */
function debounce(func, wait) {
    let timeout;
    return function(...args) {
        clearTimeout(timeout);
        timeout = setTimeout(() => func.apply(this, args), wait);
    };
}

/**
 * Fetches data whenever the map is clicked on, resized or dragged
 * @type {(function(...[*]): void)|*}
 */
const handleMapChange = debounce(() => {
    fetch(`map/renovations/${encodeURIComponent(rawCoordinates.join(","))}`, {method: "GET"})
        .then(response => response.json())
        .then(responseData => {
            renovationData = responseData;
            populateMap()
        })
}, waitTime)

/**
 * Loads all renovations onto the map
 * @returns void
 */
function populateMap() {
    renovationData.forEach(renovation => {
        const icon = renovation.unownedPublic ? publicRenovation : userRenovation
        L.marker([renovation.location.latitude, renovation.location.longitude], {icon}).addTo(map);
    })
}
populateMap();
for (const eventName of ["click", "moveend", "zoomend"]) {
    map.on(eventName, handleMapChange)
}

