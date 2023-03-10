package pl.edu.mimuw.matrix;

public class ConstantMatrix extends RegularMatrix {
    private final double value;

    public ConstantMatrix(int rows, int columns, double value) {
        super(rows, columns);
        this.value = value;
    }

    public ConstantMatrix(Shape shape, double value) {
        super(shape);
        this.value = value;
    }

    @Override
    protected IDoubleMatrix timesInternal(IDoubleMatrix other) {
        if (other instanceof ConstantMatrix) {
            double resVal = this.value * ((ConstantMatrix) other).value * this.matrixShape.columns;

            return new ConstantMatrix(this.matrixShape.rows, other.shape().columns, resVal);
        }

        return this.genericTimes(other);
    }

    @Override
    protected IDoubleMatrix timesInternal(double scalar) {
        return new ConstantMatrix(this.matrixShape, this.value * scalar);
    }

    @Override
    protected IDoubleMatrix plusInternal(IDoubleMatrix other) {
        if (other instanceof ConstantMatrix) {
            double resVal = this.value + ((ConstantMatrix) other).value;

            if (resVal == 0) {
                return new ZeroMatrix(this.matrixShape);
            }

            return new ConstantMatrix(this.matrixShape, resVal);
        }

        return this.genericPlus(other);
    }

    @Override
    protected IDoubleMatrix plusInternal(double scalar) {
        if (this.value + scalar == 0) {
            return new ZeroMatrix(this.matrixShape);
        }

        return new ConstantMatrix(this.matrixShape, this.value + scalar);
    }

    @Override
    protected IDoubleMatrix minusInternal(IDoubleMatrix other) {
        if (other instanceof ConstantMatrix) {
            double resVal = this.value - ((ConstantMatrix) other).value;

            if (resVal == 0) {
                return new ZeroMatrix(this.matrixShape);
            }

            return new ConstantMatrix(this.matrixShape, resVal);
        }

        return this.genericMinus(other);
    }

    @Override
    protected double getInternal(int row, int column) {
        return this.value;
    }

    // Norms are calculated based on formulas got from specifying type of matrix
    // where all values are the same, therefore we can calculate them in O(1) time.
    @Override
    public double normOne() {
        return this.value * this.matrixShape.rows;
    }

    @Override
    public double normInfinity() {
        return this.value * this.matrixShape.columns;
    }

    @Override
    public double frobeniusNorm() {
        return this.value * Math.sqrt(this.matrixShape.columns * this.matrixShape.rows);
    }

    @Override
    protected String toStringInternal() {
        StringBuilder resString = new StringBuilder();

        if (this.matrixShape.columns >= 3) {
            StringBuilder row = new StringBuilder();

            row.append(DoubleMatrix.formatter.format(this.value));
            row.append(" ... ");
            row.append(DoubleMatrix.formatter.format(this.value));
            row.append('\n');

            String rowString = row.toString();
            for (int _row = 0; _row < this.matrixShape.rows; _row++) {
                resString.append(rowString);
            }
        } else {
            for (int _row = 0; _row < this.matrixShape.rows; _row++) {
                for (int col = 0; col < this.matrixShape.columns; col++) {
                    if (col != 0) {
                        resString.append(' ');
                    }
                    resString.append(DoubleMatrix.formatter.format(this.value));
                }

                resString.append('\n');
            }
        }

        return resString.toString();
    }
}
