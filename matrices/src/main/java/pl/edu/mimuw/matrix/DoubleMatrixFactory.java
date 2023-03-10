package pl.edu.mimuw.matrix;

import java.util.Arrays;

public class DoubleMatrixFactory {

    private DoubleMatrixFactory() {
    }

    public static IDoubleMatrix sparse(Shape shape, MatrixCellValue... values) {
        assert values.length != 0;
        assert shape != null;

        return new IrregularMatrix(shape, Arrays.asList(values));
    }

    public static IDoubleMatrix full(double[][] values) {
        assert values != null;
        assert values.length != 0;

        return new FullMatrix(values.length, values[0].length, values);
    }

    public static IDoubleMatrix identity(int size) {
        return new IdentityMatrix(size);
    }

    public static IDoubleMatrix diagonal(double... diagonalValues) {
        assert diagonalValues.length != 0;

        return new DiagonalSquareMatrix(diagonalValues.length, diagonalValues);
    }

    public static IDoubleMatrix antiDiagonal(double... antiDiagonalValues) {
        assert antiDiagonalValues.length != 0;

        return new AntiDiagonalSquareMatrix(antiDiagonalValues.length, antiDiagonalValues);
    }

    public static IDoubleMatrix vector(double... values) {
        assert values.length != 0;

        return new ColumnMatrix(values.length, 1, values);
    }

    public static IDoubleMatrix zero(Shape shape) {
        assert shape != null;

        return new ZeroMatrix(shape);
    }

    public static IDoubleMatrix columnMatrix(int rows, int columns, double... rowValues) {
        assert rowValues.length == rows;

        return new ColumnMatrix(rows, columns, rowValues);
    }

    public static IDoubleMatrix constantMatrix(int rows, int columns, double values) {
        return new ConstantMatrix(rows, columns, values);
    }

    public static IDoubleMatrix rowMatrix(int rows, int columns, double... columnValues) {
        assert columnValues.length == columns;

        return new RowMatrix(rows, columns, columnValues);
    }
}
