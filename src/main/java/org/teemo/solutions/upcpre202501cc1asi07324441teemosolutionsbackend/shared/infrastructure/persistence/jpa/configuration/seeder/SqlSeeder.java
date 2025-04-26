package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.shared.infrastructure.persistence.jpa.configuration.seeder;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

@Component
public class SqlSeeder implements CommandLineRunner {

    private final DataSource dataSource;

    public SqlSeeder(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("data.sql"));
            System.out.println("✅ Se ejecutó data.sql manualmente 🎯");
        } catch (Exception e) {
            System.err.println("❌ Error ejecutando data.sql: " + e.getMessage());
        }
    }
}
