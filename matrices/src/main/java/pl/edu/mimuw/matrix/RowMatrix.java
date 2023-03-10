package pl.edu.mimuw.matrix;

import java.util.Arrays;

public class RowMatrix extends RegularMatrix {
    // Values of columns since rows are the same.
    private final double[] columnValues;

    public RowMatrix(int rows, int columns, double[] columnValues) {
        super(rows, columns);
        assert columnValues.length == rows;

        this.columnValues = Arrays.copyOfRange(columnValues, 0, columnValues.length);
    }

    public RowMatrix(Shape shape, double[] columnValues) {
        super(shape);
        assert this.matrixShape.rows == columnValues.length;

        this.columnValues = Arrays.copyOfRange(columnValues, 0, columnValues.length);
    }

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
        return this.columnValues[column];
    }

    // Norms calculation can be optimised by knowing matrix sizes and column values.
    @Override
    public double normOne() {
        double maxCol = 0;
        for (double val : this.columnValues) {
            maxCol = Math.max(maxCol, Math.abs(val));
        }

        return maxCol * this.matrixShape.rows;
    }

    @Override
    public double normInfinity() {
        double sum = 0;
        for (double val : this.columnValues) {
            sum += Math.abs(val);
        }

        return sum;
    }

    @Override
    public double frobeniusNorm() {
        double squareSum = 0;
        for (double val : this.columnValues) {
            squareSum += val * val;
        }

        return Math.sqrt(squareSum * this.matrixShape.rows);
    }
}
