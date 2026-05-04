# Role-Based Authentication Implementation Guide

## Overview

This document describes the role-based authentication system implemented for the Project Management Application using Spring Security with JWT tokens.

## Roles Defined

The application supports two primary roles:

### 1. MANAGER

- Full access to all resources
- Can create, update, and delete projects
- Can manage project members
- Can create and modify tasks, epics, and stories
- Can approve and reject risk management actions

### 2. GENERAL

- Limited read and write access
- Can view and update assigned tasks
- Can view projects they are members of
- Can comment on tasks, stories, and epics
- Cannot create or delete projects

## Security Configuration

### JWT Token Structure

The JWT token should include a `roles` claim with an array of roles:

```json
{
  "sub": "user@example.com",
  "name": "John Doe",
  "roles": ["MANAGER"],
  "iat": 1234567890,
  "exp": 1234571490
}
```

### Spring Security Configuration

- **File**: `Securityconfig.java`
- **Enabled**: `@EnableMethodSecurity(prePostEnabled = true)`
- **JWT Converter**: Automatically extracts roles from JWT and converts them to Spring Security authorities with `ROLE_` prefix

Example:

- JWT role: `MANAGER` → Spring Authority: `ROLE_MANAGER`
- JWT role: `GENERAL` → Spring Authority: `ROLE_GENERAL`

## Endpoint Authorization

### Controller Annotations

All controller endpoints use `@PreAuthorize` annotations to enforce role-based access control:

#### Manager-Only Operations

```java
@PreAuthorize("hasRole('MANAGER')")
public ResponseEntity<ProjectDto> createProject(@RequestBody ProjectDto projectDto)
```

#### Both Manager and General Users

```java
@PreAuthorize("hasAnyRole('MANAGER','GENERAL')")
public ResponseEntity<List<ProjectDto>> getAllProjects()
```

## Updated Controllers

The following controllers have been updated with role-based authorization:

1. **UserController** - Get users and their tasks
2. **ProjectController** - Full project management
3. **TaskController** - Task CRUD operations
4. **EpicController** - Epic management
5. **CommentController** - Comment operations
6. **PerformanceController** - Performance metrics
7. **MyWorkController** - Personal work dashboard

## Database Schema (Recommended)

If using database-backed user roles:

```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE users ADD CHECK (role IN ('MANAGER', 'GENERAL'));
```

## Usage Examples

### JWT Token Claim Examples

#### Manager User

```json
{
  "roles": ["MANAGER"]
}
```

#### General User

```json
{
  "roles": ["GENERAL"]
}
```

### Testing with cURL

```bash
# Test as MANAGER
curl -H "Authorization: Bearer <MANAGER_JWT_TOKEN>" \
  http://localhost:8080/api/projects

# Test as GENERAL
curl -H "Authorization: Bearer <GENERAL_JWT_TOKEN>" \
  http://localhost:8080/api/projects
```

## Public Endpoints

The following endpoints do NOT require authentication:

- `/public/**`
- `/swagger-ui/**`
- `/v3/api-docs/**`
- `/actuator/**`

All other endpoints require:

1. Valid JWT token
2. Appropriate role for the operation

## Spring Security Exception Handling

When a user lacks the required role, the application returns:

- **HTTP 403 Forbidden**: User is authenticated but lacks required role
- **HTTP 401 Unauthorized**: Missing or invalid JWT token

## Implementation Details

### UserRole Enum

- Location: `com.example.projectmanagement.entity.UserRole`
- Values: `MANAGER`, `GENERAL`

### Security Configuration

- Location: `com.example.projectmanagement.security.Securityconfig`
- Features:
  - JWT authentication with OAuth2 Resource Server
  - Method-level security with `@EnableMethodSecurity`
  - CORS support
  - CSRF disabled (stateless API)

### JWT Authentication Converter

- Location: (Active in Securityconfig)
- Converts JWT roles claim to Spring Security authorities
- Automatically prefixes roles with `ROLE_`

## Future Enhancements

1. Add permission-based access control (granular permissions)
2. Implement project-level role assignments
3. Add audit logging for authorization events
4. Implement role-based API rate limiting
5. Add time-based role activation

## Troubleshooting

### Users getting 403 Forbidden

- Verify JWT token contains `roles` claim
- Check that role matches `MANAGER` or `GENERAL` exactly
- Verify Securityconfig is properly enabled with `@EnableMethodSecurity`

### Roles not being recognized

- Check JWT claim name is `roles` (lowercase)
- Verify token is being properly decoded
- Check logs for role extraction messages

### Missing @PreAuthorize annotations

- Some endpoints may not have authorization checks
- Add annotations based on business requirements
- Follow the pattern: `@PreAuthorize("hasRole('MANAGER')")` or `hasAnyRole('MANAGER','GENERAL')`
