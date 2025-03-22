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
        // Get all non-zero tiles in the current row
        val nonZeroTiles = plateau[i].filter { it.number != 0 }.toMutableList()
        
        // Create a new list for merged tiles
        val mergedTiles = mutableListOf<Tile>()
        
        // Process merges
        var index = 0
        while (index < nonZeroTiles.size) {
            if (index + 1 < nonZeroTiles.size && 
                nonZeroTiles[index].number == nonZeroTiles[index + 1].number) {
                // Merge tiles
                mergedTiles.add(Tile(
                    nonZeroTiles[index].id,
                    nonZeroTiles[index].number * 2
                ))
                newScore += mergedTiles.last().number
                index += 2 // Skip next tile since it was merged
            } else {
                // Keep tile as is
                mergedTiles.add(nonZeroTiles[index])
                index++
            }
        }
        
        // Fill the row with merged tiles followed by empty tiles
        for (j in 0 until 4) {
            plateau[i][j] = if (j < mergedTiles.size) {
                mergedTiles[j]
            } else {
                Tile(0, 0)
            }
        }
    }
    return Pair(plateau, newScore)
}

fun deplacementDroite(plateau: Plateau, score: Int): Pair<Plateau, Int> {
    var newScore = score
    for (i in 0 until 4) {
        // Get all non-zero tiles in reverse order
        val nonZeroTiles = plateau[i].filter { it.number != 0 }.reversed().toMutableList()
        
        // Create a new list for merged tiles
        val mergedTiles = mutableListOf<Tile>()
        
        // Process merges
        var index = 0
        while (index < nonZeroTiles.size) {
            if (index + 1 < nonZeroTiles.size && 
                nonZeroTiles[index].number == nonZeroTiles[index + 1].number) {
                // Merge tiles
                mergedTiles.add(Tile(
                    nonZeroTiles[index].id,
                    nonZeroTiles[index].number * 2
                ))
                newScore += mergedTiles.last().number
                index += 2
            } else {
                mergedTiles.add(nonZeroTiles[index])
                index++
            }
        }
        
        // Fill the row with empty tiles followed by merged tiles
        for (j in 0 until 4) {
            plateau[i][j] = if (j >= 4 - mergedTiles.size) {
                mergedTiles[4 - j - 1]
            } else {
                Tile(0, 0)
            }
        }
    }
    return Pair(plateau, newScore)
}

fun deplacementHaut(plateau: Plateau, score: Int): Pair<Plateau, Int> {
    var newScore = score
    for (j in 0 until 4) {
        // Get all non-zero tiles in the current column
        val nonZeroTiles = (0 until 4)
            .map { i -> plateau[i][j] }
            .filter { it.number != 0 }
            .toMutableList()
        
        // Create a new list for merged tiles
        val mergedTiles = mutableListOf<Tile>()
        
        // Process merges
        var index = 0
        while (index < nonZeroTiles.size) {
            if (index + 1 < nonZeroTiles.size && 
                nonZeroTiles[index].number == nonZeroTiles[index + 1].number) {
                // Merge tiles
                mergedTiles.add(Tile(
                    nonZeroTiles[index].id,
                    nonZeroTiles[index].number * 2
                ))
                newScore += mergedTiles.last().number
                index += 2
            } else {
                mergedTiles.add(nonZeroTiles[index])
                index++
            }
        }
        
        // Fill the column with merged tiles followed by empty tiles
        for (i in 0 until 4) {
            plateau[i][j] = if (i < mergedTiles.size) {
                mergedTiles[i]
            } else {
                Tile(0, 0)
            }
        }
    }
    return Pair(plateau, newScore)
}

fun deplacementBas(plateau: Plateau, score: Int): Pair<Plateau, Int> {
    var newScore = score
    for (j in 0 until 4) {
        // Get all non-zero tiles in the current column in reverse order
        val nonZeroTiles = (0 until 4)
            .map { i -> plateau[i][j] }
            .filter { it.number != 0 }
            .reversed()
            .toMutableList()
        
        // Create a new list for merged tiles
        val mergedTiles = mutableListOf<Tile>()
        
        // Process merges
        var index = 0
        while (index < nonZeroTiles.size) {
            if (index + 1 < nonZeroTiles.size && 
                nonZeroTiles[index].number == nonZeroTiles[index + 1].number) {
                // Merge tiles
                mergedTiles.add(Tile(
                    nonZeroTiles[index].id,
                    nonZeroTiles[index].number * 2
                ))
                newScore += mergedTiles.last().number
                index += 2
            } else {
                mergedTiles.add(nonZeroTiles[index])
                index++
            }
        }
        
        // Fill the column with empty tiles followed by merged tiles
        for (i in 0 until 4) {
            plateau[i][j] = if (i >= 4 - mergedTiles.size) {
                mergedTiles[4 - i - 1]
            } else {
                Tile(0, 0)
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
