import {
    confirmPrompt,
    confirmDelete,
    confirmLogout
} from "./confirmPrompt.js";

window.confirmPrompt = confirmPrompt;
window.confirmDelete = confirmDelete;
window.confirmLogout = confirmLogout;
window.toggleUserMenus = toggleUserMenus;
window.toggleNavbarMenu = toggleNavbarMenu;

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

/**
 * Toggles the hamburger display menu to show navbar links on
 * minimised page
 */
function toggleNavbarMenu() {
    document.getElementById("navbar").classList.toggle("responsive");
    document.getElementById('navbar-home').classList.toggle("mobile-brand");
    document.getElementById("navbar-links").classList.toggle("mobile-navbar-links");
    document.getElementById("dropdown").classList.toggle("mobile-dropdown");
    document.getElementById("profile-container").classList.toggle("mobile-profile");
}

window.addEventListener("resize", closeHamburgerBar);

/**
 * Closes the hamburger display menu when screen is resized
 * back to the size where navbar elements can display normally
 */
function closeHamburgerBar() {
    if (window.innerWidth > 900) {
        console.log("element styles changed")
        document.getElementById("navbar").classList.remove("responsive");
        document.getElementById('navbar-home').classList.remove("mobile-brand");
        document.getElementById("navbar-links").classList.remove("mobile-navbar-links");
        document.getElementById("dropdown").classList.remove("mobile-dropdown");
        document.getElementById("profile-container").classList.remove("mobile-profile");
    }
}
