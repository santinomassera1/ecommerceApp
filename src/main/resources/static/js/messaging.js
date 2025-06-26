let stompClient = null;
let currentUser = null;
let activeChat = null;

document.addEventListener('DOMContentLoaded', () => {
    console.log('Inicializando sistema de mensajería');
    
    // Inicializar UI
    initUI();
    
    // Obtener usuario actual y conectar al websocket
    getCurrentUser().then(() => {
        if (currentUser) {
            connect();
            loadRecentChats();
        } else {
            console.error('No se pudo obtener el usuario actual');
            showErrorMessage('No se pudo cargar la información del usuario');
        }
    });
});

/**
 * Inicializa los elementos de la interfaz de usuario
 */
function initUI() {
    // Inicializar formulario de mensajes
    const messageForm = document.getElementById('messageForm');
    if (messageForm) {
        messageForm.addEventListener('submit', function(e) {
            e.preventDefault();
            sendMessage();
        });
    }
    
    // Inicializar búsqueda de usuarios
    const searchInput = document.getElementById('searchInputUser');
    if (searchInput) {
        searchInput.addEventListener('input', debounce(searchUsers, 300));
    }
    
    // Inicializar botón de toggle para móviles
    const toggleSidebarBtn = document.querySelector('.toggle-sidebar');
    if (toggleSidebarBtn) {
        toggleSidebarBtn.addEventListener('click', function() {
            document.querySelector('.users-sidebar').classList.toggle('show');
        });
    }
    
    // Inicializar tecla Enter para enviar mensajes
    const messageInput = document.getElementById('messageContent');
    if (messageInput) {
        messageInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                sendMessage();
            }
        });
    }
    
    // Limpiar mensajes duplicados que puedan existir
    cleanupDuplicateMessages();
}

/**
 * Limpia mensajes duplicados que puedan existir en el DOM
 */
function cleanupDuplicateMessages() {
    const messageArea = document.getElementById('messageArea');
    if (!messageArea) return;
    
    // Crear un mapa para rastrear mensajes únicos
    const messageMap = new Map();
    const messages = messageArea.querySelectorAll('.message');
    
    messages.forEach(message => {
        const messageId = message.getAttribute('data-message-id');
        if (!messageId) return;
        
        if (messageMap.has(messageId)) {
            // Mensaje duplicado, eliminar
            message.remove();
            console.log('Mensaje duplicado eliminado:', messageId);
        } else {
            // Registrar mensaje único
            messageMap.set(messageId, true);
        }
    });
}

/**
 * Obtiene el usuario actual desde el servidor
 */
function getCurrentUser() {
    return fetch('/auth/current-user')
        .then(response => {
            if (!response.ok) {
                throw new Error('Error al obtener el usuario actual');
            }
            return response.json();
        })
        .then(data => {
            currentUser = data.username;
            console.log('Usuario actual:', currentUser);
            return currentUser;
        })
        .catch(error => {
            console.error('Error:', error);
            return null;
        });
}

/**
 * Actualiza el contador de notificaciones en la barra de navegación
 */
function updateNotificationCount() {
    fetch('/api/notifications/count')
        .then(response => {
            if (!response.ok) {
                throw new Error('Error al obtener el contador de notificaciones');
            }
            return response.json();
        })
        .then(count => {
            const notificationCountElement = document.getElementById('notificationCount');
            if (notificationCountElement) {
                notificationCountElement.innerText = count > 0 ? count : '';
                if (count > 0) {
                    notificationCountElement.classList.add('pulse');
                    setTimeout(() => {
                        notificationCountElement.classList.remove('pulse');
                    }, 1000);
                }
            }
        })
        .catch(error => {
            console.error('Error:', error);
        });
}

/**
 * Conecta al websocket para recibir mensajes en tiempo real
 */
