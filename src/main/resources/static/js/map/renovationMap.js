const renovationId = document.getElementById("renovationId");

const response = await fetch(`/renovations/view/map/coords-rectangle?id=` + renovationId.toString());

const responseData = await response.json();
console.log(responseData);

const map = L.map('renovation-map').setView([responseData[0], responseData[1]], 11);


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
