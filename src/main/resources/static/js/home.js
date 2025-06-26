$(document).ready(function () {
    // Inicializar los componentes de UI
    initializeUI();

    // Manejar filtrado por categorías
    handleCategoryFilters();

    // Manejar ordenamiento de productos
    handleSorting();

    // Manejar filtrado por precio
    handlePriceFilter();

    // Manejar cambio de vista (cuadrícula/lista)
    handleViewChange();

    // Manejar búsqueda
    handleSearch();

    // Inicializar carruseles y funcionalidades relacionadas con productos
    initializeProductCarousels();

    // Conectar WebSocket para notificaciones
    connectStompClient();

    // Inicializar tooltips y popovers
    $('[data-toggle="tooltip"]').tooltip();
    $('[data-toggle="popover"]').popover();
    
    // Actualizar contador de favoritos
    updateFavoritesCount();
    
    // Inicializar estado de botones de favoritos
    initializeFavoriteButtons();
});

/**
 * Muestra un mensaje de bienvenida al usuario
 * Esta función ya no se llama automáticamente
 */
function showWelcomeMessage() {
    // Verificar si ya se mostró el mensaje (usando localStorage)
    if (!localStorage.getItem('welcomeShown')) {
        setTimeout(function() {
            const toast = `
                <div class="toast-container position-fixed bottom-0 end-0 p-3" style="z-index: 11; right: 20px; bottom: 20px;">
                    <div class="toast" role="alert" aria-live="assertive" aria-atomic="true" data-delay="5000">
                        <div class="toast-header">
                            <strong class="mr-auto">¡Bienvenido a Vintage Vogue!</strong>
                            <button type="button" class="ml-2 mb-1 close" data-dismiss="toast" aria-label="Close">
                                <span aria-hidden="true">&times;</span>
                            </button>
                        </div>
                        <div class="toast-body">
                            Explora nuestra colección de productos vintage y encuentra tu estilo único.
                        </div>
                    </div>
                </div>
            `;
            
            $('body').append(toast);
            $('.toast').toast('show');
            
            // Marcar como mostrado
            localStorage.setItem('welcomeShown', 'true');
        }, 1000);
    }
}

/**
 * Inicializa componentes de UI
 */
function initializeUI() {
    // Inicializar tooltips de Bootstrap
    $('[data-toggle="tooltip"]').tooltip();

    // Inicializar collapse para filtros
    $('.collapse').collapse({
        toggle: true
    });
    
    // Manejar los botones de colapso
    $('.filter-group button').on('click', function() {
        const icon = $(this).find('i.fas.fa-chevron-down, i.fas.fa-chevron-up');
        
        if ($($(this).data('target')).hasClass('show')) {
            icon.removeClass('fa-chevron-down').addClass('fa-chevron-up');
        } else {
            icon.removeClass('fa-chevron-up').addClass('fa-chevron-down');
        }
    });
}

/**
 * Maneja el filtrado por categorías
 */
function handleCategoryFilters() {
    const categoryCheckboxes = document.querySelectorAll('.category-list input[type="checkbox"]');
    const allCheckbox = document.querySelector('input[value="All"]');
    
    // Evento para checkbox "All"
    if (allCheckbox) {
        allCheckbox.addEventListener('change', function() {
            if (this.checked) {
                // Deseleccionar otras categorías
                categoryCheckboxes.forEach(cb => {
                    if (cb !== allCheckbox) cb.checked = false;
                });
                showAllProducts();
            }
        });
    }
    
    // Evento para otros checkboxes de categoría
    categoryCheckboxes.forEach(checkbox => {
        checkbox.addEventListener('change', function() {
            if (this.value !== 'All' && this.checked && allCheckbox) {
                // Si se selecciona una categoría específica, deseleccionar "All"
                allCheckbox.checked = false;
            }
            
            // Si no hay categorías seleccionadas, seleccionar "All"
            const anySelected = Array.from(categoryCheckboxes).some(cb => cb.value !== 'All' && cb.checked);
            if (!anySelected && allCheckbox) {
                allCheckbox.checked = true;
            }
            
            filterProducts();
        });
    });
}

