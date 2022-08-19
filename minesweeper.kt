import kotlin.random.Random

object Minesweeper {

    const val MINE_MARK = 'X'
    const val EMPTY_MARK = '#'
    const val HIDE_MARK = '.'
    const val SIGN_MARK = '@'

    private var alreadyInitialized = false
    var steppedOnAMine = false


    fun init(dimension: Int, numberOfMines: Int) {
        Field.init(dimension, numberOfMines)
        alreadyInitialized = true
    }

    fun parseInput(input: String): Boolean {
        if (!validInput(input)) {
            println("Invalid input")
            return false
        }
        val row = input.split(" ")[1].toInt() - 1
        val col = input.split(" ")[0].toInt() - 1
        val command = input.split(" ")[2]
        val cell = Field.getCellAt(row, col)!!


        if (cell.explored) {
            println("Cell already explored")
            return false
        } else {
            if (command == "mine") {
                if (!cell.signedAsMine) cell.sign()
                else cell.unSign()
            } else { // command == "free"
                if (cell.isMine()) {
                    Field.showAllMines()
                    steppedOnAMine = true
                } else if (cell.tag.isDigit()) {
                    cell.show()
                } else { // cell is not a mine neither a number
                    Field.expand(cell)
                }
            }
        }
        return true
    }

    private fun validInput(input: String): Boolean {
        if (input.matches("^[1-9][0-9]? [1-9][0-9]? (free|mine)$".toRegex())) {
            val col = input.split(" ")[0].toInt()
            val row = input.split(" ")[1].toInt()
            if (row in 1..Field.getDimension() && col in 1..Field.getDimension()) return true
        }
        return false
    }

    fun playerHasWon(): Boolean {

        for (row in 0 until Field.getDimension()) {
            for (col in 0 until Field.getDimension()) {
                val cell = Field.getCellAt(row, col)!!
                if (!cell.explored && !cell.isMine()) return false
                if (cell.signedAsMine && !cell.isMine()) return false
            }
        }

        println("Congratulations! You found all the mines!")
        return true
    }

    object Field {

        private var dimension = 9
        private var numberOfMines = 8
        private var field = mutableListOf<MutableList<Cell>>()

        class Cell {

            var row = -1
            var col = -1
            var mark: Char
            var tag: Char
            var signedAsMine = false
            var explored = false
            private var mine = false

            constructor(row: Int, col: Int, mark: Char, mine: Boolean = false) {
                this.row = row
                this.col = col
                this.mark = mark
                this.tag = mark
                this.mine = mine
            }

            fun isMine(): Boolean = mine
            fun hide() { mark = HIDE_MARK }
            fun show() {
                mark = tag
                explored = true
                signedAsMine = false
            }

            fun sign() {
                if (!signedAsMine) {
                    if (mark == HIDE_MARK) {
                        mark = SIGN_MARK
                        signedAsMine = true
                    }
                    else throw Exception("Something went wrong 1")
                } else throw Exception("Cannot sign a Cell already signed")
            }

            fun unSign() {
                if (signedAsMine) {
                    if (mark == SIGN_MARK) {
                        mark = HIDE_MARK
                        signedAsMine = false
                    }
                    else throw Exception("Something went wrong 2")
                } else throw Exception("Cannot unsign a Cell already unsigned")
            }
        }

        fun init(dimension: Int, numberOfMines: Int) {

            if (alreadyInitialized) { return }
            this.dimension = dimension
            this.numberOfMines = numberOfMines

            when {
                numberOfMines < 1 ->
                    throw Exception("The number of mines must be greater than or equal to 1")
                numberOfMines > dimension * dimension / 2 ->
                    throw Exception("The number of mines must be lower")
                dimension !in 4..40 ->
                    throw Exception("The dimension of the field must be greater than 4 and less than 40")
                MINE_MARK == EMPTY_MARK ->
                    throw Exception("The mine mark must be different than the safe cell mark")
            }
            generateField()
            generateRandomMines()
            fixCellsNumber()
            hideAll()
        }

        private fun generateField() {
            field.clear()
            for (row in 0 until dimension) {
                field.add(mutableListOf())
                for (col in 0 until dimension) {
                    field[row].add(Cell(row, col, EMPTY_MARK))
                }
            }
        }

