package br.delivery.dao;

/**
 * Singleton responsável pela "conexão" com o banco de dados.
 *
 * MVP: usa listas em memória (banco fictício).
 * Para migrar para PostgreSQL:
 *   1. Adicione a dependência JDBC (postgresql driver) ao classpath.
 *   2. Substitua o bloco "// TODO JDBC" abaixo por:
 *        connection = DriverManager.getConnection(URL, USER, PASSWORD);
 *   3. Altere o método getConnection() para retornar java.sql.Connection.
 *   4. Atualize os DAOs para usar queries SQL no lugar das listas.
 *   -- As classes model e service NÃO precisam mudar. --
 */
public class DatabaseConnection {

    // -----------------------------------------------------------------------
    // Singleton
    // -----------------------------------------------------------------------
    private static DatabaseConnection instance;

    private DatabaseConnection() {
        // TODO JDBC: connection = DriverManager.getConnection(URL, USER, PASSWORD);
        System.out.println("[DB] Banco fictício (memória) inicializado.");
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    // -----------------------------------------------------------------------
    // Stub: retorna um token simbólico para o banco fictício.
    // Quando migrar para JDBC, troque o retorno por java.sql.Connection.
    // -----------------------------------------------------------------------
    public Object getConnection() {
        // TODO JDBC: return connection;
        return "IN_MEMORY_DB";
    }

    public boolean isConnected() {
        // TODO JDBC: return connection != null && !connection.isClosed();
        return true;
    }
}