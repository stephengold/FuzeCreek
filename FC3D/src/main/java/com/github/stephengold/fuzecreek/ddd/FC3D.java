/*
 Copyright (c) 2021-2023, Stephen Gold
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 1. Redistributions of source code must retain the above copyright notice, this
    list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. Neither the name of the copyright holder nor the names of its
    contributors may be used to endorse or promote products derived from
    this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.github.stephengold.fuzecreek.ddd;

import com.github.stephengold.fuzecreek.Cause;
import com.github.stephengold.fuzecreek.Cell;
import com.github.stephengold.fuzecreek.GameState;
import com.github.stephengold.fuzecreek.Row;
import com.github.stephengold.fuzecreek.View;
import com.jme3.app.StatsAppState;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Plane;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Quad;
import com.jme3.system.AppSettings;
import java.util.logging.Level;
import java.util.logging.Logger;
import jme3utilities.Heart;
import jme3utilities.MyAsset;
import jme3utilities.MyMesh;
import jme3utilities.MyString;
import jme3utilities.Validate;
import jme3utilities.debug.Dumper;
import jme3utilities.math.noise.Generator;
import jme3utilities.mesh.Icosphere;
import jme3utilities.mesh.Octahedron;
import jme3utilities.sky.SkyControl;
import jme3utilities.sky.StarsOption;
import jme3utilities.sky.WaterProcessor;
import jme3utilities.ui.AcorusDemo;
import jme3utilities.ui.InputMode;
import jme3utilities.ui.Signals;

/**
 * A rafting game with explosives (3-D graphics version).
 *
 * @author Stephen Gold sgold@sonic.net
 */
