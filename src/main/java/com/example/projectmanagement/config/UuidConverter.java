package com.example.projectmanagement.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.UUID;

@Converter(autoApply = false)
public class UuidConverter implements AttributeConverter<UUID, String> {



    @Override
    public String convertToDatabaseColumn(UUID attributee) {
        return attributee == null ? null : attributee.toString();
    }

    @Override
    public UUID convertToEntityAttribute(String dbData) {
        return dbData == null ? null : UUID.fromString(dbData);
    }
}
