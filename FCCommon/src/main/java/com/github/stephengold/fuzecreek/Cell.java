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

import jme3utilities.Validate;

/**
 * A grid cell in the rafting game, part of a Row.
 *
 * @author Stephen Gold sgold@sonic.net
 */
abstract public class Cell {
    // *************************************************************************
    // fields

    /**
     * map X coordinate (may be negative)
     */
    final protected int mapX;
    /**
     * information used to visualize this Cell
     */
    private Object viewData;
    /**
     * Row that contains this Cell
     */
    final private Row row;
    // *************************************************************************
    // constructors

    /**
     * Instantiate a Cell without any display information.
     *
     * @param row the Row to contain the Cell (not null)
     * @param mapX the desired map X coordinate (may be negative)
     */
    protected Cell(Row row, int mapX) {
        Validate.nonNull(row, "row");

        this.row = row;
        this.mapX = mapX;
    }
    // *************************************************************************
    // new methods added

    /**
     * Callback invoked each time a player collects this Cell (by maneuvering
     * their raft alongside it). Meant to be overridden.
     */
    public void collect() {
        // do nothing
    }

    /**
     * Callback invoked each time a raft collides with this Cell. Meant to be
     * overridden.
     */
    public void collide() {
        // do nothing
    }

    /**
     * Access the information used to visualize this Cell.
     *
     * @return the display information, or null if none
     */
    final public Object getViewData() {
        return viewData;
    }

    /**
     * Replace the information used to visualize this Cell.
     *
     * @param viewData the desired display information, or null for none
     */
    final public void setViewData(Object viewData) {
        this.viewData = viewData;
    }
    // *************************************************************************
    // new protected methods

    /**
     * Access the Row containing this Cell.
     *
     * @return
     */
    final protected Row getRow() {
        return row;
    }
}
