module org.example.commande {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;
    requires java.sql;
    requires mysql.connector.java;


    opens org.example.commande to com.google.gson, javafx.fxml;
    exports org.example.commande;
}