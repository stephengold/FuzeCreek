/*
 Copyright (c) 2023, Stephen Gold

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright
 notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
 notice, this list of conditions and the following disclaimer in the
 documentation and/or other materials provided with the distribution.
 * Neither the name of the copyright holder nor the names of its contributors
 may be used to endorse or promote products derived from this software without
 specific prior written permission.

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

import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.math.Triangle;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.util.BufferUtils;
import com.jme3.util.clone.Cloner;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.logging.Logger;
import jme3utilities.MyMesh;
import jme3utilities.Validate;
import jme3utilities.math.MyVector3f;

/**
 * A dynamic, Triangles-mode Mesh (with normals but no indices) that renders a
 * 2-by-N terrain patch in the FC3D application. Cloning and serialization
 * aren't implemented.
 *
 * @author Stephen Gold sgold@sonic.net
 */
class RowMesh extends Mesh {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final static Logger logger = Logger.getLogger(RowMesh.class.getName());
    // *************************************************************************
    // fields

    /**
     * height map
     */
    final private float[][] heightMap;
    /**
     * number of grid lines parallel to the X axis
     */
    final private int numXLines;
    /**
     * temporary storage for the normal vector and vertex positions of a single
     * mesh triangle
     */
    final private Triangle tmpTriangle = new Triangle();
    final private Vector3f n = tmpTriangle.getNormal(); // alias
    final private Vector3f v1 = tmpTriangle.get1(); // alias
    final private Vector3f v2 = tmpTriangle.get2(); // alias
    final private Vector3f v3 = tmpTriangle.get3(); // alias
    // *************************************************************************
    // constructors

    /**
     * Instantiate a grid in the X-Z plane.
     */
    RowMesh() {
        this.numXLines = FC3D.numColumns + 1;
        this.heightMap = new float[2][numXLines];

        int numCells = numXLines - 1;
        int numTriangles = 2 * numCells;
        int numVertices = MyMesh.vpt * numTriangles;
        int numFloats = MyVector3f.numAxes * numVertices;

        FloatBuffer posBuffer
                = BufferUtils.createFloatBuffer(numFloats);
        setBuffer(VertexBuffer.Type.Position, MyVector3f.numAxes, posBuffer);

        FloatBuffer normBuffer
                = BufferUtils.createFloatBuffer(numFloats);
        setBuffer(VertexBuffer.Type.Normal, MyVector3f.numAxes, normBuffer);

        updateVertices();
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Update the specified element of the height map.
     *
     * @param row the row index (0 or 1)
     * @param column the column index (&ge;0, &lt;numXLines)
     * @param heightY the desired height (mesh Y coordinate)
     */
    void setHeight(int row, int column, float heightY) {
        Validate.inRange(row, "row", 0, 1);
        Validate.inRange(column, "column", 0, numXLines - 1);

        this.heightMap[row][column] = heightY;
    }

    /**
     * Update the vertex buffers for the current height map.
     */
    final void updateVertices() {
        FloatBuffer posBuffer = getFloatBuffer(VertexBuffer.Type.Position);
        FloatBuffer normBuffer = getFloatBuffer(VertexBuffer.Type.Normal);
        posBuffer.clear();
        normBuffer.clear();

        float x0 = 0f;
        float x1 = FC3D.cellXWidth;
        int numCells = numXLines - 1;
        for (int cellIndex = 0; cellIndex < numCells; ++cellIndex) {
            float z0 = cellIndex * FC3D.cellZWidth;
            float z1 = (cellIndex + 1) * FC3D.cellZWidth;

            // Copy the corner heights.
            float y00 = heightMap[0][cellIndex];
            float y01 = heightMap[0][cellIndex + 1];
            float y10 = heightMap[1][cellIndex];
            float y11 = heightMap[1][cellIndex + 1];

            if (y00 == y11) { // The cell's diagonal connects y00 with y11.
                v1.set(x0, y00, z0);
                v2.set(x0, y01, z1);
                v3.set(x1, y11, z1);
                putTriangle();

                v1.set(x0, y00, z0);
                v2.set(x1, y11, z1);
                v3.set(x1, y10, z0);
                putTriangle();

            } else { // diagonal connects y01 with y10: different vertex order
                assert y01 == y10 || (y11 - y10) == (y01 - y00);

                v1.set(x0, y00, z0);
                v2.set(x0, y01, z1);
                v3.set(x1, y10, z0);
                putTriangle();

                v1.set(x0, y01, z1);
                v2.set(x1, y11, z1);
                v3.set(x1, y10, z0);
                putTriangle();
            }
        }

        assert posBuffer.position() == posBuffer.capacity();
        assert normBuffer.position() == normBuffer.capacity();
        posBuffer.flip();
        normBuffer.flip();

        updateBound();
        setDynamic();
    }
    // *************************************************************************
    // Mesh methods

    /**
     * Callback from {@link com.jme3.util.clone.Cloner} to convert this
     * shallow-cloned Mesh into a deep-cloned one, using the specified Cloner
     * and original to resolve copied fields.
     *
     * @param cloner the Cloner that's cloning this Mesh (not null)
     * @param original the instance from which this Mesh was shallow-cloned (not
     * null, unaffected)
     */
    @Override
    public void cloneFields(Cloner cloner, Object original) {
        throw new UnsupportedOperationException();
    }

    /**
     * De-serialize this mesh from the specified importer, for example when
     * loading from a J3O file.
     *
     * @param importer (not null)
     * @throws IOException from the importer
     */
    @Override
    public void read(JmeImporter importer) throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Serialize this Mesh to the specified exporter, for example when saving to
     * a J3O file.
     *
     * @param exporter (not null)
     * @throws IOException from the exporter
     */
    @Override
    public void write(JmeExporter exporter) throws IOException {
        throw new UnsupportedOperationException();
    }
    // *************************************************************************
    // private methods

    /**
     * Write the vertices of {@code tmpTriangle} to the vertex buffers.
     */
    private void putTriangle() {
        FloatBuffer posBuffer = getFloatBuffer(VertexBuffer.Type.Position);
        posBuffer.put(v1.x).put(v1.y).put(v1.z);
        posBuffer.put(v2.x).put(v2.y).put(v2.z);
        posBuffer.put(v3.x).put(v3.y).put(v3.z);

        tmpTriangle.calculateNormal();

        FloatBuffer normBuffer = getFloatBuffer(VertexBuffer.Type.Normal);
        normBuffer.put(n.x).put(n.y).put(n.z);
        normBuffer.put(n.x).put(n.y).put(n.z);
        normBuffer.put(n.x).put(n.y).put(n.z);
    }
}
