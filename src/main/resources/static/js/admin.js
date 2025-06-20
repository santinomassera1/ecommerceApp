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