package net.highwayfrogs.editor.gui.editor;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.SneakyThrows;
import net.highwayfrogs.editor.Constants;
import net.highwayfrogs.editor.Utils;
import net.highwayfrogs.editor.file.GameFile;
import net.highwayfrogs.editor.file.map.MAPFile;
import net.highwayfrogs.editor.file.map.entity.Entity;
import net.highwayfrogs.editor.file.map.path.Path;
import net.highwayfrogs.editor.file.map.path.PathInfo;
import net.highwayfrogs.editor.file.map.view.MapMesh;
import net.highwayfrogs.editor.file.map.view.TextureMap;
import net.highwayfrogs.editor.file.standard.SVector;
import net.highwayfrogs.editor.file.standard.psx.PSXMatrix;
import net.highwayfrogs.editor.file.standard.psx.prims.polygon.PSXPolyTexture;
import net.highwayfrogs.editor.file.standard.psx.prims.polygon.PSXPolygon;
import net.highwayfrogs.editor.file.vlo.ImageFilterSettings;
import net.highwayfrogs.editor.file.vlo.ImageFilterSettings.ImageState;
import net.highwayfrogs.editor.gui.GUIMain;
import net.highwayfrogs.editor.gui.InputMenu;
import net.highwayfrogs.editor.gui.SelectionMenu;
import net.highwayfrogs.editor.system.Tuple2;

import java.util.List;

/**
 * Sets up the map editor.
 * TODO: Icon for entity
 * TODO: Icon for lighting.
 * TODO: Icon for animations, if we're unable to make them render in our tool.
 * TODO: Grid mode,
 * TODO: Edit Vertexes
 * TODO: Edit polygons
 * TODO: Show Paths.
 * TODO: Zones.
 * Created by Kneesnap on 11/22/2018.
 */
@Getter
public class MAPController extends EditorController<MAPFile> {
    @FXML private Label themeIdLabel;
    @FXML private Label startPosLabel;
    @FXML private Label cameraSourceLabel;
    @FXML private Label cameraTargetLabel;
    @FXML private Label basePointLabel; // This is the bottom left of the map group grid.

    @FXML private Label pathCountLabel;
    @FXML private Label formCountLabel;
    @FXML private Label entityCountLabel;

    @FXML private Label gridSquareLabel;
    @FXML private Label gridStackLabel;
    @FXML private Label gridCountLabel;
    @FXML private Label gridLengthLabel;
    @FXML private Label groupLabel;
    @FXML private Label groupCountLabel;
    @FXML private Label groupLengthLabel;

    @FXML private Label zoneCountLabel;
    @FXML private Label lightCountLabel;
    @FXML private Label vertexCountLabel;
    @FXML private Label polygonCountLabel;
    @FXML private Label mapAnimCountLabel;

    private double oldMouseX;
    private double oldMouseY;
    private double mouseX;
    private double mouseY;

    private MapMesh mapMesh;
    private PSXPolygon selectedPolygon;
    private PSXPolygon polygonImmuneToTarget;
    private boolean polygonSelected;

    private PerspectiveCamera camera;
    private Group uiGroup = new Group();

    private static final double ROTATION_SPEED = 0.35D;
    private static final double SCROLL_SPEED = 5;
    private static final double TRANSLATE_SPEED = 10;
    private static final int VERTEX_SPEED = 3;

    private static final ImageFilterSettings IMAGE_SETTINGS = new ImageFilterSettings(ImageState.EXPORT);
    private static final Image LIGHT_BULB = GameFile.loadIcon("lightbulb");
    private static final Image SWAMPY = GameFile.loadIcon("swampy");

    @Override
    public void onInit(AnchorPane editorRoot) {
        super.onInit(editorRoot);
        updateLabels();
    }

