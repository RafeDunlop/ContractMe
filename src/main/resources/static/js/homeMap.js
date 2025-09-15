const locationResponse = await fetch("location/localisation", {method: "GET"});

const publicRenovation = L.icon({
    iconUrl: "map/marker/public-renovation",
    iconSize: [32, 32]
})

function debounce(func, wait) {
    let timeout;
    return function(...args) {
        clearTimeout(timeout);
        timeout = setTimeout(() => func.apply(this, args), wait);
    };
}

if (locationResponse.ok) {
    const locationData = await locationResponse.json();
    const { latitude, longitude } = locationData.location;
    const map = L.map('map').setView([latitude, longitude], 11);

    const handleMapChange = () => {
        const bounds = map.getBounds();
        const southWest = bounds.getSouthWest();
        const northEast = bounds.getNorthWest();
    }

    const debouncedHandleMapChange = debounce(handleMapChange, 700)

    L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        minZoom: 3,
        attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
    }).addTo(map);

    ["click", "moveend"].forEach(eventName => {
        map.on(eventName, debouncedHandleMapChange)
    })

} else {
    console.log("Not found.")
}