final public class FC3D
        extends AcorusDemo
        implements View {
    // *************************************************************************
    // constants and loggers

    /**
     * X-width of each Cell (in world units)
     */
    final static float cellXWidth = 0.2f;
    /**
     * Z-width of each Cell (in world units)
     */
    final static float cellZWidth = 0.2f;
    /**
     * world Y coordinate for dry land
     */
    final static float dryLandY = 0.1f;
    /**
     * world Y coordinate for the water
     */
    final static float waterY = 0f;
    /**
     * number of cell columns that can be displayed
     */
    final static int numColumns = 90;
    /**
     * message logger for this class
     */
    final private static Logger logger = Logger.getLogger(FC3D.class.getName());
    /**
     * rotate map coordinates to world coordinates: mapX to worldZ
     */
    final private static Quaternion rotateAxes
            = new Quaternion(-0.5f, -0.5f, -0.5f, 0.5f);
    /**
     * application name (for the title bar of the app's window)
     */
    final private static String applicationName
            = FC3D.class.getSimpleName();
    /**
     * name of the input signal used to steer left
     */
    final private static String leftSignalName = "left";
    /**
     * name of the input signal used to steer right
     */
    final private static String rightSignalName = "right";
    // *************************************************************************
    // fields

    /**
     * ambient light in the scene
     */
    private static AmbientLight ambientLight;
    /**
     * status displayed in the upper-left corner of the GUI viewport
     */
    private static BitmapText statusText;
    /**
     * main directional light in the scene
     */
    private static DirectionalLight mainLight;
    /**
     * state of the game
     */
    private static GameState gameState;
    /**
     * generate pseudo-random values for game mechanics
     */
    private static Generator generator;
    /**
     * rectangular geometries used to visualize land, indexed by display row
     */
    private static Geometry[] rowGeometries;
    /**
     * geometries used to visualize mines, indexed by display row and display
     * column
     */
    private static Geometry[][] mineGeometries;
    /**
     * geometries used to visualize rocks, indexed by display row and display
     * column
     */
    private static Geometry[][] rockGeometries;
    /**
     * parent all sky geometries for efficient water effects
     */
    final private static Node reflectiblesNode = new Node("reflectables");
    /**
     * parent all cell geometries for efficient vertical scrolling
     */
    final private static Node verticalScrollingNode
            = new Node("vertical scrolling");
    /**
     * visualize water
     */
    private static WaterProcessor waterProcessor;
    // *************************************************************************
    // constructors

    /**
     * Instantiate an AcorusDemo without a FlyCamAppState.
     */
    private FC3D() {
        super(new StatsAppState());
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the FC3D application.
     *
     * @param arguments array of command-line arguments (not null)
     */
    public static void main(String[] arguments) {
        String title = applicationName + " " + MyString.join(arguments);

        // Mute the chatty loggers found in some imported packages.
        Heart.setLoggingLevels(Level.WARNING);

        generator = new Generator();

        AppSettings appSettings = new AppSettings(true);
        appSettings.setGammaCorrection(true);
        appSettings.setRenderer(AppSettings.LWJGL_OPENGL32);
        appSettings.setResolution(1280, 800);
        appSettings.setTitle(title); // Customize the window's title bar.
        appSettings.setVSync(true);

        FC3D application = new FC3D();
        application.setSettings(appSettings);
        application.setShowSettings(false);
        application.start();
    }
    // *************************************************************************
    // AcorusDemo methods

    /**
     * Initialize the FC3D application.
     */
    @Override
    public void acorusInit() {
        int numDownstreamRows = 125;
        gameState = new GameState(this, generator, numDownstreamRows);
        //gameState.setAdvanceMillis(999999L); // TODO while debugging

        // Disable the JME stats display, which was enabled at its creation.
        stateManager.getState(StatsAppState.class).toggleStats();

        super.acorusInit();

        // Attach the status text to the GUI node.
        statusText = new BitmapText(guiFont);
        statusText.setColor(ColorRGBA.Red); // for contrast with the sky
        statusText.setLocalTranslation(0f, cam.getHeight(), 0f);
        guiNode.attachChild(statusText);

        rootNode.attachChild(reflectiblesNode);
        rootNode.attachChild(verticalScrollingNode);

        configureCamera();

        generateMaterials();
        initializeCellGeometries();
        initializeRowGeometries();
        addMargins();
        gameState.addAllRows();

        initializeRaftGeometry();
        addLighting(rootNode);
        addSky();
        addWater();
    }

    /**
     * Initialize the library of named materials. Invoke during startup.
     */
    @Override
    public void generateMaterials() {
        float opaque = 1f;
        ColorRGBA fogColor = new ColorRGBA(0.8f, 0.7f, 0.6f, opaque);
        Vector2f linearFog = new Vector2f(5f, 30f);

        ColorRGBA color = new ColorRGBA(0.4f, 0.2f, 0.1f, opaque);
        Material material = MyAsset.createShadedMaterial(assetManager, color);
        //material.getAdditionalRenderState().setWireframe(true);
        registerMaterial("dry land", material);

        material.setBoolean("UseFog", true);
        material.setColor("FogColor", fogColor.clone());
        material.setVector2("LinearFog", linearFog.clone());

        color = new ColorRGBA(1f, 0f, 0f, opaque);
        material = MyAsset.createShadedMaterial(assetManager, color);
        registerMaterial("mine", material);

        material.setBoolean("UseFog", true);
        material.setColor("FogColor", fogColor.clone());
        material.setVector2("LinearFog", linearFog.clone());
    }

    /**
     * Add application-specific hotkey bindings and override existing ones.
     */
    @Override
    public void moreDefaultBindings() {
        InputMode diMode = getDefaultInputMode();
        /*
         * To steer, press the A and D keys on the keyboard,
         * or the left and right arrow keys.
         */
        diMode.bindSignal(leftSignalName, KeyInput.KEY_A, KeyInput.KEY_LEFT);
        diMode.bindSignal(rightSignalName, KeyInput.KEY_D, KeyInput.KEY_RIGHT);

        // To show/hide the help info, press the F1 key or the H key.
        diMode.bind(asToggleHelp, KeyInput.KEY_F1, KeyInput.KEY_H);

        //diMode.bind("dump", KeyInput.KEY_P); // TODO while debugging
    }

    /**
     * Process an action that wasn't handled by the active InputMode.
     *
     * @param actionString textual description of the action (not null)
     * @param ongoing true if the action is ongoing, otherwise false
     * @param tpf the time interval between frames (in seconds, &ge;0)
     */
    @Override
    public void onAction(String actionString, boolean ongoing, float tpf) {
        if (ongoing) {
            switch (actionString) {
                case "dump":
                    dump();
                    return;
                default:
            }
        }

        super.onAction(actionString, ongoing, tpf);
    }

    /**
     * Callback invoked once per frame.
     *
     * @param tpf the time interval between frames (in seconds, &ge;0)
     */
    @Override
    public void simpleUpdate(float tpf) {
        super.simpleUpdate(tpf);
        updateStatusText();

        int firstRow = gameState.firstRowIndex(); // highest index
        int lastRow = gameState.lastRowIndex(); // lowest index
        for (int rowIndex = firstRow; rowIndex >= lastRow; --rowIndex) {
            updateRow(rowIndex);
            updateRowCells(rowIndex);
        }

        // Implement smooth scrolling.
        float vsFraction = gameState.verticalScrollingFraction();
        float xOffset = (vsFraction - 1f) * cellXWidth;
        verticalScrollingNode.setLocalTranslation(xOffset, 0f, 0f);

        if (vsFraction < 0f) {
            testSignalsAndAdvance();
        }
    }
    // *************************************************************************
    // View methods

    /**
     * Callback to initialize the display information of a new Cell.
     *
     * @param cell the new Cell (not null, unaffected)
     */
    @Override
    public void initializeCellViewData(Cell cell) {
        Validate.nonNull(cell, "cell");

        CellViewData viewData = new CellViewData(cell);
        cell.setViewData(viewData);
    }
    // *************************************************************************
    // private methods

    /**
     * Add lighting to the specified scene.
     *
     * @param rootSpatial which scene (not null)
     */
    private static void addLighting(Spatial rootSpatial) {
        ambientLight = new AmbientLight();
        rootSpatial.addLight(ambientLight);
        ambientLight.setName("ambient");

        Vector3f direction = new Vector3f(1f, -2f, -1f).normalizeLocal();
        mainLight = new DirectionalLight(direction);
        rootSpatial.addLight(mainLight);
        mainLight.setName("main");
    }

    /**
     * Add 2 large geometries to visualize dry land outside the cell array.
     */
    private void addMargins() {
        // dimensions of the water mesh
        int numRows = gameState.countVisibleRows();
        float xWidth = numRows * cellZWidth;
        float zWidth = numColumns * cellZWidth;

        float far = cam.getFrustumFar();
        Quaternion rotateX
                = new Quaternion().fromAngles(-FastMath.HALF_PI, 0f, 0f);

        Material material = findMaterial("dry land");
        assert material != null;

        Mesh leftMesh = new Quad(xWidth, far);
        leftMesh = MyMesh.subdivideTriangles(leftMesh, 10);
        Geometry leftGeometry = new Geometry("left margin", leftMesh);
        verticalScrollingNode.attachChild(leftGeometry);

        leftGeometry.setLocalRotation(rotateX);
        leftGeometry.setLocalTranslation(0f, dryLandY, 0f);
        leftGeometry.setMaterial(material);

        Mesh rightMesh = new Quad(far, xWidth);
        rightMesh = MyMesh.subdivideTriangles(rightMesh, 100);
        Geometry rightGeometry = new Geometry("right margin", rightMesh);
        verticalScrollingNode.attachChild(rightGeometry);

        rightGeometry.setLocalRotation(rotateAxes);
        rightGeometry.setLocalTranslation(0f, dryLandY, zWidth);
        rightGeometry.setMaterial(material);
    }

    /**
     * Attach a SkyControl to the root of the main scene and configure it.
     */
    private void addSky() {
        float cloudFlattening = 0.8f;
        boolean bottomDome = false;
        SkyControl skyControl = new SkyControl(assetManager, cam,
                cloudFlattening, StarsOption.Cube, bottomDome);
        reflectiblesNode.addControl(skyControl);

        skyControl.getSunAndStars().setHour(7f);
        skyControl.getUpdater().setAmbientLight(ambientLight);
        skyControl.getUpdater().setAmbientMultiplier(0.25f);
        skyControl.getUpdater().setMainLight(mainLight);
        skyControl.setCloudiness(0.4f);
        skyControl.setCloudsYOffset(0.4f);
        skyControl.setTopVerticalAngle(1.66f);

        skyControl.setEnabled(true);
    }

    /**
     * Create a horizontal rectangle of animated water and add it to the scene.
     */
    private void addWater() {
        int numRows = gameState.countVisibleRows();
        float xWidth = numRows * cellZWidth;
        float zWidth = numColumns * cellZWidth;
        Mesh mesh = new Quad(zWidth, xWidth);
        mesh.scaleTextureCoordinates(new Vector2f(10f, 10f));
        Geometry geometry = new Geometry("WaterGeometry", mesh);
        verticalScrollingNode.attachChild(geometry);

        geometry.setLocalRotation(rotateAxes);
        geometry.setLocalTranslation(0f, waterY, 0f);

        Material material = getWaterMaterial();
        geometry.setMaterial(material);
    }

    /**
     * Configure the camera during startup.
     */
    private void configureCamera() {
        float z = (numColumns / 2 + 1) * cellZWidth;
        cam.setLocation(new Vector3f(-1f, waterY + 1f, z));
        cam.setRotation(new Quaternion(0.1f, 0.7f, -0.1f, 0.7f));
    }

    /**
     * Dump the RenderManager while debugging the application.
     */
    private void dump() {
        new Dumper().setDumpTransform(true).dump(renderManager);
    }

    /**
     * Access the Material of the WaterProcessor. If no WaterProcessor exists,
     * create one.
     *
     * @return the Material (may be new or pre-existing)
     */
    private Material getWaterMaterial() {
        if (waterProcessor == null) {
            waterProcessor = new WaterProcessor(assetManager);
            viewPort.addProcessor(waterProcessor);

            waterProcessor.setDistortionMix(0.01f);
            waterProcessor.setDistortionScale(0.01f);

            Plane surface = new Plane(Vector3f.UNIT_Y, waterY);
            waterProcessor.setPlane(surface);

            // Clip everything below the surface.
            waterProcessor.setReflectionClippingOffset(0f);

            waterProcessor.setReflectionScene(reflectiblesNode);
            waterProcessor.setWaterDepth(0.1f);
            waterProcessor.setWaveSpeed(0.03f);

            waterProcessor.getMaterial().setName("water");
        }
        Material result = waterProcessor.getMaterial();

        return result;
    }

    /**
     * Initialize the 2-D arrays of rectangular geometries used to visualize
     * cells.
     */
    private void initializeCellGeometries() {
        int numRows = gameState.countVisibleRows();
        mineGeometries = new Geometry[numRows][];
        rockGeometries = new Geometry[numRows][];

        Material dryLandMaterial = findMaterial("dry land");
        assert dryLandMaterial != null;

        Material mineMaterial = findMaterial("mine");
        assert mineMaterial != null;

        Mesh mineMesh = new Icosphere(2, 0.4f * cellZWidth);
        Mesh rockMesh = new Octahedron(0.4f * cellZWidth, true);

        float mineY = waterY - 0.05f;
        float rockY = waterY - 0.01f;

        for (int rowIndex = 0; rowIndex < numRows; ++rowIndex) {
            float centerX = (rowIndex + 0.5f) * cellXWidth;

            mineGeometries[rowIndex] = new Geometry[numColumns];
            rockGeometries[rowIndex] = new Geometry[numColumns];

            for (int column = 0; column < numColumns; ++column) {
                float centerZ = (column + 0.5f) * cellZWidth;

                // Create a mine geometry for the cell.
                String name = String.format("mine[%d][%d]", rowIndex, column);
                Geometry mineGeometry = new Geometry(name, mineMesh);
                verticalScrollingNode.attachChild(mineGeometry);
                mineGeometries[rowIndex][column] = mineGeometry;

                mineGeometry.setLocalTranslation(centerX, mineY, centerZ);
                mineGeometry.setMaterial(mineMaterial);

                // Create a rock geometry for the cell.
                name = String.format("rock[%d][%d]", rowIndex, column);
                Geometry rockGeometry = new Geometry(name, rockMesh);
                verticalScrollingNode.attachChild(rockGeometry);
                rockGeometries[rowIndex][column] = rockGeometry;

                rockGeometry.setLocalTranslation(centerX, rockY, centerZ);
                rockGeometry.setMaterial(dryLandMaterial);
            }
        }
    }

    /**
     * Initialize the Geometry used to visualize the raft.
     */
    private void initializeRaftGeometry() {
        Geometry geometry
                = (Geometry) assetManager.loadModel("Models/raft.obj");
        verticalScrollingNode.attachChild(geometry);

        geometry.setLocalScale(0.5f * cellZWidth);

        int rowIndex = gameState.raftRowIndex();
        int displayRow = rowIndex - gameState.lastRowIndex();
        float x = (displayRow + 1) * cellXWidth;
        float z = (numColumns / 2 + 1) * cellZWidth;
        geometry.setLocalTranslation(x, waterY + 0.04f, z);

        float opaque = 1f;
        ColorRGBA color = new ColorRGBA(0.1f, 0.4f, 0.1f, opaque);
        Material material = MyAsset.createShadedMaterial(assetManager, color);
        geometry.setMaterial(material);
    }

    /**
     * Initialize the array of geometries used to visualize terrain.
     */
    private void initializeRowGeometries() {
        int numRows = gameState.countVisibleRows();
        rowGeometries = new Geometry[numRows];

        Material material = findMaterial("dry land");

        for (int rowIndex = 0; rowIndex < numRows; ++rowIndex) {
            String name = String.format("row[%d]", rowIndex);
            Mesh mesh = new RowMesh();
            Geometry geometry = new Geometry(name, mesh);
            verticalScrollingNode.attachChild(geometry);
            rowGeometries[rowIndex] = geometry;

            float centerX = (rowIndex - 0.5f) * cellXWidth;
            geometry.setLocalTranslation(centerX, waterY, 0f);

            geometry.setMaterial(material);
        }
    }

    /**
     * Test the input signals and advance the simulation accordingly.
     */
    private void testSignalsAndAdvance() {
        Signals signals = getSignals();
        int deltaZ = 0;
        if (signals.test(leftSignalName)) {
            --deltaZ;
        }
        if (signals.test(rightSignalName)) {
            ++deltaZ;
        }

        Cause terminationCause = gameState.advance(deltaZ);
        if (terminationCause != null) {
            switch (terminationCause) {
                case BOOM:
                    System.out.println(
                            "You detonated a mine and were severely injured!");
                    break;
                case GROUNDED:
                    System.out.println("You landed safely.");
                    break;
                case SANK:
                    System.out.println("Damaged by rocks, your raft sank. "
                            + "However, you managed to swim ashore.");
                    break;
                default:
                    assert false : terminationCause;
            }

            float seconds = gameState.elapsedSeconds();
            float ss = seconds % 60f;
            int mm = Math.round((seconds - ss) / 60f);
            System.out.printf("Game duration:  %dm %06.3fs%n", mm, ss);

            int numAdvances = gameState.countAdvances();
            System.out.println("Number of advances:  " + numAdvances);
            int numPatches = gameState.countRemainingPatches();
            System.out.println("Number of leftover patches:  " + numPatches);

            int totalPoints = gameState.totalPoints();
            System.out.printf("Your final score:  %d point%s%n",
                    totalPoints, (totalPoints == 1) ? "" : "s");

            stop();
        }
    }

    /**
     * Update the visualization of the indexed Row.
     *
     * @param rowIndex which row to visualize (&ge;0)
     */
    private static void updateRow(int rowIndex) {
        Row row1 = gameState.findRow(rowIndex);
        Row row0 = row1;
        if (rowIndex > 0) {
            Row r1 = gameState.findRow(rowIndex - 1);
            if (r1 != null) {
                row0 = r1;
            }
        }

        int displayRow = rowIndex - gameState.lastRowIndex();
        Geometry geometry = rowGeometries[displayRow];
        RowMesh mesh = (RowMesh) geometry.getMesh();

        int lastColumn = numColumns - 1;
        int leftMarginZ = gameState.raftLeftX() - numColumns / 2;
        for (int column = 0; column < numColumns; ++column) {
            int z = column + leftMarginZ;
            Cell cell0 = row0.findCell(z);

            float leftY = CellViewData.dryY;
            float rightY = CellViewData.dryY;
            if (cell0 != null) {
                CellViewData data = (CellViewData) cell0.getViewData();
                leftY = data.leftY();
                rightY = data.rightY();
            }
            mesh.setHeight(0, column, leftY);
            if (column == lastColumn) {
                mesh.setHeight(0, numColumns, rightY);
            }

            Cell cell1 = row1.findCell(z);
            leftY = CellViewData.dryY;
            rightY = CellViewData.dryY;
            if (cell1 != null) {
                CellViewData data = (CellViewData) cell1.getViewData();
                leftY = data.leftY();
                rightY = data.rightY();
            }
            mesh.setHeight(1, column, leftY);
            if (column == lastColumn) {
                mesh.setHeight(1, numColumns, rightY);
            }
        }

        mesh.updateVertices();
    }

    /**
     * Update the visualization of all cells in the indexed Row.
     *
     * @param rowIndex which row to visualize (&ge;0)
     */
    private static void updateRowCells(int rowIndex) {
        Row row = gameState.findRow(rowIndex);
        int displayRow = rowIndex - gameState.lastRowIndex();

        int leftMarginZ = gameState.raftLeftX() - numColumns / 2;
        for (int column = 0; column < numColumns; ++column) {
            Spatial.CullHint cullMine = Spatial.CullHint.Always;
            Spatial.CullHint cullRock = Spatial.CullHint.Always;

            int z = column + leftMarginZ;
            Cell cell = row.findCell(z);
            if (cell != null) {
                CellViewData data = (CellViewData) cell.getViewData();
                if (data.hasMine()) {
                    cullMine = Spatial.CullHint.Dynamic;
                }
                if (data.hasRock()) {
                    cullRock = Spatial.CullHint.Dynamic;
                }
            }

            mineGeometries[displayRow][column].setCullHint(cullMine);
            rockGeometries[displayRow][column].setCullHint(cullRock);
        }
    }

    /**
     * Update the status text in the GUI viewport.
     */
    private static void updateStatusText() {
        int points = gameState.totalPoints();
        int patches = gameState.countRemainingPatches();
        String message = String.format(" %d point%s, %d patch%s remaining",
                points, (points == 1) ? "" : "s",
                patches, (patches == 1) ? "" : "es");
        statusText.setText(message);
    }
}
