

const userRenovation = L.icon({
    iconUrl: "/images/markers/user-renovation.png",
    iconSize: [32, 32]
})

const publicRenovation = L.icon({
    iconUrl: "/images/markers/public-renovation.png",
    iconSize: [32, 32]
})

function debounce(func, wait) {
    let timeout;
    return function(...args) {
        clearTimeout(timeout);
        timeout = setTimeout(() => func.apply(this, args), wait);
    };
}


const map = L.map('map').setView([-43.52460, 172.57710], 11);
const bounds = map.getBounds();
const southwest = bounds.getSouthWest();
const northeast = bounds.getNorthEast();
const rawCoordinates = [southwest.lat, southwest.lng, northeast.lat, northeast.lng]
console.log(rawCoordinates.join(","))


const renovationResponse = await fetch(`map/renovations/${encodeURIComponent(rawCoordinates.join(","))}`, {method: "GET"});


//const debouncedHandleMapChange = debounce(handleMapChange, 700)

L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    minZoom: 3,
    attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap contributors</a>'
}).addTo(map);

for (const eventName of ["click", "moveend"]) {
    map.on(eventName, debouncedHandleMapChange)
    const renovationData = await renovationResponse.json();
    renovationData.forEach(renovation => {
        L.marker([renovation.location.latitude, renovation.location.longitude], {icon: userRenovation}).addTo(map);
    })
}

L.marker([-43.52460, 172.57710], {icon: userRenovation}).addTo(map);
L.marker([-43.53333, 172.63333], {icon: publicRenovation}).addTo(map);