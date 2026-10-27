package com.collabeditor.controller;

import com.collabeditor.dao.DocHistoryDAO;
import com.collabeditor.dao.DocumentDAO;
import com.collabeditor.dao.PermissionDAO;
import com.collabeditor.dao.UserDAO;
import com.collabeditor.model.DocHistory;
import com.collabeditor.model.Document;
import com.collabeditor.model.Permission;
import com.collabeditor.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Date;
import java.util.List;

@WebServlet("/documents/*")
public class DocumentServlet extends HttpServlet {
    private DocumentDAO documentDAO;
    private PermissionDAO permissionDAO;
    private DocHistoryDAO docHistoryDAO;
    private UserDAO userDAO;
    
    @Override
    public void init() throws ServletException {
        super.init();
        documentDAO = new DocumentDAO();
        permissionDAO = new PermissionDAO();
        docHistoryDAO = new DocHistoryDAO();
        userDAO = new UserDAO();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }
        
        int userId = (int) session.getAttribute("userId");
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            // List all documents the user has access to
            List<Permission> permissions = permissionDAO.getPermissionsByUser(userId);
            request.setAttribute("permissions", permissions);
            
            // Add document information for each permission
            for (Permission permission : permissions) {
                Document document = documentDAO.getDocumentById(permission.getDocId());
                request.setAttribute("document_" + permission.getDocId(), document);
            }
            
