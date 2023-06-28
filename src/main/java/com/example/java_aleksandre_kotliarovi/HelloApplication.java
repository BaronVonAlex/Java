package com.example.java_aleksandre_kotliarovi;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import java.sql.*;

public class HelloApplication extends Application {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/product";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "";

    private ObservableList<Employee> employees = FXCollections.observableArrayList();
    private BorderPane root;
    private PieChart pieChart;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Employee");

        root = new BorderPane();
        root.setPadding(new Insets(10));

        GridPane inputGrid = new GridPane();
        inputGrid.setVgap(5);
        inputGrid.setHgap(5);

        Label nameLabel = new Label("Name:");
        inputGrid.add(nameLabel, 0, 0);

        TextField nameField = new TextField();
        inputGrid.add(nameField, 1, 0);

        Label lastnameLabel = new Label("Lastname:");
        inputGrid.add(lastnameLabel, 0, 1);

        TextField lastnameField = new TextField();
        inputGrid.add(lastnameField, 1, 1);

        Label departmentLabel = new Label("Department:");
        inputGrid.add(departmentLabel, 0, 2);

        TextField departmentField = new TextField();
        inputGrid.add(departmentField, 1, 2);

        Label salaryLabel = new Label("Salary:");
        inputGrid.add(salaryLabel, 0, 3);

        TextField salaryField = new TextField();
        inputGrid.add(salaryField, 1, 3);

        Button addButton = new Button("Add");
        inputGrid.add(addButton, 0, 4);

        addButton.setOnAction(e -> {
            String name = nameField.getText();
            String lastname = lastnameField.getText();
            String department = departmentField.getText();
            Double salary = Double.parseDouble(salaryField.getText());

            Employee employee = new Employee(name, lastname, department, salary);
            saveEmployee(employee);

            nameField.clear();
            lastnameField.clear();
            departmentField.clear();
            salaryField.clear();


            retrieveEmployees();
            updatePieChart();
        });

        root.setLeft(inputGrid);

        pieChart = new PieChart();
        pieChart.setTitle("Product Quantity");
        root.setCenter(pieChart);

        Scene scene = new Scene(root, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.show();

        retrieveEmployees();
        updatePieChart();
    }

    private void saveEmployee(Employee employee) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            String query = "SELECT * FROM employee WHERE name =? AND lastname =? AND department =? AND salary =?";
            PreparedStatement selectStatement = conn.prepareStatement(query);
            selectStatement.setString(1, employee.getName());
            selectStatement.setString(2, employee.getLastname());
            selectStatement.setString(3, employee.getDepartment());
            selectStatement.setDouble(4, employee.getSalary());
            ResultSet resultSet = selectStatement.executeQuery();

            if (resultSet.next()) {
                double existingSalary = resultSet.getInt("salary");

                double newSalary = existingSalary + employee.getSalary();

                String updateQuery = "UPDATE employee SET salary =? WHERE name =? AND lastname =? AND department =?";
                PreparedStatement updateStatement = conn.prepareStatement(updateQuery);
                updateStatement.setDouble(1, newSalary);
                updateStatement.setString(2, employee.getName());
                updateStatement.setString(3, employee.getLastname());
                updateStatement.setString(4, employee.getDepartment());
                updateStatement.executeUpdate();
            } else {
                String insertQuery = "INSERT INTO employee (name, lastname, department, salary) VALUES (?,?,?,?)";
                PreparedStatement insertStatement = conn.prepareStatement(insertQuery);
                insertStatement.setString(1, employee.getName());
                insertStatement.setString(2, employee.getLastname());
                insertStatement.setString(3, employee.getDepartment());
                insertStatement.setDouble(4, employee.getSalary());
                insertStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void retrieveEmployees() {
        employees.clear();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            String query = "SELECT name, lastname, department, salary FROM employee";
            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                String name = resultSet.getString("name");
                String lastname = resultSet.getString("lastname");
                String department = resultSet.getString("department");
                Double salary = resultSet.getDouble("salary");

                Employee employee = new Employee(name, lastname, department, salary);
                employees.add(employee);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updatePieChart() {
        pieChart.getData().clear();

        Map<String, List<Employee>> employeeBySalary = employees.stream()
                .collect(Collectors.groupingBy(employee -> {
                    if (employee.getSalary() < 500) {
                        return "0-500";
                    } else if (employee.getSalary() < 1000) {
                        return "500-1000";
                    } else {
                        return "1000+";
                    }
                }));

        int totalCount = employeeBySalary.values().stream()
                .mapToInt(List::size)
                .sum();

        employeeBySalary.forEach((salaryRange, employees) -> {
            PieChart.Data data = new PieChart.Data(salaryRange + " (" + employees.size() + ")", employees.size());
            pieChart.getData().add(data);
        });

        pieChart.setTitle("Employee Salary Range (Total: " + totalCount + ")");
    }

}
