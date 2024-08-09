document.addEventListener('DOMContentLoaded', function () {
    // Fetch and display users
    fetch('/admin/users')
        .then(response => response.json())
        .then(users => {
            const userTable = document.getElementById('userTable');
            userTable.innerHTML = '';
            users.forEach(user => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td>${user.id}</td>
                    <td>${user.username}</td>
                    <td>
                        <button class="btn btn-danger btn-sm" onclick="deleteUser('${user.username}')">Delete</button>
                        <button class="btn btn-warning btn-sm" onclick="blockUser('${user.username}')">Block</button>
                    </td>
                `;
                userTable.appendChild(row);
            });
        });

    // Handle role assignment form submission
    document.getElementById('assignRoleForm').addEventListener('submit', function (e) {
        e.preventDefault();
        const username = document.getElementById('username').value;
        const role = document.getElementById('role').value;
        fetch('/admin/assign-role', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: new URLSearchParams({ username, role })
        }).then(response => response.json())
            .then(data => alert(data.message));
    });
});

// Function to delete a user
function deleteUser(username) {
    fetch(`/admin/delete-user?username=${username}`, {
        method: 'DELETE'
    }).then(response => response.json())
        .then(data => {
            alert(data.message);
            location.reload(); // Reload the page to see the changes
        });
}

// Function to block a user
function blockUser(username) {
    fetch(`/admin/block-user?username=${username}`, {
        method: 'POST'
    }).then(response => response.json())
        .then(data => {
            alert(data.message);
            location.reload(); // Reload the page to see the changes
        });
}
