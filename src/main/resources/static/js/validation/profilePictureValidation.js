document.addEventListener('DOMContentLoaded', function () {
    const fileInput = document.getElementById('formFile');
    const errorAlert = document.getElementById('profile-picture-frontend-error');
    const errorList = document.getElementById('profile-picture-frontend-error-message');
    const uploadForm = fileInput.closest('form');
    const uploadButton = uploadForm.querySelector('input[type="submit"]');
    const originalInput = document.getElementById('formFile');
    const processedInput = document.getElementById('processedFile');

    const maxSizeMB = 10;
    const maxSizeBytes = maxSizeMB * 1024 * 1024;
    const allowedTypes = ['image/jpeg', 'image/png', 'image/svg+xml'];

    originalInput.addEventListener('change', async function () {
        const file = originalInput.files[0];
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
            originalInput.value = '';
            return;
        }

        // If SVG, convert to PNG
        let finalFile = file;
        if (file.type === 'image/svg+xml') {
            try {
                const pngBlob = await convertSvgToPng(file);
                finalFile = new File([pngBlob], file.name.replace('.svg', '.png'), { type: 'image/png' });
            } catch (e) {
                showError('SVG to PNG conversion failed.');
                errorAlert.hidden = false;
                uploadButton.disabled = true;
                return;
            }
        }

        // Place final file into hidden processed input
        const dt = new DataTransfer();
        dt.items.add(finalFile);
        processedInput.files = dt.files;

        processedInput.setAttribute("name", "file");
        originalInput.removeAttribute("name");
    });

    /**
     * Convert svg file to png file
     * This is needed as svg files can not be read in the backend
     * @param svgFile svg file
     * @returns {Promise<unknown>} Returns promise for png file
     */
    async function convertSvgToPng(svgFile) {
        const text = await svgFile.text();

        return new Promise((resolve, reject) => {
            const img = new Image();
            const svgBlob = new Blob([text], { type: 'image/svg+xml' });
            const url = URL.createObjectURL(svgBlob);

            img.onload = () => {
                const canvas = document.createElement('canvas');
                canvas.width = 200;
                canvas.height = 200;
                const ctx = canvas.getContext('2d');
                ctx.drawImage(img, 0, 0, 200, 200);
                canvas.toBlob(blob => {
                    URL.revokeObjectURL(url);
                    resolve(blob);
                }, 'image/png');
            };

            img.onerror = reject;
            img.src = url;
        });
    }


    uploadForm.addEventListener('submit', function (e) {
        const file = processedInput.files[0];
        if (!file || uploadButton.disabled) {
            e.preventDefault();
            showError('Cannot upload: please select a valid image.');
            errorAlert.hidden = false;
            return;
        }
        // Remove the original file input
        originalInput.remove();
    });

    /**
     * display profile picture error
     * @param message error message
     */
    function showError(message) {
        const li = document.createElement('li');
        li.textContent = message;
        errorList.appendChild(li);
    }
});
