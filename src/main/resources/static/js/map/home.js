import {fetchRenovationMappings} from "./mapCommons.js";

const togglePublicCheckbox = document.getElementById("include-public-renovations-checkbox");
togglePublicCheckbox.addEventListener("click", toggleListener)

/**
 * Listener called when the toggle button for displaying public renovations is called. Posts an immediate redisplay of
 * the mappings
 * todo fix bounds
 */
function toggleListener() {
    fetchRenovationMappings({minLat: -90, minLon: -180, maxLat: 90, maxLon: 180}, togglePublicCheckbox.checked)
        .then(response => response.json())
        .then(mappings => displayRenovations(mappings))
}

/**
 * todo update when implemented
 * Handles setting the renovations on the map and adding any interactable elements
 * Removes all pre-existing <b>renovation</b> pins
 * @param mappings The mappings to be displayed
 */
function displayRenovations(mappings) {
    mappings.forEach(mapping => console.log(mapping))
}
