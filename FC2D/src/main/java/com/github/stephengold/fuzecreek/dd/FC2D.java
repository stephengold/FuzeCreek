/*
 Copyright (c) 2021, Stephen Gold
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
import com.jme3.font.Rectangle;
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
import jme3utilities.Validate;
import jme3utilities.math.noise.Generator;
import jme3utilities.ui.ActionApplication;
import jme3utilities.ui.HelpUtils;
import jme3utilities.ui.InputMode;
import jme3utilities.ui.Signals;

/**
 * A rafting game with explosives (2-D graphics version).
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class FC2D
        extends ActionApplication
        implements View {
    // *************************************************************************
    // constants and loggers

    /**
     * number of cell columns that can displayed
     */
    final private static int numColumns = 70;
    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(FC2D.class.getName());
    /**
     * action string to toggle the help node
     */
    final private static String asToggleHelp = "toggle help";
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
     * generate pseudo-random values
     */
    private static Generator generator;
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
     * visualize clear water
     */
    private static Material waterOnlyMaterial;
    /**
     * visualize the left bank (9 variants)
     */
    final private static Material[] leftBankMaterial = new Material[9];
    /**
     * visualize the right bank (9 variants)
     */
    final private static Material[] rightBankMaterial = new Material[9];
    /**
     * Node for displaying hotkey help in the GUI scene
     */
    private static Node helpNode;
    /**
     * Node for displaying "toggle help: H" in the GUI scene
     */
    private static Node minHelpNode;
    /**
     * parent all cell geometries for efficient vertical scrolling
     */
    private static Node verticalScrollingNode;
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the FC2D application.
     *
     * @param arguments array of command-line arguments (not null)
     */
    public static void main(String... arguments) {
        /*
         * Mute the chatty loggers found in some imported packages.
         */
        Heart.setLoggingLevels(Level.WARNING);

        AppSettings appSettings = new AppSettings(true);
        appSettings.setGammaCorrection(true);
        appSettings.setResolution(1280, 720);
        appSettings.setVSync(true);

        FC2D application = new FC2D();
        application.setSettings(appSettings);
        application.setShowSettings(false);
        application.start();
    }
    // *************************************************************************
    // ActionApplication methods

    /**
     * Initialize the FC2D application.
     */
    @Override
    public void actionInitializeApplication() {
        generator = new Generator();
        /*
         * Disable the JME stats display, which was enabled at its creation.
         */
        stateManager.getState(StatsAppState.class).toggleStats();
        /*
         * Add the status text to the GUI.
         */
        statusText = new BitmapText(guiFont, false);
        statusText.setLocalTranslation(0f, cam.getHeight(), 0f);
        guiNode.attachChild(statusText);

        verticalScrollingNode = new Node("vertical scrolling");
        guiNode.attachChild(verticalScrollingNode);

        initializeCellSizes();
        initializeCellGeometries();
        initializeCellMaterials();
        gameState = new GameState(this, generator);
        initializeRaftGeometry();

        ColorRGBA dryLandColor = new ColorRGBA(0.4f, 0.2f, 0.1f, 1f);
        viewPort.setBackgroundColor(dryLandColor);
    }

    /**
     * Add application-specific hotkey bindings and build the help nodes.
     */
    @Override
    public void moreDefaultBindings() {
        InputMode diMode = getDefaultInputMode();
        diMode.unbind(KeyInput.KEY_C);
        diMode.unbind(KeyInput.KEY_Q);
        diMode.unbind(KeyInput.KEY_S);
        diMode.unbind(KeyInput.KEY_W);
        diMode.unbind(KeyInput.KEY_Z);
        /*
         * To steer, press the A and D keys on the keyboard.
         */
        diMode.bindSignal(leftSignalName, KeyInput.KEY_A, KeyInput.KEY_LEFT);
        diMode.bindSignal(rightSignalName, KeyInput.KEY_D, KeyInput.KEY_RIGHT);
        /*
         * To show/hide the help info, press the H key.
         */
        diMode.bind(asToggleHelp, KeyInput.KEY_F1, KeyInput.KEY_H);
        /*
         * Build 2 help nodes and attach the smaller one.
         */
        float x = 10f;
        float y = cam.getHeight() - 30f;
        float width = cam.getWidth() - 20f;
        float height = cam.getHeight() - 40f;
        Rectangle bounds = new Rectangle(x, y, width, height);
        attachHelpNode(bounds);
    }

    /**
     * Process an action that wasn't handled by the active input mode.
     *
     * @param actionString textual description of the action (not null)
     * @param ongoing true if the action is ongoing, otherwise false
     * @param tpf time interval between frames (in seconds, &ge;0)
     */
    @Override
    public void onAction(String actionString, boolean ongoing, float tpf) {
        if (ongoing && actionString.equals(asToggleHelp)) {
            toggleHelp();
        } else {
            super.onAction(actionString, ongoing, tpf);
        }
    }

    /**
     * Callback invoked once per frame.
     *
     * @param tpf the time interval between frames (in seconds, &ge;0)
     */
    @Override
    public void simpleUpdate(float tpf) {
        updateStatusText();

        int firstRow = gameState.firstRowIndex(); // highest index
        int lastRow = gameState.lastRowIndex(); // lowest index
        for (int rowIndex = firstRow; rowIndex >= lastRow; --rowIndex) {
            updateRow(rowIndex);
        }
        /*
         * Implement smooth vertical scrolling.
         */
        float vsFraction = gameState.verticalScrollingFraction();
        float yOffset = vsFraction * cellHeight;
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
     * In the 2-D version, the display information consists of a Material
     * that's applied to a quad mesh in the GUI view.
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
            material = waterOnlyMaterial;
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
     * Generate full and minimal versions of the hotkey help. Attach the minimal
     * one to the GUI scene.
     *
     * @param bounds the desired screen coordinates (not null, unaffected)
     */
    private void attachHelpNode(Rectangle bounds) {
        Validate.nonNull(bounds, "bounds");

        InputMode inputMode = getDefaultInputMode();
        float extraSpace = 20f;
        helpNode = HelpUtils.buildNode(inputMode, bounds, guiFont, extraSpace);
        helpNode.move(0f, 0f, 1f); // move (slightly) to the front

        InputMode dummyMode = new InputMode("dummy") {
            @Override
            protected void defaultBindings() {
            }

            @Override
            public void onAction(String s, boolean b, float f) {
            }
        };
        dummyMode.bind(asToggleHelp, KeyInput.KEY_H);

        float width = 100f; // in pixels
        float height = bounds.height;
        float x = bounds.x + bounds.width - width;
        float y = bounds.y;
        Rectangle dummyBounds = new Rectangle(x, y, width, height);

        minHelpNode = HelpUtils.buildNode(dummyMode, dummyBounds, guiFont, 0f);
        guiNode.attachChild(minHelpNode);
    }

    /**
     * Initialize the array of rectangular geometries used to visualize cells.
     */
    private static void initializeCellGeometries() {
        int numRows = GameState.countVisibleRows();
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
        ColorRGBA mineColor = new ColorRGBA(1f, 0f, 0f, 1f);
        mineMaterial = MyAsset.createUnshadedMaterial(assetManager, mineColor);

        ColorRGBA rockColor = new ColorRGBA(1f, 1f, 1f, 1f);
        rockMaterial = MyAsset.createUnshadedMaterial(assetManager, rockColor);

        ColorRGBA waterColor = new ColorRGBA(0.5f, 0.5f, 1f, 1f);
        waterOnlyMaterial
                = MyAsset.createUnshadedMaterial(assetManager, waterColor);

        for (int upstreamDX = -1; upstreamDX <= +1; ++upstreamDX) {
            for (int downstreamDX = -1; downstreamDX <= +1; ++downstreamDX) {
                boolean flipY = true;
                int variant = 3 * upstreamDX + downstreamDX + 4;
                String downstreamMpz = mpz(downstreamDX);
                String upstreamMpz = mpz(upstreamDX);

                String assetPath;
                TextureKey key;
                Texture texture;

                assetPath = String.format("Textures/cells/leftBank%s%s.png",
                        upstreamMpz, downstreamMpz);
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
        int numRows = GameState.countVisibleRows();
        cellHeight = displayHeight / numRows;

        int displayWidth = guiCamera.getWidth();
        cellWidth = displayWidth / numColumns;
    }

    /**
     * Initialize the Geometry used to visualize the raft.
     */
    private void initializeRaftGeometry() {
        int widthInPixels = GameState.raftWidth * cellWidth;
        Mesh mesh = new Quad(widthInPixels, cellHeight);
        raftGeometry = new Geometry("raft", mesh);
        verticalScrollingNode.attachChild(raftGeometry);

        ColorRGBA color = new ColorRGBA(0f, 0.2f, 0f, 1f);
        Material material = MyAsset.createUnshadedMaterial(assetManager, color);
        raftGeometry.setMaterial(material);

        float x = (numColumns / 2) * cellWidth;
        int rowIndex = gameState.raftRowIndex();
        int displayRow = rowIndex - gameState.lastRowIndex();
        float y = displayRow * cellHeight;
        raftGeometry.setLocalTranslation(x, y, -0.5f);
    }

    /**
     * Convert a deltaX value to a name.
     *
     * @param deltaX the value to convert (-1, 0, or +1)
     * @return a string of text
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

        boolean isGameOver = gameState.advance(deltaX);
        if (isGameOver) {
            int totalPoints = gameState.totalPoints();
            System.out.printf("Your final score:  %d point%s%n",
                    totalPoints, (totalPoints == 1) ? "" : "s");

            stop();
        }
    }

    /**
     * Toggle between the full help node and the minimal one.
     */
    private void toggleHelp() {
        if (helpNode.getParent() == null) {
            minHelpNode.removeFromParent();
            guiNode.attachChild(helpNode);
        } else {
            helpNode.removeFromParent();
            guiNode.attachChild(minHelpNode);
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

            Material material = null;
            if (cell != null) {
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
     * Update the status text in the GUI.
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