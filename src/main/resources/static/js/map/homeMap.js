import {fetchRenovationMappings, debounce, getCoordinateRectangle} from "./mapCommons.js";

const waitTime = 200;
const defaultZoom = 11;

const togglePublicCheckbox = document.getElementById("include-public-renovations-checkbox");

const map = L.map('map')
const renovationIconGroup = L.featureGroup().addTo(map);

const userRenovation = L.icon({
    iconUrl: new URL("images/markers/user-renovation.png", document.baseURI),
    iconSize: [32, 32]
})

const publicRenovation = L.icon({
    iconUrl: new URL("images/markers/public-renovation.png", document.baseURI),
    iconSize: [32, 32]
})

fetch(`location/localisation`)
    .then(response => response.json())
    .then(localisation => setup(localisation.location))

function setup(startingCoordinates) {
    map.setView([startingCoordinates.latitude, startingCoordinates.longitude], defaultZoom);

    L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        minZoom: 3,
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap contributors</a>'
    }).addTo(map);
    handleMapChange()
    for (const eventName of ["click", "moveend", "zoomend"]) {
        map.on(eventName, handleMapChange)
    }

    togglePublicCheckbox.addEventListener("change", toggleListener)
    const saved = localStorage.getItem("toggleState");
    if (saved !== null) {
        togglePublicCheckbox.checked = saved === "true";
    } else {
        togglePublicCheckbox.checked = true;
    }
}

/**
 * Listener called when the toggle button for displaying public renovations is called. Posts an immediate redisplay of
 * the mappings and saves the users preference for soft persistence
 */
function toggleListener() {
    fetchRenovationMappings(getCoordinateRectangle(map), togglePublicCheckbox.checked)
        .then(mappings => populateMap(mappings))
    localStorage.setItem("toggleState",togglePublicCheckbox.checked)
}


/**
 * Fetches data whenever the map is clicked on, resized or dragged
 * Debounced <code>updateMapContent</code> function
 * @type {(function(...[*]): void)|*}
 */
const handleMapChange = debounce(updateMapContent, waitTime)

/**
 * Immediately calls a fetch request for updated renovation data and updates the map data when this is retrieved
 */
function updateMapContent() {
    fetchRenovationMappings(getCoordinateRectangle(map), togglePublicCheckbox.checked)
        .then(responseData => populateMap(responseData))
}


/**
 * Adds all renovations from `renovationData` to the Leaflet map.
 * For each renovation, creates a marker with the correct icon, and attaches a popup card
 * param renovationData The renovation data to replace what is currently on the map
 * @returns {void}
 */
function populateMap(renovationData) {
    if (renovationData) {
        renovationIconGroup.clearLayers();
        renovationData.forEach(renovation => {
            const icon = renovation.unownedPublic ? publicRenovation : userRenovation;
            const marker = L.marker(
                [renovation.location.latitude, renovation.location.longitude], {icon}
            ).addTo(renovationIconGroup);

            marker.bindPopup(buildRenovationPopup(renovation), {
                autoPan: true,
                autoClose: true,
                closeButton: false,
                keepInView: true,
                maxWidth: 320
            });

            marker.on('click', (e) => {
                L.DomEvent.stop(e);
                marker.openPopup();
            });
        });
    }
}

/**
 * Creates a bootstrap styled card, to display as a tooltip on the map.
 * @param renovation The renovation corresponding to the tooltips, whose information is displayed.
 * @returns {HTMLDivElement} The card element to display as tooltip.
 */
function buildRenovationPopup(renovation) {
    const tooltipCard = document.createElement('div');
    tooltipCard.className = 'map-tooltip card border-0 shadow-sm';

    const cardBody = document.createElement('div');
    cardBody.className = 'card-body p-4';
    tooltipCard.appendChild(cardBody);

    const title = document.createElement('h6');
    title.className = 'fw-bold mb-0';
    title.textContent = renovation.name || '';
    cardBody.appendChild(title);

    const addressParts = [
        renovation.location.address,
        renovation.location.suburb,
        renovation.location.city,
        renovation.location.postcode
    ].filter(Boolean);

    if (addressParts.length) {
        const address = document.createElement('p');
        address.className = 'text-muted address mb-3';
        address.textContent = addressParts.join(', ');
        cardBody.appendChild(address);
    }

    const viewRenovationUrl = new URL('renovations/view', document.baseURI);
    viewRenovationUrl.searchParams.set('id', renovation.id);

    const viewButton = document.createElement('a');
    viewButton.className = 'btn btn-primary text-white';
    viewButton.href = viewRenovationUrl.toString();
    viewButton.textContent = 'View Renovation';
    cardBody.appendChild(viewButton);

    return tooltipCard;
}
