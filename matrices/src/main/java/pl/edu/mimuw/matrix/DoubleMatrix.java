package pl.edu.mimuw.matrix;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

public abstract class DoubleMatrix implements IDoubleMatrix {
    protected static final DecimalFormat formatter = new DecimalFormat("0.00");
    protected Shape matrixShape;

    protected DoubleMatrix(int rows, int columns) {
        assert rows > 0;
        assert columns > 0;

        this.matrixShape = Shape.matrix(rows, columns);
    }

    protected DoubleMatrix(Shape shape) {
        assert shape.rows > 0;
        assert shape.columns > 0;

        this.matrixShape = shape;
    }

    /**
     * Shape of matrix getter.
     *
     * @return shape of matrix.
     */
    @Override
    public Shape shape() {
        return this.matrixShape;
    }

    // Block of functions with verified arguments.

    /**
     * Function to implement multiplication based on matrix implementation.
     *
     * @param other: second matrix of multiplication, which can be safely multiplied (has correct sizes).
     * @return matrix - result of multiplication.
     */
    protected abstract IDoubleMatrix timesInternal(IDoubleMatrix other);

    /**
     * Function to implement multiplication by non-zero scalar, based on matrix implementation.
     *
     * @param scalar: non-zero scalar to multiply matrix by.
     * @return matrix - result of multiplication.
     */
    protected abstract IDoubleMatrix timesInternal(double scalar);

    /**
     * Function to implement addition based on matrix implementation.
     *
     * @param other: second matrix of addition, which can be safely added (has correct sizes).
     * @return matrix - result of addition.
     */
    protected abstract IDoubleMatrix plusInternal(IDoubleMatrix other);

    /**
     * Function to implement implementation-based addition of scalar.
     *
     * @param scalar non-zero scalar to be added.
     * @return result of addition.
     */
    protected abstract IDoubleMatrix plusInternal(double scalar);

    /**
     * Function to implement subtraction based on matrix implementation.
     *
     * @param other: second matrix of subtraction, which can be safely subtract (has correct sizes).
     * @return matrix - result of subtraction.
     */
    protected abstract IDoubleMatrix minusInternal(IDoubleMatrix other);

    /**
     * Function to implement implemenation-based getting of value. Parameter are within the matrix.
     *
     * @param row    row of cell to get value of.
     * @param column column of cell to get value of.
     * @return value of cell.
     */
    protected abstract double getInternal(int row, int column);

    @Override
    public IDoubleMatrix minus(double scalar) {
        return this.plus(-scalar);
    }

    /*
    Following methods implement argument checking to make sure that operation can be done.
    They also do some kind of optimizations in certain kind of operations.
     */
    @Override
    public IDoubleMatrix times(IDoubleMatrix other) {
        assert this.matrixShape.columns == other.shape().rows;

        if (other instanceof ZeroMatrix) {
            return new ZeroMatrix(this.matrixShape.rows, other.shape().columns);
        }

        return this.timesInternal(other);
    }

    @Override
    public IDoubleMatrix times(double scalar) {
        if (scalar == 0) {
            return new ZeroMatrix(this.matrixShape);
        }

        return this.timesInternal(scalar);
    }

    @Override
    public IDoubleMatrix plus(IDoubleMatrix other) {
        assert this.matrixShape.rows == other.shape().rows;
        assert this.matrixShape.columns == other.shape().columns;

        return this.plusInternal(other);
    }

    @Override
    public IDoubleMatrix plus(double scalar) {
        if (scalar == 0) {
            return this;
        }

        return this.plusInternal(scalar);
    }

    @Override
    public IDoubleMatrix minus(IDoubleMatrix other) {
        assert this.matrixShape.rows == other.shape().rows;
        assert this.matrixShape.columns == other.shape().columns;

        return this.minusInternal(other);
    }

    @Override
    public double get(int row, int column) {
        assert row >= 0 && row < this.matrixShape.rows;
        assert column >= 0 && column < this.matrixShape.columns;

        return this.getInternal(row, column);
    }

    /**
     * General function to get array-representation. Should be overload if matrix implementation can be used
     * to perform that operation faster.
     *
     * @return array representation of given matrix.
     */
    @Override
    public double[][] data() {
        double[][] resultArray = new double[this.matrixShape.rows][this.matrixShape.columns];

        for (int row = 0; row < this.matrixShape.rows; row++) {
            for (int column = 0; column < this.matrixShape.columns; column++) {
                resultArray[row][column] = this.getInternal(row, column);
            }
        }

        return resultArray;
    }

    /*
    Following methods implements general - not optimized ways to perform matrix operations.
    Operations results in fullMatrix generated by brut-force non stituation-specic-algorithms.
     */

