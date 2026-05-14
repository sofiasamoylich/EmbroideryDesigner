package ua.kma.vyshyvka.model;

import javafx.scene.paint.Color;

/**
 * The authoritative grid model. Stores colors as JavaFX Color objects.
 * null means "empty cell" (unstitched linen).
 */
public class EmbroideryGrid {

    private final int rows;
    private final int cols;
    private final Color[][] cells;

    public EmbroideryGrid(int rows, int cols) {
        this.rows  = rows;
        this.cols  = cols;
        this.cells = new Color[rows][cols];  // all null = empty
    }

    public int getRows() { return rows; }
    public int getCols() { return cols; }

    public Color getColor(int row, int col) { return cells[row][col]; }
    public void  setColor(int row, int col, Color c) { cells[row][col] = c; }
    public void  clearCell(int row, int col)         { cells[row][col] = null; }

    /** Deep copy — used by CommandHistory for undo snapshots. */
    public EmbroideryGrid snapshot() {
        EmbroideryGrid copy = new EmbroideryGrid(rows, cols);
        for (int r = 0; r < rows; r++)
            System.arraycopy(cells[r], 0, copy.cells[r], 0, cols);
        return copy;
    }

    /** Count non-empty cells. */
    public int countElements() {
        int n = 0;
        for (Color[] row : cells)
            for (Color c : row)
                if (c != null) n++;
        return n;
    }
}
