
function loadNotifications() {
    fetch('/api/notifications/list')
        .then(response => response.json())
        .then(notifications => {
            const notificationsList = document.getElementById('notificationsList');
            notificationsList.innerHTML = '';

            if (notifications.length === 0) {
                notificationsList.innerHTML = '<a class="dropdown-item">No new notifications</a>';
            } else {
                notifications.forEach(notification => {
                    const notificationItem = document.createElement('a');
                    notificationItem.className = 'dropdown-item';
                    notificationItem.innerText = notification.content;
                    notificationsList.appendChild(notificationItem);
                });

                markNotificationsAsRead();
            }
        })
        .catch(error => console.error('Error fetching notifications:', error));
}


function markNotificationsAsRead() {
    fetch('/api/notifications/mark-read', {
        method: 'PATCH'
    })
        .then(() => {

            document.getElementById('notificationCount').innerText = '';
        })
        .catch(error => console.error('Error marking notifications as read:', error));
}


document.getElementById('notificationsDropdown').addEventListener('click', loadNotifications);


function updateNotificationCount() {
    fetch('/api/notifications/count')
        .then(response => response.json())
        .then(count => {
            const notificationCountElement = document.getElementById('notificationCount');
            notificationCountElement.innerText = count > 0 ? count : '';
        })
        .catch(error => console.error('Error fetching notification count:', error));
}


setInterval(updateNotificationCount, 5000);
