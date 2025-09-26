export { buildRenovationTooltip, buildContractorTooltip };

/**
 * Creates a bootstrap styled card, to display as a tooltip on the map.
 * @param renovation The renovation corresponding to the tooltips, whose information is displayed.
 * @param containViewButton boolean to decide if a view button is in the tooltip.
 * @returns {HTMLDivElement} The card element to display as tooltip.
 */
function buildRenovationTooltip(renovation, containViewButton) {
    const tooltipCard = document.createElement('div');
    tooltipCard.className = 'map-tooltip card border-0 shadow-sm';

    const cardBody = document.createElement('div');
    cardBody.className = 'card-body p-4';
    tooltipCard.appendChild(cardBody);

    const title = document.createElement('h6');
    title.className = 'fw-bold mb-0';
    title.textContent = renovation.name || '';
    cardBody.appendChild(title);

    const addressParts = [
        renovation.location.address,
        renovation.location.suburb,
        renovation.location.city,
        renovation.location.postcode
    ].filter(Boolean);

    if (addressParts.length) {
        const address = document.createElement('p');
        address.className = 'text-muted address mb-3';
        address.textContent = addressParts.join(', ');
        cardBody.appendChild(address);
    }

    const viewRenovationUrl = new URL('renovations/view', document.baseURI);
    viewRenovationUrl.searchParams.set('id', renovation.id);

    if (containViewButton) {
        const viewButton = document.createElement('a');
        viewButton.className = 'btn btn-primary text-white';
        viewButton.href = viewRenovationUrl.toString();
        viewButton.textContent = 'View Renovation';
        cardBody.appendChild(viewButton);
    }

    return tooltipCard;
}

/**
 * Creates a bootstrap styled card, to display as a contractor tooltip on the map.
 * @param contractor The contractor corresponding to the icon, whose name and picture is displayed.
 * @returns {HTMLDivElement} The card element to display as tooltip.
 */
function buildContractorTooltip(contractor) {
    const tooltipCard = document.createElement('div');
    tooltipCard.className = 'map-tooltip card border-0 shadow-sm';

    const cardBody = document.createElement('div');
    cardBody.className = 'card-body p-3 pe-1';
    tooltipCard.appendChild(cardBody);

    const row = document.createElement('div');
    row.className = 'd-flex align-items-center gap-0';
    cardBody.appendChild(row);

    const pic = document.createElement('img');
    pic.className = 'rounded-circle';
    pic.style.width = '56px';
    pic.style.height = '56px';
    pic.style.objectFit = 'cover';
    pic.loading = 'lazy';
    pic.src = getProfilePictureUrl(contractor);
    row.appendChild(pic);

    const name = document.createElement('h6');
    name.className = 'fw-bold mb-0 text-truncate';
    name.textContent = contractor.fullName || '';
    row.appendChild(name);

    return tooltipCard;
}

/**
 * Returns the given contractors profile pic url or the default one.
 * @param contractor the contractors whose picture to get or fallback to the default profile picture.
 * @returns {string} the url of the contractors profile pic or default.
 */
function getProfilePictureUrl(contractor) {
    if (contractor.profilePicture) {
        return new URL(`profile_pictures/${contractor.profilePicture}`, document.baseURI).toString();
    }
    return new URL("icons/profile-icon.svg", document.baseURI).toString();
}
