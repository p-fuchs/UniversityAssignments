package pl.edu.mimuw.matrix;

public class ZeroMatrix extends ConstantMatrix {
    public ZeroMatrix(int rows, int columns) {
        super(rows, columns, 0);
    }

    public ZeroMatrix(Shape shape) {
        super(shape, 0);
    }

    /**
     * Optimalization done -> zero matrix times any other is equal to zero matrix.
     *
     * @param other: second matrix of multiplication, which can be safely multiplied (has correct sizes).
     * @return zero matrix of appropriate size (rows of this, columns of other)
     */
    @Override
    protected IDoubleMatrix timesInternal(IDoubleMatrix other) {
        return new ZeroMatrix(this.matrixShape.rows, other.shape().columns);
    }

    /**
     * Function to multiply by scalar, it doesn't change anything.
     *
     * @param scalar: non-zero scalar to multiply matrix by.
     * @return this matrix.
     */
    @Override
    protected IDoubleMatrix timesInternal(double scalar) {
        return this;
    }

    /**
     * Function to add other matrix (nothing is changed).
     *
     * @param other: second matrix of addition, which can be safely added (has correct sizes).
     * @return other matrix.
     */
    @Override
    protected IDoubleMatrix plusInternal(IDoubleMatrix other) {
        return other;
    }

    /**
     * Adds a scalar to all values of matrix.
     *
     * @param scalar non-zero scalar to be added.
     * @return new constant matrix with @p scalar values.
     */
    @Override
    protected IDoubleMatrix plusInternal(double scalar) {
        return new ConstantMatrix(this.matrixShape, scalar);
    }

    /**
     * Function to subtract other matrix.
     *
     * @param other: second matrix of subtraction, which can be safely subtract (has correct sizes).
     * @return other matrix multiplied by -1.
     */
    @Override
    protected IDoubleMatrix minusInternal(IDoubleMatrix other) {
        return other.times(-1);
    }

    // Following methods all return 0 -> norms and all values of zero matrix are 0.
    @Override
    protected double getInternal(int row, int column) {
        return 0;
    }

    @Override
    public double normOne() {
        return 0;
    }

    @Override
    public double normInfinity() {
        return 0;
    }

    @Override
    public double frobeniusNorm() {
        return 0;
    }

    @Override
    protected String toStringInternal() {
        StringBuilder resString = new StringBuilder();

        for (int row = 0; row < this.matrixShape.rows; row++) {
            if (this.matrixShape.columns >= 3) {
                resString.append("0 ... 0\n");
            } else {
                for (int zero = 0; zero < this.matrixShape.columns; zero++) {
                    if (zero != 0) {
                        resString.append(' ');
                    }
                    resString.append('0');
                }

                resString.append('\n');
            }
        }

        return resString.toString();
    }
}
