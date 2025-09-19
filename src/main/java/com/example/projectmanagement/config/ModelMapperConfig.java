package com.example.projectmanagement.config;

import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();

        mapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE);

        // ✅ Converter: Hibernate PersistentBag (or any Collection) → List
        Converter<Collection<?>, List<?>> toList =
                ctx -> ctx.getSource() == null ? null : new ArrayList<>(ctx.getSource());

        // ✅ Converter: Hibernate PersistentSet (or any Collection) → Set
        Converter<Collection<?>, Set<?>> toSet =
                ctx -> ctx.getSource() == null ? null : new HashSet<>(ctx.getSource());

        // Register both
        mapper.addConverter(toList);
        mapper.addConverter(toSet);

        return mapper;
    }
}