    private void updateLabels() {
        MAPFile map = getFile();

        themeIdLabel.setText("Theme: " + map.getTheme());
        startPosLabel.setText("Start Pos: (" + map.getStartXTile() + ", " + map.getStartYTile() + ") Rotation: " + map.getStartRotation());
        cameraSourceLabel.setText("Camera Source: (" + map.getCameraSourceOffset().toCoordinateString() + ")");
        cameraTargetLabel.setText("Camera Target: (" + map.getCameraTargetOffset().toCoordinateString() + ")");
        basePointLabel.setText("Base Point: (" + map.getBasePoint().toCoordinateString() + ")");

        // Labels in entity section.
        pathCountLabel.setText("Paths: " + map.getPaths().size());
        formCountLabel.setText("Forms: " + map.getForms().size());
        entityCountLabel.setText("Entities: " + map.getEntities().size());

        // Labels in environment section.
        zoneCountLabel.setText("Zones: " + map.getZones().size());
        lightCountLabel.setText("Lights: " + map.getLights().size());
        vertexCountLabel.setText("Vertices: " + map.getVertexes().size());
        polygonCountLabel.setText("Polygons: " + map.getCachedPolygons().values().stream().mapToInt(List::size).sum());
        mapAnimCountLabel.setText("Animations: " + map.getMapAnimations().size());

        // Grid
        gridStackLabel.setText("Grid Stacks: " + map.getGridStacks().size());
        gridSquareLabel.setText("Grid Squares: " + map.getGridSquares().size());
        gridCountLabel.setText("Count: [" + map.getGridXCount() + ", " + map.getGridZCount() + "]");
        gridLengthLabel.setText("Length: [" + map.getGridXLength() + ", " + map.getGridZLength() + "]");

        // Group
        groupLabel.setText("Groups: " + map.getGroups().size());
        groupCountLabel.setText("Count: [" + map.getGroupXCount() + ", " + map.getGroupZCount() + "]");
        groupLengthLabel.setText("Length: [" + map.getGroupXLength() + ", " + map.getGroupZLength() + "]");
    }

    @FXML
    private void onMapButtonClicked(ActionEvent event) {
        getFile().getParentMWD().promptVLOSelection(getFile().getTheme(), vlo -> {
            TextureMap textureMap = TextureMap.newTextureMap(getFile(), vlo, getMWIEntry().getDisplayName());
            setupMapViewer(GUIMain.MAIN_STAGE, new MapMesh(getFile(), textureMap), textureMap);
        }, false);
    }

    @FXML
    private void onRemapButtonClicked(ActionEvent event) {
        getFile().getParentMWD().promptVLOSelection(getFile().getTheme(), vlo -> {
            TextureMap texMap = TextureMap.newTextureMap(getFile(), vlo, null);

            InputMenu.promptInput("Please enter the address to start reading from.", str -> {
                int address;

                String levelName = Utils.getRawFileName(str);
                if (GUIMain.EXE_CONFIG.hasRemapInfo(levelName)) {
                    Tuple2<Integer, Integer> remapData = GUIMain.EXE_CONFIG.getRemapInfo(levelName);
                    address = remapData.getA() + (Constants.SHORT_SIZE * remapData.getB());
                } else {
                    try {
                        address = Integer.decode(str);
                    } catch (Exception ex) {
                        System.out.println(str + " is not formatted properly.");
                        return;
                    }
                }

                setupMapViewer(GUIMain.MAIN_STAGE, new MapMesh(getFile(), texMap, address, vlo.getImages().size()), texMap);
            });

        }, false);
    }


