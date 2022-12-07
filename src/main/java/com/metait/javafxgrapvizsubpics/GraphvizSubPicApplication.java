package com.metait.javafxgrapvizsubpics;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * This javafx application is capable to show a dot grapchviz png picture and generate automatic sub pictures
 * from that main dot file. This dot file is made of an xsd file or more, where 3th part software are generating
 * main odt text file. The application is calling dot app to execution. So it call 3 external applications: 2 java
 * apps and one dot graphviz application.
 */
public class GraphvizSubPicApplication extends Application {
    private Stage m_primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(GraphvizSubPicApplication.class.getResource("graphvizsubpics-view.fxml"));
        GraphvizSubPicController controller = new GraphvizSubPicController();
        m_primaryStage = stage;
        controller.setPrimaryStage(m_primaryStage);
        fxmlLoader.setController(controller);
        Parent loadedRoot = fxmlLoader.load();
        Scene scene = new Scene(loadedRoot, 1200, 714);
        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                controller.handleKeyEvent(event);
            }
        });
        stage.setTitle("Generates and shows automatic sub pictures of main Xsd graphviz picture");
        stage.setScene(scene);
        stage.show();
        //
    }

    public static void main(String[] args) {
        launch();
    }
}