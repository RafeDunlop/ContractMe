document.addEventListener('DOMContentLoaded', function () {
    const fileInput = document.getElementById('formFile');
    const errorAlert = document.getElementById('profile-picture-frontend-error');
    const errorList = document.getElementById('profile-picture-frontend-error-message');
    const uploadForm = fileInput.closest('form');
    const uploadButton = uploadForm.querySelector('input[type="submit"]');

    const maxSizeMB = 10;
    const maxSizeBytes = maxSizeMB * 1024 * 1024;
    const allowedTypes = ['image/jpeg', 'image/png', 'image/svg+xml'];

    fileInput.addEventListener('change', function () {
        const file = fileInput.files[0];
        errorList.innerHTML = '';
        errorAlert.hidden = true;
        uploadButton.disabled = false;

        if (!file) {
            showError('No file selected.');
            return;
        }

        const errors = [];

        if (!allowedTypes.includes(file.type)) {
            errors.push('Image must be of type png, jpg or svg.');
        }

        if (file.size > maxSizeBytes) {
            errors.push(`Image must be less than ${maxSizeMB} MB.`);
        }

        if (errors.length > 0) {
            errors.forEach(showError);
            errorAlert.hidden = false;
            uploadButton.disabled = true;
            fileInput.value = ''; // clear the file input
        }
    });

    uploadForm.addEventListener('submit', function (e) {
        const file = fileInput.files[0];
        if (!file || uploadButton.disabled) {
            e.preventDefault(); // prevent form submission
            showError('Cannot upload: please select a valid image.');
            errorAlert.hidden = false;
        }
    });

    function showError(message) {
        const li = document.createElement('li');
        li.textContent = message;
        errorList.appendChild(li);
    }
});
