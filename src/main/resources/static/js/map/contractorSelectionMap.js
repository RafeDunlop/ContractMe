let mapInstance;

const locationResponse = await fetch("location/localisation", {method: "GET"});

/* Latitude and longitude of Christchurch. */
let latitude = -43.52460;
let longitude = 172.57710;

if (locationResponse.ok) {
    const locationData = await locationResponse.json();
    if (locationData.location) {
        latitude = locationData.location.latitude;
        longitude = locationData.location.longitude;
    }
}

/**
 * Shows the "Select Contractor" modal and initializes or updates the Leaflet map inside it.
 *
 * Opens the Bootstrap modal with id "mapModal".
 * On first open, creates a Leaflet map in "mapModalMap" centered on [latitude, longitude].
 * On subsequent opens, resizes and recenters the existing map.
 *
 * @param {Event} event - Click event from the Invite Contractor button.
 */
export function selectContractorToInvite(event) {
    const modalEl = document.getElementById('mapModal');
    const modal = new bootstrap.Modal(modalEl);
    const skill = event.target.dataset.skill;
    modal.show();

    modalEl.addEventListener('shown.bs.modal', () => {
        if (!mapInstance) {
            mapInstance = L.map('mapModalMap').setView([latitude, longitude], 11);
            L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
                maxZoom: 19,
                minZoom: 3,
                attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap contributors</a>'
            }).addTo(mapInstance);
            fetchEligibleContractors(skill).then(contractors => populateContractors(contractors, mapInstance));


        } else {
            mapInstance.invalidateSize();
            mapInstance.setView([latitude, longitude], 11);
        }
    }, {once: true});
}

/**
 * Async function which returns a promise of the eligible contractors for the given team and skill.
 * @param skill the skill contractors need for the particular role we are selecting
 * @returns {Promise<any>} a json object containing the contractors found if any
 */
async function fetchEligibleContractors(skill) {
    const teamId = document.getElementById("teamId").value;
    const params = new URLSearchParams();
    params.append("skill", skill);
    params.append("teamId", teamId);
    const contractors = await fetch(`map/eligible?${params}`);
    return await contractors.json();
}

/**
 * Display the contractors as markers on the map.
 * @param contractors a json object containing all the contractors to display
 * @param map the leaflet map object
 */
function populateContractors(contractors, map) {
    if(contractors) {
        contractors.forEach(contractor => {
            const contractorIcon = L.icon({
                iconUrl: new URL(`profile_pictures/${contractor.profilePicture}`, document.baseURI),
                iconSize: [32, 32]
            });
            L.marker([contractor.location.latitude, contractor.location.longitude],
                {icon: contractorIcon}
            ).addTo(map);
        })
    }
}

window.selectContractorToInvite = selectContractorToInvite;



