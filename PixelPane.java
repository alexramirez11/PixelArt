import java.util.LinkedList;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ColorPicker;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

/**
 * This class represents the canvas itslf. This object contains all the functionality for building the canvas,
 * and allowing for each cell to change color. This class extends the Pane class.
 * @author Alex Ramirez
 */
public class PixelPane extends Pane {

    // private final double CELL_SIZE = 20;

    private int rows, cols;
    private Color[][] gridColors;
    private Canvas canvas;
    private ColorPicker picker;
    private Color backBoardColor = Color.WHITE;
    private boolean bucket = false;
    private String name;
    private String lastSavedTime;
    private boolean showGridLines = true;
    
    /**
     * This constructor is generating a new instance of the LBPane.
     * @param rows The rows of the light brite pane. NOTE: This can be no bigger than 100
     * @param columns The columns of the light brite pane. NOTE: This can be no bigger than 100
     * @param color The default color for each cell in the light brite
     * @param wBound The maximum size of the width. This is used to resize button width accordingly
     * @param hBound The maximum size of the height. This is used to resize button height accordingly
     * @param picker The color picker that is used in the light brite
     */
    public PixelPane(int rows, int columns, String color, ColorPicker picker) {
        this.rows = rows;
        this.cols = columns;
        this.picker = picker;
        lastSavedTime = "never";
        if (color == null) {
            color = "black";
        }

        gridColors = new Color[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                gridColors[i][j] = Color.web(color);
            }
        }

        canvas = new Canvas();
        canvas.widthProperty().bind(this.widthProperty());
        canvas.heightProperty().bind(this.heightProperty());
        this.name = "untitled";
        getChildren().add(canvas);

        drawGrid();
        mouseEvents();
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        drawGrid();
    }

    public void drawGrid() {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        double paneWidth = canvas.getWidth();
        double paneHeight = canvas.getHeight();

        gc.setFill(backBoardColor);
        gc.fillRect(0, 0, paneWidth, paneHeight);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                double x1 = Math.round(j * paneWidth / cols);
                double y1 = Math.round(i * paneHeight / rows);
                double x2 = Math.round((j + 1) * paneWidth / cols);
                double y2 = Math.round((i + 1) * paneHeight / rows);
                double w = x2 - x1;
                double h = y2 - y1;
                gc.setFill(gridColors[i][j]);
                gc.fillRect(x1, y1, w, h);
            }
        }

        if (showGridLines) {
            gc.setStroke(backBoardColor);
            for (int i = 0; i <= rows; i++) {
                double y = Math.round(i * paneHeight / rows);
                gc.strokeLine(0, y, paneWidth, y);
            }
            for (int i = 0; i <= cols; i++) {
                double x = Math.round(i * paneWidth / cols);
                gc.strokeLine(x, 0, x, paneHeight);
            }
        }
    }

    private void mouseEvents() {
        canvas.setOnMousePressed(e -> paintCell(e.getX(), e.getY()));
        canvas.setOnMouseDragged(e -> paintCell(e.getX(), e.getY()));
    }

    public void toggleGridLines() {
        showGridLines = !showGridLines;
        drawGrid();
    }

    public boolean showingGridLines() {
        return showGridLines;
    }

    private void paintCell(double x, double y) {
        double cellWidth = canvas.getWidth() / cols;
        double cellHeight = canvas.getHeight() / rows;

        int col = (int) (x / cellWidth);
        int row = (int) (y / cellHeight);

        if (row >= 0 && row < rows && col >= 0 && col < cols) {
            if (bucket) {
                floodFillIterative(row, col, picker.getValue());
            } else {
                gridColors[row][col] = picker.getValue();
                drawGrid();
            }
        }
    }

    private void floodFillIterative(int startRow, int startCol, Color replacement) {
        Color target = gridColors[startRow][startCol];
        if (colorsAreEqual(target, replacement)) return;

        boolean[][] visited = new boolean[rows][cols];
        LinkedList<int[]> stack = new LinkedList<>();
        stack.push(new int[]{startRow, startCol});

        while (!stack.isEmpty()) {
            int[] cell = stack.pop();
            int r = cell[0], c = cell[1];

            if (r < 0 || r >= rows || c < 0 || c >= cols) continue;
            if (visited[r][c]) continue;
            if (!colorsAreEqual(gridColors[r][c], target)) continue;

            gridColors[r][c] = replacement;
            visited[r][c] = true;

            stack.push(new int[]{r + 1, c});
            stack.push(new int[]{r - 1, c});
            stack.push(new int[]{r, c + 1});
            stack.push(new int[]{r, c - 1});
        }

        drawGrid();
    }

    private boolean colorsAreEqual(Color a, Color b) {
        double threshold = 0.001;
        return Math.abs(a.getRed() - b.getRed()) < threshold &&
            Math.abs(a.getGreen() - b.getGreen()) < threshold &&
            Math.abs(a.getBlue() - b.getBlue()) < threshold &&
            Math.abs(a.getOpacity() - b.getOpacity()) < threshold;
    }

    public Color[][] getGridColors() {
        return gridColors;
    }

    public void setBackgroundColor(String col) {
        if (col.startsWith("fx-background-color: ")) {
            col = col.replace("fx-background-color: ", "").trim();
        }
        backBoardColor = Color.web(col);
    }

    /**
     * This constructor is used to load and rebuild an already existing image into a light brite pane. 
     * @param metaData The meta data for the light bright, containing information on each peg's row index, column index, and color
     * @param wBound The maximum size of the width. This is used to resize peg width accordingly
     * @param hBound The maximum size of the height. This is used to resize peg height accordingly
     * @param picker The color picker that is used in the light brite
     */
    public PixelPane(LinkedList<String> metaData, ColorPicker picker, String name) {
        lastSavedTime = metaData.removeFirst();
        backBoardColor = Color.web(metaData.removeFirst());
        String[] dimStr = metaData.removeFirst().split("/");
        this.rows = Integer.parseInt(dimStr[0]);
        this.cols = Integer.parseInt(dimStr[1]);
        gridColors = new Color[this.rows][this.cols];
        this.name = name;
        this.picker = picker;


        for (String str : metaData) {
            String[] spiltStr = str.split("//");
            int rowIndex = Integer.parseInt(spiltStr[0]);
            int colIndex = Integer.parseInt(spiltStr[1]);
            String color = spiltStr[2];
            gridColors[rowIndex][colIndex] = Color.web(color);
        }
        canvas = new Canvas();
        canvas.widthProperty().bind(this.widthProperty());
        canvas.heightProperty().bind(this.heightProperty());
        getChildren().add(canvas);

        drawGrid();
        mouseEvents();
    }

    /**
     * This method gives the number of rows in the light brite.
     * @return An integer count of the rows
     */
    public int getRows() {
        return this.rows;
    }

    /**
     * This method gives the number of columns in th light brite.
     * @return An integer count of the columns
     */
    public int getColumns() {
        return this.cols;
    }

    public void saveGridlines(Color colStr) {
        backBoardColor = colStr;
    }

    public Color getGridLineColor() {
        return backBoardColor;
    }

    public boolean getBucketMode() {
        return bucket;
    }

    public void toggleBucket() {
        bucket = !bucket;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLastSavedTime(String s) {
        lastSavedTime = s;
    }

    public String getLastSavedTime() {
        return lastSavedTime;
    }

    public String toString() {
        return this.name;
    }
    
}