$(document).ready(function () {
    // Tooltip activation
    $('[data-toggle="tooltip"]').tooltip();

    // Event listener for category filter
    $('.list-group-item input[type="checkbox"]').on('change', function () {
        const selectedCategories = $('.list-group-item input[type="checkbox"]:checked')
            .map(function () { return $(this).val().toLowerCase(); }).get();
        filterProducts(selectedCategories);
    });

    // Event listener for sorting products
    $('#sortButton').on('click', '.dropdown-item', function () {
        const sortBy = $(this).data('sort');
        sortProducts(sortBy);
    });

    // Function to filter products by category
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

    // Function to sort products
    function sortProducts(sortBy) {
        const products = $('.card').get();
        products.sort(function (a, b) {
            const priceA = parseFloat($(a).find('.card-price').text().replace('$', '').replace(',', ''));
            const priceB = parseFloat($(b).find('.card-price').text().replace('$', '').replace(',', ''));
            if (sortBy === 'price-asc') {
                return priceA - priceB;
            } else if (sortBy === 'price-desc') {
                return priceB - priceA;
            } else {
                return 0;
            }
        });
        $.each(products, function (idx, product) {
            $('.product-row').append(product);
        });
    }
});
