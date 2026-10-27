<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard - Collaborative Editor</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bulma@0.9.3/css/bulma.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <!-- Notification style -->
    <style>
        .notification-container {
            position: fixed;
            top: 20px;
            right: 20px;
            z-index: 1000;
        }
        .notification-container .notification {
            margin-bottom: 10px;
            animation: fadeIn 0.3s, fadeOut 0.5s 2.5s forwards;
        }
        @keyframes fadeIn {
            from { opacity: 0; }
            to { opacity: 1; }
        }
        @keyframes fadeOut {
            from { opacity: 1; }
            to { opacity: 0; }
        }
    </style>
</head>
<body>
    <!-- Notification Container -->
    <div class="notification-container" id="notification-container"></div>
    
    <!-- Navigation Bar -->
    <nav class="navbar is-primary" role="navigation" aria-label="main navigation">
        <div class="navbar-brand">
            <a class="navbar-item" href="${pageContext.request.contextPath}/">
                <strong>CollabEditor</strong>
            </a>
            
            <a role="button" class="navbar-burger" aria-label="menu" aria-expanded="false" data-target="navbarBasic">
                <span aria-hidden="true"></span>
                <span aria-hidden="true"></span>
                <span aria-hidden="true"></span>
            </a>
        </div>
        
        <div id="navbarBasic" class="navbar-menu">
            <div class="navbar-start">
                <a class="navbar-item is-active" href="${pageContext.request.contextPath}/dashboard">
                    Dashboard
                </a>
                
                <a class="navbar-item" href="${pageContext.request.contextPath}/documents/new">
                    New Document
                </a>
            </div>
            
            <div class="navbar-end">
                <div class="navbar-item">
                    <p>Hello, <strong>${sessionScope.username}</strong></p>
                </div>
                <div class="navbar-item">
                    <div class="buttons">
                        <a class="button is-light" href="${pageContext.request.contextPath}/auth/logout">
                            Log out
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </nav>

    <!-- Main Content -->
    <section class="section">
        <div class="container">
            <h1 class="title">My Documents</h1>
            
            <div class="buttons my-3">
                <a href="${pageContext.request.contextPath}/documents/new" class="button is-primary">
                    <span>New Document</span>
                </a>
            </div>
            
            <c:if test="${empty myDocuments}">
                <div class="notification is-info">
                    You don't have any documents yet. <a href="${pageContext.request.contextPath}/documents/new">Create your first document</a>.
                </div>
            </c:if>
            
            <c:if test="${not empty myDocuments}">
                <div class="table-container">
                    <table class="table is-fullwidth is-hoverable">
                        <thead>
                            <tr>
                                <th>Title</th>
                                <th>Created</th>
                                <th>Last Modified</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="doc" items="${myDocuments}">
                                <tr>
                                    <td>
                                        <a href="${pageContext.request.contextPath}/documents/${doc.docId}">
                                            ${doc.title}
                                        </a>
                                    </td>
                                    <td><fmt:formatDate value="${doc.creationDate}" pattern="dd/MM/yyyy HH:mm" /></td>
                                    <td><fmt:formatDate value="${doc.lastModified}" pattern="dd/MM/yyyy HH:mm" /></td>
                                    <td>
                                        <div class="buttons are-small">
                                            <a href="${pageContext.request.contextPath}/documents/${doc.docId}" class="button is-primary">
                                                <span>Edit</span>
                                            </a>
                                            <button class="button is-info share-doc" data-doc-id="${doc.docId}" data-doc-title="${doc.title}">
                                                <span>Share</span>
                                            </button>
                                            <button class="button is-danger delete-doc-direct" data-doc-id="${doc.docId}" data-doc-title="${doc.title}">
                                                <span>Delete</span>
                                            </button>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:if>
            
            <h1 class="title mt-6">Shared With Me</h1>
            <div style="background-color: #f8f9fa; padding: 10px; margin-bottom: 10px; border-left: 4px solid #6c757d; font-family: monospace; font-size: 12px;">
		    <p><strong>Debug Info:</strong></p>
		    <p>sharedDocuments size: ${fn:length(sharedDocuments)}</p>
		    <c:forEach var="doc" items="${sharedDocuments}" varStatus="status">
		        <p>Document ${status.index + 1}: ${doc.title} (ID: ${doc.docId}) - Owner: ${doc.ownerName}</p>
		    </c:forEach>
			</div>
            <c:if test="${empty sharedDocuments}">
                <div class="notification is-info">
                    No documents have been shared with you yet.
                </div>
            </c:if>
            
            <c:if test="${not empty sharedDocuments}">
                <div class="table-container">
                    <table class="table is-fullwidth is-hoverable">
                        <thead>
                            <tr>
                                <th>Title</th>
                                <th>Owner</th>
                                <th>Last Modified</th>
                                <th>Access</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="doc" items="${sharedDocuments}">
                                <tr>
                                    <td>
                                        <a href="${pageContext.request.contextPath}/documents/${doc.docId}">
                                            ${doc.title}
                                        </a>
                                    </td>
                                    <td>${doc.ownerName}</td>
                                    <td><fmt:formatDate value="${doc.lastModified}" pattern="dd/MM/yyyy HH:mm" /></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${doc.permission.adminAccess}">
                                                <span class="tag is-success">Admin</span>
                                            </c:when>
                                            <c:when test="${doc.permission.writeAccess}">
                                                <span class="tag is-primary">Write</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="tag is-info">Read</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <div class="buttons are-small">
                                            <a href="${pageContext.request.contextPath}/documents/${doc.docId}" class="button is-primary">
                                                <span>Open</span>
                                            </a>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:if>
        </div>
    </section>

    <!-- Delete Confirmation Modal -->
    <div class="modal" id="deleteModal">
        <div class="modal-background"></div>
        <div class="modal-card">
            <header class="modal-card-head">
                <p class="modal-card-title">Confirm Deletion</p>
                <button class="delete" aria-label="close"></button>
            </header>
            <section class="modal-card-body">
                <p>Are you sure you want to delete the document "<span id="docTitle"></span>"?</p>
                <p class="has-text-danger">This action cannot be undone.</p>
            </section>
            <footer class="modal-card-foot">
                <form id="deleteForm" method="post">
                    <input type="hidden" id="docIdInput" name="docId" value="">
                    <button type="submit" class="button is-danger">Delete</button>
                    <button type="button" class="button cancel-delete">Cancel</button>
                </form>
            </footer>
        </div>
    </div>
    
    <!-- Share Document Modal -->
    <div class="modal" id="shareModal">
        <div class="modal-background"></div>
        <div class="modal-card">
            <header class="modal-card-head">
                <p class="modal-card-title">Share Document: <span id="shareDocTitle"></span></p>
                <button class="delete" aria-label="close"></button>
            </header>
            <section class="modal-card-body">
                <div id="current-sharing" class="mb-4">
                    <h3 class="title is-5">Currently Shared With</h3>
                    <div id="shared-users-list">
                        <!-- Existing users with access will be populated here -->
                        <div class="notification is-info is-light">
                            Loading shared users...
                        </div>
                    </div>
                </div>
                
                <hr>
                
                <h3 class="title is-5">Share with a New User</h3>
                <form id="shareForm" class="mb-4">
                    <input type="hidden" id="shareDocId" name="docId" value="">
                    
                    <div class="field">
                        <label class="label">Username or Email</label>
                        <div class="control">
                            <input class="input" type="text" id="shareUsername" name="username" placeholder="Enter username or email" required>
                        </div>
                    </div>
                    
                    <div class="field">
                        <label class="label">Permission Level</label>
                        <div class="control">
                            <div class="select is-fullwidth">
                                <select id="sharePermission" name="permission">
                                    <option value="read">Read Only</option>
                                    <option value="write" selected>Read & Write</option>
                                    <option value="admin">Admin (Can revert & manage)</option>
                                </select>
                            </div>
                        </div>
                    </div>
                    
                    <div class="field">
                        <div class="control">
                            <button type="submit" class="button is-primary">
                                Share Document
                            </button>
                        </div>
                    </div>
                </form>
                
                <div id="shareResult"></div>
            </section>
            <footer class="modal-card-foot">
                <button class="button" id="closeShare">Close</button>
            </footer>
        </div>
    </div>

    <footer class="footer">
        <div class="content has-text-centered">
            <p>
                <strong>Collaborative Document Editor</strong> by L3 Informatique.
            </p>
        </div>
    </footer>

    <script>
        // Fonction pour montrer des notifications
        function showNotification(message, type) {
		    const container = document.getElementById('notification-container');
		    const notif = document.createElement('div');
		    notif.className = `notification is-${type}`;
		    notif.innerHTML = `
		        <button class="delete"></button>
		        ${message}
		    `;
		    
		    container.appendChild(notif);
		    
		    // Ajouter un écouteur d'événements au bouton de fermeture
		    notif.querySelector('.delete').addEventListener('click', () => {
		        notif.remove();
		    });
		    
		    // Assurer que le texte est visible
		    notif.style.color = type === 'light' ? '#333' : '#fff';
		    notif.style.opacity = '1';
		    
		    // Suppression automatique après 3 secondes
		    setTimeout(() => {
		        if (notif.parentNode === container) {
		            container.removeChild(notif);
		        }
		    }, 3000);
		}

        // Fonction pour supprimer directement un document
        function deleteDocument(docId, title) {
            if (confirm(`Are you sure you want to delete the document "${title}"? This action cannot be undone.`)) {
                console.log("Attempting to delete document:", docId);
                
                fetch('${pageContext.request.contextPath}/documents/' + docId, {
                    method: 'DELETE',
                    headers: {
                        'Content-Type': 'application/json'
                    }
                })
                .then(response => {
                    console.log("Delete response status:", response.status);
                    return response.text().then(text => {
                        return {
                            status: response.status,
                            text: text
                        };
                    });
                })
                .then(data => {
                    if (data.status === 200) {
                        showNotification('Document deleted successfully', 'success');
                        setTimeout(() => {
                            window.location.reload();
                        }, 1000);
                    } else {
                        showNotification('Failed to delete document: ' + data.text, 'danger');
                    }
                })
                .catch(error => {
                    console.error("Error:", error);
                    showNotification('An error occurred while deleting', 'danger');
                });
            }
        }
        
        // Récupérer les utilisateurs avec qui un document est partagé
        function fetchSharedUsers(docId) {
            const sharedUsersList = document.getElementById('shared-users-list');
            sharedUsersList.innerHTML = '<div class="notification is-info is-light">Loading shared users...</div>';
            
            fetch('${pageContext.request.contextPath}/api/permissions?docId=' + docId)
                .then(response => {
                    if (!response.ok) {
                        throw new Error('Server returned ' + response.status);
                    }
                    return response.json();
                })
                .then(data => {
                    if (data.length === 0) {
                        sharedUsersList.innerHTML = '<div class="notification is-light">This document is not shared with anyone yet.</div>';
                        return;
                    }
                    
                    // Créer une table pour les utilisateurs partagés
                    let tableHTML = `
                        <table class="table is-fullwidth is-narrow">
                            <thead>
                                <tr>
                                    <th>User</th>
                                    <th>Permission</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                    `;
                    
                    data.forEach(user => {
                        let permissionTag = '';
                        if (user.adminAccess) {
                            permissionTag = '<span class="tag is-success">Admin</span>';
                        } else if (user.writeAccess) {
                            permissionTag = '<span class="tag is-primary">Write</span>';
                        } else {
                            permissionTag = '<span class="tag is-info">Read</span>';
                        }
                        
                        tableHTML += `
                            <tr data-perm-id="${user.permId}">
                                <td>${user.username}</td>
                                <td>${permissionTag}</td>
                                <td>
                                    <div class="buttons are-small">
                                        <button class="button is-warning edit-permission" 
                                                data-perm-id="${user.permId}" 
                                                data-username="${user.username}"
                                                data-read="${doc.permission.readAccess}" 
                                                data-write="${doc.permission.writeAccess}" 
                                                data-admin="${doc.permission.adminAccess}">
                                            <span>Edit</span>
                                        </button>
                                        <button class="button is-danger remove-permission" data-perm-id="${user.permId}">
                                            <span>Remove</span>
                                        </button>
                                    </div>
                                </td>
                            </tr>
                        `;
                    });
                    
                    tableHTML += `
                            </tbody>
                        </table>
                    `;
                    
                    sharedUsersList.innerHTML = tableHTML;
                    
                    // Ajouter des écouteurs d'événements aux boutons
                    document.querySelectorAll('.edit-permission').forEach(button => {
                        button.addEventListener('click', function() {
                            const permId = this.dataset.permId;
                            const username = this.dataset.username;
                            const read = this.dataset.read === 'true';
                            const write = this.dataset.write === 'true';
                            const admin = this.dataset.admin === 'true';
                            
                            // Créer un formulaire d'édition
                            // (Par simplicité, nous allons simplement créer une nouvelle permission au lieu d'éditer)
                            document.getElementById('shareUsername').value = username;
                            let permission = 'read';
                            if (admin) permission = 'admin';
                            else if (write) permission = 'write';
                            document.getElementById('sharePermission').value = permission;
                            
                            // Focus sur la liste déroulante de permission
                            document.getElementById('sharePermission').focus();
                            
                            showNotification(`Updating permissions for ${username}`, 'info');
                        });
                    });
                    
                    document.querySelectorAll('.remove-permission').forEach(button => {
                        button.addEventListener('click', function() {
                            const permId = this.dataset.permId;
                            const row = document.querySelector(`tr[data-perm-id="${permId}"]`);
                            const username = row.querySelector('td').textContent;
                            
                            if (confirm(`Are you sure you want to remove sharing for ${username}?`)) {
                                removePermission(permId, username);
                            }
                        });
                    });
                })
                .catch(error => {
                    console.error('Error fetching shared users:', error);
                    sharedUsersList.innerHTML = `
                        <div class="notification is-danger is-light">
                            Error loading shared users: ${error.message}
                        </div>
                    `;
                });
        }
        
        // Supprimer une permission pour un utilisateur
        function removePermission(permId, username) {
            fetch('${pageContext.request.contextPath}/api/permissions/' + permId, {
                method: 'DELETE'
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Server returned ' + response.status);
                }
                
                // Supprimer la ligne de la table
                const row = document.querySelector(`tr[data-perm-id="${permId}"]`);
                if (row) {
                    row.remove();
                }
                
                // Vérifier si la table est maintenant vide
                const tbody = document.querySelector('#shared-users-list table tbody');
                if (tbody && !tbody.querySelector('tr')) {
                    document.getElementById('shared-users-list').innerHTML = 
                        '<div class="notification is-light">This document is not shared with anyone yet.</div>';
                }
                
                showNotification(`Removed sharing for ${username}`, 'success');
            })
            .catch(error => {
                console.error('Error removing permission:', error);
                showNotification('Error removing sharing: ' + error.message, 'danger');
            });
        }
        
        function shareDocument(event) {
            event.preventDefault();
            
            console.log("shareDocument function called");
            
            const docId = document.getElementById('shareDocId').value;
            const username = document.getElementById('shareUsername').value.trim();
            const permission = document.getElementById('sharePermission').value;
            
            console.log("Form values:", { docId, username, permission });
            
            if (!username) {
                showNotification('Please enter a username or email', 'warning');
                return;
            }
            
            // Créer des paramètres pour l'URL
            const params = new URLSearchParams();
            params.append('docId', docId);
            params.append('username', username);
            params.append('readAccess', 'true'); // Toujours vrai
            
            // Définir les accès en fonction de la permission sélectionnée
            if (permission === 'write' || permission === 'admin') {
                params.append('writeAccess', 'true');
            }
            
            if (permission === 'admin') {
                params.append('adminAccess', 'true');
            }
            
            // Afficher la notification de chargement
            showNotification('Partage du document en cours...', 'info');
            
            console.log("URL params:", params.toString());
            
            // Envoyer la requête avec fetch
            fetch(`${pageContext.request.contextPath}/api/permissions`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: params
            })
            .then(response => {
                console.log("Response received:", response.status, response.statusText);
                
                if (!response.ok) {
                    return response.text().then(text => {
                        throw new Error(text || `Server returned ${response.status}`);
                    });
                }
                return response.text();
            })
            .then(data => {
                console.log("Success:", data);
                showNotification('Document shared successfully with ' + username, 'success');
                document.getElementById('shareUsername').value = '';
                
                // Rafraîchir la liste des utilisateurs partagés
                fetchSharedUsers(docId);
                
                // Recharger la page après un partage réussi pour voir les modifications
                setTimeout(() => window.location.reload(), 2000);
            })
            .catch(error => {
                console.error("Error:", error);
                showNotification('Error: ' + error.message, 'danger');
            });
        }
        
        // Initialiser les écouteurs d'événements lorsque le DOM est chargé
        document.addEventListener('DOMContentLoaded', () => {
            console.log("DOM fully loaded");
            
            if (!document.getElementById('notification-container')) {
                const container = document.createElement('div');
                container.id = 'notification-container';
                container.className = 'notification-container';
                document.body.appendChild(container);
            }
            
            // Basculer le menu de navigation sur mobile
            const navbarBurgers = Array.prototype.slice.call(document.querySelectorAll('.navbar-burger'), 0);
            if (navbarBurgers.length > 0) {
                navbarBurgers.forEach(el => {
                    el.addEventListener('click', () => {
                        const target = document.getElementById(el.dataset.target);
                        el.classList.toggle('is-active');
                        target.classList.toggle('is-active');
                    });
                });
            }
            
            // Pour les boutons de suppression directs
            const deleteDirectButtons = document.querySelectorAll('.delete-doc-direct');
            console.log("Found", deleteDirectButtons.length, "direct delete buttons");
            
            deleteDirectButtons.forEach(button => {
                button.addEventListener('click', function() {
                    const docId = this.dataset.docId;
                    const title = this.dataset.docTitle;
                    console.log("Direct delete button clicked for document:", docId, title);
                    deleteDocument(docId, title);
                });
            });
            
            // Modal de confirmation de suppression (ancienne méthode)
            const deleteModal = document.getElementById('deleteModal');
            const docTitle = document.getElementById('docTitle');
            const docIdInput = document.getElementById('docIdInput');
            const deleteButtons = document.querySelectorAll('.delete-doc');
            const closeModalButtons = document.querySelectorAll('.delete, .cancel-delete');
            const deleteForm = document.getElementById('deleteForm');
            
            if (deleteButtons.length > 0) {
                console.log("Found", deleteButtons.length, "delete buttons (old method)");
                deleteButtons.forEach(button => {
                    button.addEventListener('click', () => {
                        const docId = button.dataset.docId;
                        const title = button.dataset.docTitle;
                        console.log("Delete button clicked (old method) for:", docId, title);
                        docTitle.textContent = title;
                        docIdInput.value = docId;
                        deleteForm.action = '${pageContext.request.contextPath}/documents/' + docId;
                        deleteModal.classList.add('is-active');
                    });
                });
            }
            
            if (closeModalButtons.length > 0) {
                closeModalButtons.forEach(button => {
                    button.addEventListener('click', () => {
                        document.querySelectorAll('.modal').forEach(modal => {
                            modal.classList.remove('is-active');
                        });
                    });
                });
            }
            
            // Modal de partage de document
            const shareModal = document.getElementById('shareModal');
		    const shareDocTitle = document.getElementById('shareDocTitle');
		    const shareDocId = document.getElementById('shareDocId');
		    const shareButtons = document.querySelectorAll('.share-doc');
		    const closeShareButton = document.getElementById('closeShare');
		    const shareForm = document.getElementById('shareForm');
            
		    console.log("Share form:", shareForm);
		    
		    if (shareForm) {
		        console.log("Attaching submit event to share form");
		        shareForm.addEventListener('submit', function(event) {
		            console.log("Share form submitted");
		            shareDocument(event);
		        });
		    } else {
		        console.error("Share form not found!");
		    }
		    
            if (shareButtons.length > 0) {
                console.log("Found", shareButtons.length, "share buttons");
                shareButtons.forEach(button => {
                    button.addEventListener('click', () => {
                        const docId = button.dataset.docId;
                        const title = button.dataset.docTitle;
                        console.log("Share button clicked for:", docId, title);
                        shareDocTitle.textContent = title;
                        shareDocId.value = docId;
                        shareModal.classList.add('is-active');
                        
                        // Récupérer et afficher les utilisateurs partagés
                        fetchSharedUsers(docId);
                    });
                });
            }
            
            if (closeShareButton) {
                closeShareButton.addEventListener('click', () => {
                    shareModal.classList.remove('is-active');
                });
            }
            
        });
    </script>
</body>
</html>