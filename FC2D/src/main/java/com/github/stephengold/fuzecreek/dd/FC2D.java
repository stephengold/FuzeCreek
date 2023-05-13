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
package com.github.stephengold.fuzecreek.dd;

import com.github.stephengold.fuzecreek.BankCell;
import com.github.stephengold.fuzecreek.Cause;
import com.github.stephengold.fuzecreek.Cell;
import com.github.stephengold.fuzecreek.DryLandCell;
import com.github.stephengold.fuzecreek.GameState;
import com.github.stephengold.fuzecreek.LeftBankCell;
import com.github.stephengold.fuzecreek.MineCell;
import com.github.stephengold.fuzecreek.RightBankCell;
import com.github.stephengold.fuzecreek.RockCell;
import com.github.stephengold.fuzecreek.Row;
import com.github.stephengold.fuzecreek.View;
import com.github.stephengold.fuzecreek.WaterOnlyCell;
import com.jme3.app.StatsAppState;
import com.jme3.asset.TextureKey;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Quad;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import java.util.logging.Level;
import java.util.logging.Logger;
import jme3utilities.Heart;
import jme3utilities.MyAsset;
import jme3utilities.MyString;
import jme3utilities.Validate;
import jme3utilities.math.noise.Generator;
import jme3utilities.ui.AcorusDemo;
import jme3utilities.ui.InputMode;
import jme3utilities.ui.Signals;

/**
 * A rafting game with explosives (2-D graphics version).
 *
 * @author Stephen Gold sgold@sonic.net
 */
