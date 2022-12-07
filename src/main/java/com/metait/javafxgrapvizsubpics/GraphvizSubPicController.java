package com.metait.javafxgrapvizsubpics;

import com.metait.javafxgrapvizsubpics.data.FileData;
import com.metait.javafxgrapvizsubpics.tasks.GenerateDotFilesFromOneDotfile;
import com.metait.javafxgrapvizsubpics.tasks.TaskResult;
import com.metait.javafxgrapvizsubpics.tasks.TaskResultHolder;
import com.metait.javafxgrapvizsubpics.tasks.XsdFileIntoDotFile;

import javafx.collections.FXCollections;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import javafx.util.converter.IntegerStringConverter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.concurrent.locks.ReentrantLock;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.function.UnaryOperator;


public class GraphvizSubPicController {
    private ImageView imageView;
    @FXML
    protected TreeView treeView;
    @FXML
    protected Label labelMsg;
    @FXML
    protected Button buttonFile;
    @FXML
    protected Label labelFileTime;
    @FXML
    protected Button buttonOpenPictureDialog;
    @FXML
    protected Button buttonDataDir;
    @FXML
    protected TextField textFieldDataDir;
    @FXML
    protected ComboBox<ComboFile> comboBoxDirs;
    @FXML
    protected Button buttonCreateDir;
    @FXML
    protected TextField textFieldCreateDir;
    @FXML
    protected Button buttonCreateSubPics;
    @FXML
    protected /* ComboBox<File> */ ChoiceBox<File> comboBoxDotFiles;
    @FXML
    protected CheckBox checkBoxDotFiles;
    @FXML
    protected TextField textFieldRelLevel;
    @FXML
    protected Button buttonReadDirFiles;
    @FXML
    protected Button buttonCancel;
    @FXML
    protected Button buttonCreateMainPic;
    @FXML
    protected Tab tabImport;
    @FXML
    protected Tab tabPic;
    @FXML
    protected ScrollPane scrollPaneImage;
    @FXML
    protected Slider sliderImage;
    @FXML
    protected AnchorPane anchorPaneImage;
    @FXML
    protected TextField textFieldImport;
    @FXML
    protected Button buttonImport;
    @FXML
    protected Button buttonSelectImport;
    @FXML
    protected ListView<FileData> listViewDetalj;
    @FXML
    protected Button buttonClosePictureDialog;
    @FXML
    protected Button buttonCenterWindow;
    @FXML
    protected ListView<GraphVizNodeListItem> lstOpenDialogs;
    @FXML
    protected Label labelImaveView;
    private HashMap<String, FileData> hmMainTreeItems = new HashMap<>();
    private ZoomingPane zoomingPane = null;
    protected Stage m_primaryStage;
    private EventHandler<KeyEvent> keyEventHandler = null;
    public static boolean bDebug = true;
    private boolean bReadMainDirFiles = true;
    private FileChooser fileChooser = new FileChooser();
    private DirectoryChooser dirChooser = new DirectoryChooser();
    private String strSelectDirButtonText = null;
    private File selectedDir = null;
    private File [] readFiles = null;
    private File selectedXsdOrDotFile = null;
    private XsdFileIntoDotFile xsdService = new XsdFileIntoDotFile();
    private TaskResultHolder xsdServiceTaskResultHolder = new TaskResultHolder(xsdService);

    private GenerateDotFilesFromOneDotfile dotFilesService = new GenerateDotFilesFromOneDotfile();
    private TaskResultHolder dotFilesServiceTaskResultHolder = new TaskResultHolder(dotFilesService);
    private Service<Void> updateTreeViewService = null;
    private File outputDotDir = null;
    private List<String> listResult = new ArrayList<>();
    private volatile boolean bExecuted = false;
    private boolean bProsesRestarted = false;
    private TreeItem<FileData> rootTreeItem = new TreeItem<>(new FileData("All items"));
    private TreeItem<FileData> mainTreeItem = new TreeItem<>(new FileData("Main Dot items"));
    private TreeItem<FileData> onwTreeItem = new TreeItem<>(new FileData("Own created items"));
    private int iCount_pressedTabPic = 0;
    private int serviceId = 0;
    private ObservableMap<Stage, PictureWindowController> hmOpenDialogs = FXCollections.observableHashMap();
    private ReentrantLock lock = new ReentrantLock();
    private ObservableList<GraphVizNodeListItem> listOpenStages = FXCollections.observableArrayList();
    private File selectedRootDir = null;

