const renovationId = document.getElementById("renovationId").value;
console.log(renovationId);
const response = await fetch(`/map/renovation?id=` + renovationId.toString());

const { latitude: lat, longitude: lon } = await response.json();
console.log(lat,lon);

const map = L.map('renovation-map').setView([lat, lon], 14);


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
