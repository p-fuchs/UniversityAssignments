package pl.edu.mimuw.matrix;

public abstract class RegularMatrix extends RareMatrix {
    protected RegularMatrix(int rows, int columns) {
        super(rows, columns);
    }

    protected RegularMatrix(Shape shape) {
        super(shape);
    }
}
