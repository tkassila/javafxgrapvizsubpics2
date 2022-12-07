package com.metait.javafxgrapvizsubpics;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.transform.Scale;

/**
 * This class holds mouse zoom able panel, which is workoing with a slider together.
 */
public class ZoomingPane extends AnchorPane {
    private Scale scale = null;
    ImageView content;
    private DoubleProperty zoomFactor = new SimpleDoubleProperty(1);

    public ZoomingPane(ImageView content) {
        this.content = content;
        getChildren().add(content);
        scale = new Scale(1, 1);
        content.getTransforms().add(scale);

        zoomFactor.addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                scale.setX(newValue.doubleValue());
                scale.setY(newValue.doubleValue());
                requestLayout();
            }
        });
    }

    protected void layoutChildren() {
        Pos pos = Pos.TOP_LEFT;
        double width = getWidth();
        double height = getHeight();
        double top = getInsets().getTop();
        double right = getInsets().getRight();
        double left = getInsets().getLeft();
        double bottom = getInsets().getBottom();
        double contentWidth = (width - left - right)/zoomFactor.get();
        double contentHeight = (height - top - bottom)/zoomFactor.get();
        layoutInArea(content, left, top,
                contentWidth, contentHeight,
                0, null,
                pos.getHpos(),
                pos.getVpos());
    }

    public final Double getZoomFactor() {
        return zoomFactor.get();
    }
    public final void setZoomFactor(Double zoomFactor) {
        this.zoomFactor.set(zoomFactor);
    }
    public final DoubleProperty zoomFactorProperty() {
        return zoomFactor;
    }
}

