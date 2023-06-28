module com.example.java_aleksandre_kotliarovi {
    requires javafx.controls;
    requires javafx.fxml;
    requires static lombok;
    requires java.sql;
                            
    opens com.example.java_aleksandre_kotliarovi to javafx.fxml;
    exports com.example.java_aleksandre_kotliarovi;
}