    // ==== BLOCK OF GENERAL METHODS
    protected IDoubleMatrix genericTimes(IDoubleMatrix other) {
        ArrayList<MatrixCellValue> values = new ArrayList<>();

        int resRows = this.matrixShape.rows;
        int resCols = other.shape().columns;
        int range = this.matrixShape.columns;

        for (int row = 0; row < resRows; row++) {
            for (int col = 0; col < resCols; col++) {
                double value = 0;
                for (int place = 0; place < range; place++) {
                    value += this.get(row, place) * other.get(place, col);
                }

                values.add(new MatrixCellValue(row, col, value));
            }
        }

        return new FullMatrix(resRows, resCols, values);
    }

    protected IDoubleMatrix genericPlus(IDoubleMatrix other) {
        ArrayList<MatrixCellValue> values = new ArrayList<>();

        int resRows = this.matrixShape.rows;
        int resCols = this.matrixShape.columns;

        for (int row = 0; row < resRows; row++) {
            for (int col = 0; col < resCols; col++) {
                double value = this.get(row, col) + other.get(row, col);

                values.add(new MatrixCellValue(row, col, value));
            }
        }

        return new FullMatrix(resRows, resCols, values);
    }

    protected IDoubleMatrix genericMinus(IDoubleMatrix other) {
        ArrayList<MatrixCellValue> values = new ArrayList<>();

        int resRows = this.matrixShape.rows;
        int resCols = this.matrixShape.columns;

        for (int row = 0; row < resRows; row++) {
            for (int col = 0; col < resCols; col++) {
                double value = this.get(row, col) - other.get(row, col);

                values.add(new MatrixCellValue(row, col, value));
            }
        }

        return new FullMatrix(resRows, resCols, values);
    }

    protected IDoubleMatrix genericPlus(double scalar) {
        ArrayList<MatrixCellValue> values = new ArrayList<>();

        int resRows = this.matrixShape.rows;
        int resCols = this.matrixShape.columns;

        for (int row = 0; row < resRows; row++) {
            for (int col = 0; col < resCols; col++) {
                double value = this.get(row, col) + scalar;

                values.add(new MatrixCellValue(row, col, value));
            }
        }

        return new FullMatrix(resRows, resCols, values);
    }

    protected IDoubleMatrix genericTimes(double scalar) {
        ArrayList<MatrixCellValue> values = new ArrayList<>();

        int resRows = this.matrixShape.rows;
        int resCols = this.matrixShape.columns;

        for (int row = 0; row < resRows; row++) {
            for (int col = 0; col < resCols; col++) {
                double value = this.get(row, col);
                if (value != 0) {
                    value *= scalar;
                }

                values.add(new MatrixCellValue(row, col, value));
            }
        }

        return new FullMatrix(resRows, resCols, values);
    }

    // ==== END OF BLOCK OF GENERAL METHODS

    /**
     * Function checks if given collection has correct data to be inserted into matrix.
     * It also checks if some value is not doubled.
     *
     * @param cellValues collection to be checked.
     */
    protected void checkCollectionCorectness(Collection<MatrixCellValue> cellValues) {
        HashSet<CellCord> addedCords = new HashSet<>();

        for (MatrixCellValue mcv : cellValues) {
            assert mcv.row >= 0 && mcv.row < this.matrixShape.rows;
            assert mcv.column >= 0 && mcv.column < this.matrixShape.columns;

            CellCord cord = new CellCord(mcv.row, mcv.column);
            assert !addedCords.contains(cord);

            addedCords.add(cord);
        }
    }

    /**
     * Universal way to convert matrix to string representation. Should be overload if matrix implementation
     * can be used to perform it faster or implementation allows to do it faster.
     *
     * @return string representation of the matrix (without sizes).
     */
    protected String toStringInternal() {
        StringBuilder resString = new StringBuilder();

        for (int row = 0; row < this.matrixShape.rows; row++) {
            for (int col = 0; col < this.matrixShape.columns; col++) {
                if (col != 0) {
                    resString.append(' ');
                }
                resString.append(DoubleMatrix.formatter.format(this.get(row, col)));
            }

            resString.append('\n');
        }

        return resString.toString();
    }

    // toString calls toStringInternal, which describes inner structure of the matrix.
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SIZE: (");
        sb.append(this.matrixShape.rows);
        sb.append(" rows) x (");
        sb.append(this.matrixShape.columns);
        sb.append(" columns)\n");

        sb.append(this.toStringInternal());

        return sb.toString();
    }

    /**
     * Record to track a pair of int values.
     *
     * @param row    row of matrix cell.
     * @param column column of matrix cell.
     */
    private record CellCord(int row, int column) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CellCord cellCord = (CellCord) o;
            return row == cellCord.row && column == cellCord.column;
        }

        @Override
        public int hashCode() {
            return Objects.hash(row, column);
        }
    }
}
