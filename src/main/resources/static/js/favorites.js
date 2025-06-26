$(document).ready(function() {
    console.log('Inicializando página de favoritos');
    
    // Cargar productos favoritos
    loadFavorites();
    
    // Actualizar contador de favoritos
    updateFavoritesCount();
    
    // Manejar eventos de clic en botones de eliminar
    $(document).on('click', '.remove-favorite', function(e) {
        e.preventDefault();
        e.stopPropagation();
        
        const button = $(this);
        const productId = button.data('id');
        
        console.log('Botón de eliminar clickeado, ID:', productId);
        
        handleRemoveFavorite(e, productId);
    });
    
    // Manejar botón de eliminar todos los favoritos
    $('#clear-all-favorites').on('click', function() {
        if (confirm('¿Estás seguro de que deseas eliminar todos tus productos favoritos?')) {
            // Animar la eliminación de todos los productos
            $('.product-item').addClass('removing');
            
            setTimeout(function() {
                localStorage.removeItem('favoriteProducts');
                loadFavorites();
                updateFavoritesCount();
                showToast('Todos los productos han sido eliminados de tus favoritos.');
            }, 500);
        }
    });
});

/**
 * Carga los productos favoritos desde localStorage y los muestra en la página
 */
function loadFavorites() {
    const favorites = JSON.parse(localStorage.getItem('favoriteProducts') || '[]');
    const container = $('#favorites-container');
    const noFavorites = $('#no-favorites');
    
    console.log('Cargando favoritos:', favorites);
    
    // Limpiar el contenedor
    container.empty();
    
    if (favorites.length > 0) {
        // Ocultar mensaje de "no favoritos"
        noFavorites.hide();
        
        // Ordenar por fecha de adición (más recientes primero)
        favorites.sort((a, b) => new Date(b.addedAt) - new Date(a.addedAt));
        
        // Crear tarjetas de productos
        favorites.forEach(product => {
            // Crear HTML directamente
            const productCard = `
                <div class="col-md-4 mb-4 product-item" data-product-id="${product.id}">
                    <div class="card product-card h-100 shadow-sm">
                        <div class="product-image-container">
                            <img src="${product.image || '/images/default-image.png'}" class="card-img-top" alt="${product.name}" 
                                 style="height: 200px; object-fit: cover;">
                        </div>
                        
                        <div class="card-body d-flex flex-column">
                            <h5 class="card-title">
                                <a href="/products/details/${product.id}" class="text-decoration-none text-dark">
                                    ${product.name}
                                </a>
                            </h5>
                            <div class="d-flex justify-content-between align-items-center mt-2">
                                <span class="card-price font-weight-bold">$${product.price}</span>
                                <small class="text-muted">Añadido el ${formatDate(product.addedAt)}</small>
                            </div>
                        </div>
                        
                        <div class="card-footer bg-white border-top-0">
                            <div class="d-flex justify-content-between">
                                <a href="/products/details/${product.id}" class="btn btn-sm btn-primary">
                                    <i class="fas fa-eye"></i> Ver detalles
                                </a>
                                <button type="button" class="btn btn-sm btn-outline-danger remove-favorite" data-id="${product.id}">
                                    <i class="fas fa-trash"></i> Eliminar
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            `;
            
            container.append(productCard);
        });
        
        // Verificar que los botones se hayan creado correctamente
        console.log('Botones de eliminar creados:', $('.remove-favorite').length);
    } else {
        // Mostrar mensaje de "no favoritos"
        noFavorites.show();
    }
}

/**
 * Maneja la eliminación de un producto favorito
 * @param {Event} e - Evento del clic
 * @param {String} productId - ID del producto
 */
function handleRemoveFavorite(e, productId) {
    console.log('Manejando eliminación de favorito:', productId);
    
    // Obtener el elemento del producto y su nombre
    const productItem = $(e.target).closest('.product-item');
    let productName = 'este producto';
    
    try {
        productName = productItem.find('.card-title a').text().trim();
    } catch (error) {
        console.error('Error al obtener el nombre del producto:', error);
    }
    
    // Confirmar eliminación
    if (confirm(`¿Estás seguro de que deseas eliminar "${productName}" de tus favoritos?`)) {
        if (productItem.length > 0) {
            console.log('Elemento encontrado, iniciando animación');
            productItem.fadeOut(300, function() {
                removeFavorite(productId);
                showToast(`"${productName}" ha sido eliminado de tus favoritos.`);
            });
        } else {
            console.error('No se encontró el elemento .product-item');
            removeFavorite(productId);
            showToast(`"${productName}" ha sido eliminado de tus favoritos.`);
        }
    }
}

/**
 * Elimina un producto de favoritos
 * @param {String} productId - ID del producto a eliminar
 */
function removeFavorite(productId) {
    const favorites = JSON.parse(localStorage.getItem('favoriteProducts') || '[]');
    const updatedFavorites = favorites.filter(fav => fav.id !== productId);
    
    localStorage.setItem('favoriteProducts', JSON.stringify(updatedFavorites));
    
    // Recargar la lista de favoritos si no hay productos visibles o si se eliminaron todos
    if ($('.product-item:visible').length === 0 || updatedFavorites.length === 0) {
        loadFavorites();
    }
    
    // Actualizar contador
    updateFavoritesCount();
}

/**
 * Actualiza el contador de productos favoritos en el navbar
 */
function updateFavoritesCount() {
    const favorites = JSON.parse(localStorage.getItem('favoriteProducts') || '[]');
    const favCountElement = document.getElementById('favoritesCount');
    
    if (favCountElement) {
        // Añadir clase para animación
        favCountElement.classList.add('updating');
        
        // Actualizar el contador
        favCountElement.textContent = favorites.length > 0 ? favorites.length : '';
        
        // Quitar la clase después de la animación
        setTimeout(() => {
            favCountElement.classList.remove('updating');
        }, 500);
    }
}

/**
 * Formatea una fecha ISO a un formato legible
 * @param {String} isoDate - Fecha en formato ISO
 * @returns {String} Fecha formateada
 */
function formatDate(isoDate) {
    const date = new Date(isoDate);
    return date.toLocaleDateString('es-ES', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric'
    });
}

/**
 * Muestra un mensaje toast en la parte inferior de la pantalla
 * @param {String} message - Mensaje a mostrar
 */
function showToast(message) {
    // Crear el elemento toast si no existe
    if ($('#toast-container').length === 0) {
        $('body').append(`
            <div id="toast-container" class="position-fixed bottom-0 right-0 p-3" style="z-index: 5; right: 0; bottom: 0;">
            </div>
        `);
    }
    
    // Generar un ID único para este toast
    const toastId = 'toast-' + Date.now();
    
    // Crear el toast
    const toast = `
        <div id="${toastId}" class="toast" role="alert" aria-live="assertive" aria-atomic="true" data-delay="3000">
            <div class="toast-header">
                <strong class="mr-auto">Vintage Vogue</strong>
                <button type="button" class="ml-2 mb-1 close" data-dismiss="toast" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div class="toast-body">
                ${message}
            </div>
        </div>
    `;
    
    // Añadir el toast al contenedor
    $('#toast-container').append(toast);
    
    // Mostrar el toast
    $(`#${toastId}`).toast('show');
    
    // Eliminar el toast cuando se oculte
    $(`#${toastId}`).on('hidden.bs.toast', function() {
        $(this).remove();
    });
} 