let mapInstance;

const locationResponse = await fetch("location/localisation", {method: "GET"});

/* Latitude and longitude of Christchurch. */
let latitude = -43.52460;
let longitude =  172.57710;

if (locationResponse.ok) {
  const locationData = await locationResponse.json();
  if (locationData.location) {
    latitude = locationData.location.latitude;
    longitude = locationData.location.longitude;
  }
}


export function selectContractorToInvite(event) {
  const modalEl = document.getElementById('mapModal');
  const modal = new bootstrap.Modal(modalEl);
  modal.show();

  modalEl.addEventListener('shown.bs.modal', () => {
    if (!mapInstance) {
      mapInstance = L.map('mapModalMap').setView([latitude, longitude], 11);
      L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        minZoom: 3,
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap contributors</a>'
      }).addTo(mapInstance);


    } else {
      mapInstance.invalidateSize();
      mapInstance.setView([latitude, longitude], 11);
    }
  }, { once: true });
}

window.selectContractorToInvite = selectContractorToInvite;



