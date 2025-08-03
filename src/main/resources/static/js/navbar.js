function displayMinimisedMenu() {}


function toggleUserMenus() {
    console.log("funct loaded")

    document.getElementById("dropdown").classList.toggle("show-dropdown-box");
}

window.toggleUserMenus = toggleUserMenus;