/**
 * Maneja el ordenamiento de productos
 */
function handleSorting() {
    $('.sort-option').on('click', function(e) {
        e.preventDefault();
        
        // Actualizar UI para mostrar la opción seleccionada
        $('.sort-option').removeClass('active');
        $(this).addClass('active');
        
        const sortBy = $(this).data('sort');
        sortProducts(sortBy);
    });
}

/**
 * Maneja el filtrado por rango de precio
 */
function handlePriceFilter() {
    const minPriceInput = $('#priceRangeMin');
    const maxPriceInput = $('#priceRangeMax');
    const minPriceDisplay = $('#minPrice');
    const maxPriceDisplay = $('#maxPrice');
    
    // Actualizar displays cuando se mueven los sliders
    minPriceInput.on('input', function() {
        const minValue = parseInt($(this).val());
        const maxValue = parseInt(maxPriceInput.val());
        
        // Evitar que min sea mayor que max
        if (minValue > maxValue) {
            $(this).val(maxValue);
            minPriceDisplay.text(maxValue);
        } else {
            minPriceDisplay.text(minValue);
        }
    });
    
    maxPriceInput.on('input', function() {
        const maxValue = parseInt($(this).val());
        const minValue = parseInt(minPriceInput.val());
        
        // Evitar que max sea menor que min
        if (maxValue < minValue) {
            $(this).val(minValue);
            maxPriceDisplay.text(minValue);
        } else {
            maxPriceDisplay.text(maxValue);
        }
    });
    
    // Aplicar filtro de precio al hacer clic en el botón
    $('#applyPriceFilter').on('click', function() {
        filterProducts();
    });
}

/**
 * Maneja el cambio entre vista de cuadrícula y lista
 */
function handleViewChange() {
    $('.view-grid').on('click', function() {
        $('.view-options .btn').removeClass('active');
        $(this).addClass('active');
        $('.product-row').removeClass('list-view');
    });
    
    $('.view-list').on('click', function() {
        $('.view-options .btn').removeClass('active');
        $(this).addClass('active');
        $('.product-row').addClass('list-view');
    });
}

/**
 * Maneja la búsqueda de productos
 */
function handleSearch() {
    $('#searchInput').on('input', function() {
        const query = $(this).val().trim();

        if (query !== '') {
            fetch(`/search/suggestions?query=${encodeURIComponent(query)}`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                }
            })
                .then(response => response.json())
                .then(suggestions => {
                    const suggestionsBox = $('#suggestions');
                    suggestionsBox.empty();

                suggestions.forEach(function(item) {
                        const suggestionItem = $(`
                        <p class="suggestion-item" data-id="${item.id}" data-type="${item.type}">
                            ${item.name || item.username || item.description}
                        </p>
                    `);
                        suggestionsBox.append(suggestionItem);
                    });

                $('.suggestion-item').on('click', function() {
                        const itemId = $(this).data('id');
                        const itemType = $(this).data('type');
                        if (itemType === 'user') {
                            window.location.href = `/profile/${itemId}`;
                        } else if (itemType === 'product') {
                            window.location.href = `/products/details/${itemId}`;
                        } else if (itemType === 'category') {
                            window.location.href = `/categories/${itemId}`;
                        }
                    });
                })
                .catch(error => console.error('Error fetching suggestions:', error));
        }
    });

    $('#searchForm').on('submit', function(e) {
        e.preventDefault();
        const query = $('#searchInput').val().trim();

        if (query !== '') {
            fetch(`/search?query=${encodeURIComponent(query)}`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                }
            })
                .then(response => {
                    if (!response.ok) {
                        throw new Error('Search request failed');
                    }
                    return response.json();
                })
                .then(results => {
                    displaySearchResults(results);
                })
                .catch(error => {
                    console.error('Search failed:', error);
                });
        }
    });
}

/**
 * Filtra productos basados en categorías y rango de precio
 */
