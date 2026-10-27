package com.collabeditor.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static Connection connection = null;
    
    // Paramètres de connexion directs et cohérents avec web.xml
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String URL = "jdbc:mysql://localhost:3306/collabeditor?allowPublicKeyRetrieval=true&useSSL=false";
    private static final String USER = "root";
    private static final String PASSWORD = "1234";
    
    static {
        try {
            // Chargement du driver
            Class.forName(DRIVER);
            System.out.println("Driver " + DRIVER + " chargé avec succès!");
        } catch (ClassNotFoundException e) {
            System.err.println("Erreur chargement du driver: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Impossible de charger le driver de base de données", e);
        }
    }
    
    /**
     * Retourne une connexion à la base de données
     * @return Connection objet représentant la connexion à la BD
     */
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                System.out.println("Tentative de connexion à la base de données: " + URL);
                System.out.println("Avec l'utilisateur: " + USER);
                
                // Ajouter une logique de reconnexion avec 3 tentatives max
                int attempts = 0;
                SQLException lastException = null;
                
                while (attempts < 3) {
                    try {
                        connection = DriverManager.getConnection(URL, USER, PASSWORD);
                        System.out.println("Connexion à la base de données établie avec succès!");
                        return connection;
                    } catch (SQLException e) {
                        lastException = e;
                        attempts++;
                        System.out.println("Échec de connexion (tentative " + attempts + "/3): " + e.getMessage());
                        
                        if (attempts < 3) {
                            // Attendre avant de réessayer
                            try {
                                Thread.sleep(1000);  // Attendre 1 seconde
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                            }
                        }
                    }
                }
                
                // Si on arrive ici, c'est que toutes les tentatives ont échoué
                throw lastException;
            }
            return connection;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la connexion à la base de données: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Impossible de se connecter à la base de données", e);
        }
    }
    
    /**
     * Ferme la connexion à la base de données
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                System.out.println("Connexion à la base de données fermée");
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture de la connexion: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}