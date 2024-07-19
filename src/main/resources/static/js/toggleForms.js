function toggleForms() {
    const loginForm = document.getElementById('loginForm');
    const registerForm = document.getElementById('registerForm');
    const button1 = document.querySelector('.button-1');
    const button2 = document.querySelector('.button-2');

    if (loginForm.style.display === 'none') {
        loginForm.style.display = 'block';
        registerForm.style.display = 'none';
        button1.textContent = 'Sign up';
        button2.textContent = 'Login';
    } else {
        loginForm.style.display = 'none';
        registerForm.style.display = 'block';
        button1.textContent = 'Login';
        button2.textContent = 'Sign up';
    }

}

