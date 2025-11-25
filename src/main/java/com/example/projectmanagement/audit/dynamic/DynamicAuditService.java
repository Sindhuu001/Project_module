package com.example.projectmanagement.audit.dynamic;

import org.springframework.stereotype.Service;

@Service
public class DynamicAuditService {

    private final DynamicAuditRepository repository;

    public DynamicAuditService(DynamicAuditRepository repository) {
        this.repository = repository;
    }

    public void saveEntityAudit(String entity,
                                String entityId,
                                String oldData,
                                String newData,
                                String operation) {

        String tableName = "audit_" + entity.toLowerCase();
        repository.createTableIfNotExists(tableName);
        repository.insertAuditRow(tableName, entityId, oldData, newData, operation);
    }
}