function connect() {
    if (stompClient && stompClient.connected) {
        console.log('Ya conectado al websocket');
        return;
    }

    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.debug = null; // Desactivar logs de debug

    stompClient.connect({}, function(frame) {
        console.log('Conectado al websocket:', frame);

        // Suscribirse al canal de mensajes
        stompClient.subscribe('/topic/messages', function(messageOutput) {
            const message = JSON.parse(messageOutput.body);
            handleIncomingMessage(message);
        });
        
        // Suscribirse al canal de estado de usuarios
        stompClient.subscribe('/topic/status', function(statusOutput) {
            const status = JSON.parse(statusOutput.body);
            updateUserStatus(status.username, status.status);
        });
    }, function(error) {
        console.error('Error al conectar al websocket:', error);
        setTimeout(connect, 5000); // Reintentar conexión
    });
}

/**
 * Maneja los mensajes entrantes
 */
function handleIncomingMessage(message) {
    // Actualizar contador de notificaciones
    updateNotificationCount();
    
    // Verificar si el mensaje ya existe en la interfaz para evitar duplicados
    const messageId = message.id || `${message.fromUser}-${message.toUser}-${message.timestamp}`;
    const existingMessage = document.querySelector(`[data-message-id="${messageId}"]`);
    
    if (existingMessage) {
        console.log('Mensaje duplicado detectado, ignorando:', messageId);
        return;
    }
    
    // Si el mensaje es para la conversación activa, mostrarlo
    if (activeChat === message.fromUser || activeChat === message.toUser) {
        displayMessage(message);
    }
    
    // Actualizar la lista de chats recientes
    updateChatList(message);
}

/**
 * Envía un mensaje al usuario seleccionado
 */
function sendMessage() {
    const messageInput = document.getElementById('messageContent');
    const content = messageInput.value.trim();
    
    if (!content || !activeChat || !stompClient) {
        return;
    }
    
    console.log('Enviando mensaje a:', activeChat);
    
    // Crear un ID único temporal para este mensaje
    const tempId = `temp-${Date.now()}`;
    
    const message = {
        content: content,
        fromUser: currentUser,
        toUser: activeChat,
        timestamp: new Date().toISOString(),
        tempId: tempId
    };
    
    // Mostrar el mensaje inmediatamente en la interfaz con un ID temporal
    displayMessage(message, tempId);
    
    // Limpiar el campo de entrada
    messageInput.value = '';
    messageInput.focus();
    
    // Enviar el mensaje al servidor
    stompClient.send("/app/sendMessage", {}, JSON.stringify(message));
    
    // Actualizar la lista de chats recientes
    updateChatList(message);
}

/**
 * Muestra un mensaje en la interfaz
 * @param {Object} message - El mensaje a mostrar
 * @param {string} [customId] - ID personalizado opcional para el mensaje
 */
function displayMessage(message, customId) {
    const messageArea = document.getElementById('messageArea');
    const isSent = message.fromUser === currentUser;
    const messageClass = isSent ? 'message message-sent' : 'message message-received';
    const formattedTime = formatTimestamp(message.timestamp);

    // Generar un ID único para el mensaje
    const messageId = customId || message.id || `${message.fromUser}-${message.toUser}-${message.timestamp}`;
    
    // Verificar si el mensaje ya existe
    const existingMessage = document.querySelector(`[data-message-id="${messageId}"]`);
    if (existingMessage) {
        console.log('Mensaje ya existe en el DOM, no duplicando:', messageId);
        return;
    }
    
    const messageElement = document.createElement('div');
    messageElement.className = messageClass;
    messageElement.setAttribute('data-message-id', messageId);
    
    messageElement.innerHTML = `
        <div class="message-content">${escapeHtml(message.content)}</div>
        <div class="message-time">${formattedTime}</div>
    `;
    
    messageArea.appendChild(messageElement);
    
    // Scroll al final del área de mensajes
    setTimeout(() => {
        messageArea.scrollTop = messageArea.scrollHeight;
    }, 50);
}

/**
 * Carga el historial de conversación con un usuario
 */
