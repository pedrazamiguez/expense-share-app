## No Fully Qualified Names (No FQN)

NEVER use Fully Qualified Names (FQN) inside production or test source code (e.g., `java.util.UUID.randomUUID()`, `com.google.firebase.Timestamp.now()`, `kotlinx.coroutines.Dispatchers.IO`).
ALWAYS import all classes, interfaces, objects, and types at the top of the file using standard `import` statements. This is strictly enforced by Konsist rule `Production code must not use Fully Qualified Names (FQN) for classes or objects`.
