package pl.edu.mimuw.matrix;

import java.util.*;

public class IrregularMatrix extends RareMatrix {
    private static final RowDataOrder rowOrder = new RowDataOrder();
    private static final CellOrder cellOrder = new CellOrder();
    // Irregular (Sparse) Matrix is implemented as list of rows (which are pairs of row number and list of array
    // values). It also contains two Ordering class, which are used to make binary search available for getting value
    // from matrix.
    private final ArrayList<RowData> rows;

    public IrregularMatrix(int rows, int columns, Collection<MatrixCellValue> values) {
        super(rows, columns);

        this.rows = new ArrayList<>();
        this.setRows(values);
    }

    public IrregularMatrix(Shape shape, Collection<MatrixCellValue> values) {
        super(shape);

        this.rows = new ArrayList<>();
        this.setRows(values);
    }

    private void addValue(MatrixCellValue val) {
        for (RowData rd : this.rows) {
            if (rd.row == val.row) {
                rd.list.add(val);
                return;
            }
        }

        RowData rd = new RowData(val.row, new ArrayList<>());
        rd.list.add(val);
        rows.add(rd);
    }

    private void setRows(Collection<MatrixCellValue> values) {
        this.checkCollectionCorectness(values);

        for (MatrixCellValue mcv : values) {
            if (mcv.value == 0) {
                continue;
            }

            addValue(mcv);
        }

        Collections.sort(this.rows, IrregularMatrix.rowOrder);
        for (RowData rd : this.rows) {
            Collections.sort(rd.list, IrregularMatrix.cellOrder);
        }
    }

    // internal operations are optimised to take into account only that values which occur in given Matrix whenever
    // that is possible, it results in time optimisation for large matrices.
    @Override
    protected IDoubleMatrix timesInternal(IDoubleMatrix other) {
        ArrayList<CellData> newCells = new ArrayList<>();
        int resCols = other.shape().columns;

        if (other.getClass() == IrregularMatrix.class) {
            IrregularMatrix second = (IrregularMatrix) other;
            for (RowData rdMe : this.rows) {
                for (MatrixCellValue mcvMe : rdMe.list) {
                    RowData secondRow = null;
                    for (RowData rowData : second.rows) {
                        if (rowData.row == mcvMe.column) {
                            secondRow = rowData;
                            break;
                        }
                    }

                    if (secondRow == null) {
                        continue;
                    }

                    for (MatrixCellValue mcvOther : secondRow.list) {
                        CellData data = new CellData(mcvMe.row, mcvOther.column, mcvMe.value * mcvOther.value);

                        int index = newCells.indexOf(data);
                        if (index == -1) {
                            newCells.add(data);
                        } else {
                            newCells.get(index).value += data.value;
                        }
                    }
                }
            }

            ArrayList<MatrixCellValue> newVals = new ArrayList<>();
            for (CellData cd : newCells) {
                newVals.add(new MatrixCellValue(cd.row, cd.column, cd.value));
            }

            return new IrregularMatrix(this.matrixShape.rows, resCols, newVals);
        } else {
            for (RowData rd : this.rows) {
                for (MatrixCellValue mcv : rd.list) {
                    for (int col = 0; col < resCols; col++) {
                        double otherVal = other.get(mcv.column, col);
                        CellData data = new CellData(mcv.row, col, mcv.value * otherVal);

                        int index = newCells.indexOf(data);
                        if (index == -1) {
                            newCells.add(data);
                        } else {
                            newCells.get(index).value += data.value;
                        }
                    }
                }
            }

            ArrayList<MatrixCellValue> newVals = new ArrayList<>();
            for (CellData cd : newCells) {
                newVals.add(new MatrixCellValue(cd.row, cd.column, cd.value));
            }

            return new FullMatrix(this.matrixShape.rows, resCols, newVals);
        }
    }

    @Override
    protected IDoubleMatrix timesInternal(double scalar) {
        ArrayList<MatrixCellValue> newVals = new ArrayList<>();

        for (RowData rd : this.rows) {
            for (MatrixCellValue mcv : rd.list) {
                newVals.add(new MatrixCellValue(mcv.row, mcv.column, mcv.value * scalar));
            }
        }

        return new IrregularMatrix(this.matrixShape, newVals);
    }

    @Override
    protected IDoubleMatrix plusInternal(IDoubleMatrix other) {
        if (other.getClass() == IrregularMatrix.class) {
            IrregularMatrix second = (IrregularMatrix) other;
            ArrayList<MatrixCellValue> newVals = new ArrayList<>();

            ArrayList<CellData> newCells = new ArrayList<>();

            for (RowData rd : second.rows) {
                for (MatrixCellValue mcv : rd.list) {
                    newCells.add(new CellData(mcv.row, mcv.column, mcv.value));
                }
            }

            for (RowData rd : this.rows) {
                for (MatrixCellValue mcv : rd.list) {
                    CellData data = new CellData(mcv.row, mcv.column, mcv.value);
                    int index = newCells.indexOf(data);

                    if (index == -1) {
                        newCells.add(data);
                    } else {
                        newCells.get(index).value += data.value;
                    }
                }
            }

            for (CellData cd : newCells) {
                newVals.add(new MatrixCellValue(cd.row, cd.column, cd.value));
            }

            return new IrregularMatrix(this.matrixShape, newVals);
        }

        return this.genericPlus(other);
    }

