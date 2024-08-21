document.addEventListener('DOMContentLoaded', function () {
    // Obtén todos los checkboxes
    const categoryCheckboxes = document.querySelectorAll('.list-group-item input[type="checkbox"]');

    categoryCheckboxes.forEach(checkbox => {
        checkbox.addEventListener('change', function () {
            // Obtén las categorías seleccionadas
            const selectedCategories = Array.from(categoryCheckboxes)
                .filter(checkbox => checkbox.checked)
                .map(checkbox => checkbox.value);

            // Filtrar productos en el frontend
            filterProducts(selectedCategories);
        });
    });

    function filterProducts(categories) {
        const products = document.querySelectorAll('.product-card');

        products.forEach(product => {
            const productCategory = product.getAttribute('data-category');
            if (categories.length === 0 || categories.includes(productCategory) || categories.includes('all')) {
                product.style.display = 'block';
            } else {
                product.style.display = 'none';
            }
        });
    }
});
