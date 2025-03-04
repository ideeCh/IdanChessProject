package com.example.idanchessgame;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BitboardTest {

    @Test
    public void testSetAndClearBit() {
        Bitboard bitboard = new Bitboard();

        // Initially all bits should be 0
        assertEquals(0L, bitboard.getValue());

        // Test setting bits
        bitboard.setBit(0);  // a1
        assertEquals(1L, bitboard.getValue());
        Assertions.assertTrue(bitboard.isBitSet(0));

        bitboard.setBit(7);  // h1
        assertEquals(129L, bitboard.getValue());  // 2^0 + 2^7 = 1 + 128 = 129
        Assertions.assertTrue(bitboard.isBitSet(7));

        bitboard.setBit(63); // h8
        assertEquals(0x8000000000000081L, bitboard.getValue());
        Assertions.assertTrue(bitboard.isBitSet(63));

        // Test clearing bits
        bitboard.clearBit(7);
        assertEquals(0x8000000000000001L, bitboard.getValue());
        Assertions.assertFalse(bitboard.isBitSet(7));

        bitboard.clearBit(0);
        assertEquals(0x8000000000000000L, bitboard.getValue());
        Assertions.assertFalse(bitboard.isBitSet(0));

        bitboard.clearBit(63);
        assertEquals(0L, bitboard.getValue());
        Assertions.assertFalse(bitboard.isBitSet(63));
    }

    @Test
    public void testWithBitSetAndCleared() {
        Bitboard bitboard = new Bitboard(0x1L);  // Bit 0 (a1) is set

        // Test withBitSet
        Bitboard result1 = bitboard.withBitSet(4);
        assertEquals(0x11L, result1.getValue());  // Bits 0 and 4 are set
        assertEquals(0x1L, bitboard.getValue());  // Original bitboard should be unchanged

        // Test withBitCleared
        Bitboard result2 = bitboard.withBitCleared(0);
        assertEquals(0L, result2.getValue());  // All bits are cleared
        assertEquals(0x1L, bitboard.getValue());  // Original bitboard should be unchanged
    }

    @Test
    public void testBitwiseOperations() {
        Bitboard a = new Bitboard(0x0F0FL);  // Bits 0-3 and 8-11 are set
        Bitboard b = new Bitboard(0x00FFL);  // Bits 0-7 are set

        // Test OR
        Bitboard orResult = a.or(b);
        assertEquals(0x0FFFL, orResult.getValue());

        // Test AND
        Bitboard andResult = a.and(b);
        assertEquals(0x000FL, andResult.getValue());

        // Test NOT
        Bitboard notResult = a.not();
        assertEquals(~0x0F0FL, notResult.getValue());

        // Test shifts
        Bitboard leftShift = a.shiftLeft(4);
        assertEquals(0x0F0F0L, leftShift.getValue());

        Bitboard rightShift = a.shiftRight(4);
        assertEquals(0x00F0L, rightShift.getValue());
    }

    @Test
    public void testCountBits() {
        Bitboard empty = new Bitboard();
        assertEquals(0, empty.countBits());

        Bitboard single = new Bitboard(0x1L);
        assertEquals(1, single.countBits());

        Bitboard multiple = new Bitboard(0x1111L);
        assertEquals(4, multiple.countBits());

        Bitboard manyBits = new Bitboard(0xFFFF_FFFF_FFFF_FFFFL);
        assertEquals(64, manyBits.countBits());
    }

    @Test
    public void testLeastSignificantBit() {
        Bitboard empty = new Bitboard();
        assertEquals(-1, empty.getLeastSignificantBitIndex());

        Bitboard single = new Bitboard(0x1L);
        assertEquals(0, single.getLeastSignificantBitIndex());

        Bitboard multiple = new Bitboard(0x100L);
        assertEquals(8, multiple.getLeastSignificantBitIndex());

        // Test clearing least significant bit
        Bitboard toBeCleared = new Bitboard(0x5L);  // Bits 0 and 2 are set
        assertEquals(0, toBeCleared.getLeastSignificantBitIndex());
        toBeCleared.clearLeastSignificantBit();
        assertEquals(2, toBeCleared.getLeastSignificantBitIndex());
        assertEquals(0x4L, toBeCleared.getValue());
    }


    @Test
    public void testOccupiedSquares() {
        // Create all the Bitboard objects with proper initialization
        Bitboard whitePawns = new Bitboard(0xFF00L);
        Bitboard whiteKnights = new Bitboard(0x42L);
        Bitboard whiteBishops = new Bitboard(0x24L);
        Bitboard whiteRooks = new Bitboard(0x81L);
        Bitboard whiteQueens = new Bitboard(0x8L);
        Bitboard whiteKing = new Bitboard(0x10L);

        Bitboard blackPawns = new Bitboard(0xFF000000000000L);
        Bitboard blackKnights = new Bitboard(0x4200000000000000L);
        Bitboard blackBishops = new Bitboard(0x2400000000000000L);
        Bitboard blackRooks = new Bitboard(0x8100000000000000L);
        Bitboard blackQueens = new Bitboard(0x800000000000000L);
        Bitboard blackKing = new Bitboard(0x1000000000000000L);

        // Verify none of them are null before passing to the method
        assertNotNull(whitePawns);
        assertNotNull(whiteKnights);
        assertNotNull(whiteBishops);
        assertNotNull(whiteRooks);
        assertNotNull(whiteQueens);
        assertNotNull(whiteKing);

        assertNotNull(blackPawns);
        assertNotNull(blackKnights);
        assertNotNull(blackBishops);
        assertNotNull(blackRooks);
        assertNotNull(blackQueens);
        assertNotNull(blackKing);

        // Now call the methods
        Bitboard whiteOccupied = Bitboard.getWhiteOccupiedSquares(
                whitePawns, whiteKnights, whiteBishops, whiteRooks, whiteQueens, whiteKing
        );

        Bitboard blackOccupied = Bitboard.getBlackOccupiedSquares(
                blackPawns, blackKnights, blackBishops, blackRooks, blackQueens, blackKing
        );

        Bitboard allOccupied = Bitboard.getOccupiedSquares(
                whitePawns, whiteKnights, whiteBishops, whiteRooks, whiteQueens, whiteKing,
                blackPawns, blackKnights, blackBishops, blackRooks, blackQueens, blackKing
        );

        // From your error it looks like the exact expected values should be:
        assertEquals(0xFFFF, whiteOccupied.getValue());  // Updated from 0x5535
        assertEquals(0xFFFF000000000000L, blackOccupied.getValue());  // Updated
        assertEquals(0xFFFF00000000FFFFL, allOccupied.getValue());  // Updated
    }
    @Test
    public void testRanksAndFiles() {
        // Test rank constants
        assertEquals(0xFFL, Bitboard.RANK_1);
        assertEquals(0xFF00L, Bitboard.RANK_2);
        assertEquals(0xFF0000L, Bitboard.RANK_3);
        assertEquals(0xFF000000L, Bitboard.RANK_4);
        assertEquals(0xFF00000000L, Bitboard.RANK_5);
        assertEquals(0xFF0000000000L, Bitboard.RANK_6);
        assertEquals(0xFF000000000000L, Bitboard.RANK_7);
        assertEquals(0xFF00000000000000L, Bitboard.RANK_8);

        // Test file constants
        assertEquals(0x0101010101010101L, Bitboard.FILE_A);
        assertEquals(0x0202020202020202L, Bitboard.FILE_B);
        assertEquals(0x0404040404040404L, Bitboard.FILE_C);
        assertEquals(0x0808080808080808L, Bitboard.FILE_D);
        assertEquals(0x1010101010101010L, Bitboard.FILE_E);
        assertEquals(0x2020202020202020L, Bitboard.FILE_F);
        assertEquals(0x4040404040404040L, Bitboard.FILE_G);
        assertEquals(0x8080808080808080L, Bitboard.FILE_H);
    }

    @Test
    public void testInvalidSquares() {
        Bitboard bitboard = new Bitboard();

        // Test setting invalid squares
        Assertions.assertThrows(IllegalArgumentException.class, () -> bitboard.setBit(-1));
        Assertions.assertThrows(IllegalArgumentException.class, () -> bitboard.setBit(64));

        // Test clearing invalid squares
        Assertions.assertThrows(IllegalArgumentException.class, () -> bitboard.clearBit(-1));
        Assertions.assertThrows(IllegalArgumentException.class, () -> bitboard.clearBit(64));

        // Test checking invalid squares
        Assertions.assertThrows(IllegalArgumentException.class, () -> bitboard.isBitSet(-1));
        Assertions.assertThrows(IllegalArgumentException.class, () -> bitboard.isBitSet(64));

        // Test with* methods with invalid squares
        Assertions.assertThrows(IllegalArgumentException.class, () -> bitboard.withBitSet(-1));
        Assertions.assertThrows(IllegalArgumentException.class, () -> bitboard.withBitSet(64));
        Assertions.assertThrows(IllegalArgumentException.class, () -> bitboard.withBitCleared(-1));
        Assertions.assertThrows(IllegalArgumentException.class, () -> bitboard.withBitCleared(64));
    }
}