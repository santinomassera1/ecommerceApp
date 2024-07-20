document.addEventListener('DOMContentLoaded', function() {
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.has('loginSuccess')) {
        Swal.fire({
            title: 'Welcome!',
            text: 'You have successfully logged in. Welcome to Vintage Vogue!',
            icon: 'success',
            confirmButtonText: 'OK'
        });
    }
});