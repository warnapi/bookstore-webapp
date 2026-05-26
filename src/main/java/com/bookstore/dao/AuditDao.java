package com.bookstore.dao;

import com.bookstore.model.AuditLog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO для работы с логами аудита.
 */
public class AuditDao extends BaseDao {
    
    /**
     * Создаёт новую запись аудита.
     */
    public AuditLog create(AuditLog auditLog) throws SQLException {
        String sql = """
            INSERT INTO audit_logs (admin_user_id, admin_user_name, action, 
                                   entity_type, entity_id, details)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setLong(1, auditLog.adminUserId());
            stmt.setString(2, auditLog.adminUserName());
            stmt.setString(3, auditLog.action());
            stmt.setString(4, auditLog.entityType());
            stmt.setObject(5, auditLog.entityId());
            stmt.setString(6, auditLog.details());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Не удалось создать запись аудита");
            }
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    Long id = rs.getLong(1);
                    // timestamp генерируется базой данных
                    String selectSql = "SELECT timestamp FROM audit_logs WHERE id = ?";
                    try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                        selectStmt.setLong(1, id);
                        try (ResultSet selectRs = selectStmt.executeQuery()) {
                            if (selectRs.next()) {
                                return new AuditLog(
                                    id,
                                    auditLog.adminUserId(),
                                    auditLog.adminUserName(),
                                    auditLog.action(),
                                    auditLog.entityType(),
                                    auditLog.entityId(),
                                    auditLog.details(),
                                    selectRs.getTimestamp("timestamp").toLocalDateTime()
                                );
                            }
                        }
                    }
                }
            }
        }
        
        throw new SQLException("Не удалось создать запись аудита");
    }
    
    /**
     * Получает все записи аудита с пагинацией.
     */
    public List<AuditLog> findAll(int offset, int limit) throws SQLException {
        String sql = """
            SELECT id, admin_user_id, admin_user_name, action, 
                   entity_type, entity_id, details, timestamp
            FROM audit_logs 
            ORDER BY timestamp DESC 
            LIMIT ? OFFSET ?
            """;
        
        List<AuditLog> logs = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapRow(rs));
                }
            }
        }
        
        return logs;
    }
    
    /**
     * Получает записи аудита для сущности.
     */
    public List<AuditLog> findByEntityType(String entityType, Long entityId, int offset, int limit) throws SQLException {
        String sql = """
            SELECT id, admin_user_id, admin_user_name, action, 
                   entity_type, entity_id, details, timestamp
            FROM audit_logs 
            WHERE entity_type = ? AND entity_id = ?
            ORDER BY timestamp DESC 
            LIMIT ? OFFSET ?
            """;
        
        List<AuditLog> logs = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, entityType);
            stmt.setLong(2, entityId);
            stmt.setInt(3, limit);
            stmt.setInt(4, offset);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapRow(rs));
                }
            }
        }
        
        return logs;
    }
    
    /**
     * Получает записи аудита администратора.
     */
    public List<AuditLog> findByAdminUserId(Long adminUserId, int offset, int limit) throws SQLException {
        String sql = """
            SELECT id, admin_user_id, admin_user_name, action, 
                   entity_type, entity_id, details, timestamp
            FROM audit_logs 
            WHERE admin_user_id = ?
            ORDER BY timestamp DESC 
            LIMIT ? OFFSET ?
            """;
        
        List<AuditLog> logs = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, adminUserId);
            stmt.setInt(2, limit);
            stmt.setInt(3, offset);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapRow(rs));
                }
            }
        }
        
        return logs;
    }
    
    /**
     * Получает общее количество записей аудита.
     */
    public long count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM audit_logs";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getLong(1);
            }
        }
        
        return 0;
    }
    
    /**
     * Маппинг строки ResultSet в объект AuditLog.
     */
    private AuditLog mapRow(ResultSet rs) throws SQLException {
        Long entityId = rs.getObject("entity_id", Long.class);
        
        return new AuditLog(
            rs.getLong("id"),
            rs.getLong("admin_user_id"),
            rs.getString("admin_user_name"),
            rs.getString("action"),
            rs.getString("entity_type"),
            entityId,
            rs.getString("details"),
            rs.getTimestamp("timestamp").toLocalDateTime()
        );
    }
}
