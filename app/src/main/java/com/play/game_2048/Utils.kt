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

fun plateauVide(size: Int = 4): Plateau {
    return List(size) { MutableList(size) { Tile(0, 0) } }
}

fun plateauInitial(gameState: GameState): Plateau {
    val size = gameState.boardSize
    val plateau = plateauVide(size)

    val choixRow = generateInRangeExcept(size)
    val choixColumn = generateInRangeExcept(size)
    plateau[choixRow][choixColumn] = Tile(gameState.id++, tireDeuxOuQuatre())

    val choixRow2 = generateInRangeExcept(size, choixRow)
    val choixColumn2 = generateInRangeExcept(size, choixColumn)
    plateau[choixRow2][choixColumn2] = Tile(gameState.id++, tireDeuxOuQuatre())

    return plateau
}

fun deplacementGauche(plateau: Plateau, score: Int): Pair<Plateau, Int> {
    var newScore = score
    for (i in plateau.indices) {
        val nonZeroTiles = plateau[i].filter { it.number != 0 }.toMutableList()
        val mergedTiles = mutableListOf<Tile>()
        
        var index = 0
        while (index < nonZeroTiles.size) {
            if (index + 1 < nonZeroTiles.size && 
                nonZeroTiles[index].number == nonZeroTiles[index + 1].number) {
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
        
        for (j in plateau[i].indices) {
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
    val size = plateau.size
    for (i in plateau.indices) {
        val nonZeroTiles = plateau[i].filter { it.number != 0 }.reversed().toMutableList()
        val mergedTiles = mutableListOf<Tile>()
        
        var index = 0
        while (index < nonZeroTiles.size) {
            if (index + 1 < nonZeroTiles.size && 
                nonZeroTiles[index].number == nonZeroTiles[index + 1].number) {
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
        
        for (j in plateau[i].indices) {
            plateau[i][j] = if (j >= size - mergedTiles.size) {
                mergedTiles[size - j - 1]
            } else {
                Tile(0, 0)
            }
        }
    }
    return Pair(plateau, newScore)
}

fun deplacementHaut(plateau: Plateau, score: Int): Pair<Plateau, Int> {
    var newScore = score
    val size = plateau.size
    for (j in 0 until size) {
        val nonZeroTiles = (0 until size)
            .map { i -> plateau[i][j] }
            .filter { it.number != 0 }
            .toMutableList()
        
        val mergedTiles = mutableListOf<Tile>()
        
        var index = 0
        while (index < nonZeroTiles.size) {
            if (index + 1 < nonZeroTiles.size && 
                nonZeroTiles[index].number == nonZeroTiles[index + 1].number) {
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
        
        for (i in 0 until size) {
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
    val size = plateau.size
    for (j in 0 until size) {
        val nonZeroTiles = (0 until size)
            .map { i -> plateau[i][j] }
            .filter { it.number != 0 }
            .reversed()
            .toMutableList()
        
        val mergedTiles = mutableListOf<Tile>()
        
        var index = 0
        while (index < nonZeroTiles.size) {
            if (index + 1 < nonZeroTiles.size && 
                nonZeroTiles[index].number == nonZeroTiles[index + 1].number) {
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
        
        for (i in 0 until size) {
            plateau[i][j] = if (i >= size - mergedTiles.size) {
                mergedTiles[size - i - 1]
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
    val size = plateau.size
    var a: Int
    var b: Int
    do {
        a = generateInRangeExcept(size)
        b = generateInRangeExcept(size)
    } while (plateau[a][b].number != 0)
    return Pair(a, b)
}

fun deplacement(gameState: GameState, direction: Int): GameState {
    val plateauCopy = gameState.plateau.map { row -> row.map { tile -> Tile(tile.id, tile.number) }.toMutableList() }
    
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
        
        return gameState.copy(
            plateau = plateauDeplace, 
            score = newScore,
            id = gameState.id + 1
        ).apply { updateState() }
    }
    
    return gameState
}

fun estTermine(plateau: Plateau): Boolean {
    val size = plateau.size
    // Check for empty cells
    for (i in 0 until size) {
        for (j in 0 until size) {
            if (plateau[i][j].number == 0) {
                return false
            }
        }
    }
    
    // Check horizontally
    for (i in 0 until size) {
        for (j in 0 until size - 1) {
            if (plateau[i][j].number == plateau[i][j+1].number) {
                return false
            }
        }
    }
    
    // Check vertically
    for (j in 0 until size) {
        for (i in 0 until size - 1) {
            if (plateau[i][j].number == plateau[i+1][j].number) {
                return false
            }
        }
    }
    
    return true
}

fun estGagnant(plateau: Plateau): Boolean {
    for (row in plateau) {
        for (tile in row) {
            if (tile.number == 2048) {
                return true
            }
        }
    }
    return false
}
