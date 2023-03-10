package pl.edu.mimuw.matrix;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConstantMatrixTest {

    @Test
    void addition() {
        ConstantMatrix cs = new ConstantMatrix(1000, 1000, 10);
        ConstantMatrix cs2 = new ConstantMatrix(1000, 1000, 150);

        double[][] values = cs.plus(cs2).data();
        assertEquals(1000, values.length);

        for (int row = 0 ; row < 1000 ; row++) {
            assertEquals(1000, values[row].length);
            for (int col = 0 ; col < 1000 ; col++) {
                assertEquals(160.0, values[row][col]);
            }
        }
    }

    @Test
    void classOptimalization() {
        ConstantMatrix cm1 = new ConstantMatrix(150, 150, 1515);

        assertEquals(true, cm1.plus(cm1).getClass() == ConstantMatrix.class);
    }

    @Test
    void normOne() {
        ConstantMatrix cm = new ConstantMatrix(1000000000, 1000000000, 2);

        assertEquals(2000000000, cm.normOne());
    }
}