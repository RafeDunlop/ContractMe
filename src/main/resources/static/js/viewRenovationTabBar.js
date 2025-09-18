const detailsTabItem = document.getElementById("details-tab-item");
detailsTabItem.classList.add("active");

document.querySelectorAll('.nav-link').forEach(btn => {
    btn.addEventListener('click', function () {
        console.log(this);
        if (this.classList.contains('disabled')) {
            return;
        }
        document.querySelector('.nav-link.active')?.classList.remove('active');
        this.classList.add('active');
    });
});