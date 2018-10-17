package Controller;

import GameEngine.*;
import GameEngine.utils.Point;
import View.Screen;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static Controller.Config.GRID_SIZE;

public class DesignScreenController extends Controller {
    @FXML
    private ListView<ImageView> objectsListView;

    @FXML
    private AnchorPane dungeonPane;

    @FXML
    private TextField mapNameTextField;

    @FXML
    private TextField mapAuthorTextField;

    @FXML
    private TextField mapRowSizeTextField;

    @FXML
    private TextField mapColSizeTextField;

    @FXML
    private TextField filterTextField;

    private ResourceManager resources;

    private Set<KeyCode> keyPressed;
    /**
     * Classname of the current dragging object
     */
    private StringProperty draggingClass;
    private ObjectProperty<GameObject> draggingObject;
    private int maxRow;
    private int maxCol;
    private List<ImageView> allObjectImgViews;

    private MapBuilder mapBuilder;

    public DesignScreenController(Stage s) {
        super(s);
        resources = new ResourceManager();
        draggingClass = new SimpleStringProperty();
        draggingObject = new SimpleObjectProperty<>();
        keyPressed = new HashSet<>();
        allObjectImgViews = new LinkedList<>();
        // set the dungeon gird pane to 11 x 11 by default
        maxCol = 11;
        maxRow = 11;
    }

    @Override
    public void afterInitialize() {
        stage.getScene().setOnKeyPressed(event -> keyPressed.add(event.getCode()));
        stage.getScene().setOnKeyReleased(event -> keyPressed.remove(event.getCode()));
    }

    @FXML
    public void initialize(){
        mapBuilder = new MapBuilder(maxCol, maxRow);

        dungeonPane.setMaxWidth(maxCol * GRID_SIZE);
        dungeonPane.setMaxHeight(maxRow * GRID_SIZE);

        // clear anything on dungeon pane
        dungeonPane.getChildren().clear();

        // set map size text fields to current size
        mapColSizeTextField.setText(Integer.toString(maxCol));
        mapRowSizeTextField.setText(Integer.toString(maxRow));

        resources.drawGridLine(dungeonPane.getChildren(), maxCol, maxRow);

        initWallBoundary();

        // clear list view first
        objectsListView.getItems().clear();
        allObjectImgViews.clear();

        for (String cls : resources.getAllClassNames()) {
            ImageView imageView = resources.createImageViewByClassName(cls);
            imageView.setId(cls);
            allObjectImgViews.add(imageView);
            objectsListView.getItems().add(imageView);
        }

        objectsListView.setCellFactory(this::objectListViewCellFactory);
        filterTextField.textProperty().addListener(this::handleFilterChange);

        dungeonPane.setOnDragOver(this::handleDragOver);
        dungeonPane.setOnDragDropped(this::handleDragDropped);

        // resize the stage to fit the scene
        stage.sizeToScene();
    }

    @FXML
    public void onExitBtnClicked(){
        Screen cs = new Screen(this.getStage(), "Select Mode to Play", "View/ModeScreen.fxml");
        Controller controller = new ModeScreenController(this.getStage());
        cs.display(controller);
    }

    public void handleFilterChange(Observable obs) {
        String filter = filterTextField.getText();
        objectsListView.getItems().clear();
        if(filter.isEmpty()){
            objectsListView.getItems().addAll(allObjectImgViews);
        } else {
            objectsListView.getItems().addAll(
                    allObjectImgViews.stream()
                            .filter(imageView -> imageView.getId().toLowerCase().contains(filter.toLowerCase()))
                            .collect(Collectors.toList()));
        }
    }

    private void saveDungeonSnapshot(String mapName) throws IOException {
        File out = new File("map/" + mapName + ".png");
        WritableImage writableImage =
                new WritableImage((int)dungeonPane.getWidth(), (int)dungeonPane.getHeight());
        dungeonPane.snapshot(null, writableImage);
        ImageIO.write(SwingFXUtils.fromFXImage(writableImage, null), "png", out);
    }

