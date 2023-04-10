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
package com.github.stephengold.fuzecreek;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import jme3utilities.Validate;
import jme3utilities.math.noise.Generator;

/**
 * State of a rafting game with explosives.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class GameState {
    // *************************************************************************
    // constants and loggers

    /**
     * number of points scored for each advance
     */
    final private static int advancePoints = 1;
    /**
     * maximum width of the creek, excluding its banks (in cells)
     */
    final private static int maxCreekWidth = 60;
    /**
     * minimum width of the creek, excluding its banks (in cells)
     */
    final public static int minCreekWidth = 10;
    /**
     * number of rows visible downstream of the raft
     */
    final private static int numDownstreamRows = 23;
    /**
     * number of rows visible upstream of the raft
     */
    final private static int numUpstreamRows = 2;
    /**
     * width of the raft (in cells)
     */
    final public static int raftWidth = 2;
    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(GameState.class.getName());
    // *************************************************************************
    // fields

    /**
     * non-null &rarr; terminate after advancing, null &rarr; continue
     */
    private Cause isOver;
    /**
     * generate pseudo-random values
     */
    final public Generator generator;
    /**
     * number of times the simulation has advanced (&ge;0)
     */
    private int numAdvances;
    /**
     * number of patches in the player's inventory (&ge;0)
     */
    private int numRemainingPatches;
    /**
     * world X coordinate of the left edge of the player's raft
     */
    private int raftLeftX;
    /**
     * index of the row containing the player's raft
     */
    private int raftRowIndex;
    /**
     * player's score
     */
    private int totalPoints;
    /**
     * ideal time interval between successive advances (in milliseconds)
     */
    private long advanceMillis;
    /**
     * time when the game ended (in milliseconds since 1969, valid only if
     * isOver != null)
     */
    private long endTime;
    /**
     * ideal time for the next advance (in milliseconds since 1969)
     */
    private long nextAdvanceMillis;
    /**
     * time when the game started (in milliseconds since 1969)
     */
    final private long startTime;
    /**
     * access rows by index (indices grow in the downstream direction)
     */
    final private Map<Integer, Row> rows;
    /**
     * callbacks used to visualize the game (not null)
     */
    final public View view;
    // *************************************************************************
    // constructor

    /**
     * Instantiate a GameState with the specified display information.
     *
     * @param view the desired visualization (not null)
     * @param generator the pseudo-random generator to use (not null)
     */
    public GameState(View view, Generator generator) {
        Validate.nonNull(view, "game view");
        Validate.nonNull(generator, "generator");

        this.view = view;
        this.generator = generator;

        this.isOver = null;
        this.numAdvances = 0;
        this.numRemainingPatches = 20;
        this.raftLeftX = -raftWidth / 2;
        this.raftRowIndex = numUpstreamRows;
        this.totalPoints = 0;
        this.advanceMillis = 400L;
        this.startTime = System.currentTimeMillis();
        this.nextAdvanceMillis = startTime + advanceMillis;

        int numVisibleRows = countVisibleRows();
        this.rows = new HashMap<>(numVisibleRows);
        for (int rowIndex = 0; rowIndex < numVisibleRows; ++rowIndex) {
            addRow(rowIndex);
        }
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Generate a Row and add it to this game.
     *
     * @param rowIndex the desired row index (&ge;0)
     */
    final public void addRow(int rowIndex) {
        Validate.nonNegative(rowIndex, "row index");
        assert !rows.containsKey(rowIndex);
        assert rowIndex == 0 || rows.containsKey(rowIndex - 1);

        float rockProbability, mineProbability;
        int numClearRows = numUpstreamRows + 3;
        if (rowIndex < numClearRows) {
            /*
             * Initially, we guarantee at least 2 rows of clear water
             * in front (downstream) of the raft.
             */
            rockProbability = 0f;
            mineProbability = 0f;
        } else {
            /*
             * After the initial period, the frequencies of rocks and mines
             * increase gradually over time.
             */
            rockProbability = Math.min(0.001f * rowIndex, 0.5f);
            mineProbability = Math.min(0.0001f * rowIndex, 0.5f);
        }

        int creekWidth, leftBankX, rightBankX, leftDeltaX, rightDeltaX;
        int numFunnelRows = (maxCreekWidth - minCreekWidth) / 2;
        assert numFunnelRows > 0 : numFunnelRows;
        if (rowIndex < numFunnelRows) {
            /*
             * Initially, the creek narrows steadily,
             * from its maximum width to its median width.
             */
            creekWidth = maxCreekWidth - rowIndex; // doesn't include banks
            leftBankX = -creekWidth / 2;
            rightBankX = leftBankX + creekWidth + 1;

            if (rowIndex < numFunnelRows - 1) {
                int downstreamWidth = creekWidth - 1;
                int downstreamLeftX = -downstreamWidth / 2;
                int downstreamRightX = downstreamLeftX + downstreamWidth + 1;
                leftDeltaX = downstreamLeftX - leftBankX;
                rightDeltaX = downstreamRightX - rightBankX;
            } else {
                leftDeltaX = 0;
                rightDeltaX = 0;
            }
            int downstreamWidth = creekWidth + rightDeltaX - leftDeltaX;
            assert downstreamWidth >= minCreekWidth : downstreamWidth;

        } else {
            /*
             * After the initial period, the banks move left and right randomly,
             * yet always respect the creek's max/min width.
             */
            Row upstreamRow = rows.get(rowIndex - 1);
            int upstreamLeftDeltaX = upstreamRow.leftDeltaX();
            leftBankX = upstreamRow.leftBankX + upstreamLeftDeltaX;

            int upstreamRightDeltaX = upstreamRow.rightDeltaX();
            rightBankX = upstreamRow.rightBankX + upstreamRightDeltaX;

            creekWidth = rightBankX - leftBankX - 1; // not including banks
            assert creekWidth <= maxCreekWidth : creekWidth;
            assert creekWidth >= minCreekWidth : creekWidth;

            int downstreamWidth;
            do {
                leftDeltaX = generator.nextInt(-1, +1);
                rightDeltaX = generator.nextInt(-1, +1);
                downstreamWidth = creekWidth + rightDeltaX - leftDeltaX;
            } while (downstreamWidth > maxCreekWidth
                    || downstreamWidth < minCreekWidth);
        }

        int numCells = creekWidth + 6;
        int x0index = 2 - leftBankX;
        Row row = new Row(this, rowIndex, numCells, x0index, leftBankX,
                rightBankX, leftDeltaX, rightDeltaX, rockProbability,
                mineProbability);
        rows.put(rowIndex, row);
    }

    /**
     * Advance the simulation, causing the raft to drift forward one row and
     * optionally sideways by one cell.
     *
     * @param deltaX the desired raft movement (in the world +X direction,
     * &ge;-1, &le;+1)
     * @return enum value if the game is over, otherwise null
     */
    public Cause advance(int deltaX) {
        Validate.inRange(deltaX, "delta X", -1, +1);
        assert isOver == null : isOver;

        ++numAdvances;
        raftLeftX += deltaX;
        ++raftRowIndex;
        scorePoints(advancePoints);
        this.nextAdvanceMillis += advanceMillis;

        Row raftRow = rows.get(raftRowIndex);

        // Process all collections and collisions.
        int x = raftLeftX - 1;
        Cell cell = raftRow.findCell(x);
        cell.collect();

        int raftRightX = raftLeftX + raftWidth - 1;
        for (x = raftLeftX; x <= raftRightX; ++x) {
            cell = raftRow.findCell(x);
            cell.collide();
        }

        x = raftRightX + 1;
        cell = raftRow.findCell(x);
        cell.collect();

        // Add a Row to maintain lookahead.
        int appendRowIndex = raftRowIndex + numDownstreamRows;
        addRow(appendRowIndex);
        /*
         * Purge a Row that's no longer visible,
         * so that the JVM can reclaim its memory.
         */
        int purgeRowIndex = raftRowIndex - numUpstreamRows - 1;
        rows.remove(purgeRowIndex);

        if (isOver != null) {
            this.endTime = System.currentTimeMillis();
        }

        assert rows.keySet().size() == countVisibleRows();
        return isOver;
    }

    /**
     * Consume the specified number of patches.
     *
     * @param numPatches the number to consume (if positive) or add (if
     * negative)
     */
    public void consumePatches(int numPatches) {
        if (numRemainingPatches > numPatches) {
            this.numRemainingPatches -= numPatches;
        } else {
            this.numRemainingPatches = 0;
            terminate(Cause.SANK);
        }
    }

    /**
     * Count how many times the simulation has advanced.
     *
     * @return the count (&ge;0)
     */
    public int countAdvances() {
        return numAdvances;
    }

    /**
     * Count how many patches remain in the player's inventory.
     *
     * @return the count (&ge;0)
     */
    public int countRemainingPatches() {
        return numRemainingPatches;
    }

    /**
     * Count how many rows should be visualized.
     *
     * @return the count (&ge;0)
     */
    public static int countVisibleRows() {
        int result = numDownstreamRows + numUpstreamRows + 1;
        return result;
    }

    /**
     * Determine the duration of the game so far.
     *
     * @return the elapsed time (in seconds, &ge;0)
     */
    public float elapsedSeconds() {
        long elapsedMillis;
        if (isOver == null) {
            elapsedMillis = System.currentTimeMillis() - startTime;
        } else {
            elapsedMillis = endTime - startTime;
        }
        float result = elapsedMillis / 1000f;

        assert result >= 0f : result;
        return result;
    }

    /**
     * Find the indexed Row.
     *
     * @param rowIndex (&ge;0)
     * @return the pre-existing instance, or null if none
     */
    public Row findRow(int rowIndex) {
        Validate.nonNegative(rowIndex, "row index");

        Row result = rows.get(rowIndex);
        return result;
    }

    /**
     * Determine the highest valid row index.
     *
     * @return the index (&ge;0)
     */
    public int firstRowIndex() {
        int result = raftRowIndex + numDownstreamRows;
        assert result >= 0 : result;
        return result;
    }

    /**
     * Determine the lowest valid row index.
     *
     * @return the index (&ge;0)
     */
    public int lastRowIndex() {
        int result = raftRowIndex - numUpstreamRows;
        assert result >= 0 : result;
        return result;
    }

    /**
     * Locate the left edge of the raft.
     *
     * @return the world X coordinate
     */
    public int raftLeftX() {
        return raftLeftX;
    }

    /**
     * Locate the right edge of the raft.
     *
     * @return the world X coordinate
     */
    public int raftRightX() {
        int result = raftLeftX + raftWidth - 1;
        return result;
    }

    /**
     * Locate the raft.
     *
     * @return the row index (&ge;0)
     */
    public int raftRowIndex() {
        return raftRowIndex;
    }

    /**
     * Add the specified number of points to the player's score.
     *
     * @param numPoints the number to add (if positive) or subtract (if
     * negative)
     */
    public void scorePoints(int numPoints) {
        this.totalPoints += numPoints;
    }

    /**
     * Alter the time interval between advances.
     *
     * @param millis the desired interval (in milliseconds, &gt;0)
     */
    public void setAdvanceMillis(long millis) {
        this.advanceMillis = millis;
    }

    /**
     * Cause the game to end ... after the current/next advance!
     *
     * @param cause why the game is terminating
     */
    public void terminate(Cause cause) {
        this.isOver = cause;
    }

    /**
     * Determine the current score.
     *
     * @return the total number of points scored (may be negative)
     */
    public int totalPoints() {
        return totalPoints;
    }

    /**
     * Determine the vertical scrolling fraction. The return value decreases
     * over time, except during advances, when it increases by one.
     *
     * If the fraction is negative, then it's time to advance the simulation.
     *
     * @return the vertical displacement (in cell heights, &le;1)
     */
    public float verticalScrollingFraction() {
        long currentMillis = System.currentTimeMillis();
        long remainingMillis = nextAdvanceMillis - currentMillis;
        float result = remainingMillis / (float) advanceMillis;

        assert result <= 1f : result;
        return result;
    }
}
