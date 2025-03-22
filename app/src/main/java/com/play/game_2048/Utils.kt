package com.play.game_2048

import kotlin.random.Random

typealias Plateau = List<MutableList<Tile>>

fun tireDeuxOuQuatre(): Int {
    return if (Random.nextInt(10) == 1) 4 else 2
}

fun generateInRangeExcept(range: Int, num: Int = -1): Int {
    var newNum: Int
    do {
        newNum = Random.nextInt(range)
    } while (newNum == num)
    return newNum
}

fun plateauVide(): Plateau {
    return List(4) { MutableList(4) { Tile(0, 0) } }
}

fun plateauInitial(gameState: GameState): Plateau {
    val plateau = plateauVide()

    val choixRow = generateInRangeExcept(4)
    val choixColumn = generateInRangeExcept(4)
    plateau[choixRow][choixColumn] = Tile(gameState.id++, tireDeuxOuQuatre())

    val choixRow2 = generateInRangeExcept(4, choixRow)
    val choixColumn2 = generateInRangeExcept(4, choixColumn)
    plateau[choixRow2][choixColumn2] = Tile(gameState.id++, tireDeuxOuQuatre())

    return plateau
}

fun deplacementGauche(plateau: Plateau, score: Int): Pair<Plateau, Int> {
    var newScore = score
    for (i in 0 until 4) {
        // First compact non-zero tiles to the left
        val nonZeroTiles = plateau[i].filter { it.number != 0 }.toMutableList()
        
        // Reset the row with empty tiles
        for (j in 0 until 4) {
            plateau[i][j] = Tile(0, 0)
        }
        
        // Process merges one at a time
        var currentIndex = 0
        while (currentIndex < nonZeroTiles.size - 1) {
            if (nonZeroTiles[currentIndex].number == nonZeroTiles[currentIndex + 1].number) {
                // Merge current pair
                nonZeroTiles[currentIndex] = Tile(
                    nonZeroTiles[currentIndex].id,
                    nonZeroTiles[currentIndex].number * 2
                )
                newScore += nonZeroTiles[currentIndex].number
                
                // Remove the second tile that was merged
                nonZeroTiles.removeAt(currentIndex + 1)
                
                // Don't increment currentIndex since we might need to merge with the next tile
            } else {
                currentIndex++
            }
        }
        
        // Place the merged tiles from left to right
        for (j in nonZeroTiles.indices) {
            plateau[i][j] = nonZeroTiles[j]
        }
    }
    return Pair(plateau, newScore)
}

fun deplacementDroite(plateau: Plateau, score: Int): Pair<Plateau, Int> {
    var newScore = score
    for (i in 0 until 4) {
        // Compact non-zero tiles to the right first (remove gaps)
        val nonZeroTiles = plateau[i].filter { it.number != 0 }
        val emptyTiles = MutableList(4 - nonZeroTiles.size) { Tile(0, 0) }
        
        // Fill beginning positions with empty tiles
        for (j in 0 until 4 - nonZeroTiles.size) {
            plateau[i][j] = Tile(0, 0)
        }
        // Copy filtered tiles to the row (from right to left)
        for (j in nonZeroTiles.indices) {
            plateau[i][4 - nonZeroTiles.size + j] = nonZeroTiles[j]
        }
        
        // Merge tiles with the same value (from right to left)
        var j = 3
        while (j > 0) {
            if (plateau[i][j].number != 0 && plateau[i][j].number == plateau[i][j - 1].number) {
                plateau[i][j].number *= 2
                newScore += plateau[i][j].number
                
                // Shift all tiles to the left of the merged pair
                for (k in j - 1 downTo 1) {
                    plateau[i][k] = plateau[i][k - 1]
                }
                plateau[i][0] = Tile(0, 0)
            } else {
                j--
            }
        }
    }
    return Pair(plateau, newScore)
}

fun deplacementHaut(plateau: Plateau, score: Int): Pair<Plateau, Int> {
    var newScore = score
    for (j in 0 until 4) {
        // Extract the column as a list
        val column = (0 until 4).map { i -> plateau[i][j] }
        
        // Compact non-zero tiles to the top first (remove gaps)
        val nonZeroTiles = column.filter { it.number != 0 }
        
        // Copy filtered tiles to the column
        for (i in nonZeroTiles.indices) {
            plateau[i][j] = nonZeroTiles[i]
        }
        // Fill remaining positions with empty tiles
        for (i in nonZeroTiles.size until 4) {
            plateau[i][j] = Tile(0, 0)
        }
        
        // Merge tiles with the same value (top to bottom)
        var i = 0
        while (i < 3) {
            if (plateau[i][j].number != 0 && plateau[i][j].number == plateau[i + 1][j].number) {
                plateau[i][j].number *= 2
                newScore += plateau[i][j].number
                
                // Shift all tiles below the merged pair
                for (k in i + 1 until 3) {
                    plateau[k][j] = plateau[k + 1][j]
                }
                plateau[3][j] = Tile(0, 0)
            } else {
                i++
            }
        }
    }
    return Pair(plateau, newScore)
}

