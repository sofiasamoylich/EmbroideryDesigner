package ua.kma.vyshyvka.controller;

import javafx.fxml.FXML;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.control.*;
import ua.kma.vyshyvka.model.EmbroideryGrid;
import ua.kma.vyshyvka.ui.GridCanvas;

public class MainController {

    @FXML private Pane   canvasContainer;
    @FXML private Label  lblGrid, lblElements, lblSymmetry;
    @FXML private Label  lblCoords, lblZoom, lblStatus;
    @FXML private Pane   currentColorSwatch;
    @FXML private Pane   patternPreviewPane;
    @FXML private Pane   colorWheelContainer;
    @FXML private FlowPane swatchesPane;
    @FXML private FlowPane elementLibraryPane;
    @FXML private ProgressBar progressBar;
    @FXML private ScrollPane  canvasScrollPane;
    @FXML private Slider      cellSizeSlider;
    @FXML private Label       cellSizeLabel;

    private GridCanvas gridCanvas;
    private EmbroideryGrid grid;

    @FXML
    public void initialize() {
        grid = new EmbroideryGrid(32, 32);
        gridCanvas = new GridCanvas(grid);
        canvasContainer.getChildren().add(gridCanvas);
        lblGrid.setText("32×32");
        lblStatus.setText("Ready");
    }

    // Заглушки для всіх onAction у FXML
    @FXML private void onNew()             {}
    @FXML private void onOpen()            {}
    @FXML private void onSave()            {}
    @FXML private void onExportPng()       {}
    @FXML private void onExportJson()      {}
    @FXML private void onUndo()            {}
    @FXML private void onRedo()            {}
    @FXML private void onGenerateFromName(){}
    @FXML private void onToggleSymmetry()  {}
    @FXML private void onZoomIn()          { gridCanvas.zoomIn(); }
    @FXML private void onZoomOut()         { gridCanvas.zoomOut(); }
    @FXML private void onZoomReset()       { gridCanvas.zoomReset(); }
}
