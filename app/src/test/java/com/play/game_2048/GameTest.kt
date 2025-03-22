package com.play.game_2048

import org.junit.Assert.*
import org.junit.Test

class GameTest {
    @Test
    fun testInitialPlateau() {
        val gameState = GameState(plateau = plateauVide())
        val plateau = plateauInitial(gameState)
        
        // Count non-zero tiles
        var nonZeroCount = 0
        for (row in plateau) {
            for (tile in row) {
                if (tile.number != 0) nonZeroCount++
            }
        }
        assertEquals("Initial plateau should have exactly 2 tiles", 2, nonZeroCount)
    }

    @Test
    fun testSeparateMovements() {
        // Test right edge movement
        val plateauRight = List(4) { MutableList(4) { Tile(0, 0) } }
        plateauRight[0][3] = Tile(1, 2)  // Right edge
        
        val (leftMove, _) = deplacementGauche(plateauRight, 0)
        assertEquals("Right edge tile should move to left", 2, leftMove[0][0].number)
        
        // Test bottom edge movement in a separate board
        val plateauBottom = List(4) { MutableList(4) { Tile(0, 0) } }
        plateauBottom[3][0] = Tile(1, 2)  // Bottom tile
        plateauBottom[0][0] = Tile(2, 2)  // Top tile, should merge
        
        val (upMove, upScore) = deplacementHaut(plateauBottom, 0)
        assertEquals("Tiles should merge at top", 4, upMove[0][0].number)
        assertEquals("Score should be 4", 4, upScore)
    }

    @Test
    fun testStackingMovement() {
        // Test proper stacking behavior
        val plateau = List(4) { MutableList(4) { Tile(0, 0) } }
        plateau[0][0] = Tile(1, 2)
        plateau[1][1] = Tile(2, 2)
        plateau[2][2] = Tile(3, 2)
        plateau[3][3] = Tile(4, 2)
        
        // Test vertical movement down - tiles should stack at bottom
        val (downMove, _) = deplacementBas(plateau, 0)
        
        // All tiles should move to bottom, stacking from bottom up
        assertEquals("Bottom position should be 2", 2, downMove[3][0].number)
        assertEquals("Second from bottom should be 2", 2, downMove[3][1].number)
        assertEquals("Third from bottom should be 2", 2, downMove[3][2].number)
        assertEquals("Top position should be 2", 2, downMove[3][3].number)
    }

    @Test
    fun testSequentialMergesNotAllowed() {
        // Test that tiles cannot merge multiple times in one move
        val plateau = List(4) { MutableList(4) { Tile(0, 0) } }
        plateau[0][0] = Tile(1, 2)
        plateau[0][1] = Tile(2, 2)
        plateau[0][2] = Tile(3, 2)
        plateau[0][3] = Tile(4, 2)
        
        val (leftMove, score) = deplacementGauche(plateau, 0)
        
        // Should only merge once per pair
        assertEquals("First position should be 4", 4, leftMove[0][0].number)
        assertEquals("Second position should be 4", 4, leftMove[0][1].number)
        assertEquals("Score should be 8", 8, score)
    }

    @Test
    fun testMergePreservesLeftmostId() {
        // Test that when merging, the leftmost (or topmost) tile's ID is preserved
        val plateau = List(4) { MutableList(4) { Tile(0, 0) } }
        plateau[0][1] = Tile(1, 2)
        plateau[0][2] = Tile(2, 2)
        
        val (leftMove, _) = deplacementGauche(plateau, 0)
        assertEquals("Merged tile should have first tile's ID", 1, leftMove[0][0].id)
    }

    @Test
    fun testDeplacementGaucheMerge() {
        // Test merging same numbers
        val plateau = List(4) { MutableList(4) { Tile(0, 0) } }
        plateau[0][1] = Tile(1, 2)
        plateau[0][2] = Tile(2, 2)
        
        val (newPlateau, score) = deplacementGauche(plateau, 0)
        
        assertEquals("Merged tiles should create a 4", 4, newPlateau[0][0].number)
        assertEquals("Score should be 4", 4, score)
        assertEquals("Rest of the row should be empty", 0, newPlateau[0][1].number)
    }

    @Test
    fun testDeplacementGaucheNoMerge() {
        // Test moving different numbers
        val plateau = List(4) { MutableList(4) { Tile(0, 0) } }
        plateau[0][1] = Tile(1, 2)
        plateau[0][2] = Tile(2, 4)
        
        val (newPlateau, score) = deplacementGauche(plateau, 0)
        
        assertEquals("First tile should be 2", 2, newPlateau[0][0].number)
        assertEquals("Second tile should be 4", 4, newPlateau[0][1].number)
        assertEquals("Score should not change", 0, score)
    }

