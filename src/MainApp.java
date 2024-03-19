import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.Vector;

public class MainApp {

    // Définition des composants de l'interface
    private JFrame frame;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;

    private JButton addButton;
    private JButton deleteButton;
    private JButton updateButton;
    private JButton listButton;
    private JButton createAccountButton;

    // Informations de connexion à la base de données
    String BDD = "CISI4";
    String DB_URL = "jdbc:mysql://localhost:3306/" + BDD;
    String DB_USER = "root";
    String DB_PASSWORD = "";

    // Constructeur
    public MainApp() {

        initialize();
    }

    // Méthode pour initialiser l'interface
    private void initialize() {
        frame = new JFrame("Authentification");
        frame.setBounds(100, 100, 400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);

        usernameField = new JTextField();
        usernameField.setBounds(150, 50, 150, 20);
        frame.getContentPane().add(usernameField);

        passwordField = new JPasswordField();
        passwordField.setBounds(150, 80, 150, 20);
        frame.getContentPane().add(passwordField);

        loginButton = new JButton("Login");
        loginButton.setBounds(180, 120, 80, 25);
        frame.getContentPane().add(loginButton);

        createAccountButton = new JButton("Créer un compte");
        createAccountButton.setBounds(180, 160, 150, 25);
        frame.getContentPane().add(createAccountButton);

        // Gestionnaire d'événements pour le bouton de création de compte
        createAccountButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showCreateAccountDialog();
            }
        });

        //  gestionnaire d'événements pour le bouton de connexion
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Vérifier l'authentification
                if (authenticateUser(usernameField.getText(), new String(passwordField.getPassword()))) {
                    // Si l'authentification réussit, ouvrir la page principale
                    openMainPage();
                } else {
                    JOptionPane.showMessageDialog(frame, "Authentification échouée. Veuillez réessayer.");
                }
            }
        });

        frame.setVisible(true);
    }

    // Méthode pour vérifier l'authentification dans la base de données
    private boolean authenticateUser(String username, String password) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "SELECT * FROM compte WHERE username=? AND password=?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, password);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    return resultSet.next();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Méthode pour ouvrir la page principale après une authentification réussie
    private void openMainPage() {
        frame.getContentPane().removeAll(); // Effacer les composants de l'interface de connexion

        addButton = new JButton("Ajouter ");
        addButton.setBounds(50, 50, 100, 25);
        frame.getContentPane().add(addButton);

        deleteButton = new JButton("Supprimer");
        deleteButton.setBounds(50, 90, 100, 25);
        frame.getContentPane().add(deleteButton);

        updateButton = new JButton("Modifier");
        updateButton.setBounds(50, 130, 100, 25);
        frame.getContentPane().add(updateButton);

        listButton = new JButton("Lister");
        listButton.setBounds(50, 170, 100, 25);
        frame.getContentPane().add(listButton);

        // gestionnaires d'événements pour les boutons de la page principale

        // Action au clic du bouton d'ajout
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Logique pour ajouter une personne
                JPanel panel = new JPanel();
                panel.setLayout(new GridLayout(3, 2));

                JTextField nomField = new JTextField();
                JTextField prenomField = new JTextField();
                JTextField emailField = new JTextField();

                panel.add(new JLabel("Nom:"));
                panel.add(nomField);
                panel.add(new JLabel("Prénom:"));
                panel.add(prenomField);
                panel.add(new JLabel("Email:"));
                panel.add(emailField);

                int result = JOptionPane.showConfirmDialog(frame, panel, "Ajouter une personne",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

                if (result == JOptionPane.OK_OPTION) {
                    String nom = nomField.getText();
                    String prenom = prenomField.getText();
                    String email = emailField.getText();

                    if (!nom.isEmpty() && !prenom.isEmpty() && !email.isEmpty()) {
                        addPerson(nom, prenom, email);
                        JOptionPane.showMessageDialog(frame, "Personne ajoutée avec succès.");
                    } else {
                        JOptionPane.showMessageDialog(frame, "Veuillez remplir tous les champs.");
                    }
                }
            }
        });



