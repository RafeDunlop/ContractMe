function displayMinimisedMenu() {}

console.log("script loaded")

function displayUserMenus() {
    console.log("user menus displayed");
    document.getElementById("user-menus").classList.toggle("dropDownBox");
}

window.displayUserMenus = displayUserMenus;