        private fun generateRandomMines() {

            var randomRow: Int
            var randomCol: Int

            repeat(numberOfMines) {
                do {
                    randomRow = Random.nextInt(0, dimension)
                    randomCol = Random.nextInt(0, dimension)
                } while (getCellAt(randomRow, randomCol)!!.isMine())
                field[randomRow][randomCol] = Cell(randomRow, randomCol, mark = MINE_MARK, mine = true)
            }
        }

        private fun fixCellsNumber() {

            for (row in 0 until dimension) {
                for (col in 0 until dimension) {
                    if (!getCellAt(row, col)!!.isMine()) {
                        var counter = 0

                        getCellAt(row - 1, col - 1)?.let { if (it.isMine()) counter++ }
                        getCellAt(row - 1, col)?.let { if (it.isMine()) counter++ }
                        getCellAt(row - 1, col + 1)?.let { if (it.isMine()) counter++ }
                        getCellAt(row, col - 1)?.let { if (it.isMine()) counter++ }
                        getCellAt(row, col + 1)?.let { if (it.isMine()) counter++ }
                        getCellAt(row + 1, col - 1)?.let { if (it.isMine()) counter++ }
                        getCellAt(row + 1, col)?.let { if (it.isMine()) counter++ }
                        getCellAt(row + 1, col + 1)?.let { if (it.isMine()) counter++ }

                        if (counter > 0) {
                            getCellAt(row, col)!!.mark = counter.digitToChar()
                            getCellAt(row, col)!!.tag = counter.digitToChar()
                        }
                    }
                }
            }
        }

        private fun hideAll() {
            for (row in 0 until dimension) {
                for (col in 0 until dimension) {
                    getCellAt(row, col)!!.hide()
                }
            }
        }

        fun getCellAt(row: Int, column: Int): Cell? {
            return if (row in 0 until dimension && column in 0 until dimension) field[row][column]
            else null
        }

        fun print() {
            print("---|")
            for (i in 1..dimension) print(i.toString().padStart(3))
            println(" |\n---|${"---".repeat(dimension)}-|")
            for (row in 0 until dimension) {
                print("${(row + 1).toString().padStart(3)}|")
                for (col in 0 until dimension) {
                    print("  " + getCellAt(row, col)!!.mark)
                }
                println(" |")
            }
            println("---|${"---".repeat(dimension)}-|")
        }

        fun showAllMines() {
            for (row in 0 until dimension) {
                for (col in 0 until dimension) {
                    val cell = getCellAt(row, col)!!
                    if (cell.isMine()) cell.show()
                }
            }
        }

        fun expand(cell: Cell) {

            cell.show()
            getCellAt(cell.row - 1, cell.col)?.let {
                if (!it.explored) if (it.tag.isDigit()) it.show() else expand(it)
            }
            getCellAt(cell.row + 1, cell.col)?.let {
                if (!it.explored) if (it.tag.isDigit()) it.show() else expand(it)
            }
            getCellAt(cell.row, cell.col - 1)?.let {
                if (!it.explored) if (it.tag.isDigit()) it.show() else expand(it)
            }
            getCellAt(cell.row, cell.col + 1)?.let {
                if (!it.explored) if (it.tag.isDigit()) it.show() else expand(it)
            }

            getCellAt(cell.row - 1, cell.col - 1)?.let {
                if (!it.explored) if (it.tag.isDigit()) it.show() else expand(it)
            }
            getCellAt(cell.row - 1, cell.col + 1)?.let {
                if (!it.explored) if (it.tag.isDigit()) it.show() else expand(it)
            }
            getCellAt(cell.row + 1, cell.col - 1)?.let {
                if (!it.explored) if (it.tag.isDigit()) it.show() else expand(it)
            }
            getCellAt(cell.row + 1, cell.col + 1)?.let {
                if (!it.explored) if (it.tag.isDigit()) it.show() else expand(it)
            }
        }

        fun getDimension(): Int = dimension
    }


}

fun main() {
    println("How many mines do you want on the field?")
    Minesweeper.init(dimension = 9, numberOfMines = readln().toInt())
    Minesweeper.Field.print()

    var input: String
    program@while (!Minesweeper.playerHasWon()) {
        do {
            println("Set/unset mines marks or claim a cell as free:")
            input = readln()
            if (input == "exit") {
                println("Bye!")
                break@program
            }
        } while (!Minesweeper.parseInput(input))
        Minesweeper.Field.print()
        if (Minesweeper.steppedOnAMine) {
            println("You stepped on a mine and failed!")
            break
        }
    }
}
