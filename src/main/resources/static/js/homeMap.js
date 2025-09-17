const userRenovation = document.createElement("img");
userRenovation.src = "/images/markers/user-renovation.png";
userRenovation.style.width = "32px";
userRenovation.style.height = "32px";

const publicRenovation = document.createElement("img");
publicRenovation.src = "/images/markers/public-renovation.png";
publicRenovation.style.width = "32px";
publicRenovation.style.height = "32px";

const map = new maplibregl.Map({
    container: 'map',
    style: 'https://tiles.basemaps.cartocdn.com/gl/voyager-gl-style/style.json',
    center: [172.57710, -43.52460],
    zoom: 9,
    maxZoom: 18,
    minZoom: 2,
});

map.addControl(new maplibregl.NavigationControl());

new maplibregl.Marker({element: userRenovation})
    .setLngLat([172.57710, -43.52460])
    .addTo(map);
new maplibregl.Marker({element: publicRenovation})
    .setLngLat([172.63333, -43.53333])
    .addTo(map);