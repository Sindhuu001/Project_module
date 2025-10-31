package com.example.projectmanagement.config;

import com.example.projectmanagement.entity.Bug;
import com.example.projectmanagement.dto.BugDto;
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

        // ✅ Convert collections safely
        Converter<Collection<?>, List<?>> toList =
                ctx -> ctx.getSource() == null ? null : new ArrayList<>(ctx.getSource());
        Converter<Collection<?>, Set<?>> toSet =
                ctx -> ctx.getSource() == null ? null : new HashSet<>(ctx.getSource());

        mapper.addConverter(toList);
        mapper.addConverter(toSet);

        // 🚫 Prevent overwriting IDs when mapping BugDto → Bug
        mapper.typeMap(BugDto.class, Bug.class)
                .addMappings(m -> m.skip(Bug::setId));

        return mapper;
    }
}