    @Override
    protected IDoubleMatrix plusInternal(double scalar) {
        return this.genericPlus(scalar);
    }

    @Override
    protected IDoubleMatrix minusInternal(IDoubleMatrix other) {
        if (other.getClass() == IrregularMatrix.class) {
            IrregularMatrix second = (IrregularMatrix) other;
            ArrayList<MatrixCellValue> newVals = new ArrayList<>();

            ArrayList<CellData> newCells = new ArrayList<>();

            for (RowData rd : this.rows) {
                for (MatrixCellValue mcv : rd.list) {
                    newCells.add(new CellData(mcv.row, mcv.column, mcv.value));
                }
            }

            for (RowData rd : second.rows) {
                for (MatrixCellValue mcv : rd.list) {
                    CellData data = new CellData(mcv.row, mcv.column, mcv.value);
                    int index = newCells.indexOf(data);

                    if (index == -1) {
                        newCells.add(data);
                    } else {
                        newCells.get(index).value -= data.value;
                    }
                }
            }

            for (CellData cd : newCells) {
                newVals.add(new MatrixCellValue(cd.row, cd.column, cd.value));
            }

            return new IrregularMatrix(this.matrixShape, newVals);
        }

        return this.genericMinus(other);
    }

    // Getting value from array is based on binary search.
    @Override
    protected double getInternal(int row, int column) {
        RowData rd = new RowData(row, null);

        int index = Collections.binarySearch(this.rows, rd, IrregularMatrix.rowOrder);
        if (index < 0) {
            return 0;
        }

        ArrayList<MatrixCellValue> rowValues = this.rows.get(index).list;

        index = Collections.binarySearch(rowValues, new MatrixCellValue(0, column, 0), IrregularMatrix.cellOrder);
        if (index < 0) {
            return 0;
        }

        return rowValues.get(index).value;
    }

    @Override
    public double normOne() {
        double[] colSums = new double[this.matrixShape.columns];

        for (RowData rd : this.rows) {
            for (MatrixCellValue mcv : rd.list) {
                colSums[mcv.column] += Math.abs(mcv.value);
            }
        }

        return Arrays.stream(colSums).max().getAsDouble();
    }

    @Override
    public double normInfinity() {
        double value = 0;

        for (RowData rd : this.rows) {
            double sum = 0;

            for (MatrixCellValue mcv : rd.list) {
                sum += Math.abs(mcv.value);
            }

            value = Math.max(value, sum);
        }

        return value;
    }

    @Override
    public double frobeniusNorm() {
        double sum = 0;

        for (RowData rd : this.rows) {
            for (MatrixCellValue mcv : rd.list) {
                sum += mcv.value * mcv.value;
            }
        }

        return Math.sqrt(sum);
    }

    @Override
    public double[][] data() {
        double[][] resultArray = new double[this.matrixShape.rows][this.matrixShape.columns];

        for (RowData rd : this.rows) {
            for (MatrixCellValue mcv : rd.list) {
                resultArray[mcv.row][mcv.column] = mcv.value;
            }
        }

        return resultArray;
    }

    // Classes defined below are used to implement Ordering or helpful records to manage value handling.
    private static class RowDataOrder implements Comparator<IrregularMatrix.RowData> {
        @Override
        public int compare(IrregularMatrix.RowData t0, IrregularMatrix.RowData t1) {
            if (t0.row < t1.row) {
                return -1;
            } else if (t0.row == t1.row) {
                return 0;
            } else {
                return 1;
            }
        }
    }

    private static class CellOrder implements Comparator<MatrixCellValue> {
        @Override
        public int compare(MatrixCellValue t0, MatrixCellValue t1) {
            if (t0.column < t1.column) {
                return -1;
            } else if (t0.column == t1.column) {
                return 0;
            } else {
                return 1;
            }
        }
    }

    // RowData is used as Pair to store number of row and list of values corresponding to it.
    private record RowData(int row, ArrayList<MatrixCellValue> list) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RowData rowData = (RowData) o;
            return row == rowData.row;
        }

        @Override
        public int hashCode() {
            return Objects.hash(row);
        }
    }

    private static class CellData {
        public final int row;
        public final int column;
        public double value;

        public CellData(int row, int column, double value) {
            this.row = row;
            this.column = column;
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CellData cellData = (CellData) o;
            return row == cellData.row && column == cellData.column;
        }

        @Override
        public int hashCode() {
            return Objects.hash(row, column);
        }
    }
}