    @FXML
    public void onSaveButtonClicked(MouseEvent event){
        new File("map").mkdirs();

        String mapName = mapNameTextField.getText().replaceAll("[^\\w\\-.]", "");
        String authorName = mapAuthorTextField.getText();
        mapBuilder.setAuthor(authorName);

        try {
            if(!mapBuilder.islegalMap())
                throw new Exception("Incomplete or illegal map");
            Map map = mapBuilder.build();
            map.serialize(new FileOutputStream("map/" + mapName + ".dungeon"));
            saveDungeonSnapshot(mapName);
            if(mapName.isEmpty())
                throw new Exception("Invalid map name");
        } catch (Exception e){
            new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK).showAndWait();
            return;
        }
        System.out.println("Map serialized");
        Screen cs = new Screen(this.getStage(), "Select Mode", "View/ModeScreen.fxml");
        Controller controller = new ModeScreenController(this.getStage());
        cs.display(controller);
    }

    @FXML
    public void onResizeMapButtonClicked(){
        try {
            maxCol = Integer.parseInt(mapColSizeTextField.getText());
            maxRow = Integer.parseInt(mapRowSizeTextField.getText());
            if(maxCol < 5 || maxRow < 5 || maxCol > 20 || maxRow > 20)
                throw new NumberFormatException();
        } catch (NumberFormatException e){
            new Alert(Alert.AlertType.ERROR, "Invalid map size!", ButtonType.OK).showAndWait();
            mapColSizeTextField.setText(Integer.toString(maxCol));
            mapRowSizeTextField.setText(Integer.toString(maxRow));
        }
        initialize();
    }

    /**
     * Update a given GameObject's position, and display it in dungeonPane
     *
     * @param enableInteraction whether to add handler to allow drag & delete
     */
    private void updateGameObject(GameObject obj, Point point, boolean enableInteraction){
        if(obj == null) return;

        // delete existing object at target location
        GameObject deleted = mapBuilder.deleteObject(point);
        if(deleted != null){
            dungeonPane.getChildren().remove(getNodeById(deleted.getObjID()));
        }

        mapBuilder.updateObjectLocation(obj, point);
        ImageView imageView = resources.createImageViewByGameObject(obj, dungeonPane.getChildren());

        if(enableInteraction) {
            imageView.setOnMouseClicked(this::handleRightClick);
            imageView.setOnDragDetected(e -> handleDragStart(e, imageView, obj, true));
        }
    }

    /**
     * Get the index of the cell in dungeonGridPane
     * by given absolute sceneX and sceneY
     * @return point index in grid pane
     */
    private Point getEventIndex(double sceneX, double sceneY){
        double paneX = sceneX - dungeonPane.getLayoutX();
        double paneY = sceneY - dungeonPane.getLayoutY();
        int row = (int)(paneY / GRID_SIZE);
        int col = (int)(paneX / GRID_SIZE);
        return new Point(col, row);
    }

    /**
     * Event handler for removing the object when right click it
     */
    private void handleRightClick(MouseEvent event){
        Point point = getEventIndex(event.getSceneX(), event.getSceneY());
        // right click to delete the image
        if(event.getButton() == MouseButton.SECONDARY){
            System.out.println("Right clicked");
            GameObject deleted = mapBuilder.deleteObject(point);
            if(deleted != null){
                dungeonPane.getChildren().remove(getNodeById(deleted.getObjID()));
            }
        }
    }

    private void handleDragStart(MouseEvent event, ImageView draggingNode, GameObject obj,
                                 boolean allowDeleteOriginal) {
        Point point = getEventIndex(event.getSceneX(), event.getSceneY());

        Dragboard db = draggingNode.startDragAndDrop(TransferMode.COPY);
        ClipboardContent content = new ClipboardContent();
        content.putImage(draggingNode.getImage());
        db.setContent(content);
        draggingObject.set(obj);
//        draggingClass.set(classname);
        if (allowDeleteOriginal && !(keyPressed.contains(KeyCode.SHIFT) || keyPressed.contains(KeyCode.CONTROL))) {
            draggingNode.setOnDragDone(e2 -> {
                mapBuilder.deleteObject(point);
            });
            dungeonPane.getChildren().remove(draggingNode);
        }
    }

    private void initWallBoundary(){
        String wallClassName = Wall.class.getSimpleName();
        GameObject wall = GameObject.build(wallClassName, null);
        for(int x = 0; x < maxCol; x++) {
            updateGameObject(wall.cloneObject(), new Point(x, 0), false);
            updateGameObject(wall.cloneObject(), new Point(x, maxRow - 1), false);
        }

        for(int y = 1; y < maxRow - 1; y++) {
            updateGameObject(wall.cloneObject(), new Point(0, y), false);
            updateGameObject(wall.cloneObject(), new Point(maxCol - 1, y), false);
        }
    }

    /**
     * Get a JavaFX Node by it's id which is the same
     * objId in the backend;
     * @param objId object id
     * @return node
     */
    private Node getNodeById(int objId){
        return dungeonPane.lookup("#" + Integer.toString(objId));
    }

    private void handleDragOver(DragEvent event) {
        // remove all previous indicator first
        dungeonPane.getChildren().removeIf(node -> node instanceof Rectangle);
        Dragboard db = event.getDragboard();

        Point point = getEventIndex(event.getSceneX(), event.getSceneY());

        if(db.hasImage() && draggingObject.get() != null &&
                point.getY() > 0 && point.getY() < maxRow - 1 &&
                point.getX() > 0 && point.getX() < maxCol - 1) {
            // continuous placing feature
            if(keyPressed.contains(KeyCode.CONTROL)) {
                updateGameObject(draggingObject.get().cloneObject(), point, true);
            } else {
                // visualise the current drop location
                Rectangle indicator = new Rectangle(GRID_SIZE, GRID_SIZE);
                indicator.setFill(new Color(0.5, 0.5, 1, 0.8));
                indicator.setTranslateX(point.getX() * GRID_SIZE);
                indicator.setTranslateY(point.getY() * GRID_SIZE);
                dungeonPane.getChildren().add(indicator);

                event.acceptTransferModes(TransferMode.COPY);
            }
        }
        event.consume();
    }

    private void handleDragDropped(DragEvent event){
        // remove all previous indicator first
        dungeonPane.getChildren().removeIf(node -> node instanceof Rectangle);

        Point point = getEventIndex(event.getSceneX(), event.getSceneY());

        GameObject newObj =
                // duplicate dragging object
                keyPressed.contains(KeyCode.SHIFT) || keyPressed.contains(KeyCode.CONTROL) ?
                        draggingObject.get().cloneObject() :
                        // move original object to here
                        draggingObject.get();

        updateGameObject(newObj, point, true);
        // auto create pair if it's pairable
        if(newObj instanceof Pairable && mapBuilder.getEmptyPoint() != null){
            Pairable pairable = (Pairable) newObj;
            if(pairable.getPair() == null){
                    GameObject pair =
                            GameObject.build(pairable.getPairingObjectClassName(), null);
                    pairable.setPair(pair);
                    updateGameObject(pair, mapBuilder.getEmptyPoint(), true);
            }
        }

        event.consume();
    }

    private ListCell<ImageView> objectListViewCellFactory(ListView<ImageView> param){
        ListCell<ImageView> cell = new ListCell<ImageView>() {
            @Override
            public void updateItem(ImageView imgView, boolean empty) {
                super.updateItem(imgView, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(imgView.getId());
                    setGraphic(imgView);
                }
            }
        };
        // event when dragging cell in this ListView
        cell.setOnDragDetected(event -> {
            GameObject newObj = GameObject.build(cell.getText(), null);
            handleDragStart(event, (ImageView) cell.getGraphic(), newObj, false);
        });
        return cell;
    }

    @FXML
    public void onObjectListViewClicked(MouseEvent event){
        // double click to add current selected object to the map
        if(event.getClickCount() == 2){
            ImageView currSelected = objectsListView.getSelectionModel().getSelectedItem();
            if(currSelected != null && mapBuilder.getEmptyPoint() != null) {
                GameObject obj = GameObject.build(currSelected.getId(), mapBuilder.getEmptyPoint());
                updateGameObject(obj, obj.getLocation(), true);
            }
        }
    }

}
