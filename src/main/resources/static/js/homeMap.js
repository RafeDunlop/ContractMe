const locationResponse = await fetch("location/localisation", {method: "GET"});
const renovationResponse = await fetch("map/renovations", {method: "GET"});

const userRenovation = L.icon({
    iconUrl: "map/marker/user-renovation",
    iconSize: [32, 32]
})

const publicRenovation = L.icon({
    iconUrl: "map/marker/public-renovation",
    iconSize: [32, 32]
})

if (locationResponse.ok) {
    const locationData = await locationResponse.json();
    const { latitude, longitude } = locationData.location;
    const map = L.map('map').setView([latitude, longitude], 11);

    L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        minZoom: 3,
        attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
    }).addTo(map);

    const renovationData = await renovationResponse.json();
    renovationData.forEach(renovation => {
        L.marker([renovation.location.latitude, renovation.location.longitude], {icon: userRenovation}).addTo(map);
    })
} else {
    console.log("Not found.")
}