final public class FC2D
        extends AcorusDemo
        implements View {
    // *************************************************************************
    // constants and loggers

    /**
     * world Z coordinate for the raft
     */
    final private static float raftZ = -0.5f;
    /**
     * number of cell columns that can be displayed
     */
    final private static int numColumns = 40;
    /**
     * message logger for this class
     */
    final private static Logger logger = Logger.getLogger(FC2D.class.getName());
    /**
     * application name (for the title bar of the app's window)
     */
    final private static String applicationName
            = FC2D.class.getSimpleName();
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
     * status displayed in the upper-left corner of the GUI node
     */
    private static BitmapText statusText;
    /**
     * state of the game
     */
    private static GameState gameState;
    /**
     * generate pseudo-random values for game mechanics
     */
    private static Generator generator;
    /**
     * generate pseudo-random values for visualization
     */
    private static Generator viewGenerator;
    /**
     * visualize the player's raft
     */
    private static Geometry raftGeometry;
    /**
     * rectangular geometries used to visualize cells, indexed by display row
     * and display column
     */
    private static Geometry[][] cellGeometries;
    /**
     * height of each Cell in pixels
     */
    private static int cellHeight;
    /**
     * width of each Cell in pixels
     */
    private static int cellWidth;
    /**
     * visualize naval mines
     */
    private static Material mineMaterial;
    /**
     * visualize rocks protruding from the water
     */
    private static Material rockMaterial;
    /**
     * visualize clear water (9 shades of blue)
     */
    final private static Material[] waterOnlyMaterials = new Material[9];
    /**
     * visualize the left bank (9 variants)
     */
    final private static Material[] leftBankMaterial = new Material[9];
    /**
     * visualize the right bank (9 variants)
     */
    final private static Material[] rightBankMaterial = new Material[9];
    /**
     * parent all cell geometries for efficient vertical scrolling
     */
    final private static Node verticalScrollingNode
            = new Node("vertical scrolling");
    // *************************************************************************
    // constructors

    /**
     * Instantiate an AcorusDemo without a FlyCamAppState.
     */
    private FC2D() {
        super(new StatsAppState());
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the FC2D application.
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

        FC2D application = new FC2D();
        application.setSettings(appSettings);
        application.setShowSettings(false);
        application.start();
    }
    // *************************************************************************
    // AcorusDemo methods

    /**
     * Initialize the FC2D application.
     */
    @Override
    public void acorusInit() {
        viewGenerator = new Generator(99L);

        int numDownstreamRows = 23;
        gameState = new GameState(this, generator, numDownstreamRows);

        // Disable the JME stats display, which was enabled at its creation.
        stateManager.getState(StatsAppState.class).toggleStats();

        super.acorusInit();

        // Attach the status text to the GUI node.
        statusText = new BitmapText(guiFont);
        statusText.setLocalTranslation(0f, cam.getHeight(), 0f);
        guiNode.attachChild(statusText);

        guiNode.attachChild(verticalScrollingNode);

        initializeCellSizes();
        initializeCellGeometries();
        initializeCellMaterials();
        gameState.addAllRows();

        initializeRaftGeometry();

        ColorRGBA dryLandColor = new ColorRGBA(0.4f, 0.2f, 0.1f, 1f);
        viewPort.setBackgroundColor(dryLandColor);
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
        }

        // Implement smooth vertical scrolling.
        float vsFraction = gameState.verticalScrollingFraction();
        float yOffset = (vsFraction - 1f) * cellHeight;
        verticalScrollingNode.setLocalTranslation(0f, yOffset, 0f);

        if (vsFraction < 0f) {
            testSignalsAndAdvance();
        }
    }
    // *************************************************************************
    // View methods

    /**
     * Callback to initialize the display information of a new Cell.
     *
     * The display information consists of a Material that's applied to a quad
     * mesh in the GUI view.
     *
     * @param cell the Cell to modify (not null)
     */
    @Override
    public void initializeCellViewData(Cell cell) {
        Validate.nonNull(cell, "cell");
        Material material;

        if (cell instanceof DryLandCell) {
            material = null; // relies on the background color of the ViewPort

        } else if (cell instanceof WaterOnlyCell) {
            material = (Material) viewGenerator.pick(waterOnlyMaterials);

        } else if (cell instanceof RockCell) {
            material = rockMaterial;

        } else if (cell instanceof LeftBankCell) {
            BankCell bankCell = (BankCell) cell;
            int variant = 3 * bankCell.upstreamDeltaX()
                    + bankCell.downstreamDeltaX + 4;
            material = leftBankMaterial[variant];

        } else if (cell instanceof RightBankCell) {
            BankCell bankCell = (BankCell) cell;
            int variant = 3 * bankCell.upstreamDeltaX()
                    + bankCell.downstreamDeltaX + 4;
            material = rightBankMaterial[variant];

        } else if (cell instanceof MineCell) {
            material = mineMaterial;

        } else {
            String className = cell.getClass().getSimpleName();
            throw new IllegalArgumentException(className);
        }

        cell.setViewData(material);
    }
    // *************************************************************************
    // private methods

    /**
     * Initialize the array of rectangular geometries used to visualize cells.
     */
    private static void initializeCellGeometries() {
        int numRows = gameState.countVisibleRows();
        cellGeometries = new Geometry[numRows][];

        Mesh mesh = new Quad(cellWidth, cellHeight);
        for (int rowIndex = 0; rowIndex < numRows; ++rowIndex) {
            float y = rowIndex * cellHeight;
            cellGeometries[rowIndex] = new Geometry[numColumns];
            for (int column = 0; column < numColumns; ++column) {
                String name = String.format("cell[%d][%d]", rowIndex, column);
                Geometry geometry = new Geometry(name, mesh);
                verticalScrollingNode.attachChild(geometry);
                cellGeometries[rowIndex][column] = geometry;

                float x = column * cellWidth;
                geometry.setLocalTranslation(x, y, -1f);
            }
        }
    }

    /**
     * Initialize the materials used to visualize cells.
     */
    private void initializeCellMaterials() {
        boolean flipY = true;
        TextureKey key = new TextureKey("Textures/cells/mine.png", flipY);
        Texture texture = assetManager.loadTexture(key);
        mineMaterial = MyAsset.createUnshadedMaterial(assetManager, texture);

        key = new TextureKey("Textures/cells/rock.png", flipY);
        texture = assetManager.loadTexture(key);
        rockMaterial = MyAsset.createUnshadedMaterial(assetManager, texture);

        ColorRGBA waterColor = new ColorRGBA();
        float opaque = 1f;
        for (int variant = 0; variant < 9; ++variant) {
            float chroma = 0.61f + 0.03f * variant;
            waterColor.setAsSrgb(0f, 0f, chroma, opaque); // dark blue
            waterOnlyMaterials[variant]
                    = MyAsset.createUnshadedMaterial(assetManager, waterColor);
        }

        for (int upstreamDX = -1; upstreamDX <= +1; ++upstreamDX) {
            for (int downstreamDX = -1; downstreamDX <= +1; ++downstreamDX) {
                int variant = 3 * upstreamDX + downstreamDX + 4;
                String downstreamMpz = mpz(downstreamDX);
                String upstreamMpz = mpz(upstreamDX);
                String assetPath = String.format(
                        "Textures/cells/leftBank%s%s.png", upstreamMpz,
                        downstreamMpz);
                key = new TextureKey(assetPath, flipY);
                texture = assetManager.loadTexture(key);
                leftBankMaterial[variant]
                        = MyAsset.createUnshadedMaterial(assetManager, texture);

                assetPath = String.format("Textures/cells/rightBank%s%s.png",
                        upstreamMpz, downstreamMpz);
                key = new TextureKey(assetPath, flipY);
                texture = assetManager.loadTexture(key);
                rightBankMaterial[variant]
                        = MyAsset.createUnshadedMaterial(assetManager, texture);
            }
        }
    }

    /**
     * Initialize the cell sizes in pixels.
     */
    private void initializeCellSizes() {
        Camera guiCamera = guiViewPort.getCamera();
        int displayHeight = guiCamera.getHeight();
        int numRows = gameState.countVisibleRows();
        cellHeight = displayHeight / (numRows - 1); // 2 rows partially hidden

        int displayWidth = guiCamera.getWidth();
        cellWidth = displayWidth / numColumns;
        //System.out.print("cell w=" + cellWidth + " h=" + cellHeight);
    }

    /**
     * Initialize the Geometry used to visualize the raft.
     */
    private void initializeRaftGeometry() {
        int widthInPixels = GameState.raftWidth * cellWidth;
        Mesh mesh = new Quad(widthInPixels, cellHeight);
        raftGeometry = new Geometry("raft", mesh);
        verticalScrollingNode.attachChild(raftGeometry);

        String assetPath
                = String.format("Textures/raft%d.png", GameState.raftWidth);
        boolean flipY = true;
        TextureKey key = new TextureKey(assetPath, flipY);
        Texture texture = assetManager.loadTexture(key);
        Material material
                = MyAsset.createUnshadedMaterial(assetManager, texture);
        raftGeometry.setMaterial(material);

        float x = (numColumns / 2) * cellWidth;
        int rowIndex = gameState.raftRowIndex();
        int displayRow = rowIndex - gameState.lastRowIndex();
        float y = displayRow * cellHeight;
        raftGeometry.setLocalTranslation(x, y, raftZ);
    }

    /**
     * Convert a deltaX value to a name.
     *
     * @param deltaX the value to convert (-1, 0, or +1)
     * @return a string of text (not null, not empty)
     */
    private static String mpz(int deltaX) {
        switch (deltaX) {
            case -1:
                return "Minus";
            case 0:
                return "Zero";
            case +1:
                return "Plus";
            default:
                throw new IllegalArgumentException("deltaX = " + deltaX);
        }
    }

    /**
     * Test the input signals and advance the simulation accordingly.
     */
    private void testSignalsAndAdvance() {
        Signals signals = getSignals();
        int deltaX = 0;
        if (signals.test(leftSignalName)) {
            --deltaX;
        }
        if (signals.test(rightSignalName)) {
            ++deltaX;
        }

        Cause terminationCause = gameState.advance(deltaX);
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
     * Update the visualization of all cells in the indexed Row.
     *
     * @param rowIndex which row to visualize (&ge;0)
     */
    private static void updateRow(int rowIndex) {
        Row row = gameState.findRow(rowIndex);
        int displayRow = rowIndex - gameState.lastRowIndex();
        Geometry[] geometries = cellGeometries[displayRow];

        int leftMarginX = gameState.raftLeftX() - numColumns / 2;
        for (int column = 0; column < numColumns; ++column) {
            int x = column + leftMarginX;
            Cell cell = row.findCell(x);

            Material material;
            if (cell == null) {
                material = null;
            } else {
                material = (Material) cell.getViewData();
            }

            Geometry geometry = geometries[column];
            if (material == null) { // make the Geometry invisible
                geometry.setCullHint(Spatial.CullHint.Always);
            } else {
                geometry.setMaterial(material);
                geometry.setCullHint(Spatial.CullHint.Never);
            }
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