    @Test
    fun testMultipleMerges() {
        // Test multiple merges in one move
        val plateau = List(4) { MutableList(4) { Tile(0, 0) } }
        plateau[0][0] = Tile(1, 2)
        plateau[0][1] = Tile(2, 2)
        plateau[0][2] = Tile(3, 2)
        plateau[0][3] = Tile(4, 2)
        
        val (newPlateau, score) = deplacementGauche(plateau, 0)
        
        assertEquals("First merge should be 4", 4, newPlateau[0][0].number)
        assertEquals("Second merge should be 4", 4, newPlateau[0][1].number)
        assertEquals("Score should be 8", 8, score)
    }

    @Test
    fun testNoMovePossible() {
        // Test when no moves are possible
        val plateau = List(4) { MutableList(4) { Tile(0, 0) } }
        // Fill with alternating numbers
        for (i in 0..3) {
            for (j in 0..3) {
                plateau[i][j] = Tile((i * 4 + j), if ((i + j) % 2 == 0) 2 else 4)
            }
        }
        
        assertTrue("Game should be terminated", estTermine(plateau))
    }

    @Test
    fun testWinCondition() {
        // Test win condition (reaching 2048)
        val plateau = List(4) { MutableList(4) { Tile(0, 0) } }
        plateau[0][0] = Tile(1, 2048)
        
        assertTrue("Should detect win condition", estGagnant(plateau))
    }

    @Test
    fun testComplexMergeChain() {
        // Test complex merge scenario
        val plateau = List(4) { MutableList(4) { Tile(0, 0) } }
        plateau[0][0] = Tile(1, 4)
        plateau[0][1] = Tile(2, 4)
        plateau[0][2] = Tile(3, 4)
        plateau[0][3] = Tile(4, 4)
        
        val (newPlateau, score) = deplacementGauche(plateau, 0)
        
        assertEquals("First merge should be 8", 8, newPlateau[0][0].number)
        assertEquals("Second merge should be 8", 8, newPlateau[0][1].number)
        assertEquals("Score should be 16", 16, score)
    }

    @Test
    fun testBorderConditions() {
        // Test movements at board edges
        val plateau = List(4) { MutableList(4) { Tile(0, 0) } }
        plateau[0][3] = Tile(1, 2)  // Right edge
        plateau[3][0] = Tile(2, 2)  // Bottom edge
        
        val (leftMove, _) = deplacementGauche(plateau, 0)
        assertEquals("Right edge tile should move to left", 2, leftMove[0][0].number)
        
        val (upMove, _) = deplacementHaut(plateau, 0)
        assertEquals("Bottom edge tile should move to top", 4, upMove[0][0].number)
    }

    @Test
    fun testSequentialMoves() {
        // Test sequence of moves
        var plateau = List(4) { MutableList(4) { Tile(0, 0) } }
        plateau[0][0] = Tile(1, 2)
        plateau[0][1] = Tile(2, 2)
        
        var score = 0
        
        // First move - merge left
        val moveLeft = deplacementGauche(plateau, score)
        plateau = moveLeft.first
        score = moveLeft.second
        
        assertEquals("After left move, should have 4", 4, plateau[0][0].number)
        assertEquals("Score should be 4", 4, score)
        
        // Move down
        plateau[0][0] = Tile(3, 4)
        plateau[1][0] = Tile(4, 4)
        
        val moveDown = deplacementBas(plateau, score)
        plateau = moveDown.first
        score = moveDown.second
        
        assertEquals("After down move, should have 8", 8, plateau[3][0].number)
        assertEquals("Score should be 12", 12, score)
    }

    @Test
    fun testPreventDoubleMerge() {
        // Test that tiles don't merge twice in one move
        val plateau = List(4) { MutableList(4) { Tile(0, 0) } }
        plateau[0][0] = Tile(1, 2)
        plateau[0][1] = Tile(2, 2)
        plateau[0][2] = Tile(3, 4)
        
        val (newPlateau, score) = deplacementGauche(plateau, 0)
        
        assertEquals("First position should be 4", 4, newPlateau[0][0].number)
        assertEquals("Second position should be 4", 4, newPlateau[0][1].number)
        assertEquals("Score should be 4", 4, score)
    }

    @Test
    fun testBlockedMerges() {
        // Test that merges don't happen through other tiles
        val plateau = List(4) { MutableList(4) { Tile(0, 0) } }
        plateau[0][0] = Tile(1, 2)
        plateau[0][1] = Tile(2, 4)
        plateau[0][2] = Tile(3, 2)
        
        val (newPlateau, score) = deplacementGauche(plateau, 0)
        
        assertEquals("First position should stay 2", 2, newPlateau[0][0].number)
        assertEquals("Middle position should stay 4", 4, newPlateau[0][1].number)
        assertEquals("Last position should stay 2", 2, newPlateau[0][2].number)
        assertEquals("Score should not change", 0, score)
    }

