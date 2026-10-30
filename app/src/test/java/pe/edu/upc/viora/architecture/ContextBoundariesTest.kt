package pe.edu.upc.viora.architecture

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The dependency rules between bounded contexts, read from the sources: a context reaches another one only from its
 * presentation and only through that context's application layer (its use cases and published language), or
 * through core. Domain and application stay free of Android, Room, Retrofit and Compose.
 */
class ContextBoundariesTest {

    private val root = File("src/main/java/pe/edu/upc/viora")
    private val features = File(root, "features")
    private val reference = Regex("""pe\.edu\.upc\.viora\.features\.(\w+)\.(\w+)""")

    private data class Source(val file: File, val context: String, val layer: String, val text: String)

    private fun sources(): List<Source> = features.walkTopDown().filter { it.extension == "kt" }.map { file ->
        val parts = file.relativeTo(features).invariantSeparatorsPath.split("/")
        Source(file, parts[0], parts[1], file.readText())
    }.toList()

    private fun Source.name() = file.relativeTo(root).invariantSeparatorsPath

    @Test
    fun `the sources are found`() {
        assertTrue("run from the app module: ${root.absolutePath}", features.isDirectory)
        assertTrue(sources().size > 100)
    }

    @Test
    fun `a context reaches another one only from its presentation and only through its application layer`() {
        val offending = sources().flatMap { source ->
            reference.findAll(source.text)
                .filter { it.groupValues[1] != source.context }
                .filterNot { source.layer == "presentation" && it.groupValues[2] == "application" }
                .map { "${source.name()} -> ${it.value}" }
        }.distinct()

        assertEquals(emptyList<String>(), offending)
    }

    @Test
    fun `core depends on no context`() {
        val offending = File(root, "core").walkTopDown().filter { it.extension == "kt" }
            .filter { "pe.edu.upc.viora.features." in it.readText() }
            .map { it.relativeTo(root).invariantSeparatorsPath }
            .toList()

        assertEquals(emptyList<String>(), offending)
    }

    @Test
    fun `domain and application import no framework`() {
        val frameworks = listOf("android.", "androidx.", "retrofit2.", "okhttp3.")
        val inner = sources().filter { it.layer == "domain" || it.layer == "application" } +
            File(root, "core/domain").walkTopDown().filter { it.extension == "kt" }.map { Source(it, "core", "domain", it.readText()) }
        val offending = inner.flatMap { source ->
            source.text.lineSequence()
                .filter { line -> line.startsWith("import ") && frameworks.any { line.removePrefix("import ").startsWith(it) } }
                .map { "${source.name()}: $it" }
                .toList()
        }

        assertEquals(emptyList<String>(), offending)
    }
}
