document.addEventListener('DOMContentLoaded', function () {
    const form = document.getElementById('assignRoleForm');
    if (form) {
        form.addEventListener('submit', function(event) {
            event.preventDefault();

            const username = document.getElementById('username').value;
            const role = document.getElementById('role').value;

            fetch('/admin/assign-role', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ username, roleName: role })
            })
                .then(response => response.json())
                .then(data => {
                    alert(data.message);
                })
                .catch(error => {
                    console.error('Error:', error);
                });
        });
    }

    // Code to handle the user table
    fetch('/admin/users')
        .then(response => response.json())
        .then(users => {
            const userTable = document.getElementById('userTable');
            if (userTable) {
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
            }
        });
});

// Function to delete a user
function deleteUser(username) {
    if (confirm('Are you sure you want to delete this user?')) {
        fetch(`/admin/delete-user?username=${username}`, {
            method: 'DELETE'
        }).then(response => response.json())
            .then(data => {
                alert(data.message);
                location.reload();
            })
            .catch(error => {
                console.error('Error:', error);
                alert('An error occurred while trying to delete the user.');
            });
    }
}

// Function to block a user
function blockUser(username) {
    if (confirm('Are you sure you want to block this user?')) {
        fetch(`/admin/block-user?username=${username}`, {
            method: 'POST'
        }).then(response => response.json())
            .then(data => {
                alert(data.message);
                location.reload();
            })
            .catch(error => {
                console.error('Error:', error);
                alert('An error occurred while trying to block the user.');
            });
    }
}
