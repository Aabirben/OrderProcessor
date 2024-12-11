package org.example.commande;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/Commande";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection connect() {
        try {
            // Charger le pilote JDBC pour MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Créer la connexion
            Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connexion réussie à la base de données !");
            return connection;
        } catch (ClassNotFoundException e) {
            System.err.println("Erreur : le pilote JDBC est introuvable.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la connexion à la base de données : " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        // Test de la connexion
        Connection connection = connect();
        if (connection != null) {
            try {
                // Fermer la connexion lorsque ce n'est plus nécessaire
                connection.close();
                System.out.println("Connexion fermée.");
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture de la connexion : " + e.getMessage());
            }
        }
    }
}
