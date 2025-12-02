package com.example.projectmanagement.audit.base;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.projectmanagement.audit.base.AuditTrail.AuditEntityType;
import com.example.projectmanagement.audit.dynamic.DynamicAuditService;
@RestController
@RequestMapping("api/history")
public class AuditController {
    @Autowired
    private DynamicAuditService dynamicAuditService;

    @GetMapping("{entityName}/{entityId}")
    public List<AuditHistoryDto> getHistoryById(@PathVariable AuditEntityType entityName,@PathVariable Long entityId){
        return dynamicAuditService.getHistoryById(entityName,entityId);
    }
    
}