function loadConversation(username) {
    if (!username || username === activeChat) {
        return;
    }
    
    // Actualizar usuario activo
    activeChat = username;
    document.getElementById('activeUser').innerText = username;
    
    // Mostrar avatar personalizado o placeholder
    const avatarElement = document.getElementById('activeUserAvatar');
    avatarElement.innerHTML = `<i class="fas fa-user"></i>`;
    
    // Mostrar área de mensajes y formulario
    document.getElementById('emptyChatPlaceholder').style.display = 'none';
    document.getElementById('messageArea').style.display = 'flex';
    document.getElementById('chatFooter').style.display = 'block';
    
    // Limpiar mensajes anteriores
    document.getElementById('messageArea').innerHTML = '';
    
    // Marcar usuario como activo en la lista
    const userItems = document.querySelectorAll('.user-item');
    userItems.forEach(item => {
        item.classList.remove('active');
        if (item.getAttribute('data-username') === username) {
            item.classList.add('active');
        }
    });
    
    // Cerrar sidebar en móviles
    document.querySelector('.users-sidebar').classList.remove('show');
    
    // Cargar historial de conversación
    fetch(`/messages/history?fromUser=${currentUser}&toUser=${username}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Error al cargar el historial de conversación');
            }
            return response.json();
        })
        .then(messages => {
            if (messages.length === 0) {
                // Mostrar mensaje de bienvenida si no hay historial
                const welcomeElement = document.createElement('div');
                welcomeElement.className = 'text-center text-muted my-4';
                welcomeElement.innerHTML = `
                    <i class="fas fa-comments fa-2x mb-2"></i>
                    <p>Comienza una conversación con ${username}</p>
                `;
                document.getElementById('messageArea').appendChild(welcomeElement);
            } else {
                // Mostrar mensajes del historial
                messages.forEach(message => {
                    displayMessage(message);
                });
            }
            
            // Enfocar el campo de entrada
            document.getElementById('messageContent').focus();
        })
        .catch(error => {
            console.error('Error:', error);
            showErrorMessage('No se pudo cargar el historial de conversación');
        });
}

/**
 * Carga los chats recientes del usuario
 */
function loadRecentChats() {
    fetch('/api/messages/recent-chats')
        .then(response => {
            if (!response.ok) {
                throw new Error('Error al cargar los chats recientes');
            }
            return response.json();
        })
        .then(chats => {
            const userList = document.getElementById('userList');
            
            // Mostrar mensaje si no hay chats
            if (chats.length === 0) {
                document.getElementById('noUsersMessage').style.display = 'block';
                return;
            }
            
            // Ocultar mensaje de "no hay usuarios"
            document.getElementById('noUsersMessage').style.display = 'none';
            
            // Limpiar lista de usuarios
            userList.innerHTML = '';
            
            // Mostrar chats recientes
            chats.forEach(chat => {
                addUserToList(chat.username, chat.lastMessage, chat.timestamp, chat.unreadCount);
            });
        })
        .catch(error => {
            console.error('Error:', error);
            showErrorMessage('No se pudieron cargar los chats recientes');
        });
}

/**
 * Busca usuarios por nombre
 */
function searchUsers() {
    const query = document.getElementById('searchInputUser').value.trim();
    
    if (query.length === 0) {
        loadRecentChats();
        return;
    }
    
    if (query.length < 2) {
        return;
    }
    
    fetch(`/api/users/search?query=${encodeURIComponent(query)}`)
            .then(response => {
                if (!response.ok) {
                throw new Error('Error al buscar usuarios');
                }
                return response.json();
            })
            .then(users => {
                const userList = document.getElementById('userList');
            
            // Mostrar mensaje si no hay resultados
            if (users.length === 0) {
                userList.innerHTML = `
                    <li class="text-center p-4 text-muted">
                        <i class="fas fa-search fa-2x mb-2"></i>
                        <p>No se encontraron usuarios para "${escapeHtml(query)}"</p>
                    </li>
                `;
                return;
            }
            
            // Limpiar lista de usuarios
                userList.innerHTML = '';

            // Mostrar usuarios encontrados
                users.forEach(user => {
                if (user.username !== currentUser) {
                    addUserToList(user.username, '', '', 0);
                }
                });
            })
            .catch(error => {
            console.error('Error:', error);
            showErrorMessage('No se pudo completar la búsqueda');
        });
}

/**
 * Añade un usuario a la lista de chats
 */
function addUserToList(username, lastMessage, timestamp, unreadCount) {
    const userList = document.getElementById('userList');
    
    // Crear elemento de usuario
    const userItem = document.createElement('li');
    userItem.className = 'user-item';
    userItem.setAttribute('data-username', username);
    
    // Formatear timestamp si existe
    let timeString = '';
    if (timestamp) {
        const messageDate = new Date(timestamp);
        const today = new Date();
        
        if (messageDate.toDateString() === today.toDateString()) {
            // Si es hoy, mostrar la hora
            timeString = messageDate.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
        } else {
            // Si no es hoy, mostrar la fecha
            timeString = messageDate.toLocaleDateString([], { day: '2-digit', month: '2-digit' });
        }
    }
    
    // HTML del elemento de usuario
    userItem.innerHTML = `
        <div class="user-avatar-placeholder">
            <i class="fas fa-user"></i>
        </div>
        <div class="user-info">
            <div class="d-flex justify-content-between align-items-center">
                <span class="user-name">${escapeHtml(username)}</span>
                ${timeString ? `<small class="text-muted">${timeString}</small>` : ''}
            </div>
            ${lastMessage ? `<small class="user-status text-truncate">${escapeHtml(lastMessage)}</small>` : ''}
        </div>
        ${unreadCount > 0 ? `<span class="badge bg-primary rounded-pill">${unreadCount}</span>` : ''}
    `;
    
    // Añadir evento de clic
    userItem.addEventListener('click', function() {
        loadConversation(username);
    });
    
    // Añadir a la lista
    userList.appendChild(userItem);
}

/**
 * Actualiza la lista de chats con un nuevo mensaje
 */
function updateChatList(message) {
    const otherUser = message.fromUser === currentUser ? message.toUser : message.fromUser;
    
    // Buscar si el usuario ya está en la lista
    const existingItem = document.querySelector(`.user-item[data-username="${otherUser}"]`);
    
    if (existingItem) {
        // Actualizar elemento existente
        const lastMessageElement = existingItem.querySelector('.user-status');
        if (lastMessageElement) {
            lastMessageElement.textContent = message.content;
        }
        
        // Mover al principio de la lista
        const userList = document.getElementById('userList');
        userList.insertBefore(existingItem, userList.firstChild);
    } else {
        // Añadir nuevo usuario a la lista
        addUserToList(otherUser, message.content, message.timestamp, 1);
        
        // Mover al principio de la lista
        const userList = document.getElementById('userList');
        const newItem = userList.lastChild;
        userList.insertBefore(newItem, userList.firstChild);
    }
}

/**
 * Actualiza el estado de un usuario en la lista
 */
function updateUserStatus(username, status) {
    const userItem = document.querySelector(`.user-item[data-username="${username}"]`);
    if (userItem) {
        const statusIndicator = userItem.querySelector('.status-indicator');
        if (statusIndicator) {
            statusIndicator.className = `status-indicator ${status === 'ONLINE' ? 'status-online' : 'status-offline'}`;
        }
    }
}

/**
 * Formatea una marca de tiempo para mostrarla
 */
function formatTimestamp(timestamp) {
    const date = new Date(timestamp);
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
}

/**
 * Muestra un mensaje de error en la interfaz
 */
function showErrorMessage(message) {
    const errorElement = document.createElement('div');
    errorElement.className = 'alert alert-danger alert-dismissible fade show';
    errorElement.innerHTML = `
        <strong>Error:</strong> ${escapeHtml(message)}
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    `;
    
    // Añadir al principio del contenedor
    const container = document.querySelector('.messaging-container');
    container.insertBefore(errorElement, container.firstChild);
    
    // Eliminar después de 5 segundos
    setTimeout(() => {
        errorElement.classList.remove('show');
        setTimeout(() => {
            errorElement.remove();
        }, 300);
    }, 5000);
}

/**
 * Función para evitar múltiples llamadas (debounce)
 */
function debounce(func, delay) {
    let timeout;
    return function() {
        const context = this;
        const args = arguments;
        clearTimeout(timeout);
        timeout = setTimeout(() => {
            func.apply(context, args);
        }, delay);
    };
}

/**
 * Escapa caracteres HTML para prevenir XSS
 */
function escapeHtml(text) {
    if (!text) return '';
    
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}
