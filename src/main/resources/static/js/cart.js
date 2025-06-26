// cart.js

document.addEventListener("DOMContentLoaded", function () {
    // Inicializar componentes
    initCartComponents();
    
    // Manejar botones de añadir al carrito
    setupAddToCartButtons();
    
    // Manejar botones de cantidad
    setupQuantityButtons();
    
    // Actualizar contador del carrito
    updateCartCounter();
});

/**
 * Inicializa los componentes del carrito
 */
function initCartComponents() {
    // Inicializar tooltips de Bootstrap
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });
    
    // Crear contenedor para toasts si no existe
    if (!document.getElementById('toast-container')) {
        const toastContainer = document.createElement('div');
        toastContainer.id = 'toast-container';
        toastContainer.className = 'position-fixed bottom-0 end-0 p-3';
        toastContainer.style.zIndex = '11';
        document.body.appendChild(toastContainer);
    }
}

/**
 * Configura los botones de añadir al carrito
 */
function setupAddToCartButtons() {
    const addToCartButtons = document.querySelectorAll(".add-to-cart-btn");

    addToCartButtons.forEach(button => {
        button.addEventListener("click", function (event) {
            event.preventDefault();

            const productId = button.dataset.productId;
            const quantityInput = document.querySelector(`#quantity-${productId}`);
            const quantity = quantityInput ? quantityInput.value : 1;
            
            // Añadir efecto visual al botón
            button.classList.add('btn-loading');
            button.disabled = true;
            
            // Guardar el texto original
            const originalText = button.innerHTML;
            button.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Añadiendo...';

            fetch(`/cart/add/${productId}`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded",
                    "X-CSRF-Token": document.querySelector('input[name="_csrf"]') ? document.querySelector('input[name="_csrf"]').value : ''
                },
                body: new URLSearchParams({ quantity: quantity })
            })
            .then(response => response.json())
            .then(data => {
                // Restaurar el botón
                button.classList.remove('btn-loading');
                button.disabled = false;
                button.innerHTML = originalText;
                
                if (data.success) {
                    // Mostrar toast de éxito
                    showToast('Producto añadido', 'El producto ha sido añadido al carrito correctamente.', 'success');
                    
                    // Actualizar contador del carrito
                    updateCartCounter();
                    
                    // Opcionalmente redirigir al carrito después de un breve retraso
                    setTimeout(() => {
                        window.location.href = "/cart";
                    }, 1000);
                } else {
                    // Mostrar mensaje de error específico
                    if (data.message && data.message.includes("ya está en tu carrito")) {
                        showToast('Producto ya en carrito', 'Este producto ya está en tu carrito. No puedes agregarlo nuevamente.', 'warning');
                        
                        // Opcionalmente redirigir al carrito
                        setTimeout(() => {
                            window.location.href = "/cart";
                        }, 1500);
                    } else {
                        showToast('Error', data.message || "Error al agregar el producto al carrito", 'danger');
                    }
                }
            })
            .catch(error => {
                console.error("Error:", error);
                
                // Restaurar el botón
                button.classList.remove('btn-loading');
                button.disabled = false;
                button.innerHTML = originalText;
                
                showToast('Error', 'Ocurrió un error al procesar tu solicitud. Por favor, inténtalo de nuevo.', 'danger');
            });
        });
    });
}

/**
 * Configura los botones de incremento/decremento de cantidad
 */
function setupQuantityButtons() {
    // Botones de decremento
    document.querySelectorAll('.btn-quantity-decrease').forEach(button => {
        button.addEventListener('click', function() {
            const input = this.parentNode.querySelector('input[type=number]');
            const currentValue = parseInt(input.value);
            if (currentValue > parseInt(input.min)) {
                input.value = currentValue - 1;
                input.dispatchEvent(new Event('change'));
            }
        });
    });
    
    // Botones de incremento
    document.querySelectorAll('.btn-quantity-increase').forEach(button => {
        button.addEventListener('click', function() {
            const input = this.parentNode.querySelector('input[type=number]');
            const currentValue = parseInt(input.value);
            if (currentValue < parseInt(input.max)) {
                input.value = currentValue + 1;
                input.dispatchEvent(new Event('change'));
            }
        });
    });
    
    // Validar entradas manuales
    document.querySelectorAll('.quantity-input').forEach(input => {
        input.addEventListener('change', function() {
            let value = parseInt(this.value);
            const min = parseInt(this.min) || 1;
            const max = parseInt(this.max) || 99;
            
            if (isNaN(value) || value < min) {
                this.value = min;
            } else if (value > max) {
                this.value = max;
            }
        });
    });
}

/**
 * Actualiza el contador de productos en el carrito en la barra de navegación
 */
function updateCartCounter() {
    fetch('/cart/count')
        .then(response => response.json())
        .then(data => {
            const cartCounter = document.getElementById('cartCounter');
            if (cartCounter) {
                if (data.count > 0) {
                    cartCounter.textContent = data.count;
                    cartCounter.classList.remove('d-none');
                    
                    // Añadir animación
                    cartCounter.classList.add('cart-counter-update');
                    setTimeout(() => {
                        cartCounter.classList.remove('cart-counter-update');
                    }, 500);
                } else {
                    cartCounter.textContent = '';
                    cartCounter.classList.add('d-none');
                }
            }
        })
        .catch(error => console.error('Error actualizando contador del carrito:', error));
}

/**
 * Muestra una notificación toast
 * @param {string} title - Título del toast
 * @param {string} message - Mensaje del toast
 * @param {string} type - Tipo de toast (success, warning, danger, info)
 */
function showToast(title, message, type = 'info') {
    const toastContainer = document.getElementById('toast-container');
    
    // Crear elemento toast
    const toastElement = document.createElement('div');
    toastElement.className = `toast align-items-center border-0 bg-${type === 'danger' ? 'danger' : type === 'warning' ? 'warning' : type === 'success' ? 'success' : 'light'}`;
    toastElement.setAttribute('role', 'alert');
    toastElement.setAttribute('aria-live', 'assertive');
    toastElement.setAttribute('aria-atomic', 'true');
    
    // Crear contenido del toast
    const textClass = type === 'danger' || type === 'success' ? 'text-white' : '';
    
    toastElement.innerHTML = `
        <div class="d-flex">
            <div class="toast-body ${textClass}">
                <strong>${title}</strong>: ${message}
            </div>
            <button type="button" class="btn-close ${textClass ? 'btn-close-white' : ''} me-2 m-auto" data-bs-dismiss="toast" aria-label="Cerrar"></button>
        </div>
    `;
    
    // Añadir al contenedor
    toastContainer.appendChild(toastElement);
    
    // Inicializar y mostrar toast
    const toast = new bootstrap.Toast(toastElement, {
        autohide: true,
        delay: 3000
    });
    
    toast.show();
    
    // Eliminar del DOM después de ocultarse
    toastElement.addEventListener('hidden.bs.toast', function () {
        toastElement.remove();
    });
}