package com.collabeditor.controller;

import com.collabeditor.dao.DocumentDAO;
import com.collabeditor.dao.MessageDAO;
import com.collabeditor.dao.UserDAO;
import com.collabeditor.model.Document;
import com.collabeditor.model.Message;
import com.collabeditor.model.User;
import com.collabeditor.util.WebSocketMessage;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/editor/{docId}/{userId}")
public class WebSocketEndpoint {
    // Map to store active sessions: key is docId_userId, value is the session
    private static final Map<String, Session> sessions = new ConcurrentHashMap<>();
    
    // Map to group sessions by document: key is docId, value is map of userId to session
    private static final Map<Integer, Map<Integer, Session>> documentSessions = new ConcurrentHashMap<>();
    
    private static final DocumentDAO documentDAO = new DocumentDAO();
    private static final UserDAO userDAO = new UserDAO();
    private static final MessageDAO messageDAO = new MessageDAO();
    
    @OnOpen
    public void onOpen(Session session, @PathParam("docId") int docId, @PathParam("userId") int userId) {
        System.out.println("WebSocket connection opened: docId=" + docId + ", userId=" + userId);
        String sessionKey = docId + "_" + userId;
        sessions.put(sessionKey, session);
        
        // Add to document sessions map
        documentSessions.computeIfAbsent(docId, k -> new ConcurrentHashMap<>())
                       .put(userId, session);
        
        // Get user information
        User user = userDAO.getUserById(userId);
        if (user != null) {
            System.out.println("User joined: " + user.getUsername());
            
            // Broadcast that a new user has joined
            String message = createJsonMessage("USER_JOINED", userId, user.getUsername(), docId, null, 0, 0, 0);
            broadcastToDocument(docId, message, userId);
            
            // Send the current document content to the new user
            Document document = documentDAO.getDocumentById(docId);
            if (document != null) {
                String documentMessage = createJsonMessage("TEXT_CHANGE", -1, "System", docId, document.getContent(), 0, 0, 0);
                try {
                    session.getBasicRemote().sendText(documentMessage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    @OnMessage
    public void onMessage(String message, Session session, @PathParam("docId") int docId, @PathParam("userId") int userId) {
        System.out.println("Message received: " + message);
        
        // Parse the message manually
        String messageType = getJsonValue(message, "type");
        System.out.println("Message type: " + messageType);
        
        if (messageType == null) {
            System.err.println("Invalid message format: missing 'type' field");
            return;
        }
        
        // Process the message based on its type
        switch (messageType) {
            case "TEXT_CHANGE":
                String content = getJsonValue(message, "content");
                System.out.println("Text change received, updating document");
                
                // Update the document in the database
                Document document = documentDAO.getDocumentById(docId);
                if (document != null && content != null) {
                    document.setContent(content);
                    document.setLastModified(new Date());
                    boolean updated = documentDAO.updateDocument(document);
                    System.out.println("Document update success: " + updated);
                }
                
                // Broadcast the change to all users editing this document (except the sender)
                broadcastToDocument(docId, message, userId);
                break;
                
            case "CURSOR_POSITION":
                // Broadcast cursor position to others
                broadcastToDocument(docId, message, userId);
                break;
                
            case "CHAT_MESSAGE":
                String chatContent = getJsonValue(message, "content");
                System.out.println("Chat message received: " + chatContent);
                
                // Get username for the chat message
                User user = userDAO.getUserById(userId);
                String username = (user != null) ? user.getUsername() : "Unknown";
                
                // Create a properly formatted chat message
                String chatMessage = createJsonMessage("CHAT_MESSAGE", userId, username, docId, chatContent, 0, 0, 0);
                
                // Save the chat message to the database
                Message dbMessage = new Message();
                dbMessage.setDocId(docId);
                dbMessage.setUserId(userId);
                dbMessage.setContent(chatContent);
                dbMessage.setTimestamp(new Date());
                messageDAO.createMessage(dbMessage);
                
                // Broadcast the chat message to all users in the document
                broadcastToDocument(docId, chatMessage, -1); // -1 means send to everyone including sender
                break;
                
            default:
                System.out.println("Unknown message type: " + messageType);
                break;
        }
    }
    
    @OnClose
    public void onClose(Session session, @PathParam("docId") int docId, @PathParam("userId") int userId) {
        System.out.println("WebSocket connection closed: docId=" + docId + ", userId=" + userId);
        String sessionKey = docId + "_" + userId;
        sessions.remove(sessionKey);
        
        // Remove from document sessions map
        Map<Integer, Session> docSessions = documentSessions.get(docId);
        if (docSessions != null) {
            docSessions.remove(userId);
            if (docSessions.isEmpty()) {
                documentSessions.remove(docId);
            }
        }
        
        // Get user information
        User user = userDAO.getUserById(userId);
        if (user != null) {
            // Broadcast that the user has left
            String message = createJsonMessage("USER_LEFT", userId, user.getUsername(), docId, null, 0, 0, 0);
            broadcastToDocument(docId, message, userId);
        }
    }
    
    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("Error in WebSocket session: " + session.getId());
        throwable.printStackTrace();
    }
    
    // Broadcast message to all users in a document except the sender
    private void broadcastToDocument(int docId, String message, int excludeUserId) {
        Map<Integer, Session> docSessions = documentSessions.get(docId);
        if (docSessions != null) {
            System.out.println("Broadcasting to " + docSessions.size() + " sessions, excluding userId=" + excludeUserId);
            
            for (Map.Entry<Integer, Session> entry : docSessions.entrySet()) {
                int userId = entry.getKey();
                Session userSession = entry.getValue();
                
                if (excludeUserId > 0 && userId == excludeUserId) {
                    continue;
                }
                
                try {
                    if (userSession.isOpen()) {
                        userSession.getBasicRemote().sendText(message);
                        System.out.println("Message sent to userId=" + userId);
                    } else {
                        System.out.println("Session for userId=" + userId + " is closed, removing it");
                        docSessions.remove(userId);
                    }
                } catch (IOException e) {
                    System.err.println("Error sending message to userId=" + userId + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } else {
        	if (message.contains("TEXT_CHANGE") || message.contains("CHAT_MESSAGE")) {
                System.out.println("No active sessions for docId=" + docId + ", message not broadcast");
            }
        }
    }
    
    // Helper method to create a JSON message without using external libraries
    private String createJsonMessage(String type, int userId, String username, int docId, String content, int position, int selectionStart, int selectionEnd) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"type\":\"").append(type).append("\",");
        sb.append("\"userId\":").append(userId).append(",");
        sb.append("\"username\":\"").append(escapeJson(username)).append("\",");
        sb.append("\"docId\":").append(docId);
        
        if (content != null) {
            sb.append(",\"content\":\"").append(escapeJson(content)).append("\"");
        }
        
        if (position > 0 || selectionStart > 0 || selectionEnd > 0) {
            sb.append(",\"position\":").append(position);
            sb.append(",\"selectionStart\":").append(selectionStart);
            sb.append(",\"selectionEnd\":").append(selectionEnd);
        }
        
        sb.append("}");
        return sb.toString();
    }
    
    // Helper method to escape special characters in JSON strings
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
    
    // Simple method to extract a value from a JSON string
    private String getJsonValue(String json, String key) {
        try {
            String keyPattern = "\"" + key + "\":";
            int keyIndex = json.indexOf(keyPattern);
            if (keyIndex == -1) {
                return null;
            }
            
            int valueStartIndex = keyIndex + keyPattern.length();
            
            // Skip whitespace
            while (valueStartIndex < json.length() && Character.isWhitespace(json.charAt(valueStartIndex))) {
                valueStartIndex++;
            }
            
            // Check if the value is a string
            if (json.charAt(valueStartIndex) == '\"') {
                valueStartIndex++; // Skip the opening quote
                int valueEndIndex = json.indexOf("\"", valueStartIndex);
                while (valueEndIndex > 0 && json.charAt(valueEndIndex - 1) == '\\') {
                    // This is an escaped quote, find the next one
                    valueEndIndex = json.indexOf("\"", valueEndIndex + 1);
                }
                
                if (valueEndIndex == -1) {
                    System.err.println("Malformed JSON: unterminated string");
                    return null;
                }
                
                String encodedValue = json.substring(valueStartIndex, valueEndIndex);
                return decodeJsonString(encodedValue);
            } 
            // Check if the value is a number
            else if (Character.isDigit(json.charAt(valueStartIndex)) || json.charAt(valueStartIndex) == '-') {
                int valueEndIndex = valueStartIndex;
                while (valueEndIndex < json.length() && 
                      (Character.isDigit(json.charAt(valueEndIndex)) || 
                       json.charAt(valueEndIndex) == '.' || 
                       json.charAt(valueEndIndex) == '-' || 
                       json.charAt(valueEndIndex) == 'e' || 
                       json.charAt(valueEndIndex) == 'E' || 
                       json.charAt(valueEndIndex) == '+')) {
                    valueEndIndex++;
                }
                return json.substring(valueStartIndex, valueEndIndex);
            }
            // Check if the value is a boolean
            else if (json.charAt(valueStartIndex) == 't' || json.charAt(valueStartIndex) == 'f') {
                if (json.substring(valueStartIndex).startsWith("true")) {
                    return "true";
                } else if (json.substring(valueStartIndex).startsWith("false")) {
                    return "false";
                }
            }
            // Check if the value is null
            else if (json.charAt(valueStartIndex) == 'n') {
                if (json.substring(valueStartIndex).startsWith("null")) {
                    return null;
                }
            }
            
            System.err.println("Unrecognized JSON value type for key: " + key);
            return null;
        } catch (Exception e) {
            System.err.println("Error parsing JSON value for key " + key + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    // Decode JSON string (handle escaped characters)
    private String decodeJsonString(String encoded) {
        if (encoded == null) {
            return null;
        }
        
        return encoded.replace("\\\"", "\"")
                     .replace("\\\\", "\\")
                     .replace("\\n", "\n")
                     .replace("\\r", "\r")
                     .replace("\\t", "\t")
                     .replace("\\/", "/");
    }
}