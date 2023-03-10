package pl.edu.mimuw.matrix;

public class IdentityMatrix extends DiagonalSquareMatrix {

    public IdentityMatrix(int size) {
        super(size, 1);
    }

    public IdentityMatrix(Shape shape) {
        super(shape, 1);
    }

    // Identity matrix operations, which can be optimised additionally and independent of DiagonalMatrix are
    // multiplication (arguments is not changed) and multiplying by scalar (diagonal matrix is returned).
    @Override
    protected IDoubleMatrix timesInternal(IDoubleMatrix other) {
        return other;
    }

    @Override
    protected IDoubleMatrix timesInternal(double scalar) {
        return new DiagonalSquareMatrix(this.matrixShape, scalar);
    }

    @Override
    protected double getInternal(int row, int column) {
        if (row == column) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public double normOne() {
        return 1;
    }

    // FrobeniusNorm is optimised because Identity has only ones at the diagonal.
    @Override
    public double frobeniusNorm() {
        return Math.sqrt(this.matrixShape.rows);
    }
}
