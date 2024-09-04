$(document).ready(function () {
    // Activación del tooltip
    $('[data-toggle="tooltip"]').tooltip();

    // Event listener para el filtro de categorías
    $('.list-group-item input[type="checkbox"]').on('change', function () {
        const selectedCategories = $('.list-group-item input[type="checkbox"]:checked')
            .map(function () { return $(this).val().toLowerCase(); }).get();
        filterProducts(selectedCategories);
    });

    // Event listener para ordenar productos
    $('#sortButton').on('click', '.dropdown-item', function () {
        const sortBy = $(this).data('sort');
        sortProducts(sortBy);
    });

    // Event listener para sugerencias
    $('#searchInput').on('input', function () {
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

                    suggestions.forEach(function (item) {
                        const suggestionItem = $(`
                        <p class="suggestion-item" data-id="${item.id}" data-type="${item.type}">
                            ${item.name || item.username || item.description}
                        </p>
                    `);
                        suggestionsBox.append(suggestionItem);
                    });

                    // Añadir el evento click a las sugerencias
                    $('.suggestion-item').on('click', function () {
                        const itemId = $(this).data('id');
                        const itemType = $(this).data('type');

                        // Redirigir a la página correspondiente según el tipo de elemento
                        if (itemType === 'user') {
                            window.location.href = `/profile/${itemId}`;
                        } else if (itemType === 'product') {
                            window.location.href = `/products/${itemId}`;
                        } else if (itemType === 'category') {
                            window.location.href = `/categories/${itemId}`;
                        }
                    });
                })
                .catch(error => console.error('Error fetching suggestions:', error));
        }
    });

    // Event listener para el formulario de búsqueda
    $('#searchForm').on('submit', function (e) {
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

    // Función para mostrar resultados de la búsqueda
    function displaySearchResults(results) {
        const $productRow = $('.product-row');
        $productRow.empty(); // Limpia la pantalla actual de productos

        if (results.length > 0) {
            results.forEach(function (item) {
                let productCard = `
                <div class="card">
                    ${item.imageUrl ? `<img class="card-img-top" src="${item.imageUrl}" alt="${item.name}">` : ''}
                    <div class="card-body">
                        <h5 class="card-title">${item.name}</h5>
                        <p class="card-text">${item.description || ''}</p>
                        ${typeof item.price !== 'undefined' ? `<p class="card-price">$${item.price}</p>` : ''}
                        <a href="/${item.type}/${item.id}" class="btn btn-primary">View ${item.type}</a>
                    </div>
                </div>
            `;
                $productRow.append(productCard);
            });
        } else {
            $productRow.append('<p>No results found</p>');
        }
    }

    // Función para ordenar productos
    function sortProducts(sortBy) {
        const $productRow = $('.product-row');
        const products = $('.card').get();
        products.sort(function (a, b) {
            const priceA = parseFloat($(a).find('.card-price').text().replace('$', '').replace(',', '')) || 0;
            const priceB = parseFloat($(b).find('.card-price').text().replace('$', '').replace(',', '')) || 0;
            if (sortBy === 'price-asc') {
                return priceA - priceB;
            } else if (sortBy === 'price-desc') {
                return priceB - priceA;
            } else {
                return 0;
            }
        });
        $.each(products, function (idx, product) {
            $productRow.append(product);
        });
    }

    // Función para filtrar productos
    function filterProducts(categories) {
        if (categories.length === 0 || categories.includes('all')) {
            $('.card').show();
        } else {
            $('.card').each(function () {
                const productCategory = $(this).find('.card-category').text().trim().toLowerCase();
                if (categories.includes(productCategory)) {
                    $(this).show();
                } else {
                    $(this).hide();
                }
            });
        }
    }

    function updateNotificationCount() {
        fetch('/api/notifications/count')
            .then(response => response.json())
            .then(count => {
                document.getElementById('notificationCount').innerText = count;
            });
    }

    // Inicializar la conexión con StompJS
    function connectStompClient() {
        const socket = new SockJS('/ws');
        const stompClient = Stomp.over(socket);

        stompClient.connect({}, function (frame) {
            console.log('Connected: ' + frame);

            // Suscribirse a la cola de mensajes para recibir notificaciones
            stompClient.subscribe('/topic/messages', function (messageOutput) {
                showMessage(JSON.parse(messageOutput.body));
                updateNotificationCount(); // Actualizar el contador de notificaciones
            });
        });
    }

    connectStompClient();
});