function filterProducts() {
    const selectedCategories = getSelectedCategories();
    const priceRange = getPriceRange();
    
    const products = document.querySelectorAll('.product-card');
    let visibleCount = 0;
    
    products.forEach(product => {
        const productContainer = product.closest('.product-item');
        
        // Filtrar por categoría
        const matchesCategory = selectedCategories.length === 0 || categoryMatches(product, selectedCategories);
        
        // Filtrar por precio
        const price = parseFloat(product.getAttribute('data-price')) || 0;
        const matchesPrice = price >= priceRange.min && price <= priceRange.max;
        
        // Mostrar u ocultar producto
        if (matchesCategory && matchesPrice) {
            productContainer.style.display = '';
            visibleCount++;
        } else {
            productContainer.style.display = 'none';
        }
    });
    
    // Mostrar mensaje si no hay resultados
    checkEmptyResults();
}

/**
 * Verifica si hay productos visibles y muestra mensaje si no hay resultados
 */
function checkEmptyResults() {
    // Seleccionar productos que están visibles (sin display:none)
    const visibleProducts = document.querySelectorAll('.product-item:not([style*="display: none"])');
    const emptyStateElement = document.querySelector('.empty-state');
    
    if (visibleProducts.length === 0) {
        // No hay productos visibles, mostrar mensaje
        if (!emptyStateElement) {
            const emptyState = `
                <div class="col-12 text-center py-5">
                    <div class="empty-state">
                        <i class="fas fa-box-open fa-4x text-muted mb-3"></i>
                        <h4>No hay productos disponibles</h4>
                        <p class="text-muted">No se encontraron productos que coincidan con los criterios de búsqueda.</p>
                    </div>
                </div>
            `;
            $('.product-row').append(emptyState);
        }
    } else if (emptyStateElement) {
        // Hay productos visibles, ocultar mensaje
        emptyStateElement.closest('.col-12').remove();
    }
}

/**
 * Obtiene las categorías seleccionadas
 * @returns {Array} Array de categorías seleccionadas
 */
function getSelectedCategories() {
    const checkboxes = document.querySelectorAll('.category-list input[type="checkbox"]:checked');
    const categories = [];
    
    checkboxes.forEach(checkbox => {
        if (checkbox.value !== 'All') {
            categories.push(checkbox.value.toLowerCase());
        }
    });
    
    return categories;
}

/**
 * Obtiene el rango de precio seleccionado
 * @returns {Object} Objeto con valores min y max
 */
function getPriceRange() {
    return {
        min: parseInt($('#priceRangeMin').val()) || 0,
        max: parseInt($('#priceRangeMax').val()) || 1000
    };
}

/**
 * Verifica si un producto coincide con las categorías seleccionadas
 * @param {Element} product - Elemento del producto
 * @param {Array} selectedCategories - Array de categorías seleccionadas
 * @returns {Boolean} true si coincide, false si no
 */
function categoryMatches(product, selectedCategories) {
    if (selectedCategories.length === 0) return true;
    
    const productCategoriesStr = product.getAttribute('data-categories');
    if (!productCategoriesStr) return false;
    
    const productCategories = productCategoriesStr.split(',')
        .map(cat => cat.trim().toLowerCase())
        .filter(cat => cat.length > 0);
    
    return selectedCategories.some(selectedCat => 
        productCategories.includes(selectedCat)
    );
}

/**
 * Muestra todos los productos
 */
function showAllProducts() {
    const products = document.querySelectorAll('.product-item');
    products.forEach(product => {
        product.style.display = '';
    });
    
    // Eliminar mensaje de "no hay resultados" si existe
    const emptyStateElement = document.querySelector('.empty-state');
    if (emptyStateElement) {
        emptyStateElement.closest('.col-12').remove();
    }
}

/**
 * Ordena los productos según el criterio seleccionado
 * @param {String} sortBy - Criterio de ordenamiento
 */
