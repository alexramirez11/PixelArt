import java.util.LinkedList;

import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ColorPicker;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;

/**
 * This class represents the canvas itslf. This object contains all the functionality for building the canvas,
 * and allowing for each cell to change color. This class extends the Pane class.
 * @author Alex Ramirez
 */
public class PixelPane extends Pane {

    private final Affine TRANSFORM = new Affine();

    private final double DEFAULT_CELL_SIZE = 20;
    private final double MIN_ZOOM = 0.8;
    private final double MAX_ZOOM = 8.0;

    private double zoom = 1.0;
    private double offSetX = 0, offSetY = 0;
    private int rows, cols;
    private Color[][] gridColors;
    private Canvas canvas;
    private ColorPicker picker;
    private Color backBoardColor = Color.WHITE;
    private boolean bucket = false;
    private String name;
    private String lastSavedTime;
    private boolean showGridLines = true;
    private boolean redrawQueued = false;
    
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

        this.name = "untitled";
        initializeCanvas();
    }

    private void initializeCanvas() {
        canvas = new Canvas(cols * DEFAULT_CELL_SIZE, rows * DEFAULT_CELL_SIZE);
        getChildren().add(canvas);
        mouseEvents();
        requestRedraw();
    }

    public void drawGrid() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.save();
        gc.setTransform(new Affine());
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        gc.setTransform(TRANSFORM);
        gc.setFill(backBoardColor);

        double size = DEFAULT_CELL_SIZE;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                gc.setFill(gridColors[i][j]);
                gc.fillRect(j * size, i * size, size, size);
            }
        }

        if (showGridLines) {
            gc.setStroke(backBoardColor);
            gc.setLineWidth(1.0 / zoom);
            for (int i = 0; i <= rows; i++) {
                gc.strokeLine(0, i * size, cols * size, i * size);
            }
            for (int i = 0; i <= cols; i++) {
                gc.strokeLine(i * size, 0, i * size, rows * size);
            }
        }

        gc.restore();
    }

    private void mouseEvents() {
        canvas.setOnMousePressed(e -> paintCell(e.getX(), e.getY()));
        canvas.setOnMouseDragged(e -> paintCell(e.getX(), e.getY()));
        canvas.setOnScroll(e -> {
            if (e.isInertia()) {
                return;
            }
            if (e.getTouchCount() > 0) {
                return;
            }
            if (Math.abs(e.getDeltaY()) < 10) {
                return;
            }
            double factor = (e.getDeltaY() > 0) ? 1.1 : 0.9;
            zoomAt(e.getX(), e.getY(), factor);

            e.consume();
        });
    }

    public void toggleGridLines() {
        showGridLines = !showGridLines;
        requestRedraw();
    }

    public boolean showingGridLines() {
        return showGridLines;
    }

    private void paintCell(double x, double y) {
        int col = toColumn(x);
        int row = toRow(y);

        if (row >= 0 && row < rows && col >= 0 && col < cols) {
            if (bucket) {
                floodFillIterative(row, col, picker.getValue());
            } else {
                gridColors[row][col] = picker.getValue();
                requestRedraw();
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

        requestRedraw();
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
        initializeCanvas();
    }

    public void zoomIn() {
        zoom = Math.min(10.0, zoom + 0.1);    
        requestRedraw();
    }

    public void zoomOut() {
        zoom = Math.max(0.2, zoom - 0.1);
        requestRedraw();
    }

    private int toColumn(double x) {
        double worldX = (x - offSetX) / zoom;
        return (int) (worldX / DEFAULT_CELL_SIZE);
    }

    private int toRow(double y) {
        double worldY = (y - offSetY) / zoom;
        return (int) (worldY / DEFAULT_CELL_SIZE);
    }

    public void zoomAt(double mx, double my, double zoomFactor) {
        double oldZoom = zoom;
        double newZoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, zoom * zoomFactor));

        double worldX = (mx - offSetX) / oldZoom;
        double worldY = (my - offSetY) / oldZoom;

        zoom = newZoom;

        offSetX = mx - worldX * zoom;
        offSetY = my - worldY * zoom;

        TRANSFORM.setToIdentity();
        TRANSFORM.appendTranslation(offSetX, offSetY);
        TRANSFORM.appendScale(zoom, zoom);
        
        requestRedraw();
    }

    private void requestRedraw() {
        if (redrawQueued) {
            return;
        }
        redrawQueued = true;

        Platform.runLater(() -> {
            redrawQueued = false;
            drawGrid();
        });
    }

    /**
     * This method creates a writable image of the canvas, only containing the grid and everything on it.
     * @return A WritableImage object to be exported as a png file.
     */
    public WritableImage exportImage() {
        double oldZoom = zoom;
        double oldOffsetX = offSetX;
        double oldOffsetY = offSetY;

        zoom = 1.0;
        offSetX = 0;
        offSetY = 0;

        TRANSFORM.setToIdentity();

        drawGrid();

        int width = (int)(cols * DEFAULT_CELL_SIZE);
        int height = (int)(rows * DEFAULT_CELL_SIZE);

        WritableImage image = new WritableImage(width, height);

        SnapshotParameters params = new SnapshotParameters();
        params.setViewport(new Rectangle2D(0, 0, width, height));

        canvas.snapshot(params, image);

        zoom = oldZoom;
        offSetX = oldOffsetX;
        offSetY = oldOffsetY;

        TRANSFORM.setToIdentity();
        TRANSFORM.appendTranslation(offSetX, offSetY);
        TRANSFORM.appendScale(zoom, zoom);

        drawGrid();

        return image;
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