import {fetchRenovationMappings, debounce} from "./mapCommons.js";

const waitTime = 200;

const map = L.map('map').setView([-43.52460, 172.57710], 11);
let renovationIconGroup = L.featureGroup().addTo(map);
const togglePublicCheckbox = document.getElementById("include-public-renovations-checkbox");
togglePublicCheckbox.addEventListener("click", toggleListener)

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
 * Gets The corners which represent the boundary shown; used to fetch renovations within the frame
 * @returns {{minLat: (HTMLElement|*), minLon: *, maxLat: (HTMLElement|*), maxLon: *}} The json object to be submitted
 */
function getCoorinateRectangle() {
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
 * Listener called when the toggle button for displaying public renovations is called. Posts an immediate redisplay of
 * the mappings
 */
function toggleListener() {
    fetchRenovationMappings(getCoorinateRectangle(), togglePublicCheckbox.checked)
        .then(response => response.json())
        .then(mappings => populateMap(mappings))
}

/**
 * Fetches data whenever the map is clicked on, resized or dragged
 * @type {(function(...[*]): void)|*}
 */
const handleMapChange = debounce(() => {
    fetchRenovationMappings(getCoorinateRectangle(), togglePublicCheckbox.checked)
        .then(response => response.json())
        .then(mappings =>
            populateMap(mappings)
        )
}, waitTime)

/**
 * Loads all renovations onto the map
 * @rteurns void
 */
function populateMap(renovationData) {
    if (renovationData) {
        renovationIconGroup.clearLayers();
        renovationData.forEach(renovation => {
            const icon = renovation.unownedPublic ? publicRenovation : userRenovation
            L.marker([renovation.location.latitude, renovation.location.longitude], {icon}).addTo(renovationIconGroup);
        })
    }
}

handleMapChange();

for (const eventName of ["click", "moveend", "zoomend"]) {
    map.on(eventName, handleMapChange)
}

