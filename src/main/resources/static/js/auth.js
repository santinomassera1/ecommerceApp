/* global Swal */
document.getElementById('registerForm').addEventListener('submit', async function (e) {
    e.preventDefault();

    const username = document.getElementById('registerUsername').value;
    const email = document.getElementById('registerEmail').value;
    const password = document.getElementById('registerPassword').value;

    const user = {
        username: username,
        email: email,
        password: password
    };

    try {
        const response = await fetch('/auth/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(user)
        });

        const result = await response.json();

        if (response.ok) {
            if (result.success) {
                Swal.fire('Success', result.message, 'success')
                    .then(() => {
                        window.location.href = "/auth/login";
                    });
            } else {
                Swal.fire('Error', result.message, 'error');
            }
        } else if (response.status === 409) {
            Swal.fire('Error', result.message, 'error');
        } else {
            Swal.fire('Error', 'An unexpected error occurred. Please try again later.', 'error');
        }
    } catch (error) {
        Swal.fire('Error', 'An unexpected error occurred. Please try again later.', 'error');
    }
});

// Mover el event listener del resetPasswordForm fuera del listener del registerForm
const resetPasswordForm = document.getElementById("resetPasswordForm");
if (resetPasswordForm) {
    resetPasswordForm.addEventListener("submit", async function (event) {
        event.preventDefault();

        const formData = new FormData(this);

        const response = await fetch("/auth/reset-password", {
            method: "POST",
            body: new URLSearchParams(formData),
            headers: { "Content-Type": "application/x-www-form-urlencoded" }
        });

        const result = await response.json();

        if (result.success) {
            Swal.fire("Success", "Your password has been reset successfully!", "success")
                .then(() => {
                    window.location.href = "/auth/login";
                });
        } else {
            Swal.fire("Error", result.message, "error");
        }
    });
}
