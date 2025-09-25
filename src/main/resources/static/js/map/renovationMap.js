import {computeLongitude} from "./mapCommons.js";
import { buildRenovationTooltip, buildContractorTooltip } from "./mapTooltips.js";

const renovationId = document.getElementById("renovationId").value;
console.log(renovationId);
const response = await fetch(`/map/renovation?id=` + renovationId.toString());

const { latitude: lat, longitude: lon } = await response.json();

const map = L.map('renovation-map').setView([lat, computeLongitude(lon)], 14);


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

L.marker([lat, lon], { icon: userRenovation }).addTo(map);

/**
 * Adds a single renovation tool tip to the map.
 * Uses the renovation name and address from the page DOM
 * Attaches a Bootstrap-styled popup card.
 */
function addRenovationTooltip() {
    const marker = L.marker([lat, computeLongitude(lon)], { icon: userRenovation }).addTo(map);
    const nameEl = document.querySelector("#renovation-name-header h1");
    const addressData = document.getElementById("renovation-address");

    const renovation = {
        name: nameEl?.textContent?.trim() ?? "",
        location: {
            address: addressData?.dataset.address,
            suburb: addressData?.dataset.suburb,
            city: addressData?.dataset.city,
            postcode: addressData?.dataset.postcode
        }
    };


    marker.bindPopup(
        buildRenovationTooltip(renovation, false),
        {
            autoPan: true,
            autoClose: true,
            closeButton: false,
            keepInView: true,
            maxWidth: 320
        }
    );

    marker.on('click', (e) => {
        L.DomEvent.stop(e);
        marker.openPopup();
    });
}

addRenovationTooltip()