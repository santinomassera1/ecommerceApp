document.addEventListener('DOMContentLoaded', function () {
    // Obtener todos los checkboxes de categorías
    const categoryCheckboxes = document.querySelectorAll('.list-group-item input[type="checkbox"]');
    
    // Agregar checkbox "All" si no existe
    if (!document.querySelector('input[value="All"]')) {
        const allLabel = document.createElement('label');
        allLabel.className = 'list-group-item';
        allLabel.innerHTML = '<input type="checkbox" value="All" checked> <span>All</span>';
        
        const firstCategory = document.querySelector('.list-group.mt-3 > div');
        if (firstCategory) {
            firstCategory.parentNode.insertBefore(allLabel, firstCategory);
        }
    }

    // Obtener todos los checkboxes actualizados (incluyendo "All")
    const allCheckboxes = document.querySelectorAll('.list-group-item input[type="checkbox"]');
    
    allCheckboxes.forEach(checkbox => {
        checkbox.addEventListener('change', function () {
            handleCategoryFilter();
        });
    });

    function handleCategoryFilter() {
        const allCheckbox = document.querySelector('input[value="All"]');
        const otherCheckboxes = Array.from(allCheckboxes).filter(cb => cb.value !== 'All');
        
        // Si se clickeó "All"
        if (allCheckbox && allCheckbox.checked) {
            // Deseleccionar todas las demás categorías
            otherCheckboxes.forEach(cb => cb.checked = false);
            showAllProducts();
            return;
        }
        
        // Verificar si hay categorías específicas seleccionadas
        const anyOtherSelected = otherCheckboxes.some(cb => cb.checked);
        
        // Si se seleccionó alguna categoría específica, deseleccionar "All"
        if (anyOtherSelected && allCheckbox) {
            allCheckbox.checked = false;
        }
        
        // Obtener categorías seleccionadas
        const selectedCategories = otherCheckboxes
            .filter(checkbox => checkbox.checked)
            .map(checkbox => checkbox.value.toLowerCase());
        
        // Filtrar productos según las categorías seleccionadas
        if (selectedCategories.length === 0) {
            // Si no hay categorías seleccionadas, mostrar todos los productos
            showAllProducts();
        } else {
            // Filtrar por las categorías seleccionadas
            filterProductsByCategories(selectedCategories);
        }
    }

    function showAllProducts() {
        const products = document.querySelectorAll('.product-card');
        products.forEach(product => {
            product.closest('.col-md-4').style.display = 'block';
        });
    }

    function filterProductsByCategories(selectedCategories) {
        const products = document.querySelectorAll('.product-card');
        
        products.forEach(product => {
            const productContainer = product.closest('.col-md-4');
            
            // Obtener las categorías del producto desde el atributo data-categories
            const productCategoriesStr = product.getAttribute('data-categories');
            if (!productCategoriesStr) {
                productContainer.style.display = 'none';
                return;
            }
            
            // Convertir a array y normalizar
            const productCategories = productCategoriesStr.split(',')
                .map(cat => cat.trim().toLowerCase())
                .filter(cat => cat.length > 0);
            
            // Verificar si el producto tiene al menos una de las categorías seleccionadas
            const hasMatchingCategory = selectedCategories.some(selectedCat => 
                productCategories.includes(selectedCat)
            );
            
            productContainer.style.display = hasMatchingCategory ? 'block' : 'none';
        });
    }

    // Inicializar mostrando todos los productos
    showAllProducts();
});
