const userRenovation = L.icon({
    iconUrl: "/map/markers/user-renovation",
    iconSize: [32, 32]
})

const publicRenovation = L.icon({
    iconUrl: "/map/markers/public-renovation",
    iconSize: [32, 32]
})

const map = L.map('map').setView([-43.52460, 172.57710], 11);

L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    minZoom: 3,
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap contributors</a>'
}).addTo(map);

L.marker([-43.52460, 172.57710], {icon: userRenovation}).addTo(map);
L.marker([-43.53333, 172.63333], {icon: publicRenovation}).addTo(map);