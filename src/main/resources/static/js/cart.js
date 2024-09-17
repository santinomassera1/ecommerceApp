// cart.js

document.addEventListener("DOMContentLoaded", function () {
    const addToCartButtons = document.querySelectorAll(".add-to-cart-btn");

    addToCartButtons.forEach(button => {
        button.addEventListener("click", function (event) {
            event.preventDefault();

            const productId = button.dataset.productId;
            const quantity = document.querySelector(`#quantity-${productId}`).value;

            fetch(`/cart/add/${productId}`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded",
                    "X-CSRF-Token": document.querySelector('input[name="_csrf"]').value
                },
                body: new URLSearchParams({ quantity: quantity })
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    alert("Product added to cart!");
                    // Aquí puedes actualizar el carrito sin recargar la página
                } else {
                    alert("Error adding product to cart");
                }
            })
            .catch(error => console.error("Error:", error));
        });
    });
});