function sortProducts(sortBy) {
    const $productRow = $('.product-row');
    const products = $productRow.find('.product-item').get();
    
    products.sort(function(a, b) {
        const cardA = $(a).find('.product-card');
        const cardB = $(b).find('.product-card');
        
        if (sortBy === 'price-asc') {
            const priceA = parseFloat(cardA.data('price')) || 0;
            const priceB = parseFloat(cardB.data('price')) || 0;
            return priceA - priceB;
        } 
        else if (sortBy === 'price-desc') {
            const priceA = parseFloat(cardA.data('price')) || 0;
            const priceB = parseFloat(cardB.data('price')) || 0;
            return priceB - priceA;
        }
        else if (sortBy === 'newest') {
            const dateA = parseInt(cardA.data('date')) || 0;
            const dateB = parseInt(cardB.data('date')) || 0;
            return dateB - dateA;
        }
        
        return 0;
    });
    
    // Reordenar los productos en el DOM
    $.each(products, function(idx, product) {
        $productRow.append(product);
    });
}

/**
 * Muestra los resultados de la búsqueda
 * @param {Array} results - Resultados de la búsqueda
 */
    function displaySearchResults(results) {
        const $productRow = $('.product-row');
        $productRow.empty();

        if (results.length > 0) {
        results.forEach(function(item) {
                let productCard = `
                <div class="col-md-4 mb-4 product-item">
                    <div class="card product-card h-100 shadow-sm" data-price="${item.price || 0}">
                        ${item.imageUrl ? `
                            <div class="product-image-container">
                                <img class="card-img-top" src="${item.imageUrl}" alt="${item.name}">
                            </div>
                        ` : ''}
                        <div class="card-body d-flex flex-column">
                            <h5 class="card-title">
                                <a href="${item.type === 'product' ? `/products/details/${item.id}` : `/${item.type}/${item.id}`}" class="text-decoration-none text-dark">
                                    ${item.name}
                                </a>
                            </h5>
                            <p class="card-text flex-grow-1">${item.description || ''}</p>
                            ${typeof item.price !== 'undefined' ? `<p class="card-price font-weight-bold">$${item.price}</p>` : ''}
                        </div>
                        <div class="card-footer bg-white border-top-0">
                            <a href="${item.type === 'product' ? `/products/details/${item.id}` : `/${item.type}/${item.id}`}" class="btn btn-sm btn-primary">
                                Ver ${item.type === 'product' ? 'producto' : item.type}
                            </a>
                        </div>
                    </div>
                </div>
            `;
                $productRow.append(productCard);
            });
        } else {
        $productRow.append(`
            <div class="col-12 text-center py-5">
                <div class="empty-state">
                    <i class="fas fa-search fa-4x text-muted mb-3"></i>
                    <h4>No se encontraron resultados</h4>
                    <p class="text-muted">Intenta con otros términos de búsqueda.</p>
                </div>
            </div>
        `);
    }
}

/**
 * Inicializa los carruseles de productos
 */
function initializeProductCarousels() {
    // Inicializar carrusel de anuncios con autoplay
    const adCarousel = document.getElementById('adCarousel');
    if (adCarousel) {
        $(adCarousel).carousel({
            interval: 5000,  // Cambiar cada 5 segundos
            pause: false,    // No pausar al pasar el ratón
            wrap: true,      // Continuar desde el principio al llegar al final
            keyboard: false  // Deshabilitar control por teclado
        });
    }

    // Pausar todos los carruseles de productos al inicio
    $('.product-card .carousel').carousel({
        interval: false
    });
    
    // Activar carrusel solo cuando el mouse está sobre el producto
    $('.product-card').hover(
        function() {
            // Al entrar con el mouse, activar el carrusel
            $(this).find('.carousel').carousel({
                interval: 2000
            });
        },
        function() {
            // Al salir con el mouse, pausar el carrusel
            $(this).find('.carousel').carousel('pause');
        }
    );

    // Evitar que los clicks en los controles del carrusel redirijan a la página de detalles
    $(document).on('click', '.carousel-control-prev, .carousel-control-next, .carousel-indicators li', function(e) {
        e.stopPropagation();
    });
}

/**
 * Actualiza el contador de notificaciones
 */
    function updateNotificationCount() {
        fetch('/api/notifications/count')
            .then(response => response.json())
            .then(count => {
                document.getElementById('notificationCount').innerText = count;
            });
    }

