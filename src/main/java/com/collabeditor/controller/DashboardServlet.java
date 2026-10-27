package com.collabeditor.controller;

import com.collabeditor.dao.DocumentDAO;
import com.collabeditor.dao.PermissionDAO;
import com.collabeditor.dao.UserDAO;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {
    private DocumentDAO documentDAO;
    private PermissionDAO permissionDAO;
    private UserDAO userDAO;
    
    @Override
    public void init() throws ServletException {
        super.init();
        documentDAO = new DocumentDAO();
        permissionDAO = new PermissionDAO();
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
        String username = (String) session.getAttribute("username");
        System.out.println("DashboardServlet: Récupération des documents pour l'utilisateur " + userId + " (" + username + ")");
        
        // Get documents owned by the user
        List<Document> myDocuments = documentDAO.getDocumentsByOwner(userId);
        request.setAttribute("myDocuments", myDocuments);
        System.out.println("DashboardServlet: Nombre de documents possédés: " + myDocuments.size());
        
        // Get documents shared with the user
        List<Permission> permissions = permissionDAO.getPermissionsByUser(userId);
        System.out.println("DashboardServlet: Nombre de permissions trouvées: " + permissions.size());
        
        List<Map<String, Object>> sharedDocuments = new ArrayList<>();
        
        // Créer un ensemble des IDs de documents possédés par l'utilisateur pour une recherche plus rapide
        Set<Integer> ownedDocIds = new HashSet<>();
        for (Document doc : myDocuments) {
            ownedDocIds.add(doc.getDocId());
        }
        
        for (Permission permission : permissions) {
            int docId = permission.getDocId();
            System.out.println("DashboardServlet: Traitement de la permission pour le document " + docId);
            
            // Si le document est déjà dans myDocuments, on l'ignore
            if (ownedDocIds.contains(docId)) {
                System.out.println("DashboardServlet: Document ignoré car l'utilisateur est le propriétaire");
                continue;
            }
            
            Document document = documentDAO.getDocumentById(docId);
            if (document == null) {
                System.out.println("DashboardServlet: Document non trouvé pour doc_id: " + docId);
                continue;
            }
            
            System.out.println("DashboardServlet: Document trouvé - Titre: " + document.getTitle() + ", Propriétaire: " + document.getOwnerId());
            
            // Double vérification - le document ne devrait pas appartenir à l'utilisateur actuel
            if (document.getOwnerId() == userId) {
                System.out.println("DashboardServlet: Document ignoré car l'utilisateur est le propriétaire (vérification supplémentaire)");
                continue;
            }
            
            // Get owner information
            User owner = userDAO.getUserById(document.getOwnerId());
            String ownerName = owner != null ? owner.getUsername() : "Unknown";
            System.out.println("DashboardServlet: Propriétaire du document: " + ownerName);
            
            Map<String, Object> docInfo = new HashMap<>();
            docInfo.put("docId", document.getDocId());
            docInfo.put("title", document.getTitle());
            docInfo.put("lastModified", document.getLastModified());
            docInfo.put("ownerName", ownerName);
            docInfo.put("permission", permission);
            
            sharedDocuments.add(docInfo);
            System.out.println("DashboardServlet: Document ajouté à la liste des documents partagés");
        }
        
        System.out.println("DashboardServlet: Nombre total de documents partagés trouvés: " + sharedDocuments.size());
        request.setAttribute("sharedDocuments", sharedDocuments);
        
        request.getRequestDispatcher("/WEB-INF/jsp/dashboard.jsp").forward(request, response);
    }
}