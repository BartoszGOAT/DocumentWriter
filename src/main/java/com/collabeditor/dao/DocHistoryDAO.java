package com.collabeditor.dao;

import com.collabeditor.model.DocHistory;
import com.collabeditor.model.Document;
import com.collabeditor.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DocHistoryDAO {
    // SQL queries
    private static final String INSERT_HISTORY = "INSERT INTO doc_history (doc_id, content, user_id, timestamp) VALUES (?, ?, ?, ?)";
    private static final String SELECT_HISTORY_BY_ID = "SELECT * FROM doc_history WHERE history_id = ?";
    private static final String SELECT_HISTORY_BY_DOC = "SELECT * FROM doc_history WHERE doc_id = ? ORDER BY timestamp DESC";
    private static final String DELETE_HISTORY = "DELETE FROM doc_history WHERE history_id = ?";
    
    // Create history entry
    public int createHistoryEntry(DocHistory history) {
        int historyId = -1;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(INSERT_HISTORY, Statement.RETURN_GENERATED_KEYS);
            
            pstmt.setInt(1, history.getDocId());
            pstmt.setString(2, history.getContent());
            pstmt.setInt(3, history.getUserId());
            pstmt.setTimestamp(4, new Timestamp(history.getTimestamp().getTime()));
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    historyId = rs.getInt(1);
                    history.setHistoryId(historyId);
                    System.out.println("Historique créé avec ID: " + historyId);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur SQL lors de la création de l'historique: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Fermer les ressources (pas la connexion, elle est gérée par DBConnection)
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return historyId;
    }
    
    // Get history entry by ID
    public DocHistory getHistoryById(int historyId) {
        DocHistory history = null;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_HISTORY_BY_ID)) {
            
            pstmt.setInt(1, historyId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                history = new DocHistory();
                history.setHistoryId(rs.getInt("history_id"));
                history.setDocId(rs.getInt("doc_id"));
                history.setContent(rs.getString("content"));
                history.setUserId(rs.getInt("user_id"));
                history.setTimestamp(rs.getTimestamp("timestamp"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }
    
    // Get history entries by document
    public List<DocHistory> getHistoryByDoc(int docId) {
        List<DocHistory> historyList = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_HISTORY_BY_DOC)) {
            
            pstmt.setInt(1, docId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                DocHistory history = new DocHistory();
                history.setHistoryId(rs.getInt("history_id"));
                history.setDocId(rs.getInt("doc_id"));
                history.setContent(rs.getString("content"));
                history.setUserId(rs.getInt("user_id"));
                history.setTimestamp(rs.getTimestamp("timestamp"));
                historyList.add(history);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return historyList;
    }
    
    // Delete history entry
    public boolean deleteHistory(int historyId) {
        boolean deleted = false;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(DELETE_HISTORY)) {
            
            pstmt.setInt(1, historyId);
            
            int affectedRows = pstmt.executeUpdate();
            deleted = affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return deleted;
    }
    
    // Revert document to a specific history version
    public boolean revertToVersion(int historyId, DocumentDAO documentDAO) {
        DocHistory history = getHistoryById(historyId);
        if (history == null) {
            return false;
        }
        
        Document document = documentDAO.getDocumentById(history.getDocId());
        if (document == null) {
            return false;
        }
        
        document.setContent(history.getContent());
        document.setLastModified(new java.util.Date());
        
        return documentDAO.updateDocument(document);
    }
}