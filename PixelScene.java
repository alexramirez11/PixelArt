import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Scanner;
import javax.imageio.ImageIO;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Screen;

/**
 * This class represents the scene that holds everything for the lite brite together. This class extends the Scene class.
 */
public class PixelScene extends Scene {

    private final double MIN_CELL_SIZE = 20;

    private static BorderPane root;
    private final Color FONT_COLOR = Color.WHITE;
    private Label startMes, buildMes, canvasName, saveMessage;
    private Button startButton, createButton, saveAs, save, loadButton, discardButton, change, toggleGrid;
    private VBox startBox, settingsBox, sideMenu, backBoard;
    private ComboBox<String> colorsBox;
    private TextField widthText, heightText;
    private ColorPicker picker;
    private PixelPane pixelPane;
    private ScrollPane scrollPane;
    private LinkedList<Button> loadList;
    private Image bucketOnImage = new Image("Saved_Images/bucket-on.png");
    private Image bucketOffImage = new Image("saved_Images/bucket-off.png");
    private ImageView bucketView = new ImageView(bucketOffImage);
    
    /**
     * This constructor is used to create an instance of the pixel board.
     * @return A PixelScene object to run the program
     */
    public static Scene initPixelScene() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: black");
        return new PixelScene(root);
    }

    private PixelScene(BorderPane root) {
        super(root, (Screen.getPrimary().getBounds().getWidth() - 100), (Screen.getPrimary().getBounds().getHeight() - 100), Color.BLACK);

        initStartMenu();
    }

    /**
     * This method initializes the start menu when the application is executed.
     */
    private void initStartMenu() {
        startMes = new Label("Pixel Art");
        startMes.setAlignment(Pos.CENTER);
        startMes.setTextAlignment(TextAlignment.CENTER);
        startMes.setTextFill(FONT_COLOR);
        startMes.setMaxWidth(Double.MAX_VALUE);
        startMes.setFont(new Font(100));
        startButton = new Button("Start Project");
        startButton.setStyle("-fx-background-color: purple");
        startButton.setTextFill(FONT_COLOR);
        startButton.setAlignment(Pos.CENTER);
        startButton.setOnAction(this::processStart);
        startButton.setFont(new Font(60));

        startButton.maxWidthProperty().bind(root.widthProperty().multiply(0.6));

        startBox = new VBox(startMes, startButton);
        startBox.setAlignment(Pos.CENTER);
        startBox.setSpacing(100);
        VBox.setVgrow(startMes, Priority.ALWAYS);
        VBox.setVgrow(startButton, Priority.NEVER);
        root.setCenter(startBox);
        root.setRight(null);
    }

    /**
     * This method initializes the build menu for where the user configures their canvas' settings
     */
    private void initBuildMenu() {
        buildMes = new Label("Configure your canvas settings");
        buildMes.setAlignment(Pos.CENTER);
        buildMes.setTextAlignment(TextAlignment.CENTER);
        buildMes.setFont(new Font(50));
        buildMes.setMaxWidth(Double.MAX_VALUE);
        buildMes.setTextFill(FONT_COLOR);
        GridPane pane = new GridPane();
        Label wLabel = new Label("Columns: ");
        wLabel.setTextFill(FONT_COLOR);
        wLabel.setFont(new Font(30));
        Label hLabel = new Label("Rows: ");
        hLabel.setTextFill(FONT_COLOR);
        hLabel.setFont(new Font(30));
        widthText = new TextField();
        widthText.setStyle("-fx-background-color: darkorange");
        heightText = new TextField();
        heightText.setStyle("-fx-background-color: darkorange");
        Label colorLabel = new Label("Color: ");
        colorLabel.setTextFill(FONT_COLOR);
        colorLabel.setPrefSize(50, 50);
        colorsBox = new ComboBox<String>();
        colorsBox.getItems().addAll("black", "white", "orange", "red", "blue", "cyan", "yellow", "pink", "purple", "green", "lightgreen", "grey", "dimgrey");
        pane.add(wLabel, 0, 0);
        pane.add(hLabel, 0, 1);
        pane.add(widthText, 1, 0);
        pane.add(heightText, 1, 1);
        pane.add(colorLabel, 0, 3);
        pane.add(colorsBox, 1, 3);
        pane.setVgap(20);
        pane.setAlignment(Pos.CENTER);

        loadButton = new Button("Load Existing Canvas");
        loadButton.setStyle("-fx-background-color: orange");
        loadButton.setAlignment(Pos.CENTER);
        loadButton.setTextFill(FONT_COLOR);
        loadButton.setMaxWidth(800);
        loadButton.setPrefHeight(100);
        loadButton.setOnAction(this::loadSettings);
        loadButton.setFont(new Font(60));

        createButton = new Button("Create Canvas");
        createButton.setStyle("-fx-background-color: purple");
        createButton.setAlignment(Pos.CENTER);
        createButton.setTextFill(FONT_COLOR);
        createButton.setMaxWidth(800);
        createButton.setPrefHeight(100);
        createButton.setOnAction(this::processBuild);
        createButton.setFont(new Font(60));

        settingsBox = new VBox(buildMes, pane, createButton, loadButton);
        settingsBox.setAlignment(Pos.CENTER);
        settingsBox.setSpacing(30);
        VBox.setVgrow(buildMes, Priority.NEVER);
        VBox.setVgrow(pane, Priority.NEVER);
        VBox.setVgrow(buildMes, Priority.NEVER);
        VBox.setVgrow(buildMes, Priority.NEVER);
        root.setCenter(settingsBox);
        root.setRight(null);
    }

    /**
     * This method initializes the grid of the lite brite.
     */
    private void initGrid() {
        picker = new ColorPicker();
        int height = 0, width = 0;
        try {
            height = Integer.parseInt(heightText.getText());
            width = Integer.parseInt(widthText.getText());
        } catch (NumberFormatException exception) {
            height = 10;
            width = 10;
        }
        
        pixelPane = new PixelPane(height, width, colorsBox.getValue(), picker);
        initTools(pixelPane);
    }

    private void processChangeOnBackBoard(ActionEvent e) {
        if (colorsBox.getValue() == null) {
            colorsBox.setValue("black");
        }
        String col = colorsBox.getValue().toLowerCase().trim();
        pixelPane.setBackgroundColor(col);
        pixelPane.drawGrid();
    }

    private void processDiscard(ActionEvent e) {
        initBuildMenu();
    }

    /**
     * This method takes the user to the build menu after clicking the Start Project button.
     * @param e ActionEvent object to activate this method when a button is clicked
     */
    private void processStart(ActionEvent e) {
        initBuildMenu();
    }

    /**
     * This method takes the user to the actual lite brite screen after finishing their settings and hittin the Create Canvas button.
     * @param e ActionEvent object to activate this method when a button is clicked
     */
    private void processBuild(ActionEvent e) {
        // root.getChildren().clear();
        initGrid();
    }

    /**
     * This method opens a TextInputDialog box to save the current canvas, after the user hits the save button.
     * @param e ActionEvent object to activate this method when a button is clicked
     */
    private void saveSettings(ActionEvent e) {
        TextInputDialog saveDialog = new TextInputDialog();
        saveDialog.setTitle("Save Image");
        saveDialog.setHeaderText("Save Image As: ");
        saveDialog.setContentText("Image Name: ");
        Optional<String> save = saveDialog.showAndWait();
        if (save.isPresent()) {
            processSave(save.get());
        }
    }

    private void saveNoExit(ActionEvent e) {
        processSave(pixelPane + "");
    }

    /**
     * This method opens a TextInputDialog box to load an exsisting canvas from the Saved_Images_Data folder that should be in this directory. 
     * This happens once the user clickes on the load existing canvas button.
     * @param e ActionEvent object to activate this method when a button is clicked
     */
    private void loadSettings(ActionEvent e) {
        Label loadLabel = new Label("Select the Canvas you Wish to Edit");
        loadLabel.setAlignment(Pos.CENTER);
        loadLabel.setTextAlignment(TextAlignment.CENTER);
        loadLabel.setFont(new Font(50));
        loadLabel.setTextFill(FONT_COLOR);
        VBox loadFiles = new VBox();
        File dir = new File("Saved_Images_Data");
        loadList = new LinkedList<>();
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    String img = "Saved_Images/" + f.getName().replace("txt", "png");
                    Button selectable = new Button(img);
                    try {
                        Image image = new Image(img);
                        BackgroundImage bg_img = new BackgroundImage(image, BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT, BackgroundPosition.CENTER, BackgroundSize.DEFAULT);
                        Background bg = new Background(bg_img);
                        
                        selectable.setBackground(bg);
                    } catch (IllegalArgumentException exception) {
                        selectable.setStyle("-fx-background-color: black");
                    }
                    selectable.minWidthProperty().bind(root.widthProperty().multiply(0.6));
                    selectable.minHeightProperty().bind(root.heightProperty().multiply(0.8));
                    selectable.setTextFill(Color.WHITE);
                    selectable.setFont(new Font(50));
                    selectable.setBorder(new Border(new BorderStroke(Color.PURPLE, BorderStrokeStyle.DASHED, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
                    selectable.setOnAction(this::processLoadClick);
                    loadList.add(selectable);
                }
            } else {
                System.err.println("Directory containing each saved image's data could not be found.\nProgram shutting down...");
                System.exit(1);
            }
        } else {
            System.err.println("Directory containing each saved image's data could not be found.\nProgram shutting down...");
            System.exit(1);
        }
        loadFiles.getChildren().addAll(loadList);
        loadFiles.setStyle("-fx-background-color: black");
        loadFiles.setSpacing(20);
        loadFiles.setAlignment(Pos.CENTER);

        ScrollPane sp = new ScrollPane(loadFiles);
        sp.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        sp.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
        sp.setFitToWidth(true);
        sp.setFitToHeight(true);

        root.setCenter(sp);
        root.setRight(null);
    }

    private void processLoadClick(ActionEvent event) {
        String name = "";
        for (Button b : loadList) {
            if (event.getSource() == b) {
                String[] splitPath = b.getText().split("/");
                name = splitPath[1].replace(".png", "");
                break;
            }
        }
        processLoad(name);
    }

    /**
     * This method loads the specifed existing canvas and rebuilds it into an instance of a LBPane so that the user can edit the image.
     * @param fileName String of the name of the file of the existing image
     */
    private void processLoad(String fileName) {
        File input = new File("Saved_Images_Data", fileName + ".txt");
        picker = new ColorPicker();
        try {
            Scanner scan = new Scanner(input);
            LinkedList<String> metaData = new LinkedList<String>();
            while (scan.hasNextLine()) {
                metaData.add(scan.nextLine());
            }
            scan.close();
            pixelPane = new PixelPane(metaData, picker, fileName);
            initTools(pixelPane);
        } catch (FileNotFoundException e) {
            System.err.println("FILE \"" + fileName + "\" DOES NOT EXIST. PLEASE ENSURE THE GIVEN FILE NAME IS CORRECT\nProgram shutting down...\n");
            System.exit(1);
        }
    }

    private void processBucketClick(MouseEvent e) {
        pixelPane.toggleBucket();
        if (pixelPane.getBucketMode()) {
            bucketView.setImage(bucketOnImage);
        } else {
            bucketView.setImage(bucketOffImage);
        }
    }

    private void processGridToggle(ActionEvent e) {
        pixelPane.toggleGridLines();
    }

    /**
     * This method saves the current lite brite pane open. A png screenshot of the PixelPane is saved in the Saved_Images folder
     * and the metadata for it is saved in the Saved_Images_Data folder, both of which should be in this directory.
     * @param name String representation of the name of the PixelPane to save
     */
    private void processSave(String name) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter =
        DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' h:mm a");
        pixelPane.setLastSavedTime(now.format(formatter));
        saveMessage.setText("Last saved on:\n" + pixelPane.getLastSavedTime());

        WritableImage image = new WritableImage((int)pixelPane.getWidth(), (int)pixelPane.getHeight());
        pixelPane.snapshot(null, image);
        File output = new File("Saved_Images", name + ".png");
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", output);
            // System.out.println("Screenshot saved to " + output.getAbsolutePath()); for debugging
        } catch (IOException exception) {
            System.err.println("Error: Failed to save screenshot: " + exception.getMessage());
            System.exit(1);
        }
        File boardData = new File("Saved_Images_Data", name + ".txt");
        try {
            FileWriter writer = new FileWriter(boardData);
            Color[][] board = pixelPane.getGridColors();
            writer.write(pixelPane.getLastSavedTime() + "\n");
            writer.write(pixelPane.getGridLineColor() + "\n");
            writer.write(pixelPane.getRows() + "/" + pixelPane.getColumns() + "\n");
            for (int i = 0; i < board.length; i++) {
                for (int j = 0; j < board[i].length; j++) {
                    writer.write(i + "//" + j + "//" + colorToString(board[i][j]) + "\n");
                }
            }
            writer.close();
            pixelPane.setName(name);
            canvasName.setText("Canvas Name:\n" + pixelPane);
            
        } catch (IOException e) {
            System.err.println("Error: Failed to save Image Data: " + e.getMessage());
            System.exit(1); // TODO: eventually make a screen for failed saves and troubleshooting methods!
        }
    }

    private String colorToString(Color c) {
        return String.format("#%02X%02X%02X", (int)(c.getRed()*255), (int)(c.getGreen()*255), (int)(c.getBlue()*255));
    }

    /**
     * This method initializes all of the tools that work on the given PixelPane object. It also initializes all of the
     * information about the pane and displays it.
     * @param pane The PixelPane to initialize tools for
     */
    private void initTools(PixelPane pane) {
        pixelPane.setPrefWidth(pixelPane.getColumns() * MIN_CELL_SIZE);
        pixelPane.setPrefHeight(pixelPane.getRows() * MIN_CELL_SIZE);
        // Label rowLabel = new Label("Rows: " + pane.getRows());
        // rowLabel.setTextFill(FONT_COLOR);
        // Label colLabel = new Label("Columns: " + pane.getColumns());
        // colLabel.setTextFill(FONT_COLOR);
        bucketView.setPreserveRatio(false);
        bucketView.setFitWidth(80);
        bucketView.setFitHeight(80);
        bucketView.setOnMouseClicked(this::processBucketClick);
        bucketView.setCursor(Cursor.HAND);
        canvasName = new Label("Canvas Name:\n" + pane);
        canvasName.setTextFill(FONT_COLOR);
        saveAs = new Button("Save As");
        saveAs.setStyle("-fx-background-color: purple");
        saveAs.setTextFill(FONT_COLOR);
        saveAs.setOnAction(this::saveSettings);
        saveMessage = new Label("Last saved on:\n" + pixelPane.getLastSavedTime());
        saveMessage.setTextFill(Color.LIMEGREEN);
        save = new Button("Save");
        save.setStyle("-fx-background-color: purple");
        save.setTextFill(FONT_COLOR);
        save.setOnAction(this::saveNoExit);

        discardButton = new Button("Exit");
        discardButton.setStyle("-fx-background-color: darkorange");
        discardButton.setTextFill(FONT_COLOR);
        discardButton.setOnAction(this::processDiscard);
        change = new Button("Change\nGridlines");
        change.setMinSize(40, 20);
        change.setStyle("-fx-background-color: blue");
        change.setTextFill(Color.WHITE);
        change.setOnAction(this::processChangeOnBackBoard);
        change.setCursor(Cursor.HAND);
        toggleGrid = new Button("Toggle Gridlines");
        toggleGrid.setMinSize(40, 20);
        toggleGrid.setStyle("-fx-background-color: blue");
        toggleGrid.setTextFill(Color.WHITE);
        toggleGrid.setOnAction(this::processGridToggle);
        toggleGrid.setCursor(Cursor.HAND);


        saveAs.prefHeightProperty().bind(root.heightProperty().multiply(0.05));

        save.prefWidthProperty().bind(saveAs.prefWidthProperty());
        save.prefHeightProperty().bind(saveAs.prefHeightProperty());

        discardButton.prefWidthProperty().bind(saveAs.prefWidthProperty());
        discardButton.prefHeightProperty().bind(saveAs.prefHeightProperty());

        change.prefWidthProperty().bind(saveAs.prefWidthProperty());
        change.prefHeightProperty().bind(saveAs.prefHeightProperty());

        backBoard = new VBox(pane);
        backBoard.setAlignment(Pos.CENTER);
        backBoard.setStyle(colorToString(pane.getGridLineColor()));
        scrollPane = new ScrollPane(backBoard);
        pane.prefWidthProperty().bind(scrollPane.widthProperty());
        pane.prefHeightProperty().bind(scrollPane.heightProperty());
        backBoard.prefWidthProperty().bind(scrollPane.widthProperty());
        backBoard.prefHeightProperty().bind(scrollPane.heightProperty());

        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background-color: transparent");

        sideMenu = new VBox(picker/*, rowLabel, colLabel*/, saveAs, save, discardButton, colorsBox, change, toggleGrid, bucketView, canvasName, saveMessage);
        picker.prefWidthProperty().bind(sideMenu.widthProperty().multiply(1));
        picker.prefHeightProperty().bind(sideMenu.heightProperty().multiply(0.08));
        saveAs.prefWidthProperty().bind(sideMenu.widthProperty().multiply(0.8));
        VBox.setVgrow(sideMenu, Priority.ALWAYS);
        sideMenu.setSpacing(30);
        sideMenu.setFillWidth(true);
        sideMenu.setAlignment(Pos.TOP_CENTER);

        root.setCenter(scrollPane);
        root.setRight(sideMenu);
    }
}
