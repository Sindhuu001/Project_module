// package com.example.projectmanagement.audit;

// import com.example.audit.entity.AuditTrail;
// import com.example.audit.service.AuditTrailService;
// import lombok.RequiredArgsConstructor;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import java.util.List;

// @RestController
// @RequestMapping("/api/audit")
// @RequiredArgsConstructor
// public class AuditTrailController {
    
//     private final AuditTrailService auditTrailService;
    
//     /**
//      * Get audit history for a specific entity
//      * Example: GET /api/audit/tasks/abc-123
//      */
//     @GetMapping("/{entityName}/{entityId}")
//     public ResponseEntity<List<AuditTrail>> getEntityHistory(
//             @PathVariable String entityName,
//             @PathVariable String entityId) {
//         List<AuditTrail> history = auditTrailService.getEntityHistory(entityName, entityId);
//         return ResponseEntity.ok(history);
//     }
    
//     /**
//      * Get all audit logs for a specific user
//      * Example: GET /api/audit/user/user-123
//      */
//     @GetMapping("/user/{userId}")
//     public ResponseEntity<List<AuditTrail>> getUserAuditLogs(@PathVariable String userId) {
//         List<AuditTrail> logs = auditTrailService.getUserAuditLogs(userId);
//         return ResponseEntity.ok(logs);
//     }
// }
