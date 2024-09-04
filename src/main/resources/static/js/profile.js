document.addEventListener('DOMContentLoaded', function () {
    const editProfileBtn = document.getElementById('editProfileBtn');
    if (editProfileBtn) {
        editProfileBtn.addEventListener('click', function () {
            document.getElementById('profileForm').style.display = 'block';
            this.style.display = 'none';
        });
    }
});