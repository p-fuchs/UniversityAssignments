package pl.edu.mimuw.matrix;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DiagonalSquareMatrixTest {
    @Test
    public void normTest() {
        double[] values = {2,5,67,1,25,3,6,17,3,71,135,1,4,124,1,51,62,1,61,6,12,5,124,21,4,1,56,126,12,612};

        DiagonalSquareMatrix dsm1 = new DiagonalSquareMatrix(values.length, values);
        DiagonalSquareMatrix dsm2 = new DiagonalSquareMatrix(values.length, values);

        assertEquals(612, dsm1.normInfinity());
        assertEquals(612, dsm2.normOne());
    }

    @Test
    public void timesTest() {
        double[] values = {1,2,3,4,5,6,7,8,9};

        DiagonalSquareMatrix dsm1 = new DiagonalSquareMatrix(values.length, values);
        IDoubleMatrix res = dsm1.times(dsm1);

        for (int row = 0 ; row < 9 ; row++) {
            for (int col = 0 ; col < 9 ; col++) {
                if (row == col) {
                    assertEquals((row + 1) * (row + 1), res.get(row, col));
                } else {
                    assertEquals(0, res.get(row, col));
                }
            }
        }
    }

    @Test
    public void shapeTest() {
        Shape s = Shape.matrix(15, 50);
        IDoubleMatrix res = new ConstantMatrix(15, 50, 2);

        assertEquals(s, res.shape());
    }
}