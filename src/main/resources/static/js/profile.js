document.addEventListener('DOMContentLoaded', function () {
    console.log('Inicializando perfil de usuario');
    
    // Inicializar la navegación del perfil
    initProfileNavigation();
    
    // Inicializar formularios
    initForms();
    
    // Inicializar efectos visuales
    initVisualEffects();
    
    // Inicializar validación de formularios
    initFormValidation();
    
    // Inicializar estadísticas
    updateProfileStats();
});

/**
 * Inicializa la navegación del perfil
 */
function initProfileNavigation() {
    // Obtener todos los elementos de navegación
    const navItems = document.querySelectorAll('.profile-nav .list-group-item');
    
    // Añadir clase active al primer elemento por defecto
    if (navItems.length > 0) {
        navItems[0].classList.add('active');
    }
    
    // Añadir evento de clic a cada elemento de navegación
    navItems.forEach(item => {
        item.addEventListener('click', function(e) {
            e.preventDefault();
            
            // Remover clase active de todos los elementos
            navItems.forEach(nav => nav.classList.remove('active'));
            
            // Añadir clase active al elemento clickeado
            this.classList.add('active');
            
            // Obtener el ID de la sección a mostrar
            const targetId = this.getAttribute('data-target');
            
            // Mostrar la sección correspondiente
            showSection(targetId);
        });
    });
}

/**
 * Muestra la sección seleccionada y oculta las demás
 * @param {string} sectionId - ID de la sección a mostrar
 */
function showSection(sectionId) {
    // Ocultar todas las secciones
    const sections = document.querySelectorAll('.profile-section');
    sections.forEach(section => {
        section.style.display = 'none';
    });
    
    // Mostrar la sección seleccionada
    const targetSection = document.getElementById(sectionId);
    if (targetSection) {
        targetSection.style.display = 'block';
        
        // Efecto de animación
        targetSection.style.opacity = '0';
        targetSection.style.transform = 'translateY(20px)';
        
        setTimeout(() => {
            targetSection.style.opacity = '1';
            targetSection.style.transform = 'translateY(0)';
            targetSection.style.transition = 'opacity 0.5s ease, transform 0.5s ease';
        }, 50);
    }
}

/**
 * Inicializa los formularios del perfil
 */
function initForms() {
    // Botón para editar perfil
    const editProfileBtn = document.getElementById('editProfileBtn');
    const profileForm = document.getElementById('profileForm');
    
    if (editProfileBtn && profileForm) {
        editProfileBtn.addEventListener('click', function() {
            profileForm.style.display = 'block';
            this.style.display = 'none';
            
            // Enfocar el primer campo del formulario
            const firstInput = profileForm.querySelector('input');
            if (firstInput) {
                firstInput.focus();
            }
        });
    }
    
    // Botón para cancelar edición
    const cancelEditBtn = document.getElementById('cancelEditBtn');
    if (cancelEditBtn && profileForm && editProfileBtn) {
        cancelEditBtn.addEventListener('click', function(e) {
            e.preventDefault();
            profileForm.style.display = 'none';
            editProfileBtn.style.display = 'block';
        });
    }
    
    // Inicializar subida de imagen de perfil
    initProfileImageUpload();
}

/**
 * Inicializa la subida de imagen de perfil
 */
function initProfileImageUpload() {
    const profileImageInput = document.getElementById('profileImageInput');
    const profileImagePreview = document.getElementById('profileImagePreview');
    
    if (profileImageInput && profileImagePreview) {
        profileImageInput.addEventListener('change', function() {
            const file = this.files[0];
            
            if (file) {
                const reader = new FileReader();
                
                reader.addEventListener('load', function() {
                    profileImagePreview.src = reader.result;
                });
                
                reader.readAsDataURL(file);
            }
        });
    }
}

/**
 * Inicializa efectos visuales en la página de perfil
 */
function initVisualEffects() {
    // Efecto hover para las tarjetas de productos
    const productCards = document.querySelectorAll('.product-card-profile');
    
    productCards.forEach(card => {
        card.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-5px)';
            this.style.boxShadow = '0 5px 15px rgba(0, 0, 0, 0.15)';
        });
        
        card.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0)';
            this.style.boxShadow = '0 3px 10px rgba(0, 0, 0, 0.1)';
        });
    });
}

/**
 * Inicializa la validación de formularios
 */
function initFormValidation() {
    // Validación del formulario de cambio de contraseña
    const passwordForm = document.getElementById('passwordForm');
    
    if (passwordForm) {
        passwordForm.addEventListener('submit', function(e) {
            const newPassword = document.getElementById('newPassword').value;
            const confirmPassword = document.getElementById('confirmPassword').value;
            
            if (newPassword !== confirmPassword) {
                e.preventDefault();
                
                // Mostrar mensaje de error
                const errorMsg = document.getElementById('passwordError');
                if (errorMsg) {
                    errorMsg.textContent = 'Las contraseñas no coinciden';
                    errorMsg.style.display = 'block';
                }
            }
        });
    }
}

/**
 * Actualiza las estadísticas del perfil
 */
function updateProfileStats() {
    // Obtener elementos de estadísticas
    const productCount = document.getElementById('productCount');
    const salesCount = document.getElementById('salesCount');
    
    // Actualizar contador de productos
    if (productCount) {
        const products = document.querySelectorAll('.product-card-profile');
        productCount.textContent = products.length;
    }
    
    // Actualizar contador de ventas (ejemplo)
    if (salesCount) {
        // Aquí se podría hacer una petición AJAX para obtener el número real de ventas
        // Por ahora, simplemente mostramos un número aleatorio
        salesCount.textContent = Math.floor(Math.random() * 10);
    }
}