package com.example.projectmanagement.loader;

import com.example.projectmanagement.config.RiskCategoryProperties;
import com.example.projectmanagement.entity.RiskCategory;
import com.example.projectmanagement.repository.RiskCategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class RiskCategoryDataLoader implements CommandLineRunner {

    private final RiskCategoryRepository repository;
    private final RiskCategoryProperties properties;

    public RiskCategoryDataLoader(
            RiskCategoryRepository repository,
            RiskCategoryProperties properties
    ) {
        this.repository = repository;
        this.properties = properties;
    }

    @Override
    public void run(String... args) {

        if (properties.getCategories() == null) return;

        properties.getCategories().forEach(cat -> {
            repository.findByName(cat.getName())
                    .ifPresentOrElse(
                            existing -> {
                                System.out.println(
                                        "[SKIP] Risk category already exists: " + cat.getName()
                                );
                            },
                            () -> {
                                RiskCategory rc = new RiskCategory(
                                        cat.getName(),
                                        cat.getDescription()
                                );
                                repository.save(rc);
                                System.out.println("[ADDED] Added new risk category: " + cat.getName());
                            }
                    );
        });
    }
}
