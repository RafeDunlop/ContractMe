/**
 * Opens or closes the dropdown menu when the user clicks on the profile image
 */
function toggleUserMenus() {

    document.getElementById("dropdown").classList.toggle("show-hidden-element");
}


/**
 * Closes the dropdown menu when the user clicks anywhere on the
 * screen apart from the profile image
 * @param event the MouseEvent tracking where the user clicked
 */
window.onclick = function closeDropdownMenu(event) {
    let profileDropdown = document.getElementById("dropdown");
    let profileImage = document.getElementById("profile-image");
    if (!profileImage.contains(event.target)) {
        if (profileDropdown.classList.contains("show-hidden-element")) {
            profileDropdown.classList.remove('show-hidden-element');
        }
    }
}

function toggleMenu() {
    document.getElementById("navbar").classList.toggle("responsive");
    document.getElementById('navbar-home').classList.toggle("mobile-brand");
    document.getElementById("navbar-links").classList.toggle("mobile-navbar-links");
}