/**
 * Conecta el cliente STOMP para WebSockets
 */
    function connectStompClient() {
        const socket = new SockJS('/ws');
        const stompClient = Stomp.over(socket);

    stompClient.connect({}, function(frame) {
            console.log('Connected: ' + frame);

        stompClient.subscribe('/topic/messages', function(messageOutput) {
                showMessage(JSON.parse(messageOutput.body));
                updateNotificationCount();
            });
        });
    }

/**
 * Muestra un mensaje recibido por WebSocket
 * @param {Object} message - Mensaje recibido
 */
function showMessage(message) {
    // Implementar lógica para mostrar mensajes
    console.log('Mensaje recibido:', message);
}

/**
 * Función para manejar la navegación a detalles del producto
 * @param {Number} productId - ID del producto
 */
function goToProductDetail(productId) {
    window.location.href = '/products/details/' + productId;
}

/**
 * Función para alternar el estado de "me gusta" (solo visual, sin implementación de backend)
 * @param {HTMLElement} button - El botón que se ha pulsado
 */
function toggleLike(button) {
    const icon = button.querySelector('i');
    const productCard = button.closest('.product-card');
    const productId = productCard.dataset.id || productCard.closest('.product-item').querySelector('a[href*="/products/details/"]').href.split('/').pop();
    const productName = productCard.querySelector('.card-title').textContent.trim();
    const productPrice = productCard.dataset.price || productCard.querySelector('.card-price').textContent.replace('$', '').trim();
    const productImage = productCard.querySelector('.carousel-item.active img')?.src || productCard.querySelector('.product-image-container img')?.src || '';
    
    // Obtener los favoritos actuales del localStorage
    const favorites = JSON.parse(localStorage.getItem('favoriteProducts') || '[]');
    
    if (icon.classList.contains('far')) {
        // Cambiar a corazón relleno (añadir a favoritos)
        icon.classList.remove('far');
        icon.classList.add('fas');
        icon.style.color = '#dc3545'; // Color rojo
        
        // Efecto de animación
        button.classList.add('liked');
        setTimeout(() => {
            button.classList.remove('liked');
        }, 500);
        
        // Añadir a favoritos si no existe ya
        if (!favorites.some(fav => fav.id === productId)) {
            favorites.push({
                id: productId,
                name: productName,
                price: productPrice,
                image: productImage,
                addedAt: new Date().toISOString()
            });
            localStorage.setItem('favoriteProducts', JSON.stringify(favorites));
            
            // Actualizar contador de favoritos en el navbar si existe
            updateFavoritesCount();
        }
    } else {
        // Cambiar a corazón vacío (quitar de favoritos)
        icon.classList.remove('fas');
        icon.classList.add('far');
        icon.style.color = ''; // Color por defecto
        
        // Eliminar de favoritos
        const updatedFavorites = favorites.filter(fav => fav.id !== productId);
        localStorage.setItem('favoriteProducts', JSON.stringify(updatedFavorites));
        
        // Actualizar contador de favoritos en el navbar si existe
        updateFavoritesCount();
    }
}

/**
 * Actualiza el contador de productos favoritos en el navbar
 */
function updateFavoritesCount() {
    const favorites = JSON.parse(localStorage.getItem('favoriteProducts') || '[]');
    const favCountElement = document.getElementById('favoritesCount');
    
    if (favCountElement) {
        favCountElement.textContent = favorites.length > 0 ? favorites.length : '';
    }
}

/**
 * Inicializa el estado de los botones de favoritos según localStorage
 */
function initializeFavoriteButtons() {
    const favorites = JSON.parse(localStorage.getItem('favoriteProducts') || '[]');
    const likeButtons = document.querySelectorAll('.btn-like');
    
    likeButtons.forEach(button => {
        const productCard = button.closest('.product-card');
        const productId = productCard.dataset.id || productCard.closest('.product-item').querySelector('a[href*="/products/details/"]').href.split('/').pop();
        
        // Si el producto está en favoritos, mostrar el corazón lleno
        if (favorites.some(fav => fav.id === productId)) {
            const icon = button.querySelector('i');
            icon.classList.remove('far');
            icon.classList.add('fas');
            icon.style.color = '#dc3545'; // Color rojo
        }
    });
}
