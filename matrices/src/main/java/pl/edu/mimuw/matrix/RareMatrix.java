package pl.edu.mimuw.matrix;

public abstract class RareMatrix extends DoubleMatrix {
    public RareMatrix(int rows, int columns) {
        super(rows, columns);
    }

    public RareMatrix(Shape shape) {
        super(shape);
    }
}
