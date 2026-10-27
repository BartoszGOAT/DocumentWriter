// Fichier editor.js à placer dans votre dossier webapp/js/
// Ce script gère toutes les fonctionnalités de l'éditeur

document.addEventListener('DOMContentLoaded', function() {
    // Récupérer les informations du document depuis les éléments cachés
    const docId = parseInt(document.getElementById('docId').value);
    const userId = parseInt(document.getElementById('userId').value);
    const username = document.getElementById('username') ? document.getElementById('username').value : '';
    const canWrite = document.getElementById('canWrite') ? document.getElementById('canWrite').value === 'true' : false;
    
    // Récupérer le contextPath
    const contextPath = window.location.pathname.substring(0, window.location.pathname.indexOf('/', 1));
    
    // Connexion WebSocket
    let socket;
    let isConnected = false;
    let reconnectAttempts = 0;
    const statusElement = document.getElementById('status');
    
    // Fonction pour se connecter au WebSocket
    function connectWebSocket() {
        console.log("Tentative de connexion au WebSocket...");
        
        // Fermer la connexion existante s'il y en a une
        if (socket) {
            try {
                socket.close();
            } catch (e) {
                console.error("Erreur lors de la fermeture du socket:", e);
            }
        }
        
        const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
        const wsUrl = `${protocol}//${window.location.host}${contextPath}/editor/${docId}/${userId}`;
        
        try {
            socket = new WebSocket(wsUrl);
            
            socket.onopen = function(event) {
                console.log("Connexion WebSocket établie!");
                isConnected = true;
                reconnectAttempts = 0;
                if (statusElement) {
                    statusElement.textContent = 'Connecté';
                    statusElement.className = 'has-text-success';
                }
                showNotification('Connecté à la session d\'édition en temps réel', 'success');
            };
            
            socket.onclose = function(event) {
                console.log("Connexion WebSocket fermée. Code:", event.code, "Raison:", event.reason);
                isConnected = false;
                if (statusElement) {
                    statusElement.textContent = 'Déconnecté';
                    statusElement.className = 'has-text-danger';
                }
                
                // Tentative de reconnexion avec back-off exponentiel
                const delay = Math.min(30000, 1000 * Math.pow(1.5, reconnectAttempts));
                reconnectAttempts++;
                console.log(`Reconnexion dans ${delay}ms (tentative ${reconnectAttempts})`);
                
                setTimeout(connectWebSocket, delay);
            };
            
            socket.onerror = function(error) {
                console.error("Erreur WebSocket:", error);
                showNotification('Erreur de connexion WebSocket', 'danger');
            };
            
            socket.onmessage = function(event) {
                console.log("Message reçu:", event.data);
                
                try {
                    const message = JSON.parse(event.data);
                    
                    switch (message.type) {
                        case 'TEXT_CHANGE':
                            if (message.userId != userId) { // N'appliquer les changements que des autres utilisateurs
                                const editor = document.getElementById('editor');
                                if (editor) {
                                    const oldContent = editor.value;
                                    editor.value = message.content;
                                    if (oldContent !== message.content) {
                                        showNotification('Document mis à jour par un autre utilisateur', 'info');
                                    }
                                }
                            }
                            break;
                        
                        case 'USER_JOINED':
                            // Gérer l'événement d'un utilisateur qui rejoint
                            const onlineUsers = document.getElementById('onlineUsers');
                            if (onlineUsers) {
                                // Vérifier si l'utilisateur existe déjà dans la liste
                                if (!document.getElementById(`user-${message.userId}`)) {
                                    const userTag = document.createElement('span');
                                    userTag.id = `user-${message.userId}`;
                                    userTag.className = 'tag is-info';
                                    userTag.textContent = message.username;
                                    onlineUsers.appendChild(userTag);
                                }
                            }
                            
                            // Ajouter un message système au chat
                            addChatSystemMessage(`${message.username} a rejoint le document`);
                            break;
                        
                        case 'USER_LEFT':
                            // Gérer l'événement d'un utilisateur qui part
                            const userTag = document.getElementById(`user-${message.userId}`);
                            if (userTag) {
                                userTag.remove();
                            }
                            
                            // Ajouter un message système au chat
                            addChatSystemMessage(`${message.username} a quitté le document`);
                            break;
                        
                        case 'CHAT_MESSAGE':
                            // Gérer les messages de chat
                            addChatMessage(message.username, message.content);
                            break;
                    }
                } catch (e) {
                    console.error("Erreur lors de l'analyse du message:", e);
                }
            };
        } catch (e) {
            console.error("Erreur lors de la création de la connexion WebSocket:", e);
            showNotification('Impossible de se connecter au serveur WebSocket', 'danger');
            
            // Réessayer après un délai
            const delay = Math.min(30000, 1000 * Math.pow(1.5, reconnectAttempts));
            reconnectAttempts++;
            setTimeout(connectWebSocket, delay);
        }
    }
    
    // Ajouter un message système au chat
    function addChatSystemMessage(text) {
        const chatMessages = document.getElementById('chatMessages');
        if (!chatMessages) return;
        
        const messageDiv = document.createElement('div');
        messageDiv.className = 'chat-message system-message';
        messageDiv.textContent = text;
        
        chatMessages.appendChild(messageDiv);
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }
    
    // Ajouter un message de chat
    function addChatMessage(username, content) {
        const chatMessages = document.getElementById('chatMessages');
        if (!chatMessages) return;
        
        const messageDiv = document.createElement('div');
        messageDiv.className = 'chat-message';
        
        const header = document.createElement('div');
        header.className = 'chat-message-header';
        
        const usernameSpan = document.createElement('span');
        usernameSpan.className = 'chat-username';
        usernameSpan.textContent = username;
        
        const time = document.createElement('span');
        time.className = 'chat-time';
        const now = new Date();
        time.textContent = now.toLocaleTimeString();
        
        header.appendChild(usernameSpan);
        header.appendChild(time);
        
        const contentDiv = document.createElement('div');
        contentDiv.className = 'chat-message-content';
        contentDiv.textContent = content;
        
        messageDiv.appendChild(header);
        messageDiv.appendChild(contentDiv);
        
        chatMessages.appendChild(messageDiv);
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }
    
    // Envoyer les changements de texte au serveur
    function sendTextChange() {
        if (isConnected && canWrite) {
            const editor = document.getElementById('editor');
            if (!editor) return;
            
            const message = {
                type: 'TEXT_CHANGE',
                userId: userId,
                username: username,
                docId: docId,
                content: editor.value
            };
            
            try {
                socket.send(JSON.stringify(message));
                console.log("Changement de texte envoyé");
            } catch (e) {
                console.error("Erreur lors de l'envoi du changement de texte:", e);
                showNotification('Échec de l\'envoi des modifications. Veuillez vérifier votre connexion.', 'warning');
            }
        } else if (!isConnected) {
            console.warn("Impossible d'envoyer les modifications: WebSocket déconnecté");
            showNotification('Déconnecté - les modifications ne seront pas sauvegardées ou partagées', 'warning');
        }
    }
    
    // Envoyer un message de chat
    function sendChatMessage() {
        if (isConnected) {
            const chatInput = document.getElementById('chatInput');
            if (!chatInput) return;
            
            const content = chatInput.value.trim();
            
            if (content) {
                const message = {
                    type: 'CHAT_MESSAGE',
                    userId: userId,
                    username: username,
                    docId: docId,
                    content: content
                };
                
                try {
                    socket.send(JSON.stringify(message));
                    chatInput.value = '';
                    console.log("Message de chat envoyé");
                } catch (e) {
                    console.error("Erreur lors de l'envoi du message de chat:", e);
                    showNotification('Échec de l\'envoi du message', 'warning');
                }
            }
        } else {
            showNotification('Impossible d\'envoyer le message: Vous êtes déconnecté', 'warning');
        }
    }
    
    // Sauvegarder le document sur le serveur
    function saveDocument() {
        const editor = document.getElementById('editor');
        const documentTitle = document.getElementById('documentTitle');
        
        if (!editor) return;
        
        // Encoder les données pour éviter les problèmes
        const content = editor.value;
        const title = documentTitle ? documentTitle.value : '';
        
        console.log("Sauvegarde du document...");
        showNotification('Sauvegarde du document...', 'info');
        
        const formData = new FormData();
        formData.append('docId', docId);
        formData.append('content', content);
        if (title) {
            formData.append('title', title);
        }
        
        fetch(`${contextPath}/documents/save`, {
            method: 'POST',
            body: formData
        })
        .then(response => {
            console.log("Statut de la réponse de sauvegarde:", response.status);
            if (!response.ok) {
                throw new Error('Le serveur a retourné ' + response.status);
            }
            return response.text();
        })
        .then(data => {
            console.log("Réponse de sauvegarde:", data);
            // Mettre à jour la date de dernière sauvegarde
            const now = new Date();
            const lastSaved = document.getElementById('lastSaved');
            if (lastSaved) {
                lastSaved.textContent = `Dernière sauvegarde: ${now.toLocaleString()}`;
            }
            
            // Mettre à jour le titre de la page
            if (documentTitle) {
                document.title = `${documentTitle.value} - Collaborative Editor`;
            }
            
            showNotification('Document sauvegardé avec succès', 'success');
        })
    }
    
	function revertDocument(historyId) {
	    if (!confirm('Êtes-vous sûr de vouloir revenir à cette version? Cette action ne peut pas être annulée.')) {
	        return;
	    }
	    
	    showNotification('Restauration du document...', 'info');
	    
	    // Créer un formulaire et l'envoyer directement
	    const form = document.createElement('form');
	    form.method = 'POST';
	    form.action = `${contextPath}/documents/revert`;
	    form.style.display = 'none';
	    
	    const historyIdInput = document.createElement('input');
	    historyIdInput.type = 'hidden';
	    historyIdInput.name = 'historyId';
	    historyIdInput.value = historyId;
	    
	    form.appendChild(historyIdInput);
	    document.body.appendChild(form);
	    
	    form.submit();
	}
    
    // Afficher une notification
    function showNotification(message, type) {
        // Créer un div de notification s'il n'existe pas déjà
        let container = document.getElementById('notification-container');
        if (!container) {
            container = document.createElement('div');
            container.id = 'notification-container';
            container.className = 'notification-container';
            document.body.appendChild(container);
        }
        
        const notif = document.createElement('div');
        notif.className = `notification is-${type}`;
        notif.innerHTML = `
            <button class="delete"></button>
            ${message}
        `;
        
        container.appendChild(notif);
        
        // Ajouter un écouteur d'événements pour le bouton de fermeture
        notif.querySelector('.delete').addEventListener('click', () => {
            notif.remove();
        });
        
        // Supprimer automatiquement après 3 secondes
        setTimeout(() => {
            if (notif.parentNode === container) {
                container.removeChild(notif);
            }
        }, 3000);
    }
    
    // Initialiser l'application
    function init() {
        // Se connecter au WebSocket
        connectWebSocket();
        
        // Événement de changement de l'éditeur
        const editor = document.getElementById('editor');
        if (editor) {
            let typingTimer;
            
            editor.addEventListener('input', () => {
                if (canWrite) {
                    clearTimeout(typingTimer);
                    typingTimer = setTimeout(() => {
                        sendTextChange();
                    }, 500);
                }
            });
        }
        
        // Fonctionnalité de chat
        const chatInput = document.getElementById('chatInput');
        const sendButton = document.getElementById('sendMessage');
        
        if (chatInput) {
            chatInput.addEventListener('keypress', event => {
                if (event.key === 'Enter') {
                    sendChatMessage();
                    event.preventDefault();
                }
            });
        }
        
        if (sendButton) {
            sendButton.addEventListener('click', () => {
                sendChatMessage();
            });
        }
        
        // Bouton de sauvegarde
        const saveButton = document.getElementById('saveDocument');
        if (saveButton) {
            saveButton.addEventListener('click', () => {
                saveDocument();
            });
        }
        
        // Commande clavier pour la sauvegarde (Ctrl+S / Cmd+S)
        document.addEventListener('keydown', event => {
            if ((event.ctrlKey || event.metaKey) && event.key === 's') {
                event.preventDefault(); // Empêcher la boîte de dialogue de sauvegarde du navigateur
                saveDocument();
            }
        });
        
        // Auto-sauvegarde toutes les 30 secondes si des modifications ont été apportées
        if (editor) {
            let lastContent = editor.value;
            setInterval(() => {
                if (canWrite && editor.value !== lastContent) {
                    saveDocument();
                    lastContent = editor.value;
                }
            }, 30000);
        }
        
        // Fonctionnalité du modal d'historique
        const historyModal = document.getElementById('historyModal');
        const showHistoryButton = document.getElementById('showHistory');
        const closeHistoryButton = document.getElementById('closeHistory');
        const modalCloseButtons = document.querySelectorAll('.modal .delete, .modal .cancel-delete');
        
        if (showHistoryButton && historyModal) {
            showHistoryButton.addEventListener('click', () => {
                historyModal.classList.add('is-active');
            });
        }
        
        if (closeHistoryButton && historyModal) {
            closeHistoryButton.addEventListener('click', () => {
                historyModal.classList.remove('is-active');
            });
        }
        
        if (modalCloseButtons.length > 0) {
            modalCloseButtons.forEach(button => {
                button.addEventListener('click', () => {
                    document.querySelectorAll('.modal').forEach(modal => {
                        modal.classList.remove('is-active');
                    });
                });
            });
        }
        
        // Fonctionnalité des boutons de restauration de version
        const revertVersionButtons = document.querySelectorAll('.revert-version');
        
        if (revertVersionButtons.length > 0) {
            revertVersionButtons.forEach(button => {
                button.addEventListener('click', () => {
                    const historyId = button.dataset.historyId;
                    revertDocument(historyId);
                });
            });
        }
        
        // Fonctionnalité de suppression de document
        const deleteDocumentButton = document.getElementById('deleteDocument');
        const deleteModal = document.getElementById('deleteModal');
        
        if (deleteDocumentButton && deleteModal) {
            deleteDocumentButton.addEventListener('click', () => {
                deleteModal.classList.add('is-active');
            });
        }
        
        const deleteForm = document.getElementById('deleteForm');
		
		console.log("Delete form found:", deleteForm);
		
        if (deleteForm) {
            deleteForm.addEventListener('submit', (e) => {
                e.preventDefault();
                
                showNotification('Suppression du document...', 'info');
                
                const xhr = new XMLHttpRequest();
                xhr.open('DELETE', `${contextPath}/documents/${docId}`, true);
                
                xhr.onload = function() {
                    if (xhr.status === 200) {
                        showNotification('Document supprimé avec succès', 'success');
                        setTimeout(() => {
                            window.location.href = `${contextPath}/dashboard`;
                        }, 1000);
                    } else {
                        showNotification('Échec de la suppression du document', 'danger');
                    }
                };
                
                xhr.onerror = function() {
                    console.error("Erreur:", xhr);
                    showNotification('Une erreur est survenue lors de la suppression', 'danger');
                };
                
                xhr.send();
            });
        }
        
        // Bascule du menu mobile
        const navbarBurgers = Array.prototype.slice.call(document.querySelectorAll('.navbar-burger'), 0);
        if (navbarBurgers.length > 0) {
            navbarBurgers.forEach(el => {
                el.addEventListener('click', () => {
                    const target = document.getElementById(el.dataset.target);
                    el.classList.toggle('is-active');
                    if (target) {
                        target.classList.toggle('is-active');
                    }
                });
            });
        }
    }
    
    // Initialiser l'application
    init();
});