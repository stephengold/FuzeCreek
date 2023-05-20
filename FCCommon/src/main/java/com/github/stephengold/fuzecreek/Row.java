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

import java.util.logging.Logger;
import jme3utilities.Validate;

/**
 * A row (of grid cells) that represents a short section of the creek in the
 * rafting game.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class Row {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(Row.class.getName());
    // *************************************************************************
    // fields

    /**
     * access cells in this Row by index, from leftmost (0) to rightmost
     * (numCells - 1)
     */
    final private Cell[] cells;
    /**
     * game that contains this Row
     */
    final public GameState gameState;
    /**
     * map X coordinate of the left bank (may be negative)
     */
    final public int leftBankX;
    /**
     * map X coordinate of the right bank (may be negative)
     */
    final public int rightBankX;
    /**
     * index in the game's array of rows (&ge;0)
     */
    final private int rowIndex;
    /**
     * index of the cell with X=0 (&ge;-leftBankX)
     */
    final private int x0Index;
    // *************************************************************************
    // constructors

    /**
     * Instantiate a Row without any display information.
     *
     * @param gameState the game to contain the Row (not null)
     * @param rowIndex the desired row index (&ge;0)
     * @param numCells the desired number of cells (&ge; minWidth + 4)
     * @param x0Index the index of the cell with X=0 (&ge; 2 - leftBankX)
     * @param leftBankX the map X coordinate of the left bank (&lt; rightBankX,
     * &ge;-x0Index)
     * @param rightBankX the map X coordinate of the right bank (&gt; leftBankX)
     * @param leftDeltaX the relative position of the left bank in the
     * downstream Row (-1, 0, or +1)
     * @param rightDeltaX the relative position of the right bank in the
     * downstream Row (-1, 0, or +1)
     * @param rockProbability the probability that a particular cell contains a
     * rock (&ge;0, &le;1)
     * @param mineProbability the probability that a particular cell contains a
     * mine (&ge;0, &le;1)
     */
    Row(GameState gameState, int rowIndex, int numCells, int x0Index,
            int leftBankX, int rightBankX, int leftDeltaX, int rightDeltaX,
            float rockProbability, float mineProbability) {
        Validate.nonNull(gameState, "game state");
        Validate.nonNegative(rowIndex, "row index");
        Validate.inRange(numCells, "number of cells",
                GameState.minCreekWidth + 4, Integer.MAX_VALUE);
        Validate.require(leftBankX < rightBankX, "left-bank X < right-bank X");
        Validate.inRange(leftDeltaX, "left delta X", -1, +1);
        Validate.inRange(rightDeltaX, "right delta X", -1, +1);
        Validate.fraction(rockProbability, "rock probability");
        Validate.fraction(mineProbability, "mine probability");

        int leftBankIndex = leftBankX + x0Index;
        assert leftBankIndex >= 2 : leftBankX + " + " + x0Index;
        int rightBankIndex = rightBankX + x0Index;
        assert rightBankIndex < numCells - 1 : rightBankX + " + " + x0Index;

        this.gameState = gameState;
        this.rowIndex = rowIndex;
        this.leftBankX = leftBankX;
        this.rightBankX = rightBankX;
        this.x0Index = x0Index;

        this.cells = new Cell[numCells];

        float nonEmptyProbability = rockProbability + mineProbability;
        for (int cellIndex = 0; cellIndex < numCells; ++cellIndex) {
            int x = cellIndex - x0Index;

            Cell newCell;
            if (x == leftBankX) {
                newCell = new LeftBankCell(this, x, leftDeltaX);
            } else if (x == rightBankX) {
                newCell = new RightBankCell(this, x, rightDeltaX);
            } else if (x < leftBankX || x > rightBankX) {
                newCell = new DryLandCell(this, x);
            } else {
                float randomValue = gameState.generator.nextFloat();
                if (randomValue < mineProbability) {
                    newCell = new MineCell(this, x);
                } else if (randomValue < nonEmptyProbability) {
                    newCell = new RockCell(this, x);
                } else {
                    newCell = new WaterOnlyCell(this, x);
                }
            }

            this.cells[cellIndex] = newCell;
            gameState.view.initializeCellViewData(newCell);
        }
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Collect the Cell at the specified X coordinate.
     *
     * @param mapX the map X coordinate of the Cell (may be negative)
     */
    public void collectCell(int mapX) {
        int cellIndex = mapX + x0Index;
        Cell newCell = new WaterOnlyCell(this, mapX);
        this.cells[cellIndex] = newCell;

        gameState.view.initializeCellViewData(newCell);
    }

    /**
     * Access the cell at the specified X coordinate.
     *
     * @param mapX the map X coordinate (may be negative)
     * @return the pre-existing instance, or null if none
     */
    public Cell findCell(int mapX) {
        int cellIndex = mapX + x0Index;

        Cell result = null;
        if (cellIndex >= 0 && cellIndex < cells.length) {
            result = cells[cellIndex];
        }

        return result;
    }

    /**
     * Find the row just upstream from this one.
     *
     * @return the pre-existing instance, or null if none
     */
    public Row findUpstreamRow() {
        Row result = null;
        if (rowIndex > 0) {
            result = gameState.findRow(rowIndex - 1);
        }

        return result;
    }

    /**
     * Determine the downstream deltaX of the left bank.
     *
     * @return the relative position of the left bank in the downstream Row (-1,
     * 0, or +1)
     */
    public int leftDeltaX() {
        LeftBankCell leftBankCell = (LeftBankCell) findCell(leftBankX);
        int result = leftBankCell.downstreamDeltaX;

        assert result >= -1 : result;
        assert result <= +1 : result;
        return result;
    }

    /**
     * Determine the downstream deltaX of the right bank.
     *
     * @return the relative position of the right bank in the downstream Row
     * (-1, 0, or +1)
     */
    public int rightDeltaX() {
        RightBankCell rightBankCell = (RightBankCell) findCell(rightBankX);
        int result = rightBankCell.downstreamDeltaX;

        assert result >= -1 : result;
        assert result <= +1 : result;
        return result;
    }
}
