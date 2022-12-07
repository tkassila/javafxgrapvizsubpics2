package com.metait.javafxgrapvizsubpics;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
// import javafx.stage.WindowEvent;

/**
 * This control class can open with .fxml file a zoom able (png) picture inside a javafx window.
 */
public class PictureWindowController {
    @FXML
    protected Label labelWindow;
    @FXML
    protected Button buttonClose;
    @FXML
    protected Slider sliderImage;
    @FXML
    protected AnchorPane anchorPaneImage;
    @FXML
    protected Label labelMsg;

    private String strImageUrl;
    private ZoomingPane zoomingPane = null;
    private ImageView imageView;
    private Stage m_stage;

    public void setImageUrl(String p_strImageUrl)
    {
        strImageUrl = p_strImageUrl;
    }

    public void setStage(Stage stage)
    {
        m_stage = stage;
    }

    @FXML
    public void initialize() {
        try {
            imageView = new ImageView(strImageUrl);
            zoomingPane = new ZoomingPane(imageView);
            anchorPaneImage.getChildren().add(zoomingPane);

            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setPannable(true);
            Group scrollGroup = new Group();
            scrollPane.setFitToHeight(true);
            scrollPane.setFitToWidth(true);
            // scrollPane.setPrefSize(anchorPaneImage.getPrefWidth(), anchorPaneImage.getPrefHeight());
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS); // ScrollPane.ScrollBarPolicy.AS_NEEDED
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
            // scrollPane.setContent(this.imageView);

            this.sliderImage.setMin(0.01);
            this.sliderImage.setMax(2);
            this.sliderImage.setValue(1);

            zoomingPane.zoomFactorProperty().bind(sliderImage.valueProperty());
            labelMsg.setStyle("-fx-font-weight: bold; -fx-font-size: 14");


            scrollPane.setPrefSize(anchorPaneImage.getPrefWidth(), anchorPaneImage.getPrefHeight());
            scrollPane.setMaxHeight(anchorPaneImage.getMaxHeight());
            scrollPane.setMaxWidth(anchorPaneImage.getMaxWidth());
            scrollPane.prefWidthProperty().bind(anchorPaneImage.widthProperty());
            scrollPane.prefHeightProperty().bind(anchorPaneImage.heightProperty());
            scrollPane.setMaxHeight(anchorPaneImage.getMaxHeight());
            zoomingPane.setMaxWidth(anchorPaneImage.getMaxWidth());
            zoomingPane.prefWidthProperty().bind(scrollPane.widthProperty());
            zoomingPane.prefHeightProperty().bind(scrollPane.heightProperty());
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);
            scrollGroup.getChildren().add(zoomingPane);
            scrollPane.setContent(scrollGroup);
            //this.anchorPaneImage.setTopAnchor(scrollPane, 0.0);
            BackgroundFill background_fill = new BackgroundFill(Color.BLACK,
                    CornerRadii.EMPTY, Insets.EMPTY);

            Background bgColor = new Background(background_fill);
            anchorPaneImage.setBackground(bgColor);
            this.anchorPaneImage.getChildren().add(scrollPane);

            this.sliderImage.setMin(0.01);
            this.sliderImage.setMax(2);
            this.sliderImage.setValue(1);

            zoomingPane.zoomFactorProperty().bind(sliderImage.valueProperty());


        }catch (Exception e){
            labelMsg.setText("Error in opening this picture (" + strImageUrl +": " +e.getMessage());
        }
    }

    public void handleKeyEvent(KeyEvent event) {

    }
    @FXML
    protected void pressedButtonClose()
    {
       // that is which picture window is closing
        m_stage.fireEvent(
                new WindowEvent(
                        m_stage,
                        WindowEvent.WINDOW_CLOSE_REQUEST
                )
        );
    }

}