    @Test
    fun testAlmostFullBoard() {
        // Test movement with almost full board
        val plateau = List(4) { MutableList(4) { Tile(0, 0) } }
        var id = 1
        // Fill board with unique numbers except one spot
        for (i in 0..3) {
            for (j in 0..3) {
                if (i != 3 || j != 3) {
                    plateau[i][j] = Tile(id++, (i+j+2)*2)
                }
            }
        }
        
        assertFalse("Game should not be terminated with one empty spot", estTermine(plateau))
    }

    @Test
    fun testFullLineWithDifferentNumbers() {
        // Test line with all different numbers
        val plateau = List(4) { MutableList(4) { Tile(0, 0) } }
        plateau[0][0] = Tile(1, 2)
        plateau[0][1] = Tile(2, 4)
        plateau[0][2] = Tile(3, 8)
        plateau[0][3] = Tile(4, 16)
        
        val (newPlateau, score) = deplacementGauche(plateau, 0)
        
        // Numbers should maintain their order
        assertEquals("First number should be 2", 2, newPlateau[0][0].number)
        assertEquals("Second number should be 4", 4, newPlateau[0][1].number)
        assertEquals("Third number should be 8", 8, newPlateau[0][2].number)
        assertEquals("Fourth number should be 16", 16, newPlateau[0][3].number)
        assertEquals("Score should not change", 0, score)
    }

    @Test
    fun testCornerToCornerMovement() {
        // Test movement from one corner to the opposite
        val plateau = List(4) { MutableList(4) { Tile(0, 0) } }
        plateau[0][0] = Tile(1, 2)
        plateau[1][1] = Tile(2, 2)
        plateau[2][2] = Tile(3, 2)
        plateau[3][3] = Tile(4, 2)
        
        // Move to bottom-left corner
        var currentPlateau = plateau
        var totalScore = 0
        
        val (moveDown, score1) = deplacementBas(currentPlateau, totalScore)
        currentPlateau = moveDown
        totalScore += score1
        
        val (moveLeft, score2) = deplacementGauche(currentPlateau, totalScore)
        currentPlateau = moveLeft
        totalScore += score2
        
        assertEquals("Should have 4 in bottom-left", 4, currentPlateau[3][0].number)
        assertEquals("Should have 4 in bottom-left", 4, currentPlateau[3][1].number)
        assertEquals("Total score should be 8", 8, totalScore)
    }

    @Test
    fun testIsolatedTiles() {
        // Test that isolated tiles don't affect each other
        val plateau = List(4) { MutableList(4) { Tile(0, 0) } }
        plateau[0][0] = Tile(1, 2)
        plateau[0][2] = Tile(2, 2)
        plateau[2][0] = Tile(3, 4)
        plateau[2][2] = Tile(4, 4)
        
        val (newPlateau, score) = deplacementGauche(plateau, 0)
        
        assertEquals("Top row should merge to 4", 4, newPlateau[0][0].number)
        assertEquals("Bottom row should merge to 8", 8, newPlateau[2][0].number)
        assertEquals("Score should be 12", 12, score)
    }

    @Test
    fun testIdPreservation() {
        // Test that tile IDs are preserved after merging
        val plateau = List(4) { MutableList(4) { Tile(0, 0) } }
        plateau[0][1] = Tile(1, 2)  // Use ID 1
        plateau[0][2] = Tile(2, 2)  // Use ID 2
        
        val (newPlateau, _) = deplacementGauche(plateau, 0)
        
        // The merged tile should keep the ID of the first tile
        assertEquals("Merged tile should keep first tile's ID", 1, newPlateau[0][0].id)
    }

    @Test
    fun testMultipleRowMerges() {
        // Test merging happening in multiple rows simultaneously
        val plateau = List(4) { MutableList(4) { Tile(0, 0) } }
        plateau[0][1] = Tile(1, 2)
        plateau[0][2] = Tile(2, 2)
        plateau[1][0] = Tile(3, 4)
        plateau[1][1] = Tile(4, 4)
        plateau[2][2] = Tile(5, 8)
        plateau[2][3] = Tile(6, 8)
        
        val (newPlateau, score) = deplacementGauche(plateau, 0)
        
        assertEquals("First row should have 4", 4, newPlateau[0][0].number)
        assertEquals("Second row should have 8", 8, newPlateau[1][0].number)
        assertEquals("Third row should have 16", 16, newPlateau[2][0].number)
        assertEquals("Total score should be 28", 28, score)
    }

    @Test
    fun testNoChangeNoNewTile() {
        // Test that a move that doesn't change anything doesn't add a new tile
        val plateau = List(4) { MutableList(4) { Tile(0, 0) } }
        plateau[0][0] = Tile(1, 2)
        plateau[0][1] = Tile(2, 4)
        
        val gameState = GameState(plateau = plateau, id = 3)
        val newGameState = deplacement(gameState, 4)  // Move left
        
        // Since tiles are already on the left, nothing should change
        assertTrue("Plateaus should be equal", sontPlateauxEgaux(plateau, newGameState.plateau))
        assertEquals("ID should not change", 3, newGameState.id)
    }

