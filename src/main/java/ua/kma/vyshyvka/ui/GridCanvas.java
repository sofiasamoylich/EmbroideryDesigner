package ua.kma.vyshyvka.ui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import ua.kma.vyshyvka.model.EmbroideryGrid;

import java.util.function.BiConsumer;

/**
 * A zoomable, pannable Canvas that renders the embroidery grid.
 * Zoom is implemented by changing cellSize (pixel-grid scaling).
 * Pan is implemented via Canvas translate within the parent Pane.
 */
public class GridCanvas extends Pane {

    // ── Constants ───────────────────────────────────────────
    private static final double CELL_SIZE_MIN  = 4.0;
    private static final double CELL_SIZE_MAX  = 64.0;
    private static final double ZOOM_FACTOR    = 1.12;
    private static final Color  GRID_LINE_COLOR = Color.rgb(181, 191, 160, 0.45);
    private static final Color  BG_COLOR        = Color.web("#F5F0E8");

    // ── State ────────────────────────────────────────────────
    private double cellSize = 16.0;
    private final Canvas canvas;
    private final EmbroideryGrid grid;

    // Pan tracking
    private double panStartX, panStartY;
    private double translateX = 0, translateY = 0;

    // Callback: notified when the user clicks/drags a cell
    private BiConsumer<Integer, Integer> onCellPainted;
    private boolean isDragging = false;

    // ── Constructor ──────────────────────────────────────────
    public GridCanvas(EmbroideryGrid grid) {
        this.grid = grid;
        int w = (int)(grid.getCols() * cellSize);
        int h = (int)(grid.getRows() * cellSize);
        this.canvas = new Canvas(w, h);
        getChildren().add(canvas);

        setupMouseHandlers();
        setupZoomHandler();
        redraw();
    }

    // ── Public API ───────────────────────────────────────────
    public void setCellSize(double size) {
        this.cellSize = Math.clamp(size, CELL_SIZE_MIN, CELL_SIZE_MAX);
        resizeCanvas();
        redraw();
    }

    public double getCellSize() { return cellSize; }

    public void setOnCellPainted(BiConsumer<Integer, Integer> callback) {
        this.onCellPainted = callback;
    }

    /** Full redraw — call after any model change. */
    public void redraw() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        int rows = grid.getRows();
        int cols = grid.getCols();

