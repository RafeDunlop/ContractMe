const locationResponse = await fetch("location/localisation", {method: "GET"});
let latitude = -43.52460;
let longitude =  172.57710;

if (locationResponse.ok) {
    const locationData = await locationResponse.json();
    if (locationData.location) {
        latitude = locationData.location.latitude;
        longitude = locationData.location.longitude;
    }
}

const userRenovation = L.icon({
    iconUrl: new URL("images/markers/user-renovation.png", document.baseURI),
    iconSize: [32, 32]
})

const publicRenovation = L.icon({
    iconUrl: new URL("images/markers/public-renovation.png", document.baseURI),
    iconSize: [32, 32]
})

const map = L.map('map').setView([latitude, longitude], 11);

L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    minZoom: 3,
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap contributors</a>'
}).addTo(map);

L.marker([-43.52460, 172.57710], {icon: userRenovation}).addTo(map);
L.marker([-43.53333, 172.63333], {icon: publicRenovation}).addTo(map);