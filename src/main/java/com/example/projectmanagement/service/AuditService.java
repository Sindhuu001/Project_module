// package com.example.projectmanagement.service;
// import java.util.List;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;
// import com.example.projectmanagement.dto.AuditDto;
// import com.example.projectmanagement.entity.AuditTrail;
// import com.example.projectmanagement.repository.AuditRepository;

// import lombok.*;
// @Builder
// @Service
// public class AuditService {
//     @Autowired
//     private AuditRepository auditRepository;
//     @Autowired
//     private AuditTrail AuditTrail;
//     @Autowired
//     private AuditDto dto;

//     public List<AuditTrail> getAllAudits() {
//         return auditRepository.allAudits();
//     }

//     public AuditTrail createAudit(AuditDto auditDto){
//         AuditTrail audit= AuditTrail.builder()
//             .entityName(dto.getEntityName())
//             .entityId(dto.getEntityId())
//             .operation(dto.getOperation())
//             .userId(dto.getUserId())
//             .build();
//         return auditRepository.save(audit);


//     }


// }
