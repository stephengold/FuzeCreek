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

/**
 * A grid cell that contains the creek's right bank.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class RightBankCell extends BankCell {
    // *************************************************************************
    // constructors

    /**
     * Instantiate a RightBankCell without any display information.
     *
     * @param row the Row to contain the Cell (not null)
     * @param mapX the desired map X coordinate (may be negative)
     * @param downstreamDeltaX the relative position of the right bank in the
     * downstream Row (-1, 0, or +1)
     */
    RightBankCell(Row row, int mapX, int downstreamDeltaX) {
        super(row, mapX, downstreamDeltaX);
    }
    // *************************************************************************
    // BankCell methods

    /**
     * Determine the change in location of the bank, from the upstream Row to
     * this cell's Row.
     *
     * @return the increase in the map X coordinate (-1, 0, or +1)
     */
    @Override
    public int upstreamDeltaX() {
        int result;

        Row r = getRow();
        Row upstream = r.findUpstreamRow();
        if (upstream == null) {
            result = 0;
        } else {
            result = upstream.rightDeltaX();
        }

        return result;
    }
}
