document.addEventListener('DOMContentLoaded', function () {
    const form = document.getElementById('assignRoleForm');
    const searchInput = document.getElementById('searchUser');
    const userTable = document.getElementById('userTable');

    if (!form || !searchInput || !userTable) {
        console.error("❌ Error: Uno o más elementos del DOM no fueron encontrados.");
        return;
    }

    // Asignar roles a los usuarios
    form.addEventListener('submit', function (event) {
        event.preventDefault();

        const username = document.getElementById('assignUsername')?.value.trim();
        const role = document.getElementById('assignRole')?.value.trim();

        if (!username || !role) {
            alert("❌ Error: Debes completar los campos de usuario y rol.");
            return;
        }

        $.ajax({
            url: "/admin/assign-role",
            type: "POST",
            contentType: "application/json",
            data: JSON.stringify({ username: username, roleName: role }),
            success: function (response) {
                alert(response.message);
                loadUsers();
            },
            error: function (error) {
                console.error("❌ Error asignando rol:", error);
                alert("Error assigning role");
            }
        });
    });

    // Función para cargar los usuarios en la tabla
    function loadUsers(query = '') {
        console.log(`🔍 Buscando usuarios con query: "${query}"`);

        $.get("/admin/search-user?username=" + query, function (users) {
            console.log("✅ Respuesta del servidor:", users);

            if (!Array.isArray(users)) {
                console.error("❌ Error: La API no devolvió un array válido de usuarios.");
                return;
            }

            userTable.innerHTML = ''; // Limpiar la tabla

            if (users.length === 0) {
                console.warn("⚠️ No se encontraron usuarios.");
                userTable.innerHTML = '<tr><td colspan="4">No users found</td></tr>';
                return;
            }

            users.forEach(user => {
                let roles = user.roles ? user.roles.map(role => role.name).join(", ") : "No roles";
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td>${user.id || 'N/A'}</td>
                    <td>${user.username || 'N/A'}</td>
                    <td>${roles}</td>
                    <td>
                        <button class="btn btn-danger btn-sm" onclick="deleteUser('${user.username}')">Delete</button>
                    </td>
                `;
                userTable.appendChild(row);
            });
        }).fail(function (error) {
            console.error("❌ Error obteniendo usuarios:", error);
            alert('Error fetching users');
        });
    }

    // Cargar todos los usuarios al inicio
    loadUsers();

    // Filtrar usuarios en tiempo real
    searchInput.addEventListener('input', function () {
        loadUsers(this.value);
    });
});

// Función para eliminar usuario
function deleteUser(username) {
    if (!username) {
        console.error("❌ Error: El nombre de usuario es inválido.");
        return;
    }

    if (confirm(`¿Estás seguro de que deseas eliminar a ${username}?`)) {
        fetch("/admin/delete-user", {
            method: "DELETE",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ username: username })
        })
            .then(response => response.json())
            .then(data => {
                alert(data.message);
                loadUsers();
            })
            .catch(error => {
                console.error('❌ Error eliminando usuario:', error);
                alert('Error deleting user');
            });
    }
}

/**
 * Funciones de utilidad para el panel de administración
 */

/**
 * Función para ayudar a obtener la URL directa de una imagen
 * @param {string} url - La URL a procesar
 * @returns {string} - La URL directa de la imagen o un mensaje de error
 */
function extractDirectImageUrl(url) {
    // Verificar si la URL está vacía
    if (!url || url.trim() === '') {
        return null;
    }

    try {
        // Intentar extraer la URL directa de una URL de Google
        if (url.includes('google.com/url')) {
            const urlObj = new URL(url);
            const params = new URLSearchParams(urlObj.search);
            const directUrl = params.get('url');
            if (directUrl) {
                return directUrl;
            }
        }

        // Intentar extraer la URL directa de una URL de Google Images
        if (url.includes('google.com/imgres')) {
            const urlObj = new URL(url);
            const params = new URLSearchParams(urlObj.search);
            const directUrl = params.get('imgurl');
            if (directUrl) {
                return directUrl;
            }
        }
        
        // Intentar extraer la URL directa de una URL de búsqueda de Google
        const googleSearchRegex = /www\.google\.com\/search\?.*tbm=isch/;
        if (googleSearchRegex.test(url)) {
            // Extraer el parámetro q de la URL
            const urlObj = new URL(url);
            const params = new URLSearchParams(urlObj.search);
            const query = params.get('q');
            if (query) {
                console.log('Esta es una búsqueda de imágenes de Google. Por favor, haga clic en una imagen específica y use "Copiar dirección de imagen".');
                return null;
            }
        }
        
        // Intentar extraer la URL directa de una URL de imagen de Google
        if (url.includes('googleusercontent.com')) {
            // Intentar limpiar parámetros innecesarios
            const urlObj = new URL(url);
            // Conservar solo la ruta base y algunos parámetros esenciales
            return urlObj.origin + urlObj.pathname;
        }
        
        // Verificar si la URL es una imagen directa
        const imageExtensions = /\.(jpeg|jpg|gif|png|webp|svg|bmp)(\?.*)?$/i;
        if (!imageExtensions.test(url)) {
            console.log('La URL no parece ser una imagen directa.');
        }

        // Si no es una URL de Google, devolver la URL original
        return url;
    } catch (e) {
        console.error('Error al procesar la URL:', e);
        return url;
    }
}

/**
 * Función para buscar usuarios
 * @param {string} username - El nombre de usuario a buscar
 */
function searchUsers(username) {
    fetch(`/admin/search-user?username=${username}`)
        .then(response => {
            if (response.status === 204) {
                document.getElementById('userTable').innerHTML = '<tr><td colspan="5" class="text-center">No se encontraron usuarios</td></tr>';
                return;
            }
            return response.json();
        })
        .then(data => {
            if (data) {
                let tableContent = '';
                data.forEach(user => {
                    tableContent += `
                    <tr>
                        <td>${user.id}</td>
                        <td>${user.username}</td>
                        <td>${user.email}</td>
                        <td>${user.role}</td>
                        <td>
                            <a href="/admin/user/edit/${user.id}" class="btn btn-warning btn-sm">
                                <i class="fas fa-edit"></i> Editar
                            </a>
                            <a href="/admin/user/delete/${user.id}" class="btn btn-danger btn-sm" onclick="return confirm('¿Estás seguro de eliminar este usuario?')">
                                <i class="fas fa-trash"></i> Eliminar
                            </a>
                        </td>
                    </tr>`;
                });
                document.getElementById('userTable').innerHTML = tableContent;
            }
        })
        .catch(error => console.error('Error:', error));
}

// Inicializar funciones cuando el DOM esté cargado
document.addEventListener('DOMContentLoaded', function() {
    // Configurar búsqueda de usuarios si existe el elemento
    const userSearchInput = document.getElementById('searchUser');
    if (userSearchInput) {
        userSearchInput.addEventListener('input', function() {
            searchUsers(this.value);
        });
        // Cargar todos los usuarios al inicio
        searchUsers('');
    }

    // Configurar ayuda para URLs de imágenes si existe el elemento
    const imageUrlInput = document.getElementById('imageUrl');
    if (imageUrlInput) {
        imageUrlInput.addEventListener('paste', function(e) {
            // Permitir que el evento de pegado ocurra primero
            setTimeout(() => {
                const pastedUrl = this.value;
                const directUrl = extractDirectImageUrl(pastedUrl);
                
                if (directUrl && directUrl !== pastedUrl) {
                    this.value = directUrl;
                    // Disparar el evento input para actualizar la vista previa
                    this.dispatchEvent(new Event('input'));
                }
            }, 0);
        });
    }
    
    // Cerrar alertas automáticamente después de 5 segundos
    setTimeout(function() {
        const alerts = document.querySelectorAll('.alert');
        alerts.forEach(function(alert) {
            const closeBtn = alert.querySelector('button.close');
            if (closeBtn) {
                closeBtn.click();
            }
        });
    }, 5000);
});