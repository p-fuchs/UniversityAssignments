package pl.edu.mimuw.matrix;

import java.util.Arrays;

public class DiagonalSquareMatrix extends RegularMatrix {
    // Array to store values at diagonal of the matrix.
    private final double[] diagonal;

    public DiagonalSquareMatrix(int size, double[] diagonal) {
        super(size, size);
        assert diagonal.length == size;

        this.diagonal = Arrays.copyOfRange(diagonal, 0, size);
    }

    public DiagonalSquareMatrix(Shape shape, double[] diagonal) {
        super(shape);

        assert shape.columns == shape.rows;
        assert shape.rows == diagonal.length;

        this.diagonal = Arrays.copyOfRange(diagonal, 0, diagonal.length);
    }

    public DiagonalSquareMatrix(int size, double diagValues) {
        super(size, size);

        this.diagonal = new double[size];
        for (int i = 0; i < size; i++) {
            this.diagonal[i] = diagValues;
        }
    }

    public DiagonalSquareMatrix(Shape shape, double diagValues) {
        super(shape);
        assert shape.rows == shape.columns;

        this.diagonal = new double[shape.rows];
        for (int i = 0; i < shape.rows; i++) {
            this.diagonal[i] = diagValues;
        }
    }

    // Operations optimized if other matrix is of the class DiagonalSquareMatrix,
    // otherwise general methods, which are defined in DoubleMatrix class are performed.
    @Override
    protected IDoubleMatrix timesInternal(IDoubleMatrix other) {
        if (other instanceof DiagonalSquareMatrix second) {
            double[] newDiag = new double[this.matrixShape.rows];

            for (int i = 0; i < newDiag.length; i++) {
                newDiag[i] = this.diagonal[i] * second.diagonal[i];
            }

            return new DiagonalSquareMatrix(this.shape(), newDiag);
        }

        return this.genericTimes(other);
    }

    @Override
    protected IDoubleMatrix timesInternal(double scalar) {
        double[] newDiag = new double[this.diagonal.length];

        for (int i = 0; i < newDiag.length; i++) {
            newDiag[i] = this.diagonal[i] * scalar;
        }

        return new DiagonalSquareMatrix(this.matrixShape, newDiag);
    }

    @Override
    protected IDoubleMatrix plusInternal(IDoubleMatrix other) {
        if (other instanceof DiagonalSquareMatrix second) {
            double[] newDiag = new double[this.diagonal.length];

            for (int i = 0; i < newDiag.length; i++) {
                newDiag[i] = this.diagonal[i] + second.diagonal[i];
            }

            return new DiagonalSquareMatrix(this.matrixShape, newDiag);
        }

        return this.genericPlus(other);
    }

    @Override
    protected IDoubleMatrix plusInternal(double scalar) {
        return this.genericPlus(scalar);
    }

    @Override
    protected IDoubleMatrix minusInternal(IDoubleMatrix other) {
        if (other instanceof DiagonalSquareMatrix second) {
            double[] newDiag = new double[this.diagonal.length];

            for (int i = 0; i < newDiag.length; i++) {
                newDiag[i] = this.diagonal[i] - second.diagonal[i];
            }

            return new DiagonalSquareMatrix(this.matrixShape, newDiag);
        }

        return this.genericMinus(other);
    }

    @Override
    protected double getInternal(int row, int column) {
        if (row == column) {
            return this.diagonal[row];
        }

        return 0;
    }

    @Override
    public double normOne() {
        double maksimum = 0;
        for (double value : this.diagonal) {
            maksimum = Math.max(Math.abs(value), maksimum);
        }

        return maksimum;
    }

    @Override
    public double normInfinity() {
        return this.normOne();
    }

    @Override
    public double frobeniusNorm() {
        double sum = 0;
        for (double value : this.diagonal) {
            sum += value * value;
        }

        return Math.sqrt(sum);
    }

    @Override
    protected String toStringInternal() {
        StringBuilder resString = new StringBuilder();

        int zerosFront = 0;
        int zerosBack = this.matrixShape.columns - 1;

        for (int row = 0; row < this.matrixShape.rows; row++) {
            appendZeros(resString, zerosFront);

            if (zerosFront != 0) {
                resString.append(' ');
            }

            resString.append(this.diagonal[row]);
            resString.append(' ');

            appendZeros(resString, zerosBack);

            zerosFront += 1;
            zerosBack -= 1;
            resString.append('\n');
        }

        return resString.toString();
    }

    private void appendZeros(StringBuilder resString, int zerosNumber) {
        if (zerosNumber >= 3) {
            resString.append("0 ... 0");
        } else {
            for (int zero = 0; zero < zerosNumber; zero++) {
                if (zero != 0) {
                    resString.append(' ');
                }
                resString.append('0');
            }
        }
    }
}