    @SneakyThrows
    private void setupMapViewer(Stage stageToOverride, MapMesh mesh, TextureMap texMap) {
        delete2DUI();
        this.mapMesh = mesh;
        MeshView meshView = new MeshView(mesh);

        PhongMaterial material = new PhongMaterial();
        material.setDiffuseMap(SwingFXUtils.toFXImage(texMap.getImage(), null));
        meshView.setMaterial(material);
        meshView.setCullFace(CullFace.NONE);

        this.camera = new PerspectiveCamera(true);
        camera.setFarClip(Double.MAX_VALUE);
        camera.setTranslateZ(-Constants.MAP_VIEW_SCALE);
        //camera.setTranslateY(-Constants.MAP_VIEW_SCALE / 7);

        Group cameraGroup = new Group();
        cameraGroup.getChildren().add(meshView);
        cameraGroup.getChildren().add(uiGroup);
        cameraGroup.getChildren().add(camera);

        Rotate rotX = new Rotate(0, Rotate.X_AXIS);
        Rotate rotY = new Rotate(0, Rotate.Y_AXIS);
        meshView.getTransforms().addAll(rotX, rotY);

        setupLights(cameraGroup, rotX, rotY);
        setupEntities(cameraGroup, rotX, rotY);

        Scene mapScene = new Scene(cameraGroup, 400, 400, true);
        mapScene.setFill(Color.GRAY);
        mapScene.setCamera(camera);

        Scene defaultScene = Utils.setSceneKeepPosition(stageToOverride, mapScene);

        mapScene.setOnKeyPressed(event -> {
            // Exit the viewer.
            if (event.getCode() == KeyCode.ESCAPE) {
                if (isPolygonSelected()) {
                    removeCursorPolygon();
                    return;
                }

                Utils.setSceneKeepPosition(stageToOverride, defaultScene);
            }

            // Toggle wireframe mode.
            if (event.getCode() == KeyCode.X)
                meshView.setDrawMode(meshView.getDrawMode() == DrawMode.FILL ? DrawMode.LINE : DrawMode.FILL);

            // [Remap Mode] Find next non-crashing remap.
            if (mesh.isRemapFinder() && event.getCode() == KeyCode.K) {
                if (!isPolygonSelected()) {
                    System.out.println("You must select a polygon to perform a remap search.");
                    return;
                }

                PSXPolygon poly = this.selectedPolygon;
                if (!(poly instanceof PSXPolyTexture)) {
                    System.out.println("This polygon is not textured.");
                    return;
                }

                int replaceTexId = ((PSXPolyTexture) poly).getTextureId();
                SelectionMenu.promptSelection("Select the replacement image.",
                        image -> mesh.findNextValidRemap(replaceTexId, image.getTextureId(), true),
                        mesh.getTextureMap().getVloArchive().getImages(),
                        image -> String.valueOf(image.getTextureId()),
                        image -> SelectionMenu.makeIcon(image.toBufferedImage(IMAGE_SETTINGS)));
            }

            if (isPolygonSelected()) {
                if (event.getCode() == KeyCode.UP) {
                    movePolygonY(VERTEX_SPEED);
                } else if (event.getCode() == KeyCode.DOWN) {
                    movePolygonY(-VERTEX_SPEED);
                } else if (event.getCode() == KeyCode.LEFT) {
                    movePolygonX(-VERTEX_SPEED);
                } else if (event.getCode() == KeyCode.RIGHT) {
                    movePolygonX(VERTEX_SPEED);
                }
            }
        });

        mapScene.setOnScroll(evt -> camera.setTranslateZ(camera.getTranslateZ() + (evt.getDeltaY() * SCROLL_SPEED)));

        mapScene.setOnMousePressed(e -> {
            mouseX = oldMouseX = e.getSceneX();
            mouseY = oldMouseY = e.getSceneY();

            if (!isPolygonSelected())
                hideCursorPolygon();
        });

        mapScene.setOnMouseReleased(evt -> {
            hideCursorPolygon();
            renderCursor(getSelectedPolygon());
        });

        mapScene.setOnMouseDragged(e -> {
            oldMouseX = mouseX;
            oldMouseY = mouseY;
            mouseX = e.getSceneX();
            mouseY = e.getSceneY();
            double mouseXDelta = (mouseX - oldMouseX);
            double mouseYDelta = (mouseY - oldMouseY);

            if (e.isPrimaryButtonDown()) {
                rotX.setAngle(rotX.getAngle() + (mouseYDelta * ROTATION_SPEED)); // Rotate the object.
                rotY.setAngle(rotY.getAngle() - (mouseXDelta * ROTATION_SPEED));
            } else if (e.isMiddleButtonDown()) {
                camera.setTranslateX(camera.getTranslateX() - (mouseXDelta * TRANSLATE_SPEED)); // Move the camera.
                camera.setTranslateY(camera.getTranslateY() - (mouseYDelta * TRANSLATE_SPEED));
            }
        });

        mapScene.setOnMouseMoved(evt -> {
            if (!isPolygonSelected())
                setCursorPolygon(mesh.getFacePolyMap().get(evt.getPickResult().getIntersectedFace()));
        });

        mapScene.setOnMouseClicked(evt -> {
            PSXPolygon clickedPoly = getMapMesh().getFacePolyMap().get(evt.getPickResult().getIntersectedFace());

            if (getSelectedPolygon() != null && (getSelectedPolygon() == clickedPoly)) {
                if (isPolygonSelected()) {
                    this.polygonImmuneToTarget = getSelectedPolygon();
                    removeCursorPolygon();
                } else {
                    setCursorPolygon(clickedPoly);
                    this.polygonSelected = true;
                }
            }
        });

        mesh.findNextValidRemap(0, 0, false);
    }

