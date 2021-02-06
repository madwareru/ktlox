import com.github.madwareru.ktlox.Scanner
import com.github.madwareru.ktlox.print
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess

fun main(args: Array<String>) = when(args.size) {
    0 -> runAsInterpreter()
    1 -> runFile(args[0])
    else -> showUsage()
}

private fun showUsage() {
    println("usage: ktlox [script]")
    exitProcess(64)
}

private fun runFile(fileName: String) {
    val bytes = Files.readAllBytes(Paths.get(fileName))
    val translationUnit = String(bytes, Charsets.UTF_8)
    runTranslationUnit(translationUnit)
}

private fun runAsInterpreter() {
    val input = InputStreamReader(System.`in`)
    val reader = BufferedReader(input)
    while (true) {
        print("> ")
        val line = reader.readLine()
        if (line == null || line.isEmpty()) {
            break
        } else {
            runTranslationUnit(line)
        }
    }
}

private fun runTranslationUnit(translationUnit: String) {
    val scanner = Scanner(translationUnit)
    for (token in scanner.scannedTokensIgnoringCommentsAndWhitespaces) {
        print("${token.type.print()} ")
    }
    println()
}