        // Background
        gc.setFill(BG_COLOR);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Cells
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Color c = grid.getColor(row, col);
                if (c != null) {
                    drawCell(gc, row, col, c);
                }
            }
        }

        // Grid lines (only when cell size is large enough to be readable)
        if (cellSize >= 6) {
            drawGridLines(gc, rows, cols);
        }
    }

    // ── Rendering Helpers ────────────────────────────────────

    /**
     * Draws a single cell. Supports SOLID and CROSS-STITCH modes.
     * The stitch mode is set on the canvas via setStitchMode().
     */
    private StitchMode stitchMode = StitchMode.SOLID;

    public void setStitchMode(StitchMode mode) {
        this.stitchMode = mode;
        redraw();
    }

    private void drawCell(GraphicsContext gc, int row, int col, Color color) {
        double x = col * cellSize;
        double y = row * cellSize;

        switch (stitchMode) {
            case SOLID -> {
                gc.setFill(color);
                gc.fillRect(x + 0.5, y + 0.5, cellSize - 1, cellSize - 1);
            }
            case CROSS -> {
                // Draw background
                gc.setFill(Color.rgb(245, 240, 232));
                gc.fillRect(x + 0.5, y + 0.5, cellSize - 1, cellSize - 1);
                // Draw cross (X) in the cell color
                gc.setStroke(color);
                gc.setLineWidth(Math.max(1.0, cellSize / 8.0));
                double margin = cellSize * 0.2;
                // \ diagonal
                gc.strokeLine(x + margin, y + margin,
                        x + cellSize - margin, y + cellSize - margin);
                // / diagonal
                gc.strokeLine(x + cellSize - margin, y + margin,
                        x + margin, y + cellSize - margin);
            }
            case HALF -> {
                gc.setFill(color);
                // Top-right triangle
                gc.fillPolygon(
                        new double[]{x, x + cellSize, x + cellSize},
                        new double[]{y, y, y + cellSize},
                        3
                );
            }
        }
    }

    private void drawGridLines(GraphicsContext gc, int rows, int cols) {
        gc.setStroke(GRID_LINE_COLOR);
        gc.setLineWidth(0.5);
        double w = cols * cellSize;
        double h = rows * cellSize;
        for (int i = 0; i <= cols; i++) {
            double x = i * cellSize;
            gc.strokeLine(x, 0, x, h);
        }
        for (int i = 0; i <= rows; i++) {
            double y = i * cellSize;
            gc.strokeLine(0, y, w, y);
        }
    }

    // ── Mouse Handlers ───────────────────────────────────────
    private void setupMouseHandlers() {
        // PRIMARY = draw/erase; MIDDLE or PRIMARY+Alt = pan
        canvas.setOnMousePressed(e -> {
            if (e.getButton() == MouseButton.MIDDLE ||
                    (e.getButton() == MouseButton.PRIMARY && e.isAltDown())) {
                panStartX = e.getSceneX() - translateX;
                panStartY = e.getSceneY() - translateY;
                isDragging = true;
            } else if (e.getButton() == MouseButton.PRIMARY) {
                fireCellEvent(e);
            }
        });

        canvas.setOnMouseDragged(e -> {
            if (isDragging) {
                translateX = e.getSceneX() - panStartX;
                translateY = e.getSceneY() - panStartY;
                canvas.setTranslateX(translateX);
                canvas.setTranslateY(translateY);
            } else if (e.getButton() == MouseButton.PRIMARY) {
                fireCellEvent(e);
            }
        });

        canvas.setOnMouseReleased(e -> isDragging = false);
    }

    private void fireCellEvent(MouseEvent e) {
        int col = (int)(e.getX() / cellSize);
        int row = (int)(e.getY() / cellSize);
        if (row >= 0 && row < grid.getRows() && col >= 0 && col < grid.getCols()) {
            if (onCellPainted != null) onCellPainted.accept(row, col);
        }
    }

    // ── Zoom via Scroll ──────────────────────────────────────
    private void setupZoomHandler() {
        // Zoom toward mouse cursor position
        canvas.setOnScroll((ScrollEvent e) -> {
            if (!e.isControlDown()) return;  // Ctrl+Scroll to zoom
            double oldSize = cellSize;
            if (e.getDeltaY() > 0) {
                cellSize = Math.min(cellSize * ZOOM_FACTOR, CELL_SIZE_MAX);
            } else {
                cellSize = Math.max(cellSize / ZOOM_FACTOR, CELL_SIZE_MIN);
            }
            // Adjust translate to zoom toward the mouse position
            double scale = cellSize / oldSize;
            translateX = e.getX() - scale * (e.getX() - translateX);
            translateY = e.getY() - scale * (e.getY() - translateY);
            canvas.setTranslateX(translateX);
            canvas.setTranslateY(translateY);

            resizeCanvas();
            redraw();
            e.consume();
        });
    }

    public void zoomIn()    { setCellSize(cellSize * ZOOM_FACTOR); }
    public void zoomOut()   { setCellSize(cellSize / ZOOM_FACTOR); }
    public void zoomReset() { setCellSize(16.0); canvas.setTranslateX(0); canvas.setTranslateY(0); }

    private void resizeCanvas() {
        double w = grid.getCols() * cellSize;
        double h = grid.getRows() * cellSize;
        canvas.setWidth(w);
        canvas.setHeight(h);
        setPrefSize(w, h);
    }

    public enum StitchMode { SOLID, CROSS, HALF }
}