// ...

        // Action du bouton de suppression d'une personne
        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Logique pour supprimer une personne par ID
                String idToDelete = JOptionPane.showInputDialog(frame, "ID de la personne à supprimer:");
                if (idToDelete != null && !idToDelete.isEmpty()) {
                    int id;
                    try {
                        id = Integer.parseInt(idToDelete);
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(frame, "Veuillez entrer un ID valide (nombre entier).");
                        return;
                    }

                    Person personToDelete = getPersonById(id);

                    if (personToDelete != null && confirmDeletion(personToDelete)) {
                        deletePerson(personToDelete);
                        JOptionPane.showMessageDialog(frame, "Personne supprimée avec succès.");
                    } else {
                        JOptionPane.showMessageDialog(frame, "Aucune personne trouvée avec l'ID : " + id);
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "Veuillez saisir un ID.");
                }
            }
        });

        // Action au clic du bouton de mise à jour
        updateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Logique pour mettre à jour les informations d'une personne
                String idToUpdate = JOptionPane.showInputDialog(frame, "ID de la personne à mettre à jour:");
                if (idToUpdate != null && !idToUpdate.isEmpty()) {
                    int id;
                    try {
                        id = Integer.parseInt(idToUpdate);
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(frame, "Veuillez saisir un ID valide (nombre entier).");
                        return;
                    }

                    Person personToUpdate = getPersonById(id);

                    if (personToUpdate != null) {
                        if (showUpdateDialog(personToUpdate)) {
                            updatePersonInDatabase(personToUpdate);
                            JOptionPane.showMessageDialog(frame, "Informations de la personne mises à jour avec succès.");
                        } else {
                            JOptionPane.showMessageDialog(frame, "Mise à jour annulée.");
                        }
                    } else {
                        JOptionPane.showMessageDialog(frame, "Aucune personne trouvée avec l'ID : " + id);
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "Veuillez saisir un ID.");
                }
            }
        });

