const userRenovation = {
    url: "/images/markers/user-renovation.png",
    scaledSize: new google.maps.Size(32, 32)
};

const publicRenovation = {
    url: "/images/markers/public-renovation.png",
    scaledSize: new google.maps.Size(32, 32)
};

var map = new google.maps.Map(document.getElementById("map"), {
    center: {lat: -43.52460, lng: 172.57710},
    zoom: 10,
    maxZoom: 19,
    minZoom: 3,
});

new google.maps.Marker({
    position: {lat: -43.52460, lng: 172.57710},
    map: map,
    icon: userRenovation,
});
new google.maps.Marker({
    position: {lat: -43.53333, lng: 172.63333},
    map,
    icon: publicRenovation
});