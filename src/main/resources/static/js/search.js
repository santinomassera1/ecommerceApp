document.getElementById("searchInput").addEventListener("input", function() {
    let query = this.value;

    if (query.length > 2) {
        fetch(`/search/suggestions?query=${query}`)
            .then(response => response.json())
            .then(data => {
                let suggestionsBox = document.getElementById("suggestions");
                suggestionsBox.innerHTML = "";  // Limpiar sugerencias anteriores

                data.forEach(item => {
                    let suggestion = document.createElement("div");
                    suggestion.classList.add("suggestion-item");
                    
                    // Mostrar solo el nombre (y opcionalmente el precio si es un producto)
                    suggestion.innerHTML = `
                        <div>
                            <span>${item.name}</span>
                            ${item.price ? `<span>$${item.price}</span>` : ''}
                        </div>
                    `;

                    // Redirigir según el tipo de resultado
                    suggestion.addEventListener("click", function() {
                        if (item.type === "product") {
                            window.location.href = `/product/details/${item.id}`;
                        } else if (item.type === "user") {
                            window.location.href = `/profile/${item.id}`;
                        } else if (item.type === "category") {
                            window.location.href = `/category/${item.id}`;
                        }
                    });

                    suggestionsBox.appendChild(suggestion);
                });
            })
            .catch(error => console.error("Error fetching suggestions:", error));
    }
});