// gestionnaire d'evenement au clic du bouton pour lister
        listButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                listPersons();
            }
        });

        frame.repaint(); // Redessiner l'interface
    }

    // Point d'entrée de l'application
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new MainApp();
            }
        });
    }

    // Méthode pour enregistrer une personne dans la base de données
    private void addPerson(String nom, String prenom, String email) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "INSERT INTO utilisateur (Nom, Prenom, Email) VALUES (?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setString(1, nom);
                preparedStatement.setString(2, prenom);
                preparedStatement.setString(3, email);

                int affectedRows = preparedStatement.executeUpdate();

                if (affectedRows > 0) {
                    ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        int id = generatedKeys.getInt(1);
                        System.out.println("Personne ajoutée avec l'ID : " + id);
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    //  méthode pour afficher la boîte de dialogue de création de compte
    private void showCreateAccountDialog() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 2));

        JTextField newUsernameField = new JTextField();
        JPasswordField newPasswordField = new JPasswordField();

        panel.add(new JLabel(" Nom d'utilisateur:"));
        panel.add(newUsernameField);
        panel.add(new JLabel(" Mot de passe:"));
        panel.add(newPasswordField);

        int result = JOptionPane.showConfirmDialog(frame, panel, "Créer un compte",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String newUsername = newUsernameField.getText();
            String newPassword = new String(newPasswordField.getPassword());

            if (!newUsername.isEmpty() && !newPassword.isEmpty()) {
                // Appeler la méthode pour créer le compte dans la base de données
                createAccount(newUsername, newPassword);
                JOptionPane.showMessageDialog(frame, "Compte créé avec succès.");
            } else {
                JOptionPane.showMessageDialog(frame, "Veuillez remplir tous les champs.");
            }
        }
    }

    //  méthode pour créer le compte en insérant les données saisies dans la base de données
    private void createAccount(String username, String password) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "INSERT INTO compte (username, password) VALUES (?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, password);

                int affectedRows = preparedStatement.executeUpdate();

                if (affectedRows > 0) {
                    System.out.println("Compte créé avec succès pour l'utilisateur : " + username);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }


    // Méthode pour récupérer la liste de toutes les personnes existantes dans la base de données
    private void listPersons() {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "SELECT ID, Nom, Prenom, Email FROM utilisateur";
            try (Statement statement = connection.createStatement()) {
                try (ResultSet resultSet = statement.executeQuery(query)) {
                    // Construire un modèle de tableau par défaut à partir du résultat de la requête
                    DefaultTableModel tableModel = buildTableModel(resultSet);

                    // Créer une table avec le modèle de tableau par défaut
                    JTable table = new JTable(tableModel);

                    // Afficher la table dans une boîte de dialogue
                    JOptionPane.showMessageDialog(frame, new JScrollPane(table));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static DefaultTableModel buildTableModel(ResultSet resultSet) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();

        // Noms des colonnes sont équivalents aux entetes dans la base de données
        Vector<String> columnNames = new Vector<>();
        int columnCount = metaData.getColumnCount();
        for (int column = 1; column <= columnCount; column++) {
            columnNames.add(metaData.getColumnName(column));
        }

        // Données du tableau
        Vector<Vector<Object>> data = new Vector<>();
        while (resultSet.next()) {
            Vector<Object> row = new Vector<>();
            for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                row.add(resultSet.getObject(columnIndex));
            }
            data.add(row);
        }

        return new DefaultTableModel(data, columnNames);
    }

// Méthode pour récupérer une personne à travers son identifiant
    private Person getPersonById(int id) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "SELECT Nom, Prenom, Email FROM utilisateur WHERE ID=?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, id);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        String nom = resultSet.getString("Nom");
                        String prenom = resultSet.getString("Prenom");
                        String email = resultSet.getString("Email");
                        return new Person(id, nom, prenom,email);
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

// Méthode de suppression d'une personne de la base de données
    private void deletePerson(Person person) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "DELETE FROM utilisateur WHERE ID=?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, person.getId());

                int affectedRows = preparedStatement.executeUpdate();

                if (affectedRows > 0) {
                    System.out.println("Personne supprimée pour l'ID : " + person.getId());
                } else {
                    System.out.println("Aucune personne trouvée avec l'ID : " + person.getId());
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // Méthode pour confirmer la suppresion de la personne
    private boolean confirmDeletion(Person person) {
        int result = JOptionPane.showConfirmDialog(frame,
                "Êtes-vous sûr de vouloir supprimer la personne :\n" +
                        "Nom : " + person.getNom() + "\n" +
                        "Prénom : " + person.getPrenom() + "\n",
                "Confirmation de suppression", JOptionPane.YES_NO_OPTION);
        return result == JOptionPane.YES_OPTION;
    }


    // Récupération et affichage des informations à modifier
    private boolean showUpdateDialog(Person person) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 2));

        JTextField nomField = new JTextField(person.getNom());
        JTextField prenomField = new JTextField(person.getPrenom());
        JTextField emailField = new JTextField(person.getEmail());

        panel.add(new JLabel("Nom:"));
        panel.add(nomField);
        panel.add(new JLabel("Prénom:"));
        panel.add(prenomField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);

        int result = JOptionPane.showConfirmDialog(frame, panel, "Mettre à jour une personne",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            person.setNom(nomField.getText());
            person.setPrenom(prenomField.getText());
            person.setEmail(emailField.getText());
            return true;
        }
        return false;
    }
    // Mise à jour des informations modifiées dans la base de données
    private void updatePersonInDatabase(Person person) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "UPDATE utilisateur SET Nom=?, Prenom=?, Email=? WHERE ID=?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, person.getNom());
                preparedStatement.setString(2, person.getPrenom());
                preparedStatement.setString(3, person.getEmail());
                preparedStatement.setInt(4, person.getId());

                int affectedRows = preparedStatement.executeUpdate();

                if (affectedRows > 0) {
                    System.out.println("Informations de la personne mises à jour pour l'ID : " + person.getId());
                } else {
                    System.out.println("Aucune personne trouvée avec l'ID : " + person.getId());
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    //   classe Person pour stocker les informations de la personne
    class Person {
        private int id;
        private String nom;
        private String prenom;
        private  String email;

        // Constructeur
        public Person(int id, String nom, String prenom, String email) {
            this.id = id;
            this.nom = nom;
            this.prenom = prenom;
            this.email = email;
        }

        // Les getters
        public int getId() {
            return id;
        }

        public String getNom() {
            return nom;
        }

        public String getPrenom() {
            return prenom;
        }
        public String getEmail() {
            return email;
        }

        // les setters
        public void setNom(String nom) {
            this.nom = nom;
        }

        public void setPrenom(String prenom) {
            this.prenom = prenom;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}
