package es.pedrazamiguez.expenseshareapp.domain.datasource.cloud

interface CloudUserDataSource {
    suspend fun saveGoogleUser(
        userId: String,
        email: String,
        displayName: String?,
        profilePictureUrl: String?
    )
}

