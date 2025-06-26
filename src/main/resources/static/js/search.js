document.addEventListener('DOMContentLoaded', function() {
    const searchInput = document.getElementById("searchInput");
    const suggestionsBox = document.getElementById("suggestions");
    
    console.log("Search.js cargado - Inicializando búsqueda");
    
    if (!searchInput) {
        console.error("Elemento de búsqueda no encontrado");
        return;
    }
    
    if (!suggestionsBox) {
        console.error("Contenedor de sugerencias no encontrado");
        return;
    }
    
    // Función para mostrar sugerencias
    function showSuggestions(query) {
        console.log("Buscando sugerencias para:", query);

    if (query.length > 2) {
            const url = `/search/suggestions?query=${encodeURIComponent(query)}`;
            console.log("Realizando petición a:", url);
            
            fetch(url)
                .then(response => {
                    if (!response.ok) {
                        throw new Error(`Error en la respuesta: ${response.status}`);
                    }
                    return response.json();
                })
            .then(data => {
                    console.log("Sugerencias recibidas:", data);
                suggestionsBox.innerHTML = "";  // Limpiar sugerencias anteriores
                    
                    if (data.length === 0) {
                        suggestionsBox.innerHTML = '<div class="suggestion-item no-results">No se encontraron resultados</div>';
                        suggestionsBox.classList.add('show');
                        console.log("No se encontraron resultados");
                        return;
                    }

                data.forEach(item => {
                    let suggestion = document.createElement("div");
                    suggestion.classList.add("suggestion-item");
                    
                        // Determinar icono según el tipo
                        let icon = 'tag';
                        if (item.type === "user") icon = 'user';
                        else if (item.type === "category") icon = 'folder';
                        
                        // Mostrar nombre, tipo y precio si es un producto
                    suggestion.innerHTML = `
                        <div>
                                <i class="fas fa-${icon} me-2 text-muted"></i>
                            <span>${item.name}</span>
                            </div>
                            <div>
                            ${item.price ? `<span>$${item.price}</span>` : ''}
                        </div>
                    `;

                    // Redirigir según el tipo de resultado
                    suggestion.addEventListener("click", function() {
                            console.log("Sugerencia seleccionada:", item);
                        if (item.type === "product") {
                            window.location.href = `/products/details/${item.id}`;
                        } else if (item.type === "user") {
                            window.location.href = `/profile/${item.id}`;
                        } else if (item.type === "category") {
                            window.location.href = `/category/${item.id}`;
                        }
                    });

                    suggestionsBox.appendChild(suggestion);
                });
                    
                    suggestionsBox.classList.add('show');
                    console.log("Sugerencias mostradas:", data.length);
                    
                    // Forzar la visualización de las sugerencias
                    setTimeout(() => {
                        suggestionsBox.style.display = 'block';
                        suggestionsBox.style.visibility = 'visible';
                        suggestionsBox.style.opacity = '1';
                    }, 10);
                })
                .catch(error => {
                    console.error("Error al obtener sugerencias:", error);
                });
        } else {
            suggestionsBox.innerHTML = "";
            suggestionsBox.classList.remove('show');
            console.log("Consulta demasiado corta para mostrar sugerencias");
        }
    }
    
    // Manejar la entrada en el campo de búsqueda
    searchInput.addEventListener("input", function() {
        let query = this.value.trim();
        console.log("Entrada de búsqueda:", query);
        showSuggestions(query);
    });
    
    // Manejar el envío del formulario
    searchInput.closest('form').addEventListener('submit', function(e) {
        e.preventDefault();
        const query = searchInput.value.trim();
        
        console.log("Formulario enviado con consulta:", query);
        
        if (query.length > 0) {
            window.location.href = `/search?query=${encodeURIComponent(query)}`;
        }
    });
    
    // Limpiar búsqueda con la tecla Escape
    searchInput.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            this.value = '';
            suggestionsBox.classList.remove('show');
            this.blur();
            console.log("Búsqueda limpiada con Escape");
        }
    });
    
    // Cerrar sugerencias al hacer clic fuera
    document.addEventListener('click', function(e) {
        if (!e.target.closest('.search-container')) {
            suggestionsBox.classList.remove('show');
            console.log("Sugerencias cerradas por clic fuera");
        }
    });
    
    // Manejar navegación con teclado en las sugerencias
    searchInput.addEventListener('keydown', function(e) {
        const items = suggestionsBox.querySelectorAll('.suggestion-item');
        
        if (!items.length) return;
        
        const currentIndex = Array.from(items).findIndex(item => item.classList.contains('selected'));
        
        switch (e.key) {
            case 'ArrowDown':
                e.preventDefault();
                navigateSuggestions(currentIndex, 1, items);
                console.log("Navegación: abajo");
                break;
            case 'ArrowUp':
                e.preventDefault();
                navigateSuggestions(currentIndex, -1, items);
                console.log("Navegación: arriba");
                break;
            case 'Enter':
                e.preventDefault();
                const selectedItem = suggestionsBox.querySelector('.suggestion-item.selected');
                if (selectedItem) {
                    selectedItem.click();
                    console.log("Selección por Enter: elemento seleccionado");
                } else if (searchInput.value.trim().length > 0) {
                    searchInput.closest('form').submit();
                    console.log("Selección por Enter: envío de formulario");
                }
                break;
        }
    });
    
    // Función para navegar por las sugerencias con teclado
    function navigateSuggestions(currentIndex, direction, items) {
        // Quitar selección actual
        items.forEach(item => item.classList.remove('selected'));
        
        // Calcular nuevo índice
        let newIndex;
        if (currentIndex === -1) {
            newIndex = direction > 0 ? 0 : items.length - 1;
        } else {
            newIndex = (currentIndex + direction + items.length) % items.length;
        }
        
        // Aplicar nueva selección
        items[newIndex].classList.add('selected');
        items[newIndex].scrollIntoView({ block: 'nearest' });
    }
    
    // Verificar visibilidad inicial
    console.log("Estado inicial del contenedor de sugerencias:", {
        display: window.getComputedStyle(suggestionsBox).display,
        visibility: window.getComputedStyle(suggestionsBox).visibility,
        opacity: window.getComputedStyle(suggestionsBox).opacity,
        zIndex: window.getComputedStyle(suggestionsBox).zIndex,
        position: window.getComputedStyle(suggestionsBox).position
    });
});
