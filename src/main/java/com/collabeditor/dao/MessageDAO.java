package com.collabeditor.dao;

import com.collabeditor.model.Message;
import com.collabeditor.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageDAO {
    // SQL queries
    private static final String INSERT_MESSAGE = "INSERT INTO messages (doc_id, user_id, content, timestamp) VALUES (?, ?, ?, ?)";
    private static final String SELECT_MESSAGE_BY_ID = "SELECT * FROM messages WHERE message_id = ?";
    private static final String SELECT_MESSAGES_BY_DOC = "SELECT * FROM messages WHERE doc_id = ? ORDER BY timestamp DESC";
    private static final String DELETE_MESSAGE = "DELETE FROM messages WHERE message_id = ?";
    
    // Create message
    public int createMessage(Message message) {
        int messageId = -1;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(INSERT_MESSAGE, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, message.getDocId());
            pstmt.setInt(2, message.getUserId());
            pstmt.setString(3, message.getContent());
            pstmt.setTimestamp(4, new Timestamp(message.getTimestamp().getTime()));
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        messageId = rs.getInt(1);
                        message.setMessageId(messageId);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messageId;
    }
    
    // Get message by ID
    public Message getMessageById(int messageId) {
        Message message = null;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_MESSAGE_BY_ID)) {
            
            pstmt.setInt(1, messageId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                message = new Message();
                message.setMessageId(rs.getInt("message_id"));
                message.setDocId(rs.getInt("doc_id"));
                message.setUserId(rs.getInt("user_id"));
                message.setContent(rs.getString("content"));
                message.setTimestamp(rs.getTimestamp("timestamp"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return message;
    }
    
    // Get messages by document
    public List<Message> getMessagesByDoc(int docId) {
        List<Message> messages = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_MESSAGES_BY_DOC)) {
            
            pstmt.setInt(1, docId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Message message = new Message();
                message.setMessageId(rs.getInt("message_id"));
                message.setDocId(rs.getInt("doc_id"));
                message.setUserId(rs.getInt("user_id"));
                message.setContent(rs.getString("content"));
                message.setTimestamp(rs.getTimestamp("timestamp"));
                messages.add(message);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }
    
    // Delete message
    public boolean deleteMessage(int messageId) {
        boolean deleted = false;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(DELETE_MESSAGE)) {
            
            pstmt.setInt(1, messageId);
            
            int affectedRows = pstmt.executeUpdate();
            deleted = affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return deleted;
    }
}