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
 * @rteurns void
 */
function populateMap() {
    renovationData.forEach(renovation => {
        const icon = renovation.unownedPublic ? publicRenovation : userRenovation;
        const marker = L.marker([renovation.location.latitude, renovation.location.longitude], { icon }).addTo(map);

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
populateMap();
for (const eventName of ["click", "moveend", "zoomend"]) {
    map.on(eventName, handleMapChange)
}

/**
 * Creates a bootstrap styled card, to display as a tooltip on the map.
 * @param renovation The renovation corresponding to the tooltips, who's information is displayed.
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
    viewRenovationUrl.searchParams.set('previousUrl', 'main');

    const viewButton = document.createElement('a');
    viewButton.className = 'btn btn-primary text-white';
    viewButton.href = viewRenovationUrl.toString();
    viewButton.textContent = 'View Renovation';
    cardBody.appendChild(viewButton);

    return tooltipCard;
}
