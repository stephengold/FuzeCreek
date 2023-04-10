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
package com.github.stephengold.fuzecreek.console;

import com.github.stephengold.fuzecreek.BankCell;
import com.github.stephengold.fuzecreek.Cause;
import com.github.stephengold.fuzecreek.Cell;
import com.github.stephengold.fuzecreek.DryLandCell;
import com.github.stephengold.fuzecreek.GameState;
import com.github.stephengold.fuzecreek.MineCell;
import com.github.stephengold.fuzecreek.RockCell;
import com.github.stephengold.fuzecreek.Row;
import com.github.stephengold.fuzecreek.View;
import com.github.stephengold.fuzecreek.WaterOnlyCell;
import com.jme3.app.StatsAppState;
import com.jme3.input.KeyInput;
import com.jme3.system.AppSettings;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import jme3utilities.Heart;
import jme3utilities.MyString;
import jme3utilities.Validate;
import jme3utilities.math.noise.Generator;
import jme3utilities.ui.AcorusDemo;
import jme3utilities.ui.InputMode;
import jme3utilities.ui.Signals;

/**
 * A rafting game with explosives (console version).
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class FCConsole
        extends AcorusDemo
        implements View {
    // *************************************************************************
    // constants and loggers

    /**
     * character sequence to visualize the raft
     */
    final private static CharSequence raftString = "[]";
    /**
     * number of cell columns that can be displayed
     */
    final private static int numColumns = 40;
    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(FCConsole.class.getName());
    /**
     * application name (for the title bar of the app's window)
     */
    final private static String applicationName
            = FCConsole.class.getSimpleName();
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
     * state of the game
     */
    private static GameState gameState;
    /**
     * generate pseudo-random values for game mechanics
     */
    private static Generator generator;
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the FCConsole application.
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
        appSettings.setResolution(480, 240);
        appSettings.setTitle(title); // Customize the window's title bar.
        appSettings.setVSync(true);

        FCConsole application = new FCConsole();
        application.setSettings(appSettings);
        application.setShowSettings(false);
        application.start();
    }
    // *************************************************************************
    // AcorusDemo methods

    /**
     * Initialize the FCConsole application.
     */
    @Override
    public void acorusInit() {
        // Disable the JME stats display, which was enabled at its creation.
        stateManager.getState(StatsAppState.class).toggleStats();

        super.acorusInit();
        gameState = new GameState(this, generator);
    }

    /**
     * Add application-specific hotkey bindings and override existing ones.
     */
    @Override
    public void moreDefaultBindings() {
        InputMode diMode = getDefaultInputMode();
        diMode.unbind(KeyInput.KEY_C);
        diMode.unbind(KeyInput.KEY_Q);
        diMode.unbind(KeyInput.KEY_S);
        diMode.unbind(KeyInput.KEY_W);
        diMode.unbind(KeyInput.KEY_Z);

        // To steer, press the A and D keys on the keyboard.
        diMode.bindSignal(leftSignalName, KeyInput.KEY_A, KeyInput.KEY_LEFT);
        diMode.bindSignal(rightSignalName, KeyInput.KEY_D, KeyInput.KEY_RIGHT);
    }

    /**
     * Callback invoked once per frame.
     *
     * @param tpf the time interval between frames (in seconds, &ge;0)
     */
    @Override
    public void simpleUpdate(float tpf) {
        float vsFraction = gameState.verticalScrollingFraction();
        if (vsFraction < 0f) {
            testSignalsAndAdvance();
        }
    }
    // *************************************************************************
    // View methods

    /**
     * Callback to initialize the display information of a new Cell.
     *
     * In the console version, the display information consists of an ASCII
     * Character that's printed to System.out.
     *
     * @param cell the Cell to modify (not null)
     */
    @Override
    public void initializeCellViewData(Cell cell) {
        Validate.nonNull(cell, "cell");
        char character;

        if (cell instanceof DryLandCell) {
            character = ':';
        } else if (cell instanceof WaterOnlyCell) {
            character = ' ';
        } else if (cell instanceof RockCell) {
            character = '.';

        } else if (cell instanceof BankCell) {
            BankCell bankCell = (BankCell) cell;
            int upstreamDeltaX = bankCell.upstreamDeltaX();
            switch (upstreamDeltaX) {
                case -1:
                    switch (bankCell.downstreamDeltaX) {
                        case -1:
                            character = '\\';
                            break;
                        case +1:
                            character = '<';
                            break;
                        default:
                            character = 'L';
                    }
                    break;
                case +1:
                    character = (bankCell.downstreamDeltaX < 0) ? '>' : '/';
                    break;
                default:
                    character = (bankCell.downstreamDeltaX == 0) ? '|' : 'Y';
            }

        } else if (cell instanceof MineCell) {
            character = '0';
        } else {
            String className = cell.getClass().getSimpleName();
            throw new IllegalArgumentException(className);
        }

        cell.setViewData(character);
    }
    // *************************************************************************
    // private methods

    /**
     * Visualize the specified Row (not containing the player's raft) by
     * printing a line of ASCII text to the System.out .
     *
     * @param row the Row to visualize (not null)
     * @param leftMarginX the world X coordinate for the first column printed
     * @param rightMarginX the world X coordinate for the last column printed
     */
    private static void printlnNoRaft(Row row, int leftMarginX,
            int rightMarginX) {
        for (int x = leftMarginX; x <= rightMarginX; ++x) {
            Cell cell = row.findCell(x);
            char character;
            if (cell == null) {
                character = ':';
            } else {
                character = (char) cell.getViewData();
            }
            System.out.print(character);
        }

        System.out.println();
    }

    /**
     * Visualize the specified Row (containing the player's raft) by printing a
     * line of ASCII text to the System.out .
     *
     * @param row the Row to visualize (not null)
     * @param leftMarginX the world X coordinate for the first column printed
     * @param rightMarginX the world X coordinate for the last column printed
     */
    private static void printlnWithRaft(Row row, int leftMarginX,
            int rightMarginX) {
        int raftLeftX = gameState.raftLeftX();
        int raftRightX = gameState.raftRightX();

        for (int x = leftMarginX; x <= rightMarginX; ++x) {
            char character;
            if (x >= raftLeftX && x <= raftRightX) {
                int raftIndex = x - raftLeftX;
                character = raftString.charAt(raftIndex);
            } else {
                Cell cell = row.findCell(x);
                if (cell == null) {
                    character = ':';
                } else {
                    character = (char) cell.getViewData();
                }
            }
            System.out.print(character);
        }

        int points = gameState.totalPoints();
        int patches = gameState.countRemainingPatches();
        System.out.printf(" %d point%s, %d patch%s remaining",
                points, (points == 1) ? "" : "s",
                patches, (patches == 1) ? "" : "es");
        System.out.println();
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
        if (terminationCause == null) {
            updateView();
        } else {
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
     * Update the console view.
     */
    private static void updateView() {
        int firstRow = gameState.firstRowIndex(); // highest index, print first
        int lastRow = gameState.lastRowIndex(); // lowest index, print last
        int raftRowIndex = gameState.raftRowIndex();
        int leftMarginX = gameState.raftLeftX() - numColumns / 2;
        int rightMarginX = leftMarginX + numColumns - 1;

        PrintStream console = System.out;
        for (int rowIndex = firstRow; rowIndex >= lastRow; --rowIndex) {
            Row row = gameState.findRow(rowIndex);
            if (rowIndex == raftRowIndex) {
                printlnWithRaft(row, leftMarginX, rightMarginX);
            } else {
                printlnNoRaft(row, leftMarginX, rightMarginX);
            }
        }
        console.println();
        console.flush();
    }
}
