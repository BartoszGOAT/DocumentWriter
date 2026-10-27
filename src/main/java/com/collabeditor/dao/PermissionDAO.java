package com.collabeditor.dao;

import com.collabeditor.model.Permission;
import com.collabeditor.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PermissionDAO {
    // SQL queries
    private static final String INSERT_PERMISSION = "INSERT INTO permissions (user_id, doc_id, read_access, write_access, admin_access) VALUES (?, ?, ?, ?, ?)";
    private static final String SELECT_PERMISSION_BY_ID = "SELECT * FROM permissions WHERE perm_id = ?";
    private static final String SELECT_PERMISSIONS_BY_USER = "SELECT * FROM permissions WHERE user_id = ?";
    private static final String SELECT_PERMISSIONS_BY_DOC = "SELECT * FROM permissions WHERE doc_id = ?";
    private static final String SELECT_PERMISSION_BY_USER_DOC = "SELECT * FROM permissions WHERE user_id = ? AND doc_id = ?";
    private static final String UPDATE_PERMISSION = "UPDATE permissions SET read_access = ?, write_access = ?, admin_access = ? WHERE perm_id = ?";
    private static final String DELETE_PERMISSION = "DELETE FROM permissions WHERE perm_id = ?";
    
    // Create permission
    public int createPermission(Permission permission) {
        int permId = -1;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(INSERT_PERMISSION, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, permission.getUserId());
            pstmt.setInt(2, permission.getDocId());
            pstmt.setBoolean(3, permission.isReadAccess());
            pstmt.setBoolean(4, permission.isWriteAccess());
            pstmt.setBoolean(5, permission.isAdminAccess());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        permId = rs.getInt(1);
                        permission.setPermId(permId);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return permId;
    }
    
    // Get permission by ID
    public Permission getPermissionById(int permId) {
        Permission permission = null;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_PERMISSION_BY_ID)) {
            
            pstmt.setInt(1, permId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                permission = new Permission();
                permission.setPermId(rs.getInt("perm_id"));
                permission.setUserId(rs.getInt("user_id"));
                permission.setDocId(rs.getInt("doc_id"));
                permission.setReadAccess(rs.getBoolean("read_access"));
                permission.setWriteAccess(rs.getBoolean("write_access"));
                permission.setAdminAccess(rs.getBoolean("admin_access"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return permission;
    }
    
    // Get permissions by user
    public List<Permission> getPermissionsByUser(int userId) {
        List<Permission> permissions = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_PERMISSIONS_BY_USER)) {
            
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Permission permission = new Permission();
                permission.setPermId(rs.getInt("perm_id"));
                permission.setUserId(rs.getInt("user_id"));
                permission.setDocId(rs.getInt("doc_id"));
                permission.setReadAccess(rs.getBoolean("read_access"));
                permission.setWriteAccess(rs.getBoolean("write_access"));
                permission.setAdminAccess(rs.getBoolean("admin_access"));
                permissions.add(permission);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return permissions;
    }
    
    // Get permissions by document
    public List<Permission> getPermissionsByDoc(int docId) {
        List<Permission> permissions = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_PERMISSIONS_BY_DOC)) {
            
            pstmt.setInt(1, docId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Permission permission = new Permission();
                permission.setPermId(rs.getInt("perm_id"));
                permission.setUserId(rs.getInt("user_id"));
                permission.setDocId(rs.getInt("doc_id"));
                permission.setReadAccess(rs.getBoolean("read_access"));
                permission.setWriteAccess(rs.getBoolean("write_access"));
                permission.setAdminAccess(rs.getBoolean("admin_access"));
                permissions.add(permission);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return permissions;
    }
    
    // Get permission by user and document
    public Permission getPermissionByUserAndDoc(int userId, int docId) {
        Permission permission = null;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_PERMISSION_BY_USER_DOC)) {
            
            pstmt.setInt(1, userId);
            pstmt.setInt(2, docId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                permission = new Permission();
                permission.setPermId(rs.getInt("perm_id"));
                permission.setUserId(rs.getInt("user_id"));
                permission.setDocId(rs.getInt("doc_id"));
                permission.setReadAccess(rs.getBoolean("read_access"));
                permission.setWriteAccess(rs.getBoolean("write_access"));
                permission.setAdminAccess(rs.getBoolean("admin_access"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return permission;
    }
    
    // Update permission
    public boolean updatePermission(Permission permission) {
        boolean updated = false;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(UPDATE_PERMISSION)) {
            
            pstmt.setBoolean(1, permission.isReadAccess());
            pstmt.setBoolean(2, permission.isWriteAccess());
            pstmt.setBoolean(3, permission.isAdminAccess());
            pstmt.setInt(4, permission.getPermId());
            
            int affectedRows = pstmt.executeUpdate();
            updated = affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return updated;
    }
    
    // Delete permission
    public boolean deletePermission(int permId) {
        boolean deleted = false;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(DELETE_PERMISSION)) {
            
            pstmt.setInt(1, permId);
            
            int affectedRows = pstmt.executeUpdate();
            deleted = affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return deleted;
    }
    
    // Check if user has permission to read document
    public boolean canRead(int userId, int docId) {
        Permission permission = getPermissionByUserAndDoc(userId, docId);
        return permission != null && permission.isReadAccess();
    }
    
    // Check if user has permission to write document
    public boolean canWrite(int userId, int docId) {
        Permission permission = getPermissionByUserAndDoc(userId, docId);
        return permission != null && permission.isWriteAccess();
    }
    
    // Check if user has admin permission for document
    public boolean isAdmin(int userId, int docId) {
        Permission permission = getPermissionByUserAndDoc(userId, docId);
        return permission != null && permission.isAdminAccess();
    }
}