fun deplacementBas(plateau: Plateau, score: Int): Pair<Plateau, Int> {
    var newScore = score
    for (j in 0 until 4) {
        // Extract the column as a list
        val column = (0 until 4).map { i -> plateau[i][j] }
        
        // Compact non-zero tiles to the bottom first (remove gaps)
        val nonZeroTiles = column.filter { it.number != 0 }
        
        // Fill beginning positions with empty tiles
        for (i in 0 until 4 - nonZeroTiles.size) {
            plateau[i][j] = Tile(0, 0)
        }
        // Copy filtered tiles to the column (from bottom to top)
        for (i in nonZeroTiles.indices) {
            plateau[4 - nonZeroTiles.size + i][j] = nonZeroTiles[i]
        }
        
        // Merge tiles with the same value (bottom to top)
        var i = 3
        while (i > 0) {
            if (plateau[i][j].number != 0 && plateau[i][j].number == plateau[i - 1][j].number) {
                plateau[i][j].number *= 2
                newScore += plateau[i][j].number
                
                // Shift all tiles above the merged pair
                for (k in i - 1 downTo 1) {
                    plateau[k][j] = plateau[k - 1][j]
                }
                plateau[0][j] = Tile(0, 0)
            } else {
                i--
            }
        }
    }
    return Pair(plateau, newScore)
}

fun sontPlateauxEgaux(plateau1: Plateau, plateau2: Plateau): Boolean {
    if (plateau1.size != plateau2.size) {
        return false
    }
    for (i in plateau1.indices) {
        if (plateau1[i].size != plateau2[i].size) {
            return false
        }
        for (j in plateau1[i].indices) {
            if (plateau1[i][j].number != plateau2[i][j].number) {
                return false
            }
        }
    }
    return true
}

fun generateFreeIndex(plateau: Plateau): Pair<Int, Int> {
    var a: Int
    var b: Int
    do {
        a = generateInRangeExcept(4)
        b = generateInRangeExcept(4)
    } while (plateau[a][b].number != 0)
    return Pair(a, b)
}

fun deplacement(gameState: GameState, direction: Int): GameState {
    // Create a deep copy of the plateau to avoid modifying the original
    val plateauCopy = gameState.plateau.map { row -> row.map { tile -> Tile(tile.id, tile.number) }.toMutableList() }
    
    // Movement functions now return both plateau and updated score
    val result = when (direction) {
        4 -> deplacementGauche(plateauCopy, gameState.score)
        6 -> deplacementDroite(plateauCopy, gameState.score)
        8 -> deplacementHaut(plateauCopy, gameState.score)
        2 -> deplacementBas(plateauCopy, gameState.score)
        else -> return gameState
    }
    
    val plateauDeplace = result.first
    val newScore = result.second

    if (!sontPlateauxEgaux(gameState.plateau, plateauDeplace)) {
        val (a, b) = generateFreeIndex(plateauDeplace)
        plateauDeplace[a][b] = Tile(gameState.id + 1, tireDeuxOuQuatre())
        
        // Create a new GameState with updated plateau, score, and id
        return gameState.copy(
            plateau = plateauDeplace, 
            score = newScore,
            id = gameState.id + 1
        ).apply { updateState() }
    }
    
    // If no changes, return original state
    return gameState
}

fun estTermine(plateau: Plateau): Boolean {
    // Check if there are any empty cells
    for (i in 0 until 4) {
        for (j in 0 until 4) {
            if (plateau[i][j].number == 0) {
                return false // Game is not over if there's an empty cell
            }
        }
    }
    
    // If no empty cells, check if any adjacent tiles have the same value
    // Check horizontally
    for (i in 0 until 4) {
        for (j in 0 until 3) {
            if (plateau[i][j].number == plateau[i][j+1].number) {
                return false // Game is not over if there are possible merges
            }
        }
    }
    
    // Check vertically
    for (j in 0 until 4) {
        for (i in 0 until 3) {
            if (plateau[i][j].number == plateau[i+1][j].number) {
                return false // Game is not over if there are possible merges
            }
        }
    }
    
    // No empty cells and no possible merges, game is over
    return true
}

fun estGagnant(plateau: Plateau): Boolean {
    for (i in 0 until 4) {
        for (j in 0 until 4) {
            if (plateau[i][j].number == 2048) {
                return true
            }
        }
    }
    return false
}
