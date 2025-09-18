import {fetchRenovationMappings} from "./mapCommons.js";

const togglePublicCheckbox = document.getElementById("include-public-renovations-checkbox");
togglePublicCheckbox.addEventListener("click", toggleListener)

/**
 * Listener called when the toggle button for displaying public renovations is called. Posts an immediate redisplay of
 * the mappings
 * todo fix bounds
 */
function toggleListener() {
    fetchRenovationMappings({}, togglePublicCheckbox.checked).then(
        mappings => displayRenovations(mappings)
    )
}

/**
 * todo update when implemented
 * Handles setting the renovations on the map and adding any interactable elements
 * Removes all pre-existing <b>renovation</b> pins
 * @param mappings The mappings to be displayed
 */
function displayRenovations(mappings) {
    console.log(mappings);
}
