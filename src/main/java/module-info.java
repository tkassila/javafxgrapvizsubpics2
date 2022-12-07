module com.example.javafxgrapvizsubpics2 {
    requires javafx.controls;
    requires javafx.fxml;
    requires json.simple;

    opens com.metait.javafxgrapvizsubpics to javafx.fxml;
    exports com.metait.javafxgrapvizsubpics;
}