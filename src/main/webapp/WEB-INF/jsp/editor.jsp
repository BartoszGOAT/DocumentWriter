<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${document.title} - Collaborative Editor</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bulma@0.9.3/css/bulma.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/editor.css">
    <!-- Styles pour les notifications -->
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
        .system-message {
            font-style: italic;
            color: #888;
        }
        .chat-message {
            margin-bottom: 8px;
            padding-bottom: 8px;
            border-bottom: 1px solid #f0f0f0;
        }
        .chat-message-header {
            display: flex;
            justify-content: space-between;
            margin-bottom: 4px;
        }
        .chat-username {
            font-weight: bold;
        }
        .chat-time {
            color: #888;
            font-size: 0.8em;
        }
    </style>
</head>
<body>
    <!-- Conteneur de notifications -->
    <div id="notification-container" class="notification-container"></div>

    <!-- Barre de navigation -->
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
                <a class="navbar-item" href="${pageContext.request.contextPath}/dashboard">
                    Dashboard
                </a>
                
                <div class="navbar-item has-dropdown is-hoverable">
                    <a class="navbar-link">
                        Document
                    </a>
                    
                    <div class="navbar-dropdown">
                        <a class="navbar-item" id="saveDocument">
                            <span>Save</span>
                        </a>
                        <c:if test="${isAdmin}">
                            <a class="navbar-item" href="#historyModal" id="showHistory">
                                <span>History</span>
                            </a>
                            <hr class="navbar-divider">
                            <a class="navbar-item has-text-danger" id="deleteDocument">
                                <span>Delete</span>
                            </a>
                        </c:if>
                    </div>
                </div>
            </div>
            
            <div class="navbar-end">
                <div class="navbar-item">
                    <div id="onlineUsers" class="tags">
                        <!-- Les utilisateurs en ligne seront affichés ici -->
                        <span class="tag is-success">
                            ${sessionScope.username} (Vous)
                        </span>
                    </div>
                </div>
                <div class="navbar-item">
                    <div class="buttons">
                        <a class="button is-light" href="${pageContext.request.contextPath}/auth/logout">
                            Déconnexion
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </nav>

    <!-- Section principale de l'éditeur -->
    <section class="section p-0">
        <div class="columns is-gapless" style="height: calc(100vh - 52px);">
            <!-- Zone d'édition -->
            <div class="column is-9">
                <div class="editor-container">
                    <div class="editor-header p-2">
                        <div class="field">
                            <div class="control">
                                <input class="input" type="text" id="documentTitle" value="${document.title}" ${isAdmin || document.ownerId == sessionScope.userId ? '' : 'disabled'}>
                            </div>
                        </div>
                    </div>
                    <div class="editor-content p-2">
                        <textarea id="editor" class="textarea" ${canWrite ? '' : 'readonly'}>${document.content}</textarea>
                    </div>
                    <div class="editor-status p-2">
                        <span id="status">Connexion en cours...</span>
                        <span id="lastSaved">
                            Dernière sauvegarde: <fmt:formatDate value="${document.lastModified}" pattern="dd/MM/yyyy HH:mm:ss" />
                        </span>
                    </div>
                </div>
            </div>
            
            <!-- Zone de chat -->
            <div class="column is-3 chat-container">
                <div class="chat-header p-2">
                    <h3 class="title is-5 m-0">Chat</h3>
                </div>
                <div class="chat-messages p-2" id="chatMessages">
                    <!-- Les messages de chat seront affichés ici -->
                </div>
                <div class="chat-input p-2">
                    <div class="field has-addons">
                        <div class="control is-expanded">
                            <input class="input" type="text" id="chatInput" placeholder="Tapez un message...">
                        </div>
                        <div class="control">
                            <button class="button is-primary" id="sendMessage">
                                Envoyer
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <!-- Modal d'historique -->
    <c:if test="${isAdmin}">
        <div class="modal" id="historyModal">
            <div class="modal-background"></div>
            <div class="modal-card">
                <header class="modal-card-head">
                    <p class="modal-card-title">Historique du document</p>
                    <button class="delete" aria-label="close"></button>
                </header>
                <section class="modal-card-body">
                    <div class="table-container">
                        <table class="table is-fullwidth is-hoverable">
						    <thead>
						        <tr>
						            <th>Date</th>
						            <th>Utilisateur</th>
						            <th>Actions</th>
						        </tr>
						    </thead>
						    <tbody>
						        <c:forEach var="entry" items="${history}">
						            <tr>
						                <td><fmt:formatDate value="${entry.timestamp}" pattern="dd/MM/yyyy HH:mm:ss" /></td>
						                <td>
						                    <!-- Récupérer le nom d'utilisateur au lieu de l'ID -->
						                    <c:set var="userName" value="Utilisateur ${entry.userId}" />
						                    <c:forEach var="user" items="${users}">
						                        <c:if test="${user.userId eq entry.userId}">
						                            <c:set var="userName" value="${user.username}" />
						                        </c:if>
						                    </c:forEach>
						                    ${userName}
						                </td>
						                <td>
						                    <div class="buttons are-small">
						                        <button class="button is-info revert-version" data-history-id="${entry.historyId}">
						                            <span>Restaurer</span>
						                        </button>
						                    </div>
						                </td>
						            </tr>
						        </c:forEach>
						    </tbody>
						</table>
                    </div>
                </section>
                <footer class="modal-card-foot">
                    <button class="button" id="closeHistory">Fermer</button>
                </footer>
            </div>
        </div>
    </c:if>

    <!-- Modal de confirmation de suppression -->
    <c:if test="${isAdmin || document.ownerId == sessionScope.userId}">
        <div class="modal" id="deleteModal">
            <div class="modal-background"></div>
            <div class="modal-card">
                <header class="modal-card-head">
                    <p class="modal-card-title">Confirmer la suppression</p>
                    <button class="delete" aria-label="close"></button>
                </header>
                <section class="modal-card-body">
                    <p>Êtes-vous sûr de vouloir supprimer le document "${document.title}" ?</p>
                    <p class="has-text-danger">Cette action ne peut pas être annulée.</p>
                </section>
                <footer class="modal-card-foot">
                    <form id="deleteForm" method="post">
                        <input type="hidden" name="docId" value="${document.docId}">
                        <button type="submit" class="button is-danger">Supprimer</button>
                        <button type="button" class="button cancel-delete">Annuler</button>
                    </form>
                </footer>
            </div>
        </div>
    </c:if>

    <!-- Éléments cachés pour le JavaScript -->
    <input type="hidden" id="docId" value="${document.docId}">
    <input type="hidden" id="userId" value="${sessionScope.userId}">
    <input type="hidden" id="username" value="${sessionScope.username}">
    <input type="hidden" id="canWrite" value="${canWrite}">

    <!-- Inclusion du fichier JavaScript externe -->
    <script src="${pageContext.request.contextPath}/js/editor.js"></script>
</body>
</html>