package pl.edu.mimuw.matrix;

import java.util.Collection;

public class FullMatrix extends DoubleMatrix {
    // Full matrix requires storing all values in two-dimensional array.
    private final double[][] matrixValues;

    public FullMatrix(int rows, int columns, double[][] matrixValues) {
        super(rows, columns);

        checkMatrixSizes(matrixValues);
        double[][] valArray = new double[this.matrixShape.rows][this.matrixShape.columns];

        for (int i = 0; i < this.matrixShape.rows; i++) {
            for (int j = 0; j < this.matrixShape.columns; j++) {
                valArray[i][j] = matrixValues[i][j];
            }
        }

        this.matrixValues = valArray;
    }

    public FullMatrix(int rows, int columns, Collection<MatrixCellValue> matrixValues) {
        super(rows, columns);

        this.checkCollectionCorectness(matrixValues);
        double[][] valArray = new double[this.matrixShape.rows][this.matrixShape.columns];

        for (MatrixCellValue mcv : matrixValues) {
            valArray[mcv.row][mcv.column] = mcv.value;
        }

        this.matrixValues = valArray;
    }

    public FullMatrix(Shape shape, double[][] matrixValues) {
        super(shape);

        checkMatrixSizes(matrixValues);
        double[][] valArray = new double[this.matrixShape.rows][this.matrixShape.columns];

        for (int i = 0; i < this.matrixShape.rows; i++) {
            for (int j = 0; j < this.matrixShape.columns; j++) {
                valArray[i][j] = matrixValues[i][j];
            }
        }

        this.matrixValues = valArray;
    }

    /**
     * Function asserts if array has shape of rectangle.
     *
     * @param matrixValues array to check.
     */
    private void checkMatrixSizes(double[][] matrixValues) {
        assert matrixValues.length == this.matrixShape.rows;
        assert matrixValues[0].length == this.matrixShape.columns;

        for (int i = 1; i < matrixValues.length; i++) {
            assert matrixValues[i].length == matrixValues[0].length;
        }
    }

    // Full-Matrix operations cannot be simply optimized, so general method are called.

    @Override
    protected IDoubleMatrix timesInternal(IDoubleMatrix other) {
        return this.genericTimes(other);
    }

    @Override
    protected IDoubleMatrix timesInternal(double scalar) {
        return this.genericTimes(scalar);
    }

    @Override
    protected IDoubleMatrix plusInternal(IDoubleMatrix other) {
        return this.genericPlus(other);
    }

    @Override
    protected IDoubleMatrix plusInternal(double scalar) {
        return this.genericPlus(scalar);
    }

    @Override
    protected IDoubleMatrix minusInternal(IDoubleMatrix other) {
        return this.genericMinus(other);
    }

    @Override
    protected double getInternal(int row, int column) {
        return this.matrixValues[row][column];
    }

    // Norms are also calculated by general formulas, nothing can be optimised.
    @Override
    public double normOne() {
        double normMax = 0;

        for (int col = 0; col < this.matrixShape.columns; col++) {
            double sum = 0;
            for (int row = 0; row < this.matrixShape.rows; row++) {
                sum += Math.abs(this.matrixValues[row][col]);
            }

            normMax = Math.max(normMax, sum);
        }

        return normMax;
    }

    @Override
    public double normInfinity() {
        double normMax = 0;

        for (int row = 0; row < this.matrixShape.rows; row++) {
            double sum = 0;
            for (int col = 0; col < this.matrixShape.columns; col++) {
                sum += Math.abs(this.matrixValues[row][col]);
            }

            normMax = Math.max(normMax, sum);
        }

        return normMax;
    }

    @Override
    public double frobeniusNorm() {
        double sum = 0;

        for (int row = 0; row < this.matrixShape.rows; row++) {
            for (int col = 0; col < this.matrixShape.columns; col++) {
                sum += this.matrixValues[row][col] * this.matrixValues[row][col];
            }
        }

        return Math.sqrt(sum);
    }

    @Override
    public String toStringInternal() {
        StringBuilder sb = new StringBuilder();

        for (int row = 0; row < this.matrixShape.rows; row++) {
            for (int col = 0; col < this.matrixShape.columns; col++) {
                if (col != 0) {
                    sb.append(' ');
                }
                sb.append(formatter.format(this.matrixValues[row][col]));
            }
            sb.append('\n');
        }

        return sb.toString();
    }
}
