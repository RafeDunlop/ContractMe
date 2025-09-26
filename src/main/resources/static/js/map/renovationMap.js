import { buildRenovationTooltip, buildContractorTooltip } from "./mapTooltips.js";

/**
 * Checks whether a given latitude and longitude coordinate already exists as a marker.
 *
 * @param {Array<[number, number]>} list List of existing marker coordinates.
 * @param {number} lat Latitude of the new coordinate.
 * @param {number} lon Longitude of the new coordinate.
 * @returns {boolean} {*} True if a coordinate within a small tolerance already exists in the list.
 */
function coordinateExists(list, lat, lon) {
    const tolerance = 0.0001;
    return list.some(([existingLat, existingLon]) => {
        return Math.abs(existingLat - lat) < tolerance && Math.abs(existingLon - lon) < tolerance;
    })
}

/**
 * Returns a coordinate guaranteed to be unique within a list of marker coordinates.
 * Slightly shifts the coordinate if a duplicate is found.
 *
 * @param {Array<[number, number]>} list List of existing marker coordinates.
 * @param {number} lat Latitude of the new coordinate.
 * @param {number} lon Longitude of the new coordinate.
 * @returns {[number, number]} A unique coordinate which could be shifted to avoid complete overlap.
 */
function getUniqueCoordinate(list, lat, lon) {
    let newLat = lat;
    let newLon = lon;
    const shift = 0.00005;
    if (coordinateExists(list, newLat, newLon)) {
        newLat += shift;
        newLon += shift;
    }
    return [newLat, newLon];
}

const renovationId = document.getElementById("renovationId").value;
const renovationResponse = await fetch(`map/renovation?id=` + renovationId.toString());
const { latitude: lat, longitude: lon } = await renovationResponse.json();

let markerPositions = [[lat, lon]];

const map = L.map('renovation-map',{worldCopyJump: true});

document.getElementById("view-location-tab-item").addEventListener("click", () => {
    setTimeout(() => {
        map.invalidateSize();
    }, 100);
})

const userRenovation = L.icon({
    iconUrl: new URL("images/markers/user-renovation.png", document.baseURI),
    iconSize: [32, 32]
})

L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    minZoom: 3,
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap contributors</a>'
}).addTo(map);

const renovationMarker = L.marker([lat, lon], {
    icon: userRenovation,
    zIndexOffset: 100,
    riseOnHover: true,
    riseOffset: 250
}).addTo(map);


//Loads contractors and displays them on map
const teamId = document.getElementById("teamId").value;
if (teamId !== "") {
    const contractorResponse = await fetch(`map/contractors?id=` + teamId.toString());
    const contractors = await contractorResponse.json();

    contractors.forEach(contractor => {
        const { latitude, longitude } = contractor.location;
        const uniqueCoordinate = getUniqueCoordinate(markerPositions, latitude, longitude);
        markerPositions.push(uniqueCoordinate);
        const contractorIcon = L.divIcon({
            html: `<img src="profile_pictures/${contractor.profilePicture}"
                       alt="Profile Picture"
                       class="contractor-img"
                       style="width: clamp(32px, 0vw, 32px); height: clamp(32px, 0vw, 32px); object-fit: cover;"/>
            `,
            className: 'contractor-icon-wrapper',
            iconSize: [32, 32]
        })
        const contractorMarker = L.marker([uniqueCoordinate[0], uniqueCoordinate[1]], {
            icon: contractorIcon
        }).addTo(map);

        contractorMarker.bindPopup(buildContractorTooltip(contractor), {
            autoPan: true,
            autoClose: true,
            closeButton: false,
            keepInView: true,
            maxWidth: 320,
            riseOnHover: true,
            riseOffset: 250
        });

        contractorMarker.on('click', (e) => {
            L.DomEvent.stop(e);
            contractorMarker.openPopup();
        });


    })
}

/**
 * Adds a single renovation tool tip to the map.
 * Uses the renovation name and address from the page DOM
 * Attaches a Bootstrap-styled popup card.
 */
function addRenovationTooltip() {
    const nameElement = document.querySelector("#renovation-name-header h1");
    const addressData = document.getElementById("renovation-address");

    const renovation = {
        name: nameElement?.textContent?.trim() ?? "",
        location: {
            address: addressData?.dataset.address,
            suburb: addressData?.dataset.suburb,
            city: addressData?.dataset.city,
            postcode: addressData?.dataset.postcode
        }
    };


    renovationMarker.bindPopup(
        buildRenovationTooltip(renovation, false),
        {
            autoPan: true,
            autoClose: true,
            closeButton: false,
            keepInView: true,
            maxWidth: 320
        }
    );

    renovationMarker.on('click', (e) => {
        L.DomEvent.stop(e);
        renovationMarker.openPopup();
    });
}

document.getElementById("view-location-tab-item").addEventListener("click", () => {
    setTimeout(() => {
        map.invalidateSize();
        if (markerPositions.length > 1) {
            const bounds = L.latLngBounds(markerPositions);
            // ChatGPT was used to assist in creating some of the following code based on this stack overflow answer:
            // https://stackoverflow.com/a/38051722 by IvanSanchez https://stackoverflow.com/users/4768502/ivansanchez CC-BY-SA 4.0
            // Adds symmetric points around the renovation to ensure it is approximately in the centre and still includes
            // all the contractor markers.
            const ne = bounds.getNorthEast();
            const sw = bounds.getSouthWest();
            const neSymmetric = L.latLng(
                ne.lat + (lat - ne.lat) * 2,
                ne.lng + (lon - ne.lng) * 2
            );
            const swSymmetric = L.latLng(
                sw.lat + (lat - sw.lat) * 2,
                sw.lng + (lon - sw.lng) * 2
            );
            bounds.extend(neSymmetric);
            bounds.extend(swSymmetric);
            map.fitBounds(bounds, {padding: [50, 50]});
        } else {
            map.setView([lat, lon], 14);
        }
    }, 100);
});

addRenovationTooltip()