    private void setupLights(Group cameraGroup, Rotate rotX, Rotate rotY) {
        /*ImagePattern pattern = new ImagePattern(LIGHT_BULB);

        for (Light light : getFile().getLights()) {
            SVector position = light.getPosition();
            makeIcon(cameraGroup, pattern, rotX, rotY, Utils.unsignedShortToFloat(position.getX()), Utils.unsignedShortToFloat(position.getY()), Utils.unsignedShortToFloat(position.getZ()));
        }*/
    }

    private void setupEntities(Group cameraGroup, Rotate rotX, Rotate rotY) {
        ImagePattern pattern = new ImagePattern(SWAMPY);

        for (Entity entity : getFile().getEntities()) {
            PSXMatrix matrix = entity.getMatrixInfo();
            if (matrix != null) {
                int[] pos = matrix.getTransform();
                float x = Utils.unsignedIntToFloat(pos[0]);
                float y = Utils.unsignedIntToFloat(pos[1]);
                float z = Utils.unsignedIntToFloat(pos[2]);

                Rectangle rect = makeIcon(cameraGroup, pattern, rotX, rotY, x, y, z);
                rect.setOnMouseClicked(evt -> {
                    System.out.println("Hello, I am a " + entity.getFormBook());

                    System.out.println("Base: [" + evt.getX() + ", " + evt.getY() + ", " + evt.getZ() + "]");
                    System.out.println("Scene: [" + evt.getSceneX() + ", " + evt.getSceneY() + "]");
                    System.out.println("Screen: [" + evt.getScreenX() + ", " + evt.getScreenY() + "]");

                    buildUI(evt.getSceneX(), evt.getSceneY());
                });
            }

            PathInfo pathInfo = entity.getPathInfo();
            if (pathInfo != null) {
                Path path = getFile().getPaths().get(pathInfo.getPathId());
                SVector end = path.evaluatePosition(pathInfo);

                float x = Utils.unsignedShortToFloat(end.getX());
                float y = Utils.unsignedShortToFloat(end.getY());
                float z = Utils.unsignedShortToFloat(end.getZ());

                Rectangle rect = makeIcon(cameraGroup, pattern, rotX, rotY, x, y, z);
                rect.setOnMouseClicked(evt -> System.out.println("Hello, I am a " + entity.getFormBook()));
            }
        }
    }

    private Rectangle makeIcon(Group cameraGroup, ImagePattern image, Rotate rotX, Rotate rotY, float x, float y, float z) {
        double width = image.getImage().getWidth();
        double height = image.getImage().getHeight();
        Rectangle rect = new Rectangle(width, height);

        rect.setTranslateX((Constants.MAP_VIEW_SCALE * x) - width);
        rect.setTranslateY((Constants.MAP_VIEW_SCALE * y) - height);
        rect.setTranslateZ((Constants.MAP_VIEW_SCALE * z));
        rect.setFill(image);

        Rotate lightRotateX = new Rotate(0, Rotate.X_AXIS); // Up, Down,
        Rotate lightRotateY = new Rotate(0, Rotate.Y_AXIS); // Left, Right
        lightRotateX.angleProperty().bind(rotX.angleProperty());
        lightRotateY.angleProperty().bind(rotY.angleProperty());

        lightRotateX.setPivotY(-rect.getTranslateY());
        lightRotateX.setPivotZ(-rect.getTranslateZ()); // Depth <Closest, Furthest>
        lightRotateY.setPivotX(-rect.getTranslateX()); // <Left, Right>
        lightRotateY.setPivotZ(-rect.getTranslateZ()); // Depth <Closest, Furthest>
        rect.getTransforms().addAll(lightRotateX, lightRotateY);

        cameraGroup.getChildren().add(rect);
        return rect;
    }

