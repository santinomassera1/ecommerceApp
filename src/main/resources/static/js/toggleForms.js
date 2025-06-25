function toggleForms() {
    const loginForm = document.getElementById('loginForm');
    const registerForm = document.getElementById('registerForm');
    const button1 = document.querySelector('.button-1');
    const button2 = document.querySelector('.button-2');

    if (loginForm.style.display === 'none') {
        loginForm.style.display = 'block';
        registerForm.style.display = 'none';
        button1.textContent = 'Registrarse';
        button2.textContent = 'Iniciar Sesión';
    } else {
        loginForm.style.display = 'none';
        registerForm.style.display = 'block';
        button1.textContent = 'Iniciar Sesión';
        button2.textContent = 'Registrarse';
    }
}