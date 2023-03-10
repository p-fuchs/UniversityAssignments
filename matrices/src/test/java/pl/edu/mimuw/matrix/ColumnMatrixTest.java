package pl.edu.mimuw.matrix;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ColumnMatrixTest {

    private static final double[] rowValues = {2.6, 2.3, 7.4, 2.3};

    @Test
    void timesInternal() {
        ColumnMatrix cm1 = new ColumnMatrix(4, 4, rowValues);
        ColumnMatrix cm2 = new ColumnMatrix(4, 4, rowValues);

        IDoubleMatrix res = cm1.times(cm2);
        double[][] result = res.data();
        assertEquals(result.length, 4);
        assertEquals(result[0].length, 4);

        double[][] expected = {
                {	37.96,	37.96,	37.96,	37.96},
                {33.58,	33.58,	33.58,	33.58},
                {108.04,	108.04,	108.04,108.04},
                {33.58	,33.58	,33.58,	33.58}
        };

        for (int row = 0 ; row < result.length ; row++) {
            assertEquals(result[row].length, 4);
            for (int col = 0 ; col < 4 ; col++) {
                assertEquals(result[row][col], expected[row][col]);
            }
        }
    }

    @Test
    void frobeniusNorm() {
        double[] rowValues = {2.3, 7.2, 12.5, 7.2 ,5};

        IDoubleMatrix mat = new ColumnMatrix(5, 7, rowValues);

        assertEquals(mat.frobeniusNorm(), Math.sqrt(37.03 + 362.88 + 1093.75 + 362.88 + 175));
    }

    @Test
    void normInfinity() {
        double[] rowValues = {2.3, 7.2, 12.5, 7.2 ,5};

        IDoubleMatrix mat = new ColumnMatrix(5, 7, rowValues);

        assertEquals(87.5, mat.normInfinity());
    }
}