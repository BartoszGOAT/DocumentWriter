package com.collabeditor.controller;

import com.collabeditor.dao.PermissionDAO;
import com.collabeditor.dao.UserDAO;
import com.collabeditor.dao.DocumentDAO;
import com.collabeditor.model.Permission;
import com.collabeditor.model.User;
import com.collabeditor.model.Document;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/permissions/*")
public class PermissionApiServlet extends HttpServlet {
    private PermissionDAO permissionDAO;
    private UserDAO userDAO;
    private DocumentDAO documentDAO;
    
    @Override
    public void init() throws ServletException {
        super.init();
        permissionDAO = new PermissionDAO();
        userDAO = new UserDAO();
        documentDAO = new DocumentDAO();
    }
    
    // GET method to list permissions for a document
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("You must be logged in to access this API");
            return;
        }
        
        int userId = (int) session.getAttribute("userId");
        
        try {
            // Get document ID from query parameter
            String docIdStr = request.getParameter("docId");
            if (docIdStr == null || docIdStr.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("Document ID is required");
                return;
            }
            
            int docId = Integer.parseInt(docIdStr);
            
            // Verify the user owns the document or has admin access
            Document document = documentDAO.getDocumentById(docId);
            if (document == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("Document not found");
                return;
            }
            
            if (document.getOwnerId() != userId && !permissionDAO.isAdmin(userId, docId)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("You don't have permission to view sharing for this document");
                return;
            }
            
            // Get permissions for this document
            List<Permission> permissions = permissionDAO.getPermissionsByDoc(docId);
            List<Map<String, Object>> result = new ArrayList<>();
            
            for (Permission perm : permissions) {
                // Skip the owner's permission
                if (perm.getUserId() == document.getOwnerId()) {
                    continue;
                }
                
                User user = userDAO.getUserById(perm.getUserId());
                if (user != null) {
                    Map<String, Object> permInfo = new HashMap<>();
                    permInfo.put("permId", perm.getPermId());
                    permInfo.put("userId", perm.getUserId());
                    permInfo.put("username", user.getUsername());
                    permInfo.put("email", user.getEmail());
                    permInfo.put("readAccess", perm.isReadAccess());
                    permInfo.put("writeAccess", perm.isWriteAccess());
                    permInfo.put("adminAccess", perm.isAdminAccess());
                    
                    result.add(permInfo);
                }
            }
            
            // Return JSON response
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            
            // Simple JSON serialization without external libraries
            StringBuilder jsonBuilder = new StringBuilder("[");
            for (int i = 0; i < result.size(); i++) {
                Map<String, Object> permInfo = result.get(i);
                if (i > 0) {
                    jsonBuilder.append(",");
                }
                
                jsonBuilder.append("{");
                jsonBuilder.append("\"permId\":").append(permInfo.get("permId")).append(",");
                jsonBuilder.append("\"userId\":").append(permInfo.get("userId")).append(",");
                jsonBuilder.append("\"username\":\"").append(escapeJson(permInfo.get("username").toString())).append("\",");
                jsonBuilder.append("\"email\":\"").append(escapeJson(permInfo.get("email").toString())).append("\",");
                jsonBuilder.append("\"readAccess\":").append(permInfo.get("readAccess")).append(",");
                jsonBuilder.append("\"writeAccess\":").append(permInfo.get("writeAccess")).append(",");
                jsonBuilder.append("\"adminAccess\":").append(permInfo.get("adminAccess"));
                jsonBuilder.append("}");
            }
            jsonBuilder.append("]");
            
            PrintWriter out = response.getWriter();
            out.print(jsonBuilder.toString());
            out.flush();
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Invalid document ID format");
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Server error: " + e.getMessage());
        }
    }
    
    // POST method to create a new permission (share document)
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("======= DÉBUT PermissionApiServlet.doPost =======");
        
        // Vérification de la session
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            System.out.println("ERREUR: Session invalide ou userId manquant");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("You must be logged in to access this API");
            return;
        }
        
        int userId = (int) session.getAttribute("userId");
        System.out.println("UserId actuel (partageant): " + userId);
        
        try {
            // Débug: Afficher tous les paramètres de la requête
            System.out.println("Paramètres de la requête:");
            Enumeration<String> paramNames = request.getParameterNames();
            while (paramNames.hasMoreElements()) {
                String paramName = paramNames.nextElement();
                String[] paramValues = request.getParameterValues(paramName);
                for (String value : paramValues) {
                    System.out.println("  " + paramName + ": " + value);
                }
            }
            
            // Récupérer les paramètres
            String docIdStr = request.getParameter("docId");
            String username = request.getParameter("username");
            String readAccessStr = request.getParameter("readAccess");
            String writeAccessStr = request.getParameter("writeAccess");
            String adminAccessStr = request.getParameter("adminAccess");
            
            System.out.println("Paramètres traités:");
            System.out.println("- docId: " + docIdStr);
            System.out.println("- username: " + username);
            System.out.println("- readAccess: " + readAccessStr);
            System.out.println("- writeAccess: " + writeAccessStr);
            System.out.println("- adminAccess: " + adminAccessStr);
            
            // Validation des paramètres
            if (docIdStr == null || docIdStr.isEmpty() || username == null || username.isEmpty()) {
                System.out.println("ERREUR: Paramètres manquants (docId ou username)");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("Document ID and username are required");
                return;
            }
            
            int docId;
            try {
                docId = Integer.parseInt(docIdStr);
            } catch (NumberFormatException e) {
                System.out.println("ERREUR: Format invalide pour docId: " + docIdStr);
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("Invalid document ID format");
                return;
            }
            
            // Interprétation des valeurs booléennes
            boolean readAccess = true; // Toujours vrai (minimum requis)
            boolean writeAccess = writeAccessStr != null && 
                                 (writeAccessStr.equals("true") || writeAccessStr.equals("on") || 
                                  writeAccessStr.equals("1") || writeAccessStr.equals("yes"));
            boolean adminAccess = adminAccessStr != null && 
                                 (adminAccessStr.equals("true") || adminAccessStr.equals("on") || 
                                  adminAccessStr.equals("1") || adminAccessStr.equals("yes"));
            
            System.out.println("Valeurs booléennes interprétées:");
            System.out.println("- readAccess: " + readAccess);
            System.out.println("- writeAccess: " + writeAccess);
            System.out.println("- adminAccess: " + adminAccess);
            
            // Vérifier que le document existe
            Document document = documentDAO.getDocumentById(docId);
            if (document == null) {
                System.out.println("ERREUR: Document non trouvé avec l'ID: " + docId);
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("Document not found");
                return;
            }
            
            System.out.println("Document trouvé: " + document.getTitle() + " (ID: " + document.getDocId() + ")");
            System.out.println("Propriétaire du document: " + document.getOwnerId());
            
            // Vérifier que l'utilisateur a le droit de partager ce document
            if (document.getOwnerId() != userId && !permissionDAO.isAdmin(userId, docId)) {
                System.out.println("ERREUR: L'utilisateur " + userId + " n'a pas les droits pour partager ce document");
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("You don't have permission to share this document");
                return;
            }
            
            // Trouver l'utilisateur cible
            User targetUser = userDAO.getUserByUsername(username);
            if (targetUser == null) {
                System.out.println("Recherche de l'utilisateur par email...");
                // Essayer de trouver par email
                List<User> allUsers = userDAO.getAllUsers();
                for (User user : allUsers) {
                    if (user.getEmail() != null && user.getEmail().equalsIgnoreCase(username)) {
                        targetUser = user;
                        System.out.println("Utilisateur trouvé par email: " + targetUser.getUsername());
                        break;
                    }
                }
                
                if (targetUser == null) {
                    System.out.println("ERREUR: Utilisateur cible non trouvé: " + username);
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.getWriter().write("User not found: " + username);
                    return;
                }
            } else {
                System.out.println("Utilisateur cible trouvé: " + targetUser.getUsername() + " (ID: " + targetUser.getUserId() + ")");
            }
            
            // Ne pas partager avec le propriétaire
            if (targetUser.getUserId() == document.getOwnerId()) {
                System.out.println("ERREUR: Tentative de partage avec le propriétaire");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("You can't share a document with its owner");
                return;
            }
            
            // Vérifier si la permission existe déjà
            Permission existingPerm = permissionDAO.getPermissionByUserAndDoc(targetUser.getUserId(), docId);
            
            if (existingPerm != null) {
                System.out.println("Permission existante trouvée, mise à jour...");
                
                // Mise à jour de la permission existante
                existingPerm.setReadAccess(readAccess);
                existingPerm.setWriteAccess(writeAccess);
                existingPerm.setAdminAccess(adminAccess);
                
                boolean updated = permissionDAO.updatePermission(existingPerm);
                if (updated) {
                    System.out.println("Permission mise à jour avec succès");
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().write("Permission updated successfully");
                } else {
                    System.out.println("ERREUR: Échec de la mise à jour de la permission");
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    response.getWriter().write("Failed to update permission");
                }
            } else {
                System.out.println("Création d'une nouvelle permission...");
                
                // Création d'une nouvelle permission
                Permission newPerm = new Permission();
                newPerm.setUserId(targetUser.getUserId());
                newPerm.setDocId(docId);
                newPerm.setReadAccess(readAccess);
                newPerm.setWriteAccess(writeAccess);
                newPerm.setAdminAccess(adminAccess);
                
                int permId = permissionDAO.createPermission(newPerm);
                if (permId > 0) {
                    System.out.println("Permission créée avec succès (ID: " + permId + ")");
                    response.setStatus(HttpServletResponse.SC_CREATED);
                    response.getWriter().write("Document shared successfully");
                } else {
                    System.out.println("ERREUR: Échec de la création de la permission");
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    response.getWriter().write("Failed to share document");
                }
            }
            
            System.out.println("======= FIN PermissionApiServlet.doPost =======");
        } catch (Exception e) {
            System.out.println("EXCEPTION dans PermissionApiServlet.doPost: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Server error: " + e.getMessage());
        }
    }
    
    // DELETE method to remove a permission
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("You must be logged in to access this API");
            return;
        }
        
        int userId = (int) session.getAttribute("userId");
        String pathInfo = request.getPathInfo();
        
        try {
            // Extract permission ID from path
            if (pathInfo == null || pathInfo.equals("/")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("Permission ID is required");
                return;
            }
            
            int permId = Integer.parseInt(pathInfo.substring(1));
            
            // Get the permission
            Permission permission = permissionDAO.getPermissionById(permId);
            if (permission == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("Permission not found");
                return;
            }
            
            // Get the document
            Document document = documentDAO.getDocumentById(permission.getDocId());
            if (document == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("Document not found");
                return;
            }
            
            // Verify the user owns the document or has admin access
            if (document.getOwnerId() != userId && !permissionDAO.isAdmin(userId, document.getDocId())) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("You don't have permission to remove sharing for this document");
                return;
            }
            
            // Don't remove the owner's permission
            if (permission.getUserId() == document.getOwnerId()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("Cannot remove document owner's permission");
                return;
            }
            
            // Delete the permission
            boolean deleted = permissionDAO.deletePermission(permId);
            if (deleted) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("Permission removed successfully");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("Failed to remove permission");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Invalid permission ID format");
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Server error: " + e.getMessage());
        }
    }
    
    // Helper method to escape JSON strings
    private String escapeJson(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}