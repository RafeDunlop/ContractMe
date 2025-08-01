let profilePicture = document.getElementById("profile-picture");

document.addEventListener("DOMContentLoaded", saveProfilePictureToLocalStorage);

/**
 * Saves default profile image to localStorage.
 */
function saveProfilePictureToLocalStorage() {
    localStorage.setItem("profilePicture", profilePicture.src);
}

/**
 * Retrieves the profile picture from local storage
 * @returns profilePicture the profile picture stored in local storage
 */
function getProfilePictureFromLocalStorage() {
    profilePicture.src = localStorage.getItem("profilePicture");
}


