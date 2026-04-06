package es.pedrazamiguez.splittrip.data.firebase.firestore.document

import com.google.firebase.firestore.PropertyName
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Verifies that [ExpenseSplitDocument] `@PropertyName` annotations are correctly
 * placed on getters and setters, ensuring Firestore's `CustomClassMapper`
 * serializes/deserializes `is`-prefix fields without warnings or data loss.
 *
 * Firestore uses JavaBean introspection: it reads `@PropertyName` from getter methods
 * to determine the stored field name (serialization), and from setter methods to find
 * the matching field during object reconstruction (deserialization).
 */
class ExpenseSplitDocumentTest {

    private val clazz = ExpenseSplitDocument::class.java

    private val expectedMappings = mapOf(
        "isExcluded" to "excluded",
        "isCoveredById" to "coveredById",
        "isCoveredByRef" to "coveredByRef"
    )

    @Nested
    inner class GetterAnnotations {

        @Test
        fun `getter PropertyName annotations map is-prefix fields to Firestore field names`() {
            expectedMappings.forEach { (property, firestoreName) ->
                val getter = clazz.methods.firstOrNull { method ->
                    method.parameterCount == 0 &&
                        method.getAnnotation(PropertyName::class.java)?.value == firestoreName
                }
                assertNotNull(
                    getter,
                    "Missing @PropertyName(\"$firestoreName\") on getter for '$property'"
                )
            }
        }
    }

    @Nested
    inner class SetterAnnotations {

        @Test
        fun `setter PropertyName annotations allow deserialization from Firestore field names`() {
            expectedMappings.forEach { (property, firestoreName) ->
                val setter = clazz.methods.firstOrNull { method ->
                    method.parameterCount == 1 &&
                        method.getAnnotation(PropertyName::class.java)?.value == firestoreName
                }
                assertNotNull(
                    setter,
                    "Missing @PropertyName(\"$firestoreName\") on setter for '$property'"
                )
            }
        }
    }

    @Nested
    inner class RoundTrip {

        @Test
        fun `isExcluded survives round-trip through setter`() {
            val doc = ExpenseSplitDocument()
            doc.isExcluded = true
            assertTrue(doc.isExcluded)
        }

        @Test
        fun `isCoveredById survives round-trip through setter`() {
            val doc = ExpenseSplitDocument()
            doc.isCoveredById = "covering-user"
            assertEquals("covering-user", doc.isCoveredById)
        }
    }
}
