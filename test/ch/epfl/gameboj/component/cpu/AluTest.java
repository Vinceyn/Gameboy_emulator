// Gameboj stage 2

package ch.epfl.gameboj.component.cpu;

import static ch.epfl.test.TestRandomizer.RANDOM_ITERATIONS;
import static ch.epfl.test.TestRandomizer.newRandom;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Random;
import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;
import java.util.zip.GZIPInputStream;

import org.junit.jupiter.api.Test;

class AluTest {
    private interface IntTernaryOperator {
        int applyAsInt(int v1, int v2, int v3);
    }
    private interface IntQuaternaryOperator {
        int applyAsInt(int v1, int v2, int v3, int v4);
    }

    private static final int[] INT_1 = allValues(1);
    private static final int[] INT_3 = allValues(3);
    private static final int[] INT_8 = allValues(8);
    private static final int[] INT_16 = someValues(2018, 16, 300);

    private static int[] allValues(int bits) {
        int[] vs = new int[1 << bits];
        for (int i = 0; i < vs.length; ++i)
            vs[i] = i;
        return vs;
    }

    private static int[] someValues(int seed, int bits, int count) {
        Random rng = new Random(seed);
        int[] vs = new int[count];
        for (int i = 0; i < vs.length; ++i)
            vs[i] = rng.nextInt(1 << bits);
        return vs;
    }

    private static DataInputStream openStream(String name) throws IOException {
        String fullName = "/data/" + name + ".bin.gz";
        return new DataInputStream(
                new GZIPInputStream(
                        AluTest.class.getResourceAsStream(fullName)));
    }

    private static void test(String methName, String fileName, int[] values1, IntUnaryOperator o) throws IOException {
        try (DataInputStream in = openStream(fileName)) {
            for (int v1: values1) {
                int expected = in.readInt();
                int actual = o.applyAsInt(v1);
                assertEquals(expected, actual,
                        String.format("Alu.%s(%d), expected 0x%X, actual 0x%X",
                                methName, v1, expected, actual));
            }
        }
    }

    private static void test(String methName, String fileName, int[] values1, int[] values2, IntBinaryOperator o) throws IOException {
        try (DataInputStream in = openStream(fileName)) {
            for (int v1: values1) {
                for (int v2: values2) {
                    int expected = in.readInt();
                    int actual = o.applyAsInt(v1, v2);
                    assertEquals(expected, actual,
                            String.format("Alu.%s(%d,%d), expected 0x%X, actual 0x%X",
                                    methName, v1, v2, expected, actual));
                }
            }
        }
    }

    private static void test(String methName, String fileName, int[] values1, int[] values2, int[] values3, IntTernaryOperator o) throws IOException {
        try (DataInputStream in = openStream(fileName)) {
            for (int v1: values1) {
                for (int v2: values2) {
                    for (int v3: values3) {
                        int expected = in.readInt();
                        int actual = o.applyAsInt(v1, v2, v3);
                        assertEquals(expected, actual,
                                String.format("Alu.%s(%d,%d,%d), expected 0x%X, actual 0x%X",
                                        methName, v1, v2, v3, expected, actual));
                    }
                }
            }
        }
    }

    private static void test(String methName, String fileName, int[] values1, int[] values2, int[] values3, int[] values4, IntQuaternaryOperator o) throws IOException {
        try (DataInputStream in = openStream(fileName)) {
            for (int v1: values1) {
                for (int v2: values2) {
                    for (int v3: values3) {
                        for (int v4: values4) {
                            int expected = in.readInt();
                            int actual = o.applyAsInt(v1, v2, v3, v4);
                            assertEquals(expected, actual,
                                    String.format("Alu.%s(%d,%d,%d,%d), expected 0x%X, actual 0x%X",
                                            methName, v1, v2, v3, v4, expected, actual));
                        }
                    }
                }
            }
        }
    }

    // mask/unpack
    @Test
    void maskZNHCProducesSameResultsAsReference() throws IOException {
        test("maskZNHC", "MASKZNHC", INT_1, INT_1, INT_1, INT_1, (z, n, h, c) -> Alu.maskZNHC(z != 0, n != 0, h != 0, c != 0));
    }

    @Test
    void unpackValueProducesSameResultsAsReference() throws IOException {
        test("unpackValue", "UNPACKV", INT_16, v -> Alu.unpackValue(v << 4));
    }

    @Test
    void unpackFlagsProducesSameResultsAsReference() throws IOException {
        test("unpackFlags", "UNPACKF", INT_16, v -> Alu.unpackFlags(v << 4));
    }

    // 8-bit operations
    @Test
    void addProducesSameResultsAsReference() throws IOException {
        test("add", "ADD", INT_8, INT_8, INT_1, (l, r, c) -> Alu.add(l, r, c != 0));
    }