    private void movePolygonX(int amount) {
        for (short vertice : getSelectedPolygon().getVertices()) {
            SVector vertex = getFile().getVertexes().get(vertice);
            vertex.setX((short) (vertex.getX() + amount));
        }

        refreshView();
    }

    private void movePolygonY(int amount) {
        for (short vertice : getSelectedPolygon().getVertices()) {
            SVector vertex = getFile().getVertexes().get(vertice);
            vertex.setY((short) (vertex.getY() - amount));
        }

        refreshView();
    }

    private void movePolygonZ(int amount) {
        for (short vertice : getSelectedPolygon().getVertices()) {
            SVector vertex = getFile().getVertexes().get(vertice);
            vertex.setZ((short) (vertex.getZ() + amount));
        }

        refreshView();
    }

    /**
     * Supposedly removes the cursor polygon.
     */
    public void removeCursorPolygon() {
        if (this.selectedPolygon == null)
            return;

        this.polygonSelected = false;
        this.selectedPolygon = null;
        hideCursorPolygon();
    }

    /**
     * Hides the cursor polygon.
     */
    public void hideCursorPolygon() {
        mapMesh.getFaces().resize(mapMesh.getFaceCount());
        mapMesh.getTexCoords().resize(mapMesh.getTextureCount());
    }

    /**
     * Set the polygon that the cursor is hovering over.
     * @param newPoly The poly to highlight.
     */
    public void setCursorPolygon(PSXPolygon newPoly) {
        if (newPoly == this.selectedPolygon || newPoly == this.polygonImmuneToTarget)
            return;

        removeCursorPolygon();
        this.polygonImmuneToTarget = null;
        if (newPoly != null)
            renderCursor(this.selectedPolygon = newPoly);
    }

    private void renderCursor(PSXPolygon cursorPoly) {
        if (cursorPoly == null)
            return;

        int increment = mapMesh.getVertexFormat().getVertexIndexSize();
        boolean isQuad = (cursorPoly.getVertices().length == PSXPolygon.QUAD_SIZE);

        int face = mapMesh.getPolyFaceMap().get(cursorPoly) * mapMesh.getFaceElementSize();
        int v1 = mapMesh.getFaces().get(face);
        int v2 = mapMesh.getFaces().get(face + increment);
        int v3 = mapMesh.getFaces().get(face + (2 * increment));

        if (isQuad) {
            int v4 = mapMesh.getFaces().get(face + (3 * increment));
            int v5 = mapMesh.getFaces().get(face + (4 * increment));
            int v6 = mapMesh.getFaces().get(face + (5 * increment));
            mapMesh.addRectangle(MapMesh.CURSOR_COLOR.getTextureEntry(), v1, v2, v3, v4, v5, v6);
        } else {
            mapMesh.addTriangle(MapMesh.CURSOR_COLOR.getTextureEntry(), v1, v2, v3);
        }
    }

    /**
     * Refresh map data.
     */
    public void refreshView() {
        mapMesh.updateData();
        renderCursor(getSelectedPolygon());
    }

    private void delete2DUI() {
        uiGroup.getChildren().clear();
    }

    private void buildUI(double x, double y) {
        System.out.println("Building UI.");
        delete2DUI();

        Group rectGroup = new Group();

        Rectangle rect = new Rectangle(x, y, 100, 100);
        Rectangle rect2 = new Rectangle(x + 10, y + 10, 80, 80);
        rect.setFill(Color.RED);
        rect2.setFill(Color.YELLOW);

        rectGroup.getChildren().addAll(rect, rect2);
        rectGroup.translateXProperty().bind(this.camera.translateXProperty());
        rectGroup.translateYProperty().bind(this.camera.translateYProperty());

        uiGroup.getChildren().add(rectGroup);
    }
}