            request.getRequestDispatcher("/WEB-INF/jsp/document_list.jsp").forward(request, response);
        } else if (pathInfo.equals("/new")) {
            // Show form to create a new document
            request.getRequestDispatcher("/WEB-INF/jsp/document_form.jsp").forward(request, response);
        } else {
            // Assume the path is /documents/{docId}
            try {
                int docId = Integer.parseInt(pathInfo.substring(1));
                Document document = documentDAO.getDocumentById(docId);
                
                if (document == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                
                // Check if user has permission to read this document
                if (!permissionDAO.canRead(userId, docId) && document.getOwnerId() != userId) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
                
                // Add document to request
                request.setAttribute("document", document);
                
                // Check if user has write permission
                boolean canWrite = permissionDAO.canWrite(userId, docId) || document.getOwnerId() == userId;
                request.setAttribute("canWrite", canWrite);
                
                // Check if user has admin permission
                boolean isAdmin = permissionDAO.isAdmin(userId, docId) || document.getOwnerId() == userId;
                request.setAttribute("isAdmin", isAdmin);
                
                // Get document history
                if (isAdmin) {
                    List<DocHistory> history = docHistoryDAO.getHistoryByDoc(docId);
                    request.setAttribute("history", history);
                    
                    // Récupérer les informations sur les utilisateurs pour l'affichage de l'historique
                    List<User> users = userDAO.getAllUsers();
                    request.setAttribute("users", users);
                    
                    System.out.println("Historique du document " + docId + " : " + history.size() + " entrées trouvées");
                }
                
                // Forward to editor page
                request.getRequestDispatcher("/WEB-INF/jsp/editor.jsp").forward(request, response);
            } catch (NumberFormatException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }
        
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            System.err.println("userId est null dans la session");
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }
        
        String pathInfo = request.getPathInfo();
        System.out.println("DocumentServlet.doPost: pathInfo=" + pathInfo);
        
        // Traiter les différents chemins d'accès
        if (pathInfo == null || pathInfo.equals("/")) {
            handleCreateDocument(request, response, userId);
        } else if (pathInfo.equals("/save")) {
            handleSaveDocument(request, response, userId);
        } else if (pathInfo.equals("/revert")) {
            handleRevertDocument(request, response, userId);
        } else {
            System.err.println("Chemin non reconnu: " + pathInfo);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
    
    // Méthode pour créer un nouveau document
    private void handleCreateDocument(HttpServletRequest request, HttpServletResponse response, int userId) throws ServletException, IOException {
        System.out.println("Creating new document");
        
        String title = request.getParameter("title");
        String content = request.getParameter("content");
        
        // Validation des paramètres
        if (title == null || title.trim().isEmpty()) {
            request.setAttribute("error", "Document title is required");
            request.getRequestDispatcher("/WEB-INF/jsp/document_form.jsp").forward(request, response);
            return;
        }
        
        // Création du document
        Document document = new Document();
        document.setTitle(title);
        document.setContent(content != null ? content : "");
        document.setCreationDate(new Date());
        document.setLastModified(new Date());
        document.setOwnerId(userId);
        
        int docId = documentDAO.createDocument(document);
        
        if (docId > 0) {
            System.out.println("Document created with ID: " + docId);
            
            // Création des permissions
            Permission permission = new Permission();
            permission.setUserId(userId);
            permission.setDocId(docId);
            permission.setReadAccess(true);
            permission.setWriteAccess(true);
            permission.setAdminAccess(true);
            
            int permId = permissionDAO.createPermission(permission);
            System.out.println("Permission created with ID: " + permId);
            
            // Création de l'historique initial
            try {
                DocHistory history = new DocHistory();
                history.setDocId(docId);
                history.setContent(document.getContent());
                history.setUserId(userId);
                history.setTimestamp(new Date());
                
                int historyId = docHistoryDAO.createHistoryEntry(history);
                System.out.println("Initial history entry created with ID: " + historyId);
            } catch (Exception e) {
                System.err.println("Error creating history entry: " + e.getMessage());
                e.printStackTrace();
            }
            
            // Redirection vers le nouvel éditeur
            response.sendRedirect(request.getContextPath() + "/documents/" + docId);
        } else {
            request.setAttribute("error", "Failed to create document");
            request.getRequestDispatcher("/WEB-INF/jsp/document_form.jsp").forward(request, response);
        }
    }
    
    // Méthode pour sauvegarder un document existant
    private void handleSaveDocument(HttpServletRequest request, HttpServletResponse response, int userId) throws IOException {
        System.out.println("Saving document...");
        
        String docIdStr = request.getParameter("docId");
        String content = request.getParameter("content");
        String title = request.getParameter("title");
        
        // Vérification des paramètres obligatoires
        if (docIdStr == null || docIdStr.trim().isEmpty()) {
            System.err.println("Le paramètre docId est manquant ou vide");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Document ID is required");
            return;
        }
        
        if (content == null) {
            content = ""; // Utiliser une chaîne vide plutôt que null
            System.out.println("Le paramètre content est null, utilisant une chaîne vide");
        }
        
        // Conversion des paramètres
        int docId;
        try {
            docId = Integer.parseInt(docIdStr);
        } catch (NumberFormatException e) {
            System.err.println("Paramètre docId invalide: " + docIdStr);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Invalid document ID format");
            return;
        }
        
        System.out.println("Sauvegarde du document: docId=" + docId + ", longueur du contenu=" + content.length());
        
        try {
            // Récupération du document
            Document document = documentDAO.getDocumentById(docId);
            
            if (document == null) {
                System.err.println("Document non trouvé: docId=" + docId);
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("Document not found");
                return;
            }
            
            // Vérification des permissions
            if (!permissionDAO.canWrite(userId, docId) && document.getOwnerId() != userId) {
                System.err.println("L'utilisateur " + userId + " n'a pas la permission d'éditer le document " + docId);
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("You don't have permission to edit this document");
                return;
            }
            
            // Mise à jour du document
            if (title != null && !title.trim().isEmpty()) {
                document.setTitle(title);
            }
            document.setContent(content);
            document.setLastModified(new Date());
            
            boolean updated = documentDAO.updateDocument(document);
            
            if (updated) {
                System.out.println("Document updated successfully");
                
                // Création d'une entrée dans l'historique
                try {
                    DocHistory history = new DocHistory();
                    history.setDocId(docId);
                    history.setContent(content);
                    history.setUserId(userId);
                    history.setTimestamp(new Date());
                    
                    int historyId = docHistoryDAO.createHistoryEntry(history);
                    
                    if (historyId > 0) {
                        System.out.println("Entrée d'historique créée avec succès: historyId=" + historyId);
                    } else {
                        System.err.println("Échec de la création de l'entrée d'historique, ID retourné: " + historyId);
                    }
                } catch (Exception e) {
                    System.err.println("Erreur lors de la création de l'entrée d'historique: " + e.getMessage());
                    e.printStackTrace();
                }
                
                response.setContentType("text/plain");
                response.getWriter().write("Document saved successfully");
            } else {
                System.err.println("Échec de la mise à jour du document dans la base de données");
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("Failed to save document");
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la sauvegarde du document: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Error saving document: " + e.getMessage());
        }
    }
    
    private void handleRevertDocument(HttpServletRequest request, HttpServletResponse response, int userId) throws IOException {
        String historyIdStr = request.getParameter("historyId");
        
        // Vérification des paramètres obligatoires
        if (historyIdStr == null || historyIdStr.trim().isEmpty()) {
            System.err.println("Le paramètre historyId est manquant ou vide");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("History ID is required");
            return;
        }
        
        // Conversion des paramètres
        int historyId;
        try {
            historyId = Integer.parseInt(historyIdStr);
        } catch (NumberFormatException e) {
            System.err.println("Paramètre historyId invalide: " + historyIdStr);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Invalid history ID format");
            return;
        }
        
        System.out.println("Restauration à partir de l'historique: historyId=" + historyId);
        
        try {
            // Récupération de l'historique
            DocHistory history = docHistoryDAO.getHistoryById(historyId);
            
            if (history == null) {
                System.err.println("Version d'historique non trouvée: historyId=" + historyId);
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("History version not found");
                return;
            }
            
            int docId = history.getDocId();
            
            // Vérification des permissions
            Document document = documentDAO.getDocumentById(docId);
            if (document == null) {
                System.err.println("Document non trouvé: docId=" + docId);
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("Document not found");
                return;
            }
            
            if (!permissionDAO.isAdmin(userId, docId) && document.getOwnerId() != userId) {
                System.err.println("L'utilisateur " + userId + " n'a pas la permission de restaurer ce document");
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("You don't have permission to revert this document");
                return;
            }
            
            // MODIFICATION: Sauvegarder l'état actuel du document avant de le restaurer
            String currentContent = document.getContent();
            
            // Création d'une entrée dans l'historique pour l'état actuel
            DocHistory saveCurrentState = new DocHistory();
            saveCurrentState.setDocId(docId);
            saveCurrentState.setContent(currentContent);
            saveCurrentState.setUserId(userId);
            saveCurrentState.setTimestamp(new Date());
            
            int saveHistoryId = docHistoryDAO.createHistoryEntry(saveCurrentState);
            System.out.println("État actuel sauvegardé avant restauration: historyId=" + saveHistoryId);
            
            // Restauration de la version
            boolean reverted = docHistoryDAO.revertToVersion(historyId, documentDAO);
            
            if (reverted) {
                System.out.println("Document restauré avec succès");
                response.sendRedirect(request.getContextPath() + "/documents/" + docId);
            } else {
                System.err.println("Échec de la restauration du document");
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("Failed to revert document");
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la restauration du document: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Error reverting document: " + e.getMessage());
        }
    }
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        
        int userId = (int) session.getAttribute("userId");
        String pathInfo = request.getPathInfo();
        
        System.out.println("DELETE request received: pathInfo=" + pathInfo);
        
        try {
            // Validation du pathInfo
            if (pathInfo == null || pathInfo.equals("/") || pathInfo.length() <= 1) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("Document ID is required");
                return;
            }
            
            int docId = Integer.parseInt(pathInfo.substring(1));
            System.out.println("Attempting to delete document ID=" + docId + " by user ID=" + userId);
            
            Document document = documentDAO.getDocumentById(docId);
            
            if (document == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            // Vérification des permissions
            if (!permissionDAO.isAdmin(userId, docId) && document.getOwnerId() != userId) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            
            // Suppression des permissions associées au document
            try {
                List<Permission> permissions = permissionDAO.getPermissionsByDoc(docId);
                for (Permission permission : permissions) {
                    permissionDAO.deletePermission(permission.getPermId());
                }
                System.out.println("All permissions for document " + docId + " deleted");
            } catch (Exception e) {
                System.err.println("Error while deleting document permissions: " + e.getMessage());
                // Continue avec la suppression du document même si la suppression des permissions échoue
            }
            
            // Suppression des entrées d'historique associées au document
            DocHistoryDAO historyDAO = new DocHistoryDAO();
            try {
                List<DocHistory> historyEntries = historyDAO.getHistoryByDoc(docId);
                for (DocHistory entry : historyEntries) {
                    historyDAO.deleteHistory(entry.getHistoryId());
                }
                System.out.println("All history entries for document " + docId + " deleted");
            } catch (Exception e) {
                System.err.println("Error while deleting document history: " + e.getMessage());
            }
            
            boolean deleted = documentDAO.deleteDocument(docId);
            
            if (deleted) {
                System.out.println("Document " + docId + " deleted successfully");
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("Document deleted successfully");
            } else {
                System.out.println("Failed to delete document " + docId);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("Failed to delete document");
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid document ID format: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Invalid document ID format");
        } catch (Exception e) {
            System.err.println("Error deleting document: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Error deleting document: " + e.getMessage());
        }
    }
}