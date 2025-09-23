const overlay = document.getElementById("overlay");

function selectContractorToInvite(event) {
    overlay.style.display = 'block';
    console.log("did function")
}

function hideMap() {
    overlay.style.display = "none";
}