    @Test
    void addFailsOnNon8BitsValues() {
        Random rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            int v;
            do { v = rng.nextInt(); } while (0 <= v && v <= 0xFF);
            int v1 = v;
            assertThrows(IllegalArgumentException.class, () -> {
                Alu.add(v1, 0, false);
            });
            assertThrows(IllegalArgumentException.class, () -> {
                Alu.add(0, v1, false);
            });
        }
    }

    @Test
    void subProducesSameResultsAsReference() throws IOException {
        test("sub", "SUB", INT_8, INT_8, INT_1, (l, r, c) -> Alu.sub(l, r, c != 0));
    }

    @Test
    void subFailsOnNon8BitsValues() {
        Random rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            int v;
            do { v = rng.nextInt(); } while (0 <= v && v <= 0xFF);
            int v1 = v;
            assertThrows(IllegalArgumentException.class, () -> {
                Alu.sub(v1, 0, false);
            });
            assertThrows(IllegalArgumentException.class, () -> {
                Alu.sub(0, v1, false);
            });
        }
    }

    @Test
    void bcdAdjustProducesSameResultsAsReference() throws IOException {
        test("bcdAdjust", "BCDA", INT_8, INT_1, INT_1, INT_1, (v, n, h, c) -> Alu.bcdAdjust(v, n != 0, h != 0, c != 0));
    }

    @Test
    void bcdAdjustFailsOnNon8BitsValues() {
        Random rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            int v;
            do { v = rng.nextInt(); } while (0 <= v && v <= 0xFF);
            int v1 = v;
            assertThrows(IllegalArgumentException.class, () -> {
                Alu.bcdAdjust(v1, false, false, false);
            });
        }
    }

    @Test
    void andProducesSameResultsAsReference() throws IOException {
        test("and", "AND", INT_8, INT_8, Alu::and);
    }

    @Test
    void andFailsOnNon8BitsValues() {
        Random rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            int v;
            do { v = rng.nextInt(); } while (0 <= v && v <= 0xFF);
            int v1 = v;
            assertThrows(IllegalArgumentException.class, () -> {
                Alu.and(v1, 0);
            });
            assertThrows(IllegalArgumentException.class, () -> {
                Alu.and(0, v1);
            });
        }
    }

    @Test
    void orProducesSameResultsAsReference() throws IOException {
        test("or", "OR", INT_8, INT_8, Alu::or);
    }

    @Test
    void orFailsOnNon8BitsValues() {
        Random rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            int v;
            do { v = rng.nextInt(); } while (0 <= v && v <= 0xFF);
            int v1 = v;
            assertThrows(IllegalArgumentException.class, () -> {
                Alu.or(v1, 0);
            });
            assertThrows(IllegalArgumentException.class, () -> {
                Alu.or(0, v1);
            });
        }
    }

    @Test
    void xorProducesSameResultsAsReference() throws IOException {
        test("xor", "XOR", INT_8, INT_8, Alu::xor);
    }

    @Test
    void xorFailsOnNon8BitsValues() {
        Random rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            int v;
            do { v = rng.nextInt(); } while (0 <= v && v <= 0xFF);
            int v1 = v;
            assertThrows(IllegalArgumentException.class, () -> {
                Alu.xor(v1, 0);
            });
            assertThrows(IllegalArgumentException.class, () -> {
                Alu.xor(0, v1);
            });
        }
    }

    @Test
    void shiftLeftProducesSameResultsAsReference() throws IOException {
        test("shiftLeft", "SHL", INT_8, Alu::shiftLeft);
    }

    @Test
    void shiftLeftFailsOnNon8BitsValues() {
        Random rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            int v;
            do { v = rng.nextInt(); } while (0 <= v && v <= 0xFF);
            int v1 = v;
            assertThrows(IllegalArgumentException.class, () -> {
                Alu.shiftLeft(v1);
            });
        }
    }

    @Test
    void shiftRightAProducesSameResultsAsReference() throws IOException {
        test("shiftRightA", "SHR_A", INT_8, Alu::shiftRightA);
    }

    @Test
    void shiftRightAFailsOnNon8BitsValues() {
        Random rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            int v;
            do { v = rng.nextInt(); } while (0 <= v && v <= 0xFF);
            int v1 = v;
            assertThrows(IllegalArgumentException.class, () -> {
                Alu.shiftRightA(v1);
            });
        }
    }

    @Test
    void shiftRightLProducesSameResultsAsReference() throws IOException {
        test("shiftRightL", "SHR_L", INT_8, Alu::shiftRightL);
    }

    @Test
    void shiftRightLFailsOnNon8BitsValues() {
        Random rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            int v;
            do { v = rng.nextInt(); } while (0 <= v && v <= 0xFF);
            int v1 = v;
            assertThrows(IllegalArgumentException.class, () -> {
                Alu.shiftRightL(v1);
            });
        }
    }

    @Test
    void rotate2ProducesSameResultsAsReference() throws IOException {
        test("rotate", "ROT2", INT_1, INT_8, (d, v) -> Alu.rotate(d == 0 ? Alu.RotDir.LEFT : Alu.RotDir.RIGHT, v));
    }

    @Test
    void rotate2FailsOnNon8BitsValues() {
        Random rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            int v;
            do { v = rng.nextInt(); } while (0 <= v && v <= 0xFF);
            int v1 = v;
            assertThrows(IllegalArgumentException.class, () -> {
                Alu.rotate(Alu.RotDir.LEFT, v1);
            });
        }
    }

    @Test
    void rotate3ProducesSameResultsAsReference() throws IOException {
        test("rotate", "ROT3", INT_1, INT_8, INT_1, (d, v, c) -> Alu.rotate(d == 0 ? Alu.RotDir.LEFT : Alu.RotDir.RIGHT, v, c != 0));
    }

    @Test
    void rotate3FailsOnNon8BitsValues() {
        Random rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            int v;
            do { v = rng.nextInt(); } while (0 <= v && v <= 0xFF);
            int v1 = v;
            assertThrows(IllegalArgumentException.class, () -> {
                Alu.rotate(Alu.RotDir.LEFT, v1, false);
            });
        }
    }

    @Test
    void swapProducesSameResultsAsReference() throws IOException {
        test("swap", "SWAP", INT_8, Alu::swap);
    }

    @Test
    void swapFailsOnNon8BitsValues() {
        Random rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            int v;
            do { v = rng.nextInt(); } while (0 <= v && v <= 0xFF);
            int v1 = v;
            assertThrows(IllegalArgumentException.class, () -> {
                Alu.swap(v1);
            });
        }
    }

    @Test
    void testBitProducesSameResultsAsReference() throws IOException {
        test("testBit", "TST", INT_8, INT_3, Alu::testBit);
    }

    //    @Test
    //    void testBitProducesSameResultsAsReferenceOrOpposite() throws IOException {
    //        try (DataInputStream in = openStream("TST")) {
    //            for (int v1: INT_8) {
    //                for (int v2: INT_3) {
    //                    int expected = in.readInt();
    //                    int expectedWithZInverted = expected ^ 0b1000_0000;
    //                    int actual = ((IntBinaryOperator) Alu::testBit).applyAsInt(v1, v2);
    //                    assertTrue(actual == expected || actual == expectedWithZInverted,
    //                            String.format("Alu.%s(%d,%d), expected 0x%X or 0x%X, actual 0x%X",
    //                                    "testBit", v1, v2, expected, expectedWithZInverted, actual));
    //                }
    //            }
    //        }
    //    }

    @Test
    void testBitFailsOnNon8BitsValues() {
        Random rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            int v;
            do { v = rng.nextInt(); } while (0 <= v && v <= 0xFF);
            int v1 = v;
            assertThrows(IllegalArgumentException.class, () -> {
                Alu.testBit(v1, 0);
            });
        }
    }

    @Test
    void testBitFailsOnNon3BitsValues() {
        Random rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            int v;
            do { v = rng.nextInt(); } while (0 <= v && v <= 0b111);
            int v1 = v;
            assertThrows(IndexOutOfBoundsException.class, () -> {
                Alu.testBit(0, v1);
            });
        }
    }

    // 16-bit operations
    @Test
    void add16LProducesSameResultsAsReference() throws IOException {
        test("add16L", "ADD16L", INT_16, INT_16, Alu::add16L);
    }

    @Test
    void add16LFailsOnNon16BitsValues() {
        Random rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            int v;
            do { v = rng.nextInt(); } while (0 <= v && v <= 0xFFFF);
            int v1 = v;
            assertThrows(IllegalArgumentException.class, () -> {
                Alu.add16L(v1, 0);
            });
            assertThrows(IllegalArgumentException.class, () -> {
                Alu.add16L(0, v1);
            });
        }
    }

    @Test
    void add16HProducesSameResultsAsReference() throws IOException {
        test("add16H", "ADD16H", INT_16, INT_16, Alu::add16H);
    }

    @Test
    void add16HFailsOnNon16BitsValues() {
        Random rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            int v;
            do { v = rng.nextInt(); } while (0 <= v && v <= 0xFFFF);
            int v1 = v;
            assertThrows(IllegalArgumentException.class, () -> {
                Alu.add16H(v1, 0);
            });
            assertThrows(IllegalArgumentException.class, () -> {
                Alu.add16H(0, v1);
            });
        }
    }
}
