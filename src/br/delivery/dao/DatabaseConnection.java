package br.delivery.dao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Singleton que gerencia o pool de conexões JDBC com o PostgreSQL.
 *
 * Dependências necessárias (Maven):
 *
 *   <dependency>
 *       <groupId>org.postgresql</groupId>
 *       <artifactId>postgresql</artifactId>
 *       <version>42.7.3</version>
 *   </dependency>
 *   <dependency>
 *       <groupId>com.zaxxer</groupId>
 *       <artifactId>HikariCP</artifactId>
 *       <version>5.1.0</version>
 *   </dependency>
 *
 * Variáveis de ambiente esperadas (ou altere as constantes abaixo):
 *   DB_URL      → jdbc:postgresql://localhost:5432/delivery
 *   DB_USER     → postgres
 *   DB_PASSWORD → sua_senha
 */
public class DatabaseConnection {

    // ---------------------------------------------------------------
    // Configuração — prefira variáveis de ambiente em produção
    // ---------------------------------------------------------------
    private static final String URL  = System.getenv().getOrDefault(
            "DB_URL", "jdbc:postgresql://localhost:5432/delivery_food");
    private static final String USER = System.getenv().getOrDefault(
            "DB_USER", "postgres");
    private static final String PASS = System.getenv().getOrDefault(
            "DB_PASSWORD", "admin");

    // ---------------------------------------------------------------
    // Singleton + Pool (HikariCP)
    // ---------------------------------------------------------------
    private static DatabaseConnection instance;
    private final HikariDataSource dataSource;

    private DatabaseConnection() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(URL);
        config.setUsername(USER);
        config.setPassword(PASS);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30_000);   // 30 s
        config.setIdleTimeout(600_000);        // 10 min
        config.setMaxLifetime(1_800_000);      // 30 min
        config.setConnectionTestQuery("SELECT 1");

        this.dataSource = new HikariDataSource(config);
        System.out.println("✔ Conectado ao PostgreSQL: " + URL);
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /** Retorna uma conexão do pool. Deve ser fechada após o uso (try-with-resources). */
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

public boolean testarConexao() {

    try (Connection conn = getConnection()) {

        if (conn != null && !conn.isClosed()) {
            return true;
        }

    } catch (SQLException e) {

        System.out.println("✘ Erro ao conectar ao PostgreSQL:");
        System.out.println("Mensagem: " + e.getMessage());

    }

    return false;
}

/** Fechar o pool ao encerrar a aplicação */
public void close() {
    if (dataSource != null && !dataSource.isClosed()) {
        dataSource.close();
    }
}
}