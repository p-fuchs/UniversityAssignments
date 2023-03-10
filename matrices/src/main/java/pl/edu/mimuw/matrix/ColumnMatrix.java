package pl.edu.mimuw.matrix;

import java.util.Arrays;

public class ColumnMatrix extends RegularMatrix {
    // Since columns are the same, then values in rows are the same.
    private final double[] rowValues;

    public ColumnMatrix(int rows, int columns, double[] rowValues) {
        super(rows, columns);
        assert rowValues.length == rows;

        this.rowValues = Arrays.copyOfRange(rowValues, 0, rowValues.length);
    }

    public ColumnMatrix(Shape shape, double[] rowValues) {
        super(shape);
        assert this.matrixShape.rows == rowValues.length;

        this.rowValues = Arrays.copyOfRange(rowValues, 0, rowValues.length);
    }

    // Methods implement behaviour as described in DoubleMatrix class.
    // Some operations are optimized to return ColumnMatrix class.

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
        if (other.getClass() == ColumnMatrix.class) {
            ColumnMatrix otherMatrix = (ColumnMatrix) other;
            double[] resRowValues = new double[this.matrixShape.rows];

            for (int index = 0; index < this.matrixShape.rows; index++) {
                resRowValues[index] = this.rowValues[index] + otherMatrix.rowValues[index];
            }

            return new ColumnMatrix(this.matrixShape, resRowValues);
        }
        return this.genericPlus(other);
    }

    @Override
    protected IDoubleMatrix plusInternal(double scalar) {
        double[] resRowValues = new double[this.matrixShape.rows];

        for (int index = 0; index < this.matrixShape.rows; index++) {
            resRowValues[index] = this.rowValues[index] + scalar;
        }

        return new ColumnMatrix(this.matrixShape, resRowValues);
    }

    @Override
    protected IDoubleMatrix minusInternal(IDoubleMatrix other) {
        if (other.getClass() == ColumnMatrix.class) {
            ColumnMatrix otherMatrix = (ColumnMatrix) other;
            double[] resRowValues = new double[this.matrixShape.rows];

            for (int index = 0; index < this.matrixShape.rows; index++) {
                resRowValues[index] = this.rowValues[index] - otherMatrix.rowValues[index];
            }

            return new ColumnMatrix(this.matrixShape, resRowValues);
        }
        return this.genericMinus(other);
    }

    @Override
    protected double getInternal(int row, int column) {
        return this.rowValues[row];
    }

    @Override
    public double normOne() {
        double sum = 0;
        for (double val : this.rowValues) {
            sum += Math.abs(val);
        }

        return sum;
    }

    @Override
    public double normInfinity() {
        double maxRow = 0;
        for (double val : this.rowValues) {
            maxRow = Math.max(maxRow, Math.abs(val));
        }

        return maxRow * this.matrixShape.columns;
    }

    @Override
    public double frobeniusNorm() {
        double squareSum = 0;
        for (double val : this.rowValues) {
            squareSum += val * val;
        }

        return Math.sqrt(squareSum * this.matrixShape.columns);
    }

    @Override
    protected String toStringInternal() {
        StringBuilder sb = new StringBuilder();

        if (this.matrixShape.columns < 3) {
            for (int row = 0; row < this.matrixShape.rows; row++) {
                for (int col = 0; col < this.matrixShape.columns; col++) {
                    if (col != 0) {
                        sb.append(' ');
                    }

                    sb.append(DoubleMatrix.formatter.format(this.rowValues[row]));
                }
                sb.append('\n');
            }
        } else {
            for (int row = 0; row < this.matrixShape.rows; row++) {
                sb.append(DoubleMatrix.formatter.format(this.rowValues[row]));
                sb.append(" ... ");
                sb.append(DoubleMatrix.formatter.format(this.rowValues[row]));
                sb.append('\n');
            }
        }

        return sb.toString();
    }
}
