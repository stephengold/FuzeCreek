/*
 Copyright (c) 2023, Stephen Gold

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

import com.github.stephengold.fuzecreek.Cell;
import com.github.stephengold.fuzecreek.DryLandCell;
import com.github.stephengold.fuzecreek.LeftBankCell;
import com.github.stephengold.fuzecreek.MineCell;
import com.github.stephengold.fuzecreek.RightBankCell;
import com.github.stephengold.fuzecreek.RockCell;
import com.github.stephengold.fuzecreek.WaterOnlyCell;
import java.util.logging.Logger;

/**
 * Display information for a Cell in the FC3D application.
 *
 * @author Stephen Gold sgold@sonic.net
 */
class CellViewData {
    // *************************************************************************
    // constants and loggers

    /**
     * mesh Y coordinate for dry terrain
     */
    final static float dryY = FC3D.dryLandY - FC3D.waterY;
    /**
     * mesh Y coordinate for underwater terrain
     */
    final private static float wetY = -dryY;
    /**
     * message logger for this class
     */
    final static Logger logger = Logger.getLogger(CellViewData.class.getName());
    // *************************************************************************
    // fields

    /**
     * true if the corresponding Cell contains a mine, otherwise false
     */
    private boolean hasMine;
    /**
     * true if the corresponding Cell contains a rock, otherwise false
     */
    private boolean hasRock;
    /**
     * mesh Y coordinate for the left edge of the cell
     */
    private float leftY;
    /**
     * mesh Y coordinate for the right edge of the cell
     */
    private float rightY;
    // *************************************************************************
    // constructors

    /**
     * Construct view data for the specified Cell.
     *
     * @param cell (not null, unaffected)
     */
    CellViewData(Cell cell) {
        if (cell instanceof DryLandCell) {
            this.hasMine = false;
            this.hasRock = false;
            this.leftY = dryY;
            this.rightY = dryY;

        } else if (cell instanceof LeftBankCell) {
            this.hasMine = false;
            this.hasRock = false;
            this.leftY = dryY;
            this.rightY = wetY;

        } else if (cell instanceof MineCell) {
            this.hasMine = true;
            this.hasRock = false;
            this.leftY = wetY;
            this.rightY = wetY;

        } else if (cell instanceof RightBankCell) {
            this.hasMine = false;
            this.hasRock = false;
            this.leftY = wetY;
            this.rightY = dryY;

        } else if (cell instanceof RockCell) {
            this.hasMine = false;
            this.hasRock = true;
            this.leftY = wetY;
            this.rightY = wetY;

        } else if (cell instanceof WaterOnlyCell) {
            this.hasMine = false;
            this.hasRock = false;
            this.leftY = wetY;
            this.rightY = wetY;

        } else {
            String className = cell.getClass().getSimpleName();
            throw new IllegalArgumentException(className);
        }
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Test whether the corresponding Cell contains a mine.
     *
     * @return true if there's a mine, otherwise false
     */
    boolean hasMine() {
        return hasMine;
    }

    /**
     * Test whether the corresponding Cell contains a rock.
     *
     * @return true if there's a rock, otherwise false
     */
    boolean hasRock() {
        return hasRock;
    }

    /**
     * Return the terrain height at the left edge of the corresponding Cell.
     *
     * @return the mesh Y coordinate
     */
    float leftY() {
        return leftY;
    }

    /**
     * Return the terrain height at the right edge of the corresponding Cell.
     *
     * @return the mesh Y coordinate
     */
    float rightY() {
        return rightY;
    }
}
