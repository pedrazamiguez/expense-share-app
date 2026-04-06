package es.pedrazamiguez.splittrip.data.firebase.firestore.document

import com.google.firebase.firestore.PropertyName
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Verifies that [DeviceDocument] `@PropertyName` annotation is correctly
 * placed on the getter and setter for `isEmulator`, ensuring Firestore's
 * `CustomClassMapper` maps the `emulator` field without warnings or data loss.
 */
class DeviceDocumentTest {

    private val clazz = DeviceDocument::class.java

    @Nested
    inner class GetterAnnotation {

        @Test
        fun `getter has PropertyName annotation mapping isEmulator to emulator`() {
            val getter = clazz.methods.firstOrNull { method ->
                method.parameterCount == 0 &&
                    method.getAnnotation(PropertyName::class.java)?.value == "emulator"
            }
            assertNotNull(
                getter,
                "Missing @PropertyName(\"emulator\") on getter for 'isEmulator'"
            )
        }
    }

    @Nested
    inner class SetterAnnotation {

        @Test
        fun `setter has PropertyName annotation mapping emulator to isEmulator`() {
            val setter = clazz.methods.firstOrNull { method ->
                method.parameterCount == 1 &&
                    method.getAnnotation(PropertyName::class.java)?.value == "emulator"
            }
            assertNotNull(
                setter,
                "Missing @PropertyName(\"emulator\") on setter for 'isEmulator'"
            )
        }
    }

    @Nested
    inner class RoundTrip {

        @Test
        fun `isEmulator defaults to false`() {
            val doc = DeviceDocument()
            assertFalse(doc.isEmulator)
        }

        @Test
        fun `isEmulator survives round-trip through setter`() {
            val doc = DeviceDocument()
            doc.isEmulator = true
            assertTrue(doc.isEmulator)
        }
    }
}