    @Test
    fun testLargeNumberMerge() {
        // Test merging of large numbers
        val plateau = List(4) { MutableList(4) { Tile(0, 0) } }
        plateau[0][1] = Tile(1, 1024)
        plateau[0][2] = Tile(2, 1024)
        
        val (newPlateau, score) = deplacementGauche(plateau, 0)
        
        assertEquals("Should merge to 2048", 2048, newPlateau[0][0].number)
        assertEquals("Score should be 2048", 2048, score)
    }

    @Test
    fun testFullBoardMove() {
        // Test movement on a full board where merges are possible
        val plateau = List(4) { MutableList(4) { Tile(0, 0) } }
        var id = 1
        // Fill the board with alternating 2s and 4s
        for (i in 0..3) {
            for (j in 0..3) {
                plateau[i][j] = Tile(id++, if ((i + j) % 2 == 0) 2 else 2)
            }
        }
        
        val (newPlateau, score) = deplacementGauche(plateau, 0)
        assertTrue("Score should be positive after merges", score > 0)
    }

    @Test
    fun testNoGapsAfterMovement() {
        // Test that no gaps are left after movement (the issue reported by user)
        val plateau = List(4) { MutableList(4) { Tile(0, 0) } }
        plateau[0][1] = Tile(1, 16)
        plateau[0][2] = Tile(2, 8)
        plateau[0][3] = Tile(3, 32)
        
        val (newPlateau, _) = deplacementGauche(plateau, 0)
        
        // Check that numbers are packed to the left with no gaps
        assertEquals("First position should be 16", 16, newPlateau[0][0].number)
        assertEquals("Second position should be 8", 8, newPlateau[0][1].number)
        assertEquals("Third position should be 32", 32, newPlateau[0][2].number)
        assertEquals("Fourth position should be empty", 0, newPlateau[0][3].number)
    }

    @Test
    fun testMultipleGapsMovement() {
        // Test movement with multiple gaps between tiles
        val plateau = List(4) { MutableList(4) { Tile(0, 0) } }
        plateau[0][0] = Tile(1, 2)
        plateau[0][2] = Tile(2, 2)  // Gap at [0][1]
        
        val (newPlateau, score) = deplacementGauche(plateau, 0)
        
        assertEquals("Should merge to 4", 4, newPlateau[0][0].number)
        assertEquals("Score should be 4", 4, score)
        // Verify no tiles are lost
        var nonZeroCount = 0
        for (j in 0..3) {
            if (newPlateau[0][j].number != 0) nonZeroCount++
        }
        assertEquals("Should have exactly one non-zero tile after merge", 1, nonZeroCount)
    }

    @Test
    fun testAlternatingPatternMove() {
        // Test movement with alternating values pattern
        val plateau = List(4) { MutableList(4) { Tile(0, 0) } }
        plateau[0][0] = Tile(1, 2)
        plateau[0][1] = Tile(2, 4)
        plateau[0][2] = Tile(3, 2)
        plateau[0][3] = Tile(4, 4)
        
        val (leftMove, _) = deplacementGauche(plateau, 0)
        // Verify tiles maintain their relative positions when they can't merge
        assertEquals("First position should be 2", 2, leftMove[0][0].number)
        assertEquals("Second position should be 4", 4, leftMove[0][1].number)
        assertEquals("Third position should be 2", 2, leftMove[0][2].number)
        assertEquals("Fourth position should be 4", 4, leftMove[0][3].number)
    }

    @Test
    fun testConsecutiveMovesWithGaps() {
        // Test multiple consecutive moves with gaps
        val plateau = List(4) { MutableList(4) { Tile(0, 0) } }
        plateau[0][3] = Tile(1, 2)  // Rightmost position
        
        var currentPlateau = plateau
        var totalScore = 0
        
        // Move left first
        val (moveLeft1, score1) = deplacementGauche(currentPlateau, totalScore)
        currentPlateau = moveLeft1
        totalScore += score1
        
        // Add a new tile and move left again
        currentPlateau[0][3] = Tile(2, 2)  // Add new tile at right
        
        val (moveLeft2, score2) = deplacementGauche(currentPlateau, totalScore)
        currentPlateau = moveLeft2
        totalScore += score2
        
        assertEquals("Should have merged to 4", 4, currentPlateau[0][0].number)
        assertEquals("Score should be 4", 4, totalScore)
        // Verify no gaps with non-zero values
        for (j in 1..3) {
            assertEquals("Position $j should be empty", 0, currentPlateau[0][j].number)
        }
    }
}