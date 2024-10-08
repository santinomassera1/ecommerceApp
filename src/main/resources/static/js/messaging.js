let stompClient = null;
let currentUser = null;


function getCurrentUser() {
    return fetch('/auth/current-user')
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        })
        .then(data => {
            currentUser = data.username;
            console.log('Current user:', currentUser);
        })
        .catch(error => {
            console.error('There was a problem with the fetch operation:', error);
        });
}

function updateNotificationCount() {
    fetch('/api/notifications/count')
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        })
        .then(count => {
            const notificationCountElement = document.getElementById('notificationCount');
            if (notificationCountElement) {
                notificationCountElement.innerText = count > 0 ? count : '';
            }
        })
        .catch(error => {
            console.error('Error fetching notification count:', error);
        });
}

function connect() {
    if (stompClient && stompClient.connected) {
        console.log('Already connected');
        return;
    }

    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);


        stompClient.subscribe('/topic/messages', function (messageOutput) {
            const message = JSON.parse(messageOutput.body);
            showMessage(message);
            if (typeof updateNotificationCount === 'function') {
                updateNotificationCount();
            } else {
                console.error("updateNotificationCount no está definido");
            }
        });
    }, function (error) {
        console.error('Error connecting to STOMP:', error);
    });
}

function sendMessage() {
    const messageContent = document.getElementById('messageContent').value.trim(); // .trim() para evitar espacios vacíos
    const toUser = document.getElementById('activeUser').innerText;

    if (messageContent && stompClient && toUser && currentUser) {
        console.log('Sending message:', messageContent, 'para:', toUser);

        stompClient.send("/app/sendMessage", {}, JSON.stringify({
            'content': messageContent,
            'fromUser': currentUser,
            'toUser': toUser
        }));

        document.getElementById('messageContent').value = '';
    }
}

function showMessage(message) {
    let messageHtml;
    const formattedTime = formatTimestamp(message.timestamp);

    if (message.fromUser === currentUser) {
        messageHtml = `
            <div class="d-flex justify-content-end mb-4">
                <div class="msg_container_send">
                    ${message.content}
                    <span class="msg_time_send">${formattedTime}</span> 
                </div>
            </div>
        `;
    } else {
        messageHtml = `
            <div class="d-flex justify-content-start mb-4">
                <div class="msg_cotainer">
                    ${message.content}
                    <span class="msg_time">${formattedTime}</span> 
                </div>
            </div>
        `;
    }

    document.getElementById('messageArea').innerHTML += messageHtml;
}

function formatTimestamp(timestamp) {
    const date = new Date(timestamp);
    const hours = date.getHours();
    const minutes = date.getMinutes();
    const ampm = hours >= 12 ? 'PM' : 'AM';
    const formattedHours = hours % 12 || 12;
    const formattedMinutes = minutes < 10 ? '0' + minutes : minutes;
    return `${formattedHours}:${formattedMinutes} ${ampm}`;
}

function loadConversationHistory(currentUser, selectedUser) {
    fetch(`/messages/history?fromUser=${currentUser}&toUser=${selectedUser}`)
        .then(response => response.json())
        .then(messages => {
            document.getElementById('messageArea').innerHTML = '';
            messages.forEach(function (message) {
                showMessage(message);
            });
        })
        .catch(error => {
            console.error('Error loading conversation history:', error);
        });
}

document.addEventListener('DOMContentLoaded', () => {
    getCurrentUser().then(() => {
        if (currentUser) {
            connect();
        } else {
            console.error('Failed to fetch the current user.');
        }
    });

    const messageForm = document.getElementById('messageForm');
    if (messageForm) {
        // Asegúrate de que solo se añade un único event listener
        messageForm.addEventListener('submit', function (e) {
            e.preventDefault();
            sendMessage();
        });
    }
});

document.getElementById('searchInputUser').addEventListener('input', function() {
    const query = this.value;
    console.log('Searching for:', query);
    if (query.length > 0) {
        fetch(`/api/users/search?query=${query}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.json();
            })
            .then(users => {
                console.log('Users found:', users);
                const userList = document.getElementById('userList');
                userList.innerHTML = '';

                users.forEach(user => {
                    const userItem = document.createElement('li');
                    userItem.className = 'list-group-item';
                    userItem.innerHTML = `
                        <div class="d-flex bd-highlight">
                            <div class="user_info">
                                <span>${user.username}</span>
                            </div>
                        </div>
                    `;
                    userItem.addEventListener('click', () => {
                        document.getElementById('activeUser').innerText = user.username;
                        loadConversationHistory(currentUser, user.username);
                    });
                    userList.appendChild(userItem);
                });
            })
            .catch(error => {
                console.error('Error fetching users:', error);
            });
    } else {
        document.getElementById('userList').innerHTML = '';
    }
});
