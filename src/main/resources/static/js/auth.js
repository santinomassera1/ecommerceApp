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
