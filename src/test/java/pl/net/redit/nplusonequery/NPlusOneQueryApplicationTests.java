package pl.net.redit.nplusonequery;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.net.redit.nplusonequery.entities.CategoryEntity;
import pl.net.redit.nplusonequery.entities.ProductEntity;

import java.util.List;

@SpringBootTest
@ContextConfiguration(initializers = {NPlusOneQueryApplicationTests.Initializer.class})
@Testcontainers
class NPlusOneQueryApplicationTests {

    static final int NUMBER_OF_CATEGORIES = 4;
    static final int NUMBER_OF_PRODUCTS_IN_CATEGORY = 100;

    @Container
    static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:11.1");

    @PersistenceContext
    EntityManager entityManager;

    @Test
    @Transactional
    void nPlusOne() {
        prepareData();
        showProblem();
    }

    private void showProblem() {
        System.out.println("--- start selects ---");

        System.out.println("--- category select query ---");
        final List<CategoryEntity> categories = entityManager.createQuery("from CategoryEntity", CategoryEntity.class).getResultList();
        System.out.printf("--- number of categories %s ---%n", categories.size());

        for (CategoryEntity category : categories) {
            System.out.println();
            System.out.printf("--- product select query [category id: %d] ---%n", category.getId());
            final int numberOfProducts = category.getProducts().size();
            System.out.printf("--- number of products: %d; [category id: %d] ---%n", numberOfProducts, category.getId());
        }
    }

    private void prepareData() {
        long id = 1;
        for (int i = 0; i < NUMBER_OF_CATEGORIES; i++) {
            final CategoryEntity category = new CategoryEntity();
            category.setId(id++);
            category.setName("Category " + category.getId());

            for (int j = 0; j < NUMBER_OF_PRODUCTS_IN_CATEGORY; j++) {
                final ProductEntity product = new ProductEntity();
                product.setId(id++);
                product.setName("Product " + product.getId() + " of Category: " + category.getName());
                product.setCategory(category);
                category.getProducts().add(product);
            }
            entityManager.persist(category);
        }

        entityManager.flush();
        entityManager.clear();
    }

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                    "spring.jpa.show-sql=true",
                    "spring.jpa.properties.hibernate.format_sql=true",
                    "spring.jpa.hibernate.ddl-auto=create-drop",
                    "spring.datasource.url=" + postgreSQLContainer.getJdbcUrl(),
                    "spring.datasource.username=" + postgreSQLContainer.getUsername(),
                    "spring.datasource.password=" + postgreSQLContainer.getPassword()
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }

}