    private FilenameFilter pngOrDotFileFilter = new FilenameFilter() {
        public boolean accept(File dir, String name) {
            boolean bHas = name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".dot");
            if (bHas && !new File(dir, name).isDirectory())
                return true;
            else
                return false;
        }
    };

    private FilenameFilter packageFileOrDotFileFilter = new FilenameFilter() {
        public boolean accept(File dir, String name) {
            boolean bHas = name.toLowerCase().endsWith(".xsd") || name.toLowerCase().endsWith(".dot")
                    /* || name.toLowerCase().endsWith(".7z") */ || name.toLowerCase().endsWith(".zip");
            if (bHas && !new File(dir, name).isDirectory())
                return true;
            else
                return false;
        }
    };

    private void setSelectedDir(File f)
    {
        selectedDir = f;
        Platform.runLater(new Runnable() {
            @Override public void run() {
                if (comboBoxDotFiles.getItems() != null)
                    comboBoxDotFiles.getItems().clear();
                if (selectedDir != null && hasRightFiles(f))
                    buttonReadDirFiles.setDisable(false);
                else
                    buttonReadDirFiles.setDisable(true);
            }
        });
    }

    private void initDotService()
    {
        if (bDebug)
        {
            System.out.println("initDotService");
        }
        final StringBuffer sbResult = new StringBuffer();
        // try {


        dotFilesServiceTaskResultHolder.valueProperty().addListener((ols, oldData, newData) -> {
            if(newData != null) {
                TaskResult reseult = (TaskResult)newData;
                setExecuted(reseult.isExecuting());
                setLabelMsg(reseult.getMsg());
            }
        });

        dotFilesService.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
                System.out.println("done:" /* + t.getSource().getValue() */);
                GenerateDotFilesFromOneDotfile service = (GenerateDotFilesFromOneDotfile)t.getSource();
                int exitValue = service.getExitValue();
                TaskResult result = (TaskResult)t.getSource().getValue();
                result.setExitValue(exitValue);
                if (exitValue != 0) {
                    result.setMsg("Dot service: Error in executing dot file into a dot file!");
                    if (result == null || service.getErrorString() != null) {
                        String strError = service.getErrorString();
                        String [] strErrLines = strError.split("(\n|$)");
                        for(String str : strErrLines)
                            listResult.add(str);
                    }
                    else {
                        String [] strLines = result.getExecuteValue().split("(\n|$)");
                        for(String str : strLines)
                            listResult.add(str);
                    }
                }
                else
                {
                    result.setMsg("Dot service has done with a success.");
                }
                result.setExecuting(false);
                //listResult.getItems().addAll(strResult.split("(\n|$)"));
                dotFilesServiceTaskResultHolder.setTaskResult(result);
            }
        });

        dotFilesService.setOnFailed(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
                System.out.println("failed:" /* + t.getSource().getValue() */);
                if (bProsesRestarted)
                    return;
                GenerateDotFilesFromOneDotfile service = (GenerateDotFilesFromOneDotfile)t.getSource();
                int exitValue = service.getExitValue();
                TaskResult result = (TaskResult)t.getSource().getValue();
                result.setExitValue(exitValue);

                result.setMsg("Dot service: the execution failed.!");
                result.setExecuting(false);
                dotFilesServiceTaskResultHolder.setTaskResult(result);
            }
        });

        dotFilesService.setOnCancelled(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
                System.out.println("canceled:" /* + t.getSource().getValue() */);
                if (bProsesRestarted)
                    return;
                GenerateDotFilesFromOneDotfile service = (GenerateDotFilesFromOneDotfile)t.getSource();
                int exitValue = service.getExitValue();
                TaskResult result = (TaskResult)t.getSource().getValue();
                result.setExitValue(exitValue);

                result.setMsg("Dot service: user has been canceled the execution!");
                result.setExecuting(false);
                dotFilesServiceTaskResultHolder.setTaskResult(result);
            }
        });

            /*
                    } catch (Exception e){
            e.printStackTrace();
            sbResult.append(e.toString() +"\n\n errorlevel " +processes.getExitValue() +"\n\n" +processes.getErrorString());
            this.bExecuted = false;
        }g
             */
    }

    private void updateTreeView(){
        if (outputDotDir != null)
        {
            String selFilePath = selectedXsdOrDotFile.getAbsolutePath();
            String search = ".xsd";
            int ind = selFilePath.lastIndexOf(search);
            if (ind > -1)
            {
                selFilePath = selFilePath.substring(0, ind ) +".json";
            }
            else {
                search = ".dot";
                ind = selFilePath.lastIndexOf(search);
                if (ind > -1) {
                    selFilePath = selFilePath.substring(0, ind + search.length()) + ".json";
                }
                else
                    return;
            }

            File jsonMainfile = new File(selFilePath);
            if (!jsonMainfile.exists())
                return;

            File [] arrDotFiles = getDotFiles(outputDotDir.listFiles());
            if (arrDotFiles != null && arrDotFiles.length > 0)
            {
                FileData[] dataItems = getDataItems(arrDotFiles, jsonMainfile);
                mainTreeItem.getChildren().clear();
                List<TreeItem<FileData>> listFiles = new ArrayList<TreeItem<FileData>>();
                TreeItem<FileData> treeItem;
                hmMainTreeItems.clear();
                for(FileData dataItem : dataItems)
                {
                    treeItem = new TreeItem<FileData>(dataItem);
                    listFiles.add(treeItem);
                    hmMainTreeItems.put(dataItem.getFileName(), dataItem);
                }
                Collections.sort(listFiles, Comparator.comparing(s -> s.toString().toLowerCase()));
                mainTreeItem.getChildren().addAll(listFiles);
                rootTreeItem.setExpanded(true);
                try {
                    Thread.sleep(300);
                }catch (Exception e){
                }
                mainTreeItem.setExpanded(true);
                // DO THIS
            }
        }
    }

    private FileData parseGraphVizNode(JSONObject jsonObj)
    {
        FileData ret = null;
        boolean isDirectory_value_false = false;

        if (jsonObj != null)
        {
            String id = (String) jsonObj.get("id");
            String label = (String) jsonObj.get("label");
            System.out.println(id);
            String visualLabel = (String) jsonObj.get("visuallabel");
            System.out.println(visualLabel);
            // public FileData(String name, visuallabel, label boolean isDirectory, String strCreateTime, String strModTime) {
            ret = new FileData(id, visualLabel, label, isDirectory_value_false, null, null);
        }
        return ret;
    }

    private FileData [] getDataItems(File [] arrDotFiles, File jsonMainfile)
    {
        FileData [] ret = null;
        if (arrDotFiles != null && arrDotFiles.length > 0 && jsonMainfile != null && jsonMainfile.exists())
        {
            JSONParser jsonParser = new JSONParser();

            try {
                FileReader reader = new FileReader(jsonMainfile);

                //Read JSON file
                JSONObject obj = (JSONObject)jsonParser.parse(reader);
                JSONArray graphVizNodes = (JSONArray) obj.get("all_read_nodes");;
                System.out.println(graphVizNodes);

                //Iterate over employee array
                final List<FileData> listFD = new ArrayList<>();
                graphVizNodes.forEach(obj2 ->
                {
                    FileData fd = parseGraphVizNode( (JSONObject)obj2 );
                    if (fd != null)
                        listFD.add(fd);
                });
                FileData [] arrFileData = new FileData [listFD.size()];
                arrFileData = listFD.toArray(arrFileData);
                ret = arrFileData;
            } catch (org.json.simple.parser.ParseException e3) {
                e3.printStackTrace();
                return null;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return null;
            } catch (IOException e2) {
                e2.printStackTrace();
                return null;
            }
        }
        return ret;
    }

    private void initXsdService()
    {
        if (bDebug)
        {
            System.out.println("initXsdService");
        }
        final StringBuffer sbResult = new StringBuffer();
        // try {

        dotFilesServiceTaskResultHolder.valueProperty().addListener((ols, oldData, newData) -> {
            if(newData != null) {
                TaskResult reseult = (TaskResult)newData;
                setExecuted(reseult.isExecuting());
                setLabelMsg(reseult.getMsg());
            }
        });

        xsdService.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent t) {
                    System.out.println("done:" /* + t.getSource().getValue() */);
                    XsdFileIntoDotFile service = (XsdFileIntoDotFile)t.getSource();
                    buttonCancel.setDisable(true);
                    int exitValue = service.getExitValue();
                    TaskResult result = (TaskResult)t.getSource().getValue();
                    if (exitValue != 0) {
                        result.setMsg("Xsd service: Error in executing xsd file into a dot file!");
                        if ((result == null)
                                && service.getErrorString() != null) {
                            String strError = service.getErrorString();
                            String [] strErrLines = strError.split("(\n|$)");
                            for(String str : strErrLines)
                                listResult.add(str);

                        }
                        else {
                            String [] strLines = ((String)result.getExecuteValue()).split("(\n|$)");
                            for(String str : strLines)
                                listResult.add(str);
                        }
                    }
                    else
                    {
                        result.setMsg("Xsd service has done with a success.");
                    }
                    result.setExecuting(false);
                    dotFilesServiceTaskResultHolder.setTaskResult(result);
                    //listResult.getItems().addAll(strResult.split("(\n|$)"));
                }
            });

            xsdService.setOnFailed(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent t) {
                    System.out.println("failed:" /* + t.getSource().getValue() */);
                    if (bProsesRestarted)
                        return;
                    XsdFileIntoDotFile service = (XsdFileIntoDotFile)t.getSource();
                    int exitValue = service.getExitValue();
                    TaskResult result = (TaskResult)t.getSource().getValue();
                    result.setMsg("Xsd service: executing failed!");
                    buttonCancel.setDisable(true);
                    // isJarScanSearchOk.setBoolean(false);
                    // listResult.getItems().clear();
                    Object arrValue = t.getSource().getValue();
                    // if (arrValue != null)
                       // listResult.getItems().addAll(new ListResultData(arrValue.toString()));
                    //listResult.getItems().addAll(strResult.split("\n"));
                    result.setExecuting(false);
                    dotFilesServiceTaskResultHolder.setTaskResult(result);
                }
            });

            xsdService.setOnCancelled(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent t) {
                    System.out.println("canceled:" /* + t.getSource().getValue() */);
                    if (bProsesRestarted)
                        return;
                    XsdFileIntoDotFile service = (XsdFileIntoDotFile)t.getSource();
                    int exitValue = service.getExitValue();
                    TaskResult result = (TaskResult)t.getSource().getValue();
                    result.setMsg("Xsd service: user has been canceled the execution!");
                    buttonCancel.setDisable(true);
                    // isJarScanSearchOk.setBoolean(false);
                    // listResult.getItems().clear();
                    Object arrValue = t.getSource().getValue();
                    /* if (arrValue != null) {
                        listResult.getItems().clear();
                        listResult.getItems().addAll(new ListResultData(arrValue.toString()));
                    } */
                    result.setExecuting(false);
                    dotFilesServiceTaskResultHolder.setTaskResult(result);
                }
            });

            /*
                    } catch (Exception e){
            e.printStackTrace();
            sbResult.append(e.toString() +"\n\n errorlevel " +processes.getExitValue() +"\n\n" +processes.getErrorString());
            this.bExecuted = false;
        }
             */
    }

    @FXML
    public void initialize() {
        initXsdService();
        initDotService();;

        lstOpenDialogs.setItems(listOpenStages);

        setDisAbleOpenDialogControls(true);

        this.imageView = new ImageView();
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setPannable(true);
        Group scrollGroup = new Group();
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        // scrollPane.setPrefSize(anchorPaneImage.getPrefWidth(), anchorPaneImage.getPrefHeight());
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS); // ScrollPane.ScrollBarPolicy.AS_NEEDED
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        // scrollPane.setContent(this.imageView);

        zoomingPane = new ZoomingPane(this.imageView);

        /*
        zoomingPane.setMaxHeight(anchorPaneImage.getMaxHeight());
        zoomingPane.setMaxWidth(anchorPaneImage.getMaxWidth());
         */
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

        /*
        imageView.fitWidthProperty().bind(scrollPaneImage.widthProperty());
        imageView.fitHeightProperty().bind(scrollPaneImage.heightProperty());
        */
        // scrollPaneImage.translateZProperty().bind(sliderImage.valueProperty());

        listViewDetalj.getSelectionModel().selectedItemProperty().addListener( new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue,
                                Object newValue) {
                if (newValue == null)
                    return;
                FileData selectedItem = (FileData) newValue;
                System.out.println("Selected Text : " +selectedItem);
                if (outputDotDir == null)
                    return;
                File selectedPngFile = new File(outputDotDir, selectedItem.getFileName() + ".png");
                if (selectedPngFile.exists()) {
                    try {
                        //   AnchorPane apane = (AnchorPane)imageView.getParent();
                        imageView.setImage(new Image(selectedPngFile.toURL().toString()));
                        labelImaveView.setText(selectedPngFile.getName());
                    } catch (Exception e) {
                        labelImaveView.setText("");
                        imageView.setImage(null);
                        setLabelMsg("Error in loading an selected image!");
                    }
                } else {
                    imageView.setImage(null);
                    labelImaveView.setText("");
                    setLabelMsg("An image file does not exists!");
                    // do what ever you want
                }
            }
        });

        // imageView.setImage(new Image(selectedPngFile.toURL().toString()));

        treeView.getSelectionModel().selectedItemProperty().addListener( new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue,
                                Object newValue) {
                if (newValue == null)
                    return;
                TreeItem<FileData> selectedItem = (TreeItem<FileData>) newValue;
                FileData fd = selectedItem.getValue();
                System.out.println("Selected Text : " + fd);
                if (fd.getMsg() != null) // skip this...
                    return;
                if (outputDotDir == null)
                    return;
                File selectedPngFile = new File(outputDotDir, fd.getFileName() + ".png");
                File selectedJsonFile = new File(outputDotDir, fd.getFileName() + ".json");
                if (selectedJsonFile.exists()) {
                    updatelistViewDetalj(selectedPngFile, selectedJsonFile);
                }
                if (selectedPngFile.exists()) {
                    try {
                     //   AnchorPane apane = (AnchorPane)imageView.getParent();
                        imageView.setImage(new Image(selectedPngFile.toURL().toString()));
                        labelImaveView.setText(selectedPngFile.getName());
                        /*
                        imageView.setFitWidth(300);
                        imageView.setFitHeight(300);

                        imageView.setPreserveRatio(true);
                        imageView.setSmooth(true);
                        imageView.setCache(true);
                         */
                    } catch (Exception e) {
                        imageView.setImage(null);
                        setLabelMsg("Error in loading an selected image!");
                    }
                } else {
                    imageView.setImage(null);
                    setLabelMsg("An image file does not exists!");
                    // do what ever you want
                }
            }
        });

        Tooltip tableTip = new Tooltip(
                "This level value range from 1 - 10, by example. A bigger value will generate also bigger sub pictures and tekes long time to done after a press for the button.");
        tableTip.setStyle("-fx-font-weight: bold; -fx-text-fill: yellow; -fx-font-size: 14");
        tableTip.setShowDelay(Duration.seconds(4));
        textFieldRelLevel.setTooltip(tableTip);

        labelMsg.setStyle("-fx-font-weight: bold; -fx-font-size: 14");
        setLabelMsg("Press 'Select data root directory' -button at first.");

        rootTreeItem.getChildren().add(mainTreeItem);
        rootTreeItem.getChildren().add(onwTreeItem);

        treeView.setRoot(rootTreeItem);
        setMostControlsDisabled(true);

        dirChooser.setTitle("Select root data directory");
        // comboBoxDotFiles.setTitle("Select xsd or .dot file");
        fileChooser.setTitle("Select file(s) to import");

        UnaryOperator<TextFormatter.Change> integerFilter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("-?([1-9][0-9]*)?")) {
                return change;
            }
            return null;
        };
        textFieldRelLevel.setTextFormatter(
                new TextFormatter<Integer>(new IntegerStringConverter(), 0, integerFilter));
        textFieldRelLevel.setText("1");

        comboBoxDotFiles.valueProperty().addListener(new ChangeListener<File>() {
            @Override
            public void changed(ObservableValue<? extends File> observable, File oldValue, File newValue) {
                // If the condition is not met and the new value is not null: "rollback"
                if(newValue != null){
                    Platform.runLater(new Runnable() {
                        @Override public void run() {
                            setSelectedXsdOrDotFile(newValue, true);
                        }
                    });
                }
            }
        });

        checkBoxDotFiles.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                comboBoxDotFiles.getItems().clear();
                initDotFileCombo();
            }
        });

        comboBoxDirs.valueProperty().addListener(new ChangeListener<ComboFile>() {
            @Override
            public void changed(ObservableValue<? extends ComboFile> observable, ComboFile oldValue, ComboFile newValue) {
                // If the condition is not met and the new value is not null: "rollback"
                if(newValue != null){
                    setSelectedDir(newValue.getFile());
                }
            }
        });

        textFieldDataDir.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.trim().length() > 0 && newValue != oldValue)
            {
                File f = new File(textFieldDataDir.getText());
                if (f.exists())
                {
                    if (!f.isDirectory())
                    {
                        setLabelMsg("Data directory text field is not a directory. Change value later.");
                        return;
                    }
                    if (hasDirectories(f))
                        setMostControlsDisabled(false);
                    else
                        setMostControlsDisabled(true);
                }
                else
                    setMostControlsDisabled(true);
            }
            else
                setMostControlsDisabled(true);
        });

        // if (Main.bDebug)
        keyEventHandler = new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.V && event.isControlDown()) {
                    Clipboard clipboard = Dragboard.getSystemClipboard();
                    boolean success = false;
                    if (clipboard.hasString()) {
                        System.out.println("clibboard: " + clipboard.getString());
                        System.out.println("target: " + event.getTarget());
                        Control control = (Control)event.getTarget();
                        System.out.println("id: " + control.getId());
                        handlePossibleUrl(clipboard.getString());
                        success = true;
                    }
                    event.consume();
                }
            }
        };
    } // end of method

    @FXML
    protected void pressed_buttonFile()
    {
        System.out.println("pressed_buttonFile");
        String strSelectedNodeName = buttonFile.getText();
        if (strSelectedNodeName == null || strSelectedNodeName.trim().length()==0)
            return;
        if (outputDotDir == null)
            return;
        File pngFile = new File(outputDotDir, strSelectedNodeName);
        if (!pngFile.exists())
            return;
        try {
            imageView.setImage(new Image(pngFile.toURL().toString()));
            int index = listViewDetalj.getSelectionModel().getSelectedIndex();
            if (index > -1)
                listViewDetalj.getSelectionModel().clearSelection(index);
        }catch (Exception e){
            setLabelMsg(("Error in loading picture: " +e.getMessage()));
        }
    }

    private void updatelistViewDetalj(File selectedPngFile, File selectedJsonFile) {
        try {
            File f = (selectedPngFile != null && selectedPngFile.exists()) ? selectedPngFile : selectedJsonFile;
            if (f == null)
                return;
            buttonFile.setText(f.getName());
            if (selectedPngFile == null)
                return;

            System.out.println("updatelistViewDetalj");
            System.out.println(selectedJsonFile);
            listViewDetalj.getItems().clear();
            BufferedReader br = new BufferedReader(new FileReader(selectedJsonFile));
            try {
                StringBuilder sb = new StringBuilder();
                JSONParser jsonParser = new JSONParser();

                try {
                    FileReader reader = new FileReader(selectedJsonFile);

                    //Read JSON file
                    JSONObject obj = (JSONObject)jsonParser.parse(reader);
                    JSONArray graphVizNodes = (JSONArray) obj.get("all_read_nodes");;
                    System.out.println(graphVizNodes);
                    String dotFName = selectedJsonFile.getName();
                    int ind = dotFName.lastIndexOf(".");
                    {
                        dotFName = dotFName.substring(0, ind);
                    }
                    final String final_dotFName = dotFName;
                    //Iterate over employee array
                    final List<FileData> listFD = new ArrayList<>();
                    graphVizNodes.forEach(obj2 ->
                    {
                        FileData fd = parseGraphVizNode( (JSONObject)obj2 );
                        if (fd != null && (final_dotFName == null || final_dotFName.trim().length() > 0
                                && fd.getFileName() != final_dotFName)) // dont allow same node id to get sub list
                            listFD.add(fd);
                    });
                    /*
                    FileData [] arrFileData = new FileData [listFD.size()];
                    arrFileData = listFD.toArray(arrFileData);
                     */
                    Collections.sort(listFD, Comparator.comparing(s -> s.toString().toLowerCase()));

                    listViewDetalj.getItems().addAll(listFD);
                } catch (org.json.simple.parser.ParseException e3) {
                    e3.printStackTrace();
                    return;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    return;
                } catch (IOException e2) {
                    e2.printStackTrace();
                    return;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                if (br != null)
                    br.close();
            }
        }catch (Exception fne)
        {
            fne.printStackTrace();
            setLabelMsg("Error in reading file (" + "): " +fne.getMessage());
        }

    }

    private void setSelectedXsdOrDotFile(File f, boolean bCalledFromComboBox) {
        selectedXsdOrDotFile = f;
        //         File subDir_generateDir = getSubDirOfGenerated(selectedXsdOrDotFile);
        //        updateTreeView();

        updateTreeViewService = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    protected Void call()
                            throws IOException, InterruptedException {
                        File subDir_generateDir = getSubDirOfGenerated(selectedXsdOrDotFile);
                        updateTreeView();
                        return null;
                  }
               };
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                System.out.println("Task updateTreeViewService completed successfully");
            }

            @Override
            protected void cancelled() {
                super.cancelled();
                System.out.println("Task updateTreeViewService cancelled");
            }
        };

        updateTreeViewService.start();
        /*
        if (selectedXsdOrDotFile != null)
        {
            if (selectedXsdOrDotFile.getName().toLowerCase().endsWith(".xsd"))
            {
                // setLabelMsg("Generate automatic sub pictures by pressing a button");
                generateAutomaticSubPictures(selectedXsdOrDotFile);
                return;
            }
            File jsonFile = new File(f.getParent(), selectedXsdOrDotFile.getName().replace(".xsd",".json"));
            if (!jsonFile.exists())
            {
                // setLabelMsg("A corresponds json file is missing. Generate automatic sub pictures by pressing a button");
                generateAutomaticSubPictures(selectedXsdOrDotFile);
                return;
            }
            checkIfGeneratedSubPicturesMustGenerated(selectedXsdOrDotFile, bCalledFromComboBox);
        }
        else
        if (selectedXsdOrDotFile.getName().toLowerCase().endsWith(".dot"))
        {
            if (!selectedXsdOrDotFile.exists())
            {
                setLabelMsg("Cannot find a .dot file: " +selectedXsdOrDotFile.getAbsolutePath());
                File xsdFile = new File(selectedXsdOrDotFile.getAbsolutePath().replace(".dot",".xsd"));
                generateAutomaticSubPictures(xsdFile);
                return;
            }
            checkIfGeneratedSubPicturesMustGenerated(selectedXsdOrDotFile, bCalledFromComboBox);
        }
         */
    }

    private void checkIfGeneratedSubPicturesMustGenerated(File f, boolean bCalledFromComboBox)
    {
        if (f == null)
            return;
        if (!f.exists())
            return;
        if (!bCalledFromComboBox || getSourceFileHasBeenChanged(f))
            generateAutomaticSubPictures(selectedXsdOrDotFile);
    }

    private boolean getSourceFileHasBeenChanged(File f)
    {
        // TODD: impoplation
        return false;
    }

    private File getSubDirOfGenerated(File selectedFile)
    {
        if (selectedFile == null)
            return null;
        File generateDir = new File(selectedFile.getParent(), "generated");
        if (!generateDir.exists())
            if (!generateDir.mkdir())
            {
                setLabelMsg("Cannot create this directory: " +generateDir.getAbsolutePath());
                return null;
            }
        String baseFileName = selectedFile.getName();
        int ind = baseFileName.lastIndexOf(".");
        if (ind > -1)
        {
            baseFileName = baseFileName.substring(0, ind);
        }
        File subDir_generateDir = new File(generateDir, baseFileName);
        if (!subDir_generateDir.exists())
            if (!subDir_generateDir.mkdir())
            {
                setLabelMsg("Cannot create this directory: " +subDir_generateDir.getAbsolutePath());
                return null;
            }
        outputDotDir = subDir_generateDir;
        return outputDotDir;
    }

    private void generateAutomaticSubPictures(File f)
    {
        if (f == null)
            return;
        if (!f.exists())
            return;
        File subDir_generateDir = getSubDirOfGenerated(selectedXsdOrDotFile);
        File [] oldFiles = subDir_generateDir.listFiles(pngOrDotFileFilter);
        if (oldFiles != null && oldFiles.length > 0)
        {
            boolean bNotAllFileAreDeleted = false;
            for(File f2 : oldFiles)
            {
                try {
                    if (!f2.delete())
                        bNotAllFileAreDeleted = true;
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            if (bNotAllFileAreDeleted) {
                setLabelMsg("Not all .png or .dot files are deleted on Directory: " + subDir_generateDir.getAbsolutePath());
                return;
            }
        }
        runAutomaticPngPictureGeneration(subDir_generateDir);
    }

    private void runAutomaticPngPictureGeneration(File subDir_generateDir)
    {
        // TODD: impoplation
    }


    private void setLabelMsg(String msg)
    {
        Platform.runLater(new Runnable() {
            @Override public void run() {
                labelMsg.setText(msg);
            }
        });
    }

     private void setMostControlsDisabled(boolean bDesiAble)
    {
        Platform.runLater(new Runnable() {
            @Override public void run() {
                buttonCreateMainPic.setDisable(bDesiAble);
                buttonCreateDir.setDisable(bDesiAble);
                buttonOpenPictureDialog.setDisable(bDesiAble);
                comboBoxDirs.setDisable(bDesiAble);
                textFieldCreateDir.setDisable(bDesiAble);
                comboBoxDotFiles.setDisable(bDesiAble);
                checkBoxDotFiles.setDisable(bDesiAble);
                textFieldRelLevel.setDisable(bDesiAble);
                buttonCreateSubPics.setDisable(bDesiAble);
                treeView.setDisable(bDesiAble);
                imageView.setDisable(bDesiAble);
                buttonSelectImport.setDisable(bDesiAble);
                buttonImport.setDisable(bDesiAble);
                if (bDesiAble) {
                    buttonReadDirFiles.setDisable(bDesiAble);
                    buttonCancel.setDisable(bDesiAble);
                }
            }
        });
    }

    @FXML
    protected void pressedButtonDataDir(){
            String txt = textFieldDataDir.getText();
            if (txt != null && txt.trim().length() > 0)
                dirChooser.setInitialDirectory(new File(txt));
            File file = dirChooser.showDialog(m_primaryStage);
            if (file != null) {
                selectedRootDir = file;
                comboBoxDirs.getItems().clear();
                textFieldDataDir.setText(file.getAbsolutePath());
                strSelectDirButtonText = buttonDataDir.getText();
           //     buttonDataDir.setText("Read dir files");
                bReadMainDirFiles = true;
                readRootDirectories();
                // setMostControllsDesAbled(false);
            }
    }

    private void readRootDirectories() {
        if (selectedRootDir == null)
            return;
        comboBoxDirs.getItems().clear();
        if (!hasDirectories(selectedRootDir))
            initializeSubDirComboBox(null);
        else {
            File[] files = getXDirsOf(selectedRootDir);
            initializeSubDirComboBox(files);
        }
    }

    private boolean hasRightFiles(File f)
    {
        boolean ret = false;
        if (f == null || !f.exists() || !f.isDirectory())
            return false;

        File [] files = getXsdOrDorFilesOf(f);
        if (files != null && files.length > 0)
        {
            return true;
        }
        return ret;
    }

    private File [] getXsdOrDorFilesOf(File fDir)
    {
        if (fDir == null)
            return null;

        File [] files = fDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                boolean bHas = name.toLowerCase().endsWith(".xsd") || name.toLowerCase().endsWith(".dot");
                if (bHas && !new File(dir, name).isDirectory())
                    return true;
                else
                    return false;
            }
        });
        return files;
    }

    private boolean hasDirectories(File f)
    {
        boolean ret = false;
        if (f == null || !f.exists() || !f.isDirectory())
            return false;

        File [] files = getXDirsOf(f);
        if (files != null && files.length > 0)
        {
            return true;
        }
        return ret;
    }

    private File [] getXDirsOf(File fDir)
    {
        if (fDir == null)
            return null;
        File [] files = fDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                if (new File(dir, name).isDirectory())
                    return true;
                else
                    return false;
            }
        });
        return files;
    }

    private void initializeSubDirComboBox(File [] files)
    {
        if (files == null || files.length == 0)
        {
            comboBoxDirs.getItems().clear();
            return;
        }
        List<ComboFile> listFiles = new ArrayList<>();
        for (File f : files)
            listFiles.add(new ComboFile(f));
        comboBoxDirs.getItems().addAll(listFiles);
    }

    private File [] getDotFiles(File [] readFiles)
    {
        return getXXXFiles(readFiles, ".dot");
    }

    private File [] getXXXFiles(File [] readFiles, String strExt){
        if (readFiles == null)
            return null;

        boolean bHas;
        List<File> newFiles = new ArrayList<>();
        for(File f : readFiles)
        {
            bHas = f.getName().toLowerCase().endsWith(strExt);
            if (bHas && !f.isDirectory())
                newFiles.add(f);
        };
        File [] ret = new File[newFiles.size()];
        ret = newFiles.toArray(ret);
        return ret;
    }

    private File [] getXsdFiles(File [] readFiles)
    {
        return getXXXFiles(readFiles, ".xsd");
    }

    private void initDotFileCombo()
    {
        comboBoxDotFiles.getItems().clear();
        if (readFiles == null || readFiles.length == 0)
            return;

        if (checkBoxDotFiles.isSelected())
        {
            File [] readDotfiles = getDotFiles(readFiles);
            comboBoxDotFiles.getItems().addAll(readDotfiles);
        }
        else
        {
            File [] readXsdfiles = getXsdFiles(readFiles);
            comboBoxDotFiles.getItems().addAll(readXsdfiles);
        }
    }

    @FXML
    protected void pressedButtonReadDataDirFiles() {
        System.out.println("pressedButtonReadDataDirFiles");
        if (selectedDir == null)
            return;
        File [] files = getXsdOrDorFilesOf(selectedDir);
        if (files == null || files.length == 0)
        {
            setLabelMsg("No files to read from directory: " +selectedDir.getAbsolutePath());
            return;
        }
        readFiles = files;
        initDotFileCombo();
        setLabelMsg("Files has been read from selected directory. You can select a next tab, please.");
        /*
        else
    {
        File fDir = new File(textFieldDataDir.getText());
        if (!fDir.exists())
        {
            setLabelMsg("Directory does not exists!");
            buttonDataDir.setText(strSelectDirButtonText);
            bReadMainDirFiles = false;
            return;
        }
        if (!fDir.isDirectory())
        {
            setLabelMsg("The selected file is not a directory!");
            buttonDataDir.setText(strSelectDirButtonText);
            bReadMainDirFiles = false;
            return;
        }
        File [] filesInDir = fDir.listFiles();
        if (filesInDir == null || filesInDir.length == 0)
        {
            setLabelMsg("Directory has no files! Choose an another directory");
            buttonDataDir.setText(strSelectDirButtonText);
            bReadMainDirFiles = false;
            return;
        }
        if (filesInDir == null || filesInDir.length == 0)
        {
            setLabelMsg("Directory has no files! Chooze an another directory");
            buttonDataDir.setText(strSelectDirButtonText);
            bReadMainDirFiles = false;
            return;
        }

        bReadMainDirFiles = false;
        setMostControllsDesAbled(false);
    }
    */
}

    private void handlePossibleUrl(String fromClibBoard)
    {
        if (fromClibBoard == null || fromClibBoard.trim().length()==0)
            return;
        String tryUrl = fromClibBoard;
        try {
            try {
                new URL(tryUrl);
            }catch (MalformedURLException mfe){
                tryUrl = "https://" +fromClibBoard;
                try {
                    new URL(tryUrl);
                }catch (MalformedURLException mfe2){
                    tryUrl = "http://" +fromClibBoard;
                    try {
                        new URL(tryUrl);
                    }catch (MalformedURLException mfe3){
                        if (File.separatorChar == '\\')
                            tryUrl = "file:///" +fromClibBoard;
                        else
                            tryUrl = "file://" +fromClibBoard;
                        try {
                            new URL(tryUrl);
                        }catch (MalformedURLException mfe4) {
                            throw mfe4;
                        }
                    }
                }
            }
            // addURLIntoGridWebPages(System.out.println(msgreadFilestryUrl);
        } catch (MalformedURLException malformedURLException) {
            malformedURLException.printStackTrace();
            String msg = "Text paste is not web address: " +malformedURLException.getMessage();
            System.out.println(msg);
            Platform.runLater(new Runnable() {
                public void run() {
                    labelMsg.setText(msg);
                }
            });
            return;
        }
    }

    @FXML
    protected void onHelloButtonClick() {
      //  welcomeText.setText("Welcome to JavaFX Application!");
    }

    public void setPrimaryStage(Stage p_primaryStage)
    {
        m_primaryStage = p_primaryStage;
        m_primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                appIsClosing();
                Platform.exit();
                System.exit(0);
            }
        });
    }

    public void handleKeyEvent(KeyEvent event) {
// TODO: implementation!
    }

    private void appIsClosing()
    {
        // TODO:
        System.out.println("loppu");
    }

    @FXML
    protected void pressedButtonCreateMainPic()
    {
        if (selectedXsdOrDotFile == null)
            return;
        if (bExecuted)
            return;
        if (selectedXsdOrDotFile.getName().toLowerCase().endsWith(".xsd")) {
            setExecuted(true);
            xsdService.setXsdPath(selectedXsdOrDotFile.getParent(), selectedXsdOrDotFile.getAbsolutePath());
            setLabelMsg("Executing the main dot file into smaller sub .dot files and pictures.");
            // test buttons: Thread.sleep(10000);
            if (xsdService.getState() != Worker.State.READY) {
                this.bProsesRestarted = true; // this boolean variable into true, because to prevent
                // of extra call below:
                buttonCancel.setDisable(false);
                this.xsdService.restart(); // restart causes an extra call into on success etc
                bProsesRestarted = false; // after an extra handler catt, after this can do "normal" handler call
            }
            else
            {
                this.xsdService.start();
            }
            // strResult = processes.run(strWorkingDir, strExecute);
            return;
        }

        File subDir_generateDir = getSubDirOfGenerated(selectedXsdOrDotFile);
        if (selectedXsdOrDotFile.getName().toLowerCase().endsWith(".dot")
            && subDir_generateDir != null && subDir_generateDir.exists() && subDir_generateDir.isDirectory()) {
            // String inputDotfile, String outputDotDir, String deepLevel
            int iDeepLevel = -1;
            setExecuted(true);
            setLabelMsg("Executing the main dot file into smaller sub .dot files and pictures.");
            String strDeepLevel = "1";
            if (textFieldRelLevel.getText().trim().length()>0)
            {
                try {
                    iDeepLevel = Integer.parseInt(textFieldRelLevel.getText());
                    if (iDeepLevel > 20) {
                        iDeepLevel = 10;
                        textFieldRelLevel.setText("" + iDeepLevel);
                    }
                    strDeepLevel = "" +iDeepLevel;
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            else
                textFieldRelLevel.setText(strDeepLevel);

            // String inputDotfile, String outputDotDir, String deepLevel
            this.dotFilesService.setParams(selectedXsdOrDotFile.getAbsolutePath(), subDir_generateDir.getAbsolutePath(),
                    strDeepLevel);
        this.dotFilesService.start();
            return;
        }
    }

    @FXML
    protected void pressedButtonCancel()
    {
        System.out.println("pressedButtonCancel");
        if (selectedXsdOrDotFile == null)
            return;

        if (selectedXsdOrDotFile.getName().toLowerCase().endsWith(".xsd")) {
              if (xsdService.getState() == Worker.State.RUNNING)
                this.xsdService.cancelProcess();
              if (dotFilesService.getState() == Worker.State.RUNNING)
                this.xsdService.cancelProcess();
              if (updateTreeViewService != null && updateTreeViewService.getState() == Worker.State.RUNNING)
                  this.updateTreeViewService.cancel();
            return;
        }

        if (selectedXsdOrDotFile.getName().toLowerCase().endsWith(".dot"))
        {
           if (xsdService.getState() == Worker.State.RUNNING)
              this.xsdService.cancelProcess();
           if (dotFilesService.getState() == Worker.State.RUNNING)
              this.xsdService.cancelProcess();
            if (updateTreeViewService != null && updateTreeViewService.getState() == Worker.State.RUNNING)
                this.updateTreeViewService.cancel();
           return;
        }
    }

    private void setExecuted(boolean bValue)
    {
        this.bExecuted = bValue;
        if (this.bExecuted)
        {
            Platform.runLater(new Runnable() {
                @Override public void run() {
                    imageView.setImage(null);
                    buttonCancel.setDisable(false);
                    buttonCreateSubPics.setDisable(true);
                    buttonCreateMainPic.setDisable(true);
                }
            });
        }
        else
        {
            Platform.runLater(new Runnable() {
                @Override public void run() {
                    buttonCancel.setDisable(true);
                    buttonCreateSubPics.setDisable(false);
                    buttonCreateMainPic.setDisable(false);
                }
            });
        }
    }

    @FXML
    protected void pressedButtonCreateSubPic()
    {
        System.out.println("pressedButtonCreateSubPic");
        if (selectedXsdOrDotFile == null)
            return;
        if (bExecuted)
            return;

        String selFile = selectedXsdOrDotFile.getAbsolutePath();
        if (selectedXsdOrDotFile.getName().toLowerCase().endsWith(".xsd"))
            selFile = selFile.replace(".xsd",".dot");
        int iDeepLevel = -1;
        String strDeepLevel = "1";
        if (textFieldRelLevel.getText().trim().length()>0)
        {
            try {
                iDeepLevel = Integer.parseInt(textFieldRelLevel.getText());
                if (iDeepLevel > 20) {
                    iDeepLevel = 10;
                    textFieldRelLevel.setText("" + iDeepLevel);
                }
                strDeepLevel = "" +iDeepLevel;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        else
            textFieldRelLevel.setText(strDeepLevel);
        File subDir_generateDir = getSubDirOfGenerated(selectedXsdOrDotFile);
        dotFilesService.setParams(selFile, subDir_generateDir.getAbsolutePath(),
                    strDeepLevel);
        setExecuted(true);
        setLabelMsg("Starts to execute converting the main .dot file into sub pictures...");
        mainTreeItem.setExpanded(false);

        for(GraphVizNodeListItem item : lstOpenDialogs.getItems())
        {
            if (item == null)
                continue;
            // item = (GraphVizNodeListItem)itemObj;
            item.getOpenStage().close();
        }
        mainTreeItem.getChildren().clear();
        listViewDetalj.getItems().clear();
        hmMainTreeItems.clear();
        hmOpenDialogs.clear();
        this.dotFilesService.start();

        return;
    }

    @FXML
    protected void pressedTabImport()
    {
        System.out.println("pressedTabImport");
    }

    @FXML
    protected void pressedTabPic()
    {
        // System.out.println("pressedTabPic");
        if (readFiles != null) {
            iCount_pressedTabPic++;
            if (iCount_pressedTabPic == 1 && selectedXsdOrDotFile == null)
                setLabelMsg("Select a file in the combo box.");
        }
    }

    private String getFileDataId(String id)
    {
        String ret = id;
        int ind = id.lastIndexOf(".");
        if (ind > -1)
        {
            ret = id.substring(0, ind);
        }
        return ret;
    }

    @FXML
    protected void pressedButtonOpenPicture()
    {
        System.out.println("pressedButtonOpenPicture");
        if (imageView == null || imageView.getImage() == null || imageView.getImage().getUrl() == null
                || imageView.getImage().getUrl().trim().length() == 0)
            return;

        String imageUrl = imageView.getImage().getUrl();
        File fImage = new File(imageUrl);
        String id = fImage.getName();
        boolean bFound = false;
        for (GraphVizNodeListItem item : lstOpenDialogs.getItems())
        {
            if (item == null)
                continue;
            if (item.getIdGraphViz() == id)
            {
                bFound = true;
                break;
            }
        }
        if (bFound)
        {
            setLabelMsg("This image is all ready in open dialog.");
            return;
        }

        FXMLLoader fxmlLoader2 = new FXMLLoader(PictureWindowController.class.getResource("graphvizsubpics_picture_window-view.fxml"));
        PictureWindowController win_controller = new PictureWindowController();
        //m_primaryStage = stage;
        fxmlLoader2.setController(win_controller);
        try {
            Stage stage_window = new Stage();
            stage_window.setTitle("Sub picture " +id);
            stage_window.setOnHiding((event) -> {
                System.out.println("stage_window.setOnHiding hide");
            });
            win_controller.setStage(stage_window);
            win_controller.setImageUrl(imageView.getImage().getUrl());
            hmOpenDialogs.put(stage_window, win_controller);
            Parent loadedRoot = fxmlLoader2.load();
            Scene scene = new Scene(loadedRoot, 700, 714);
            stage_window.setScene(scene);
            scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent event) {
                    win_controller.handleKeyEvent(event);
                }
            });

            stage_window.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent e) {
                    if (e.getEventType() == WindowEvent.WINDOW_CLOSE_REQUEST)
                        pictureWindowIsClosing(e.getTarget());
                }
            });
            FileData fdata = hmMainTreeItems.get(getFileDataId(id));
            GraphVizNodeListItem item = new GraphVizNodeListItem(id, fdata.getVisualLabel(), stage_window);
            listOpenStages.add(item);
            if (lstOpenDialogs.isDisable())
                setDisAbleOpenDialogControls(false);
            stage_window.show();
        }catch (Exception e){
            e.printStackTrace();
            setLabelMsg("Error in opening dialog window: " + e.getMessage());
        }
    }

    private void setDisAbleOpenDialogControls(boolean bValue)
    {
        lstOpenDialogs.setDisable(bValue);
        buttonClosePictureDialog.setDisable(bValue);
        buttonCenterWindow.setDisable(bValue);
    }

    private void pictureWindowIsClosing(EventTarget target)
    {
        System.out.println("pictureWindowIsClosing");
        //lstOpenDialogs.getItems().remove((Stage)target);
        final Stage selectToClose = (Stage)target;
        final List<GraphVizNodeListItem> holderItems = new ArrayList<>();
        holderItems.addAll(listOpenStages);
        Platform.runLater(new Runnable() {
            @Override public void run() {
                for(GraphVizNodeListItem item : holderItems)
                {
                    if (item.getOpenStage().equals(selectToClose)) {
                        hmOpenDialogs.remove(selectToClose);
                        listOpenStages.remove(item);
                    }
                }
                if (lstOpenDialogs.getItems().size() < 1)
                    setDisAbleOpenDialogControls(true);
            }
        });
    }

    @FXML
    protected void pressedButtonClosePictureDialog()
    {
        System.out.println("pressedButtonClosePictureDialog");
        GraphVizNodeListItem item = lstOpenDialogs.getSelectionModel().getSelectedItem();
        if (item == null)
            return;
        item.getOpenStage().fireEvent(
            new WindowEvent(
                m_primaryStage,
                WindowEvent.WINDOW_CLOSE_REQUEST
           )
        );
    }

    @FXML
    protected void pressed_buttonCenterWindow()
    {
        System.out.println("pressed_buttonCenterWindow");
        GraphVizNodeListItem item = lstOpenDialogs.getSelectionModel().getSelectedItem();
        if (item == null)
            return;
        if (item.getOpenStage().isAlwaysOnTop())
            return;
        lock.lock();
        try {
            item.getOpenStage().setIconified(false); // if window is hidden!
            item.getOpenStage().setAlwaysOnTop(true); // these two are bringing it into front again:
            item.getOpenStage().setAlwaysOnTop(false);
        } finally {
            lock.unlock();
        }
    }

    @FXML
    private void pressed_buttonCreateDir(){
        System.out.println("pressed_buttonCreateDir");
        String strDirName = textFieldCreateDir.getText();
        if (strDirName == null || strDirName.trim().length()==0)
        {
            setLabelMsg("Insert a name for an directory that will be created.");
            return;
        }
        if(this.selectedRootDir == null)
            return;
        File possibleNewDir = new File(selectedRootDir, strDirName);
        if (possibleNewDir.exists())
        {
            setLabelMsg("The directory exists all ready. No adding.");
            return;
        }
        if (!possibleNewDir.mkdir())
        {
            setLabelMsg("Cannot creaet this directory: " +possibleNewDir.getAbsolutePath());
            return;
        }
        readRootDirectories();
        setLabelMsg("this directory is created: " +possibleNewDir.getAbsolutePath());
    }

    @FXML
    protected void pressed_buttonSelectImport()
    {
        if (selectedRootDir == null)
            return;
        if (selectedDir == null)
            return;
        String txt = textFieldImport.getText();
        fileChooser.setInitialDirectory(selectedRootDir);
      //  fileChooser.setSelectedExtensionFilter();
        /*
        File [] file = fileChooser.showDialog(m_primaryStage);
        if (file != null) {
        }

         */

    }

} // end of class