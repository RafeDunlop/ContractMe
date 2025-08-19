for (const element of document.getElementsByClassName("team-request")) {
    element.addEventListener("click", showRequest);
}

async function showRequest(event) {
    const element = event.currentTarget;
    const teamId = element.dataset.teamId;
    const response = await fetch(`renovations/team/join-team?id=${teamId}`);
    if (response.ok) {
        const responseText = await response.text();
        const container = document.getElementById("join-team");
        const tmp = document.createElement("div");
        tmp.innerHTML = responseText;
        const newWrapper = tmp.querySelector("#join-team");
        if (newWrapper) {
            container.replaceWith(newWrapper);
        }
    }
}