import com.github.madwareru.ktlox.*
import com.github.madwareru.ktlox.visitors.ExpressionEvaluatorVisitor
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
    val parser = Parser(scanner.scannedTokensIgnoringCommentsAndWhitespaces)
    when (val ast = parser.parse()) {
        is Result.Err -> println( ast.reason )
        is Result.Ok -> println(
            when (val evaluated = ast.value.acceptVisitor(ExpressionEvaluatorVisitor())) {
                is Result.Err -> evaluated.reason
                is Result.Ok -> when (val v = evaluated.value) {
                    is LoxValue.Boolean -> v.value.toString()
                    is LoxValue.IdentifierName -> v.value
                    LoxValue.NilLiteral -> "Nil"
                    LoxValue.None -> "None"
                    is LoxValue.Number -> v.value.toString()
                    is LoxValue.String -> v.value
                }
            }
        )
    }
}

