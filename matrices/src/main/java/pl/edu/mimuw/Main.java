package pl.edu.mimuw;

import pl.edu.mimuw.matrix.*;

import java.util.ArrayList;
import java.util.Random;

public class Main {
    private static final int matrixSize = 10;
    private static final Random r = new Random();

    private static double[][] generate2D(int rows, int columns) {
        double[][] res = new double[rows][columns];

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                if (r.nextBoolean()) {
                    res[row][col] = r.nextDouble() * 100;
                } else {
                    res[row][col] = 0;
                }
            }
        }

        return res;
    }

    private static double[] generate1D(int size) {
        double[][] generated = generate2D(1, size);

        return generated[0];
    }

    public static void main(String[] args) {
        // Macierz pełna
        System.out.println("WYPISYWANIE MACIERZY PEŁNEJ");
        double[][] fullMatrix = generate2D(matrixSize, matrixSize);

        FullMatrix full = new FullMatrix(matrixSize, matrixSize, fullMatrix);
        System.out.println(full);

        // Macierz anty-przekątniowa
        System.out.println("WYPISYWANIE MACIERZY ANTY-PRZEKĄTNIOWEJ");
        double[] antiDiagonal = generate1D(matrixSize);

        AntiDiagonalSquareMatrix antiDiag = new AntiDiagonalSquareMatrix(matrixSize, antiDiagonal);
        System.out.println(antiDiag);

        // Macierz kolumnowa
        System.out.println("WYPISYWANIE MACIERZY KOLUMNOWEJ");
        double[] rows = generate1D(matrixSize);
        ColumnMatrix column = new ColumnMatrix(matrixSize, matrixSize, rows);

        System.out.println(column);

        // Macierz stała
        System.out.println("WYPISYWANIE MACIERZY STAŁEJ");
        ConstantMatrix constant = new ConstantMatrix(matrixSize, matrixSize, r.nextDouble() * 100);

        System.out.println(constant);

        // Macierz jednostkowa
        System.out.println("WYPISYWANIE MACIERZY JEDNOSTKOWEJ");
        IdentityMatrix identity = new IdentityMatrix(matrixSize);

        System.out.println(identity);

        // Macierz nieregularna
        System.out.println("WYPISYWANIE MACIERZY NIEREGULARNEJ");
        double[][] irregular = generate2D(matrixSize, matrixSize);
        ArrayList<MatrixCellValue> values = new ArrayList<>();
        for (int row = 0; row < matrixSize; row++) {
            for (int col = 0; col < matrixSize; col++) {
                values.add(new MatrixCellValue(row, col, irregular[row][col]));
            }
        }

        IrregularMatrix ir = new IrregularMatrix(matrixSize, matrixSize, values);
        System.out.println(ir);

        // Macierz przekątniowa
        System.out.println("WYPISYWANIE MACIERZY PRZEKĄTNIOWEJ");
        DiagonalSquareMatrix dsm = new DiagonalSquareMatrix(matrixSize, r.nextInt(50));
        System.out.println(dsm);

        // Macierz wierszowa
        System.out.println("WYPISYWANIE MACIERZY WIERSZOWEJ");
        double[] columns = generate1D(matrixSize);
        RowMatrix row = new RowMatrix(matrixSize, matrixSize, columns);

        System.out.println(row);

        // Macierz zerowa
        System.out.println("WYPISYWANIE MACIERZY ZEROWEJ");
        ZeroMatrix zeroMat = new ZeroMatrix(matrixSize, matrixSize);

        System.out.println(zeroMat);

        // Obliczanie 1-normy macierzowej
        System.out.println("OBLICZANIE 1-NORMY DLA PONIŻSZEJ MACIERZY:");
        System.out.println(full);
        System.out.println("Obliczona 1-norma to: " + full.normOne());

        // Obliczanie normy nieskończonej macierzy
        System.out.println("OBLICZANIE NORMY NIESKOŃCZONEJ DLA PONIŻSZEJ MACIERZY:");
        System.out.println(full);
        System.out.println("Obliczona norma nieskończona to: " + full.normInfinity());

        // Obliczanie normy frobeniusa
        System.out.println("OBLICZANIE NORMY FROBENIUSA DLA PONIŻSZEJ MACIERZY:");
        System.out.println(full);
        System.out.println("Obliczona norma frobeniusa to: " + full.frobeniusNorm());

        System.out.println("=== SEKCJA MACIERZY JEDNOSTKOWEJ ===");
        System.out.println("DODAWANIE DWÓCH MACIERZY JEDNOSTKOWYCH ROZMIARU 10x10:");
        System.out.println(identity.plus(identity));
        System.out.println("MNOŻENIE DWÓCH MACIERZY JEDNOSTKOWYCH");
        System.out.println(identity.times(identity));
        System.out.println("ODEJMNOWANIE DWÓCH MACIERZY JEDNOSTKOWYCH");
        System.out.println(identity.minus(identity));
        System.out.println("MNOŻENIE MACIERZY JEDNOSTKOWEJ PRZEZ 5:");
        System.out.println(identity.times(5));
        System.out.println("DODAWANIE DO MACIERZY JEDNOSTKOWEJ 5:");
        System.out.println(identity.plus(5));
        System.out.println("ODEJMNOWANIE OD MACIERZY JEDNOSTKOWEJ 5");
        System.out.println(identity.minus(5));
        System.out.println("ODCZYTYWANIE WARTOŚCI");
        System.out.println("Dla wiersza 0, kolumny 1: " + identity.get(0, 1));
        System.out.println("Dla wiersza 1, kolumny 1: " + identity.get(1, 1));

        System.out.println("=== SEKCJA SPRAWDZENIA OPTYMALIZACJI ===");
        System.out.println("Optymalizacja dodania dwóch macierzy przekątniowych");
        System.out.println("Obie macierze mają taką samą postać:");
        System.out.println(dsm);
        System.out.println("Klasa 1. macierzy: " + dsm.getClass().getName());
        System.out.println("Klasa 2. macierzy: " + dsm.getClass().getName());
        System.out.println("Klasa wyniku: " + dsm.plus(dsm).getClass().getName());
        System.out.println("Wynik:\n" + dsm.plus(dsm));

        System.out.println("=== SEKCJA PODSTAWOWYCH OPERACJI NA MACIERZACH ===");
        System.out.println("MACIERZ M1 =");
        System.out.println(dsm);
        System.out.println("MACIERZ M2=");
        System.out.println(column);
        System.out.println("M1 * M2 =");
        System.out.println(dsm.times(column));
        System.out.println("M1 - M2 =");
        System.out.println(dsm.minus(column));
        System.out.println("M1 + M2 =");
        System.out.println(dsm.plus(column));
    }
}
