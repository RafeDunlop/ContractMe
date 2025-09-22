const map = L.map('renovation-map').setView([-43.52460, 172.57710], 11);
const bounds = map.getBounds();
const southwest = bounds.getSouthWest();
const northeast = bounds.getNorthEast();
const renovationResponse = await fetch("map/renovations/", {method: "GET"});


const userRenovation = L.icon({
    iconUrl: new URL("images/markers/user-renovation.png", document.baseURI),
    iconSize: [32, 32]
})

L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    minZoom: 3,
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap contributors</a>'
}).addTo(map);