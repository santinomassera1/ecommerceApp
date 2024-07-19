document.addEventListener('DOMContentLoaded', function() {
    const forgotPasswordForm = document.getElementById('forgotPasswordForm');
    if (forgotPasswordForm) {
        forgotPasswordForm.addEventListener('submit', function(event) {
            event.preventDefault();
            const form = event.target;
            const data = new URLSearchParams(new FormData(form));

            fetch('/auth/forgot-password', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(Object.fromEntries(data)),
            })
                .then(response => response.json())
                .then(result => {
                    if (result.success) {
                        Swal.fire({
                            title: 'Success!',
                            text: result.message,
                            icon: 'success',
                            confirmButtonText: 'OK'
                        });
                    } else {
                        Swal.fire({
                            title: 'Error!',
                            text: result.message,
                            icon: 'error',
                            confirmButtonText: 'OK'
                        });
                    }
                })
                .catch(error => {
                    Swal.fire({
                        title: 'Error!',
                        text: 'An error occurred. Please try again later.',
                        icon: 'error',
                        confirmButtonText: 'OK'
                    });
                });
        });
    }

    const resetPasswordForm = document.getElementById('resetPasswordForm');
    if (resetPasswordForm) {
        resetPasswordForm.addEventListener('submit', function(event) {
            event.preventDefault();
            const form = event.target;
            const data = new URLSearchParams(new FormData(form));

            fetch('/auth/reset-password', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(Object.fromEntries(data)),
            })
                .then(response => response.json())
                .then(result => {
                    if (result.success) {
                        Swal.fire({
                            title: 'Success!',
                            text: result.message,
                            icon: 'success',
                            confirmButtonText: 'OK'
                        }).then(() => {
                            window.location.href = '/auth/login';
                        });
                    } else {
                        Swal.fire({
                            title: 'Error!',
                            text: result.message,
                            icon: 'error',
                            confirmButtonText: 'OK'
                        });
                    }
                })
                .catch(error => {
                    Swal.fire({
                        title: 'Error!',
                        text: 'An error occurred. Please try again later.',
                        icon: 'error',
                        confirmButtonText: 'OK'
                    });
                });
        });
    }

    const registerForm = document.getElementById('registerForm');
    if (registerForm) {
        registerForm.addEventListener('submit', function(event) {
            event.preventDefault();
            const form = event.target;
            const data = new URLSearchParams(new FormData(form));

            fetch('/auth/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(Object.fromEntries(data)),
            })
                .then(response => response.json())
                .then(result => {
                    if (result.success) {
                        Swal.fire({
                            title: 'Success!',
                            text: 'Registration successful! Please check your email to verify your account.',
                            icon: 'success',
                            confirmButtonText: 'OK'
                        }).then(() => {
                            window.location.href = '/auth/login';
                        });
                    } else {
                        Swal.fire({
                            title: 'Error!',
                            text: 'Registration failed. Please try again.',
                            icon: 'error',
                            confirmButtonText: 'OK'
                        });
                    }
                })
                .catch(error => {
                    Swal.fire({
                        title: 'Error!',
                        text: 'An error occurred. Please try again later.',
                        icon: 'error',
                        confirmButtonText: 'OK'
                    });
                });
        });
    }
});
