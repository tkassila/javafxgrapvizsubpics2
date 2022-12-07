package com.metait.javafxgrapvizsubpics;

import javafx.stage.Stage;

public class GraphVizNodeListItem {
    private String idGraphViz = null;
    private Stage openStage = null;

    private String visualLabel = null;

    public GraphVizNodeListItem(String p_idGraphViz, String p_visualLabel, Stage p_openStage){
        idGraphViz = p_idGraphViz;
        openStage = p_openStage;
        visualLabel = p_visualLabel;
    }

    public String getIdGraphViz() {
        return idGraphViz;
    }

    public void setIdGraphViz(String idGraphViz) {
        this.idGraphViz = idGraphViz;
    }

    public Stage getOpenStage() {
        return openStage;
    }

    public void setOpenStage(Stage openStage) {
        this.openStage = openStage;
    }
    public String toString() { return idGraphViz +" (" +visualLabel +")"; }

}
