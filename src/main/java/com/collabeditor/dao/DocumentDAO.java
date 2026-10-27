package com.collabeditor.dao;

import com.collabeditor.model.Document;
import com.collabeditor.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DocumentDAO {
    // SQL queries
    private static final String INSERT_DOCUMENT = "INSERT INTO documents (title, content, creation_date, last_modified, owner_id) VALUES (?, ?, ?, ?, ?)";
    private static final String SELECT_DOCUMENT_BY_ID = "SELECT * FROM documents WHERE doc_id = ?";
    private static final String SELECT_DOCUMENTS_BY_OWNER = "SELECT * FROM documents WHERE owner_id = ?";
    private static final String SELECT_ALL_DOCUMENTS = "SELECT * FROM documents";
    private static final String UPDATE_DOCUMENT = "UPDATE documents SET title = ?, content = ?, last_modified = ? WHERE doc_id = ?";
    private static final String DELETE_DOCUMENT = "DELETE FROM documents WHERE doc_id = ?";
    
    // Create document
    public int createDocument(Document document) {
        int docId = -1;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(INSERT_DOCUMENT, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, document.getTitle());
            pstmt.setString(2, document.getContent());
            pstmt.setTimestamp(3, new Timestamp(document.getCreationDate().getTime()));
            pstmt.setTimestamp(4, new Timestamp(document.getLastModified().getTime()));
            pstmt.setInt(5, document.getOwnerId());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        docId = rs.getInt(1);
                        document.setDocId(docId);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return docId;
    }
    
    // Get document by ID
    public Document getDocumentById(int docId) {
        Document document = null;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_DOCUMENT_BY_ID)) {
            
            pstmt.setInt(1, docId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                document = new Document();
                document.setDocId(rs.getInt("doc_id"));
                document.setTitle(rs.getString("title"));
                document.setContent(rs.getString("content"));
                document.setCreationDate(rs.getTimestamp("creation_date"));
                document.setLastModified(rs.getTimestamp("last_modified"));
                document.setOwnerId(rs.getInt("owner_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return document;
    }
    
    // Get documents by owner
    public List<Document> getDocumentsByOwner(int ownerId) {
        List<Document> documents = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_DOCUMENTS_BY_OWNER)) {
            
            pstmt.setInt(1, ownerId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Document document = new Document();
                document.setDocId(rs.getInt("doc_id"));
                document.setTitle(rs.getString("title"));
                document.setContent(rs.getString("content"));
                document.setCreationDate(rs.getTimestamp("creation_date"));
                document.setLastModified(rs.getTimestamp("last_modified"));
                document.setOwnerId(rs.getInt("owner_id"));
                documents.add(document);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return documents;
    }
    
    // Get all documents
    public List<Document> getAllDocuments() {
        List<Document> documents = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL_DOCUMENTS)) {
            
            while (rs.next()) {
                Document document = new Document();
                document.setDocId(rs.getInt("doc_id"));
                document.setTitle(rs.getString("title"));
                document.setContent(rs.getString("content"));
                document.setCreationDate(rs.getTimestamp("creation_date"));
                document.setLastModified(rs.getTimestamp("last_modified"));
                document.setOwnerId(rs.getInt("owner_id"));
                documents.add(document);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return documents;
    }
    
    // Update document
    public boolean updateDocument(Document document) {
        boolean updated = false;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(UPDATE_DOCUMENT)) {
            
            pstmt.setString(1, document.getTitle());
            pstmt.setString(2, document.getContent());
            pstmt.setTimestamp(3, new Timestamp(new java.util.Date().getTime()));
            pstmt.setInt(4, document.getDocId());
            
            int affectedRows = pstmt.executeUpdate();
            updated = affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return updated;
    }
    
    // Delete document
    public boolean deleteDocument(int docId) {
        boolean deleted = false;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(DELETE_DOCUMENT)) {
            
            pstmt.setInt(1, docId);
            
            int affectedRows = pstmt.executeUpdate();
            deleted = affectedRows > 0;
            System.out.println("Document " + docId + " deletion affected " + affectedRows + " rows");
        } catch (SQLException e) {
            System.err.println("Error deleting document: " + e.getMessage());
            e.printStackTrace();
        }
        return deleted;
    }
}