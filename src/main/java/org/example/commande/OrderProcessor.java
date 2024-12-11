package org.example.commande;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class OrderProcessor {

    private static final String INPUT_FILE = "data/input.json";
    private static final String OUTPUT_FILE = "data/output.json";
    private static final String ERROR_FILE = "data/error.json";

    public static void main(String[] args) {
        // Lancer le traitement programmé des commandes toutes les heures
        startScheduledOrderProcessing();

        // Traitement initial
        processOrders();
    }

    private static void startScheduledOrderProcessing() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        Runnable task = OrderProcessor::processOrders;

        // Planifier la tâche toutes les heures
        scheduler.scheduleAtFixedRate(task, 0, 1, TimeUnit.HOURS);

        // Ajouter un hook pour arrêter le scheduler proprement à la fin du programme
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Arrêt du programme. Fermeture du scheduler...");
            scheduler.shutdown();
        }));
    }

    private static void processOrders() {
        List<Order> orders = readOrdersFromFile(INPUT_FILE);
        if (orders == null) return;

        List<Order> validOrders = new ArrayList<>();
        List<Order> invalidOrders = new ArrayList<>();

        try (Connection connection = DatabaseConnection.connect()) {
            if (connection == null) {
                System.err.println("Impossible d'établir une connexion à la base de données.");
                return;
            }

            for (Order order : orders) {
                if (!isValidOrder(order)) {
                    invalidOrders.add(order);
                    continue;
                }
                try {
                    if (customerExists(connection, order.getCustomerId())) {
                        addOrderToDatabase(connection, order);
                        validOrders.add(order);
                    } else {
                        System.err.println("Client inexistant pour la commande ID : " + order.getId());
                        invalidOrders.add(order);
                    }
                } catch (SQLException e) {
                    System.err.println("Erreur SQL pour la commande ID : " + order.getId() + ". Message : " + e.getMessage());
                    invalidOrders.add(order);
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur inattendue : " + e.getMessage());
        }

        // Écrire les commandes valides et invalides dans leurs fichiers respectifs
        writeOrdersToFile(OUTPUT_FILE, validOrders);
        writeOrdersToFile(ERROR_FILE, invalidOrders);

        // Mettre à jour le fichier input.json avec les commandes restantes (non traitées)
        updateInputFile(INPUT_FILE, orders, validOrders, invalidOrders);
    }

    private static boolean isValidOrder(Order order) {
        if (order.getAmount() <= 0) {
            System.err.println("Montant invalide pour la commande ID : " + order.getId());
            return false;
        }
        if (order.getCustomerId() <= 0) {
            System.err.println("ID client invalide pour la commande ID : " + order.getId());
            return false;
        }
        if (order.getStatus() == null || order.getStatus().isEmpty()) {
            System.err.println("Statut manquant pour la commande ID : " + order.getId());
            return false;
        }
        return true;
    }

    private static boolean customerExists(Connection connection, int customerId) throws SQLException {
        String query = "SELECT id FROM customer WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, customerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                boolean exists = resultSet.next();
                if (!exists) {
                    System.err.println("Aucun client trouvé avec l'ID : " + customerId);
                }
                return exists;
            }
        }
    }

    private static void addOrderToDatabase(Connection connection, Order order) throws SQLException {
        String query = "INSERT INTO `order` (id, date, amount, customerId, status) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, order.getId());

            // Conversion du timestamp en format DATETIME
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDate = dateFormat.format(new Date());
            statement.setString(2, formattedDate);

            statement.setDouble(3, order.getAmount());
            statement.setInt(4, order.getCustomerId());
            statement.setString(5, order.getStatus());
            statement.executeUpdate();
            System.out.println("Commande insérée : " + order.getId());
        }
    }

    private static List<Order> readOrdersFromFile(String filePath) {
        try (FileReader reader = new FileReader(filePath)) {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Order>>() {}.getType();
            return gson.fromJson(reader, listType);
        } catch (IOException e) {
            System.err.println("Erreur lors de la lecture du fichier : " + e.getMessage());
            return null;
        }
    }

    private static void writeOrdersToFile(String filePath, List<Order> orders) {
        try (FileWriter writer = new FileWriter(filePath, true)) { // Ajout mode `true` pour append
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(orders, writer);
            System.out.println("Données écrites dans le fichier : " + filePath);
        } catch (IOException e) {
            System.err.println("Erreur lors de l'écriture du fichier : " + e.getMessage());
        }
    }

    private static void updateInputFile(String filePath, List<Order> allOrders, List<Order> validOrders, List<Order> invalidOrders) {
        // Filtrer les commandes restantes
        List<Order> remainingOrders = new ArrayList<>(allOrders);
        remainingOrders.removeAll(validOrders);
        remainingOrders.removeAll(invalidOrders);

        // Réécrire input.json avec les commandes restantes
        try (FileWriter writer = new FileWriter(filePath)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(remainingOrders, writer);
            System.out.println("Fichier input.json mis à jour avec les commandes restantes.");
        } catch (IOException e) {
            System.err.println("Erreur lors de la mise à jour de input.json : " + e.getMessage());
        }
    }
}
