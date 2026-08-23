package es.pedrazamiguez.splittrip.data.firebase.repository

import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import es.pedrazamiguez.splittrip.data.firebase.R
import es.pedrazamiguez.splittrip.domain.model.DeveloperInfo
import es.pedrazamiguez.splittrip.domain.repository.AppConfigRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class FirebaseAppConfigRepository(
    private val remoteConfig: FirebaseRemoteConfig
) : AppConfigRepository {

    private val gson = Gson()

    private val _defaultCurrencyCode = MutableStateFlow(DEFAULT_CURRENCY)
    override val defaultCurrencyCode: StateFlow<String> = _defaultCurrencyCode.asStateFlow()

    private val _balanceComputationDebounceMs = MutableStateFlow(DEFAULT_BALANCE_DEBOUNCE_MS)
    override val balanceComputationDebounceMs: StateFlow<Long> = _balanceComputationDebounceMs.asStateFlow()

    private val _maxMembersPerGroup = MutableStateFlow(DEFAULT_MAX_MEMBERS_PER_GROUP)
    override val maxMembersPerGroup: StateFlow<Int> = _maxMembersPerGroup.asStateFlow()

    private val _extractedDateMaxFutureDays = MutableStateFlow(DEFAULT_EXTRACTED_DATE_MAX_FUTURE_DAYS)
    override val extractedDateMaxFutureDays: StateFlow<Int> = _extractedDateMaxFutureDays.asStateFlow()

    private val _supportEmailAddress = MutableStateFlow(DEFAULT_SUPPORT_EMAIL)
    override val supportEmailAddress: StateFlow<String> = _supportEmailAddress.asStateFlow()

    private val _settlementNudgeRateLimitHours = MutableStateFlow(DEFAULT_SETTLEMENT_NUDGE_RATE_LIMIT_HOURS)
    override val settlementNudgeRateLimitHours: StateFlow<Long> = _settlementNudgeRateLimitHours.asStateFlow()

    private val _ocrSafetyFalsePositivesBlacklist = MutableStateFlow(DEFAULT_OCR_SAFETY_FALSE_POSITIVES_BLACKLIST)
    override val ocrSafetyFalsePositivesBlacklist: StateFlow<List<String>> =
        _ocrSafetyFalsePositivesBlacklist.asStateFlow()

    private val _developerInfo = MutableStateFlow(DEFAULT_DEVELOPER_INFO)
    override val developerInfo: StateFlow<DeveloperInfo> = _developerInfo.asStateFlow()

    init {
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
        updateFlowsFromConfig()
        setupRealTimeUpdateListener()
    }

    override suspend fun fetchConfiguration(): Boolean {
        return try {
            val updated = remoteConfig.fetchAndActivate().await()
            if (updated) {
                Timber.d("Firebase Remote Config: Fetch and activate successful.")
                updateFlowsFromConfig()
            }
            updated
        } catch (e: Exception) {
            Timber.e(e, "Firebase Remote Config: Fetch failed.")
            false
        }
    }

    private fun setupRealTimeUpdateListener() {
        remoteConfig.addOnConfigUpdateListener(object : ConfigUpdateListener {
            override fun onUpdate(configUpdate: ConfigUpdate) {
                Timber.d("Firebase Remote Config real-time update: keys=${configUpdate.updatedKeys}")
                remoteConfig.activate().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        updateFlowsFromConfig()
                    }
                }
            }

            override fun onError(error: FirebaseRemoteConfigException) {
                Timber.w(error, "Firebase Remote Config real-time update error.")
            }
        })
    }

    private fun updateFlowsFromConfig() {
        _defaultCurrencyCode.value =
            remoteConfig.getString("default_currency_code").takeIf { it.isNotBlank() } ?: DEFAULT_CURRENCY
        val debounce = remoteConfig.getLong("balance_computation_debounce_ms")
        _balanceComputationDebounceMs.value = if (debounce > 0) debounce else DEFAULT_BALANCE_DEBOUNCE_MS
        val maxMembers = remoteConfig.getLong("max_members_per_group").toInt()
        _maxMembersPerGroup.value = if (maxMembers > 0) maxMembers else DEFAULT_MAX_MEMBERS_PER_GROUP
        val maxFutureDays = remoteConfig.getLong("extracted_date_max_future_days").toInt()
        _extractedDateMaxFutureDays.value =
            if (maxFutureDays > 0) maxFutureDays else DEFAULT_EXTRACTED_DATE_MAX_FUTURE_DAYS
        _supportEmailAddress.value =
            remoteConfig.getString("support_email_address").takeIf { it.isNotBlank() } ?: DEFAULT_SUPPORT_EMAIL
        val nudgeLimitHours = remoteConfig.getLong("settlement_nudge_rate_limit_hours")
        _settlementNudgeRateLimitHours.value =
            if (nudgeLimitHours > 0) nudgeLimitHours else DEFAULT_SETTLEMENT_NUDGE_RATE_LIMIT_HOURS
        val blacklistStr = remoteConfig.getString("ocr_safety_false_positives_blacklist")
        _ocrSafetyFalsePositivesBlacklist.value = if (blacklistStr.isNotBlank()) {
            blacklistStr.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }
        } else {
            DEFAULT_OCR_SAFETY_FALSE_POSITIVES_BLACKLIST
        }
        val developerInfoJson = remoteConfig.getString("developer_info_json")
        _developerInfo.value = parseDeveloperInfo(developerInfoJson)
    }

    private fun parseDeveloperInfo(jsonStr: String): DeveloperInfo {
        if (jsonStr.isBlank()) return DEFAULT_DEVELOPER_INFO
        return try {
            val dto = gson.fromJson(jsonStr, DeveloperInfoDto::class.java)
            dto?.toDomain() ?: DEFAULT_DEVELOPER_INFO
        } catch (e: Exception) {
            Timber.w(e, "Firebase Remote Config: Failed to parse developer_info_json.")
            DEFAULT_DEVELOPER_INFO
        }
    }

    companion object {
        private const val DEFAULT_CURRENCY = "EUR"
        private const val DEFAULT_BALANCE_DEBOUNCE_MS = 300L
        private const val DEFAULT_MAX_MEMBERS_PER_GROUP = 20
        private const val DEFAULT_EXTRACTED_DATE_MAX_FUTURE_DAYS = 30
        private const val DEFAULT_SUPPORT_EMAIL = "support@splittrip.com"
        private const val DEFAULT_SETTLEMENT_NUDGE_RATE_LIMIT_HOURS = 24L
        private val DEFAULT_OCR_SAFETY_FALSE_POSITIVES_BLACKLIST = listOf("razor", "private", "toothbrushes")

        val DEFAULT_DEVELOPER_INFO = DeveloperInfo(
            name = "Andrés Pedraza Míguez",
            avatarUrl = "",
            githubUrl = "https://github.com/pedrazamiguez",
            splitTripRepoUrl = "https://github.com/pedrazamiguez/split-trip",
            linkedinUrl = "https://www.linkedin.com/in/pedrazamiguez",
            portfolioUrl = "https://pedrazamiguez.github.io",
            roleMap = mapOf(
                "en" to "Senior Java & Kotlin Engineer",
                "es" to "Ingeniero Senior de Java y Kotlin",
                "es-rAN" to "Inheniero Senior de Java y Kotlin"
            ),
            bioMap = mapOf(
                "en" to "Senior Backend Engineer with over 14 years of experience designing scalable systems, " +
                    "now leveraging a strong hybrid skill set in Java (Backend) and Kotlin (Mobile/Android). " +
                    "Proven track record working as a Remote Contractor for UK-based companies, delivering " +
                    "high-quality solutions in English-speaking environments.\n\n" +
                    "Specialises in Java 21, Spring Boot, and Hexagonal Architecture, with recent hands-on " +
                    "leadership in Android Native (Jetpack Compose). Known for stepping into complex, " +
                    "legacy environments to refactor code, mentor senior peers, and drive delivery. " +
                    "Passionate about software craftsmanship, automated testing, and solving critical " +
                    "business problems across the full stack.",
                "es" to "Ingeniero Backend Senior con más de 14 años de experiencia diseñando sistemas " +
                    "escalables, aprovechando un sólido conjunto de habilidades híbridas en Java (Backend) y " +
                    "Kotlin (Móvil/Android). Trayectoria contrastada trabajando como contratista remoto para " +
                    "empresas del Reino Unido, ofreciendo soluciones de alta calidad en entornos " +
                    "angloparlantes.\n\n" +
                    "Especializado en Java 21, Spring Boot y Arquitectura Hexagonal, con liderazgo práctico " +
                    "reciente en proyectos de Android Nativo (Jetpack Compose). Reconocido por incorporarse a " +
                    "entornos heredados y complejos para refactorizar código, mentorizar a compañeros y " +
                    "acelerar entregas. Apasionado por la excelencia en el desarrollo de software, los tests " +
                    "automatizados y la resolución de problemas de negocio críticos en todo el stack.",
                "es-rAN" to "Inheniero Backend Senior con mâh de 14 añô de êpperiençia diçeñando çîttemâ " +
                    "êccalablê, aprobexando un çólido conhunto de abilidadê íbridâ en Java (Backend) y " +
                    "Kotlin (Móbî/Androîh). Trayêttoria contrâttá trabahando como contratîtta remoto pa " +
                    "empreçâ del Reino Unío, ofreçiendo çoluçionê de arta calidá en entônnô anglopâl-lantê.\n\n" +
                    "Êppeçialiçao en Java 21, Spring Boot y Arquitêttura Êççagonâh, con liderâggo práttico " +
                    "reçiente en proyêttô de Android Natibo (Jetpack Compose). Reconoçío por incorporarçe a " +
                    "entônnô eredáô y complehô pa refâttoriçâh código, mentoriçâh a compañerô y açelerâh " +
                    "entregâ. Apaçionao por la êççelençia en er deçarroyo de çôttware, lô têtts " +
                    "automatiçáô y la reçoluçión de problemâ de negoçio críticô en tó er stack."
            ),
            creditsMap = mapOf(
                "en" to "SplitTrip is a modular Android application designed for travelers to manage shared " +
                    "expenses efficiently. It allows users to create expense groups, track spending in multiple " +
                    "currencies, calculate debts, and sync data across devices.\n\n" +
                    "Built with modern Android practices—including Jetpack Compose, Clean Architecture, and " +
                    "Offline-First principles—the app serves as a reference for scalable, multi-module Android " +
                    "development.",
                "es" to "SplitTrip es una aplicación modular de Android diseñada para que los viajeros gestionen " +
                    "gastos compartidos de forma eficiente. Permite crear grupos de gastos, registrar pagos en " +
                    "múltiples monedas, calcular deudas y sincronizar datos entre dispositivos.\n\n" +
                    "Construida con las prácticas modernas de Android —incluyendo Jetpack Compose, Clean " +
                    "Architecture y principios Offline-First—, la aplicación sirve como referencia para el " +
                    "desarrollo escalable y multimódulo en Android.",
                "es-rAN" to "SplîTTrîh êh una aplicaçión modulâh de Androîh diçeñá pa que lô biaherô hêttionen " +
                    "gâttô compartíô de forma efiçiente. Permite creâh grupô de gâttô, rehîttrâh pagô en " +
                    "múrtiplê monedâ, carculâh deudâ y çincroniçâh datô entre dîppoçitibô.\n\n" +
                    "Côttruida con lâ prátticâ modênnâ de Androîh —incluyendo Hêppack Compoçe, Clean " +
                    "Architecture y prinçipiô Ôffline-Firtt—, la aplicaçión çirbe como referençia pa er " +
                    "deçarroyo êccalable y murtimódulo en Androîh."
            ),
            copyrightMap = mapOf(
                "en" to "© 2026 Andrés Pedraza Míguez.\nAll rights reserved.",
                "es" to "© 2026 Andrés Pedraza Míguez.\nTodos los derechos reservados.",
                "es-rAN" to "© 2026 Andrés Pedraza Míguez.\nTôh lô derexô reçerbaô."
            )
        )
    }
}

private data class DeveloperInfoDto(
    @SerializedName("name") val name: String? = null,
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    @SerializedName("github_url") val githubUrl: String? = null,
    @SerializedName("splittrip_repo_url") val splitTripRepoUrl: String? = null,
    @SerializedName("linkedin_url") val linkedinUrl: String? = null,
    @SerializedName("portfolio_url") val portfolioUrl: String? = null,
    @SerializedName("role_map") val roleMap: Map<String, String>? = null,
    @SerializedName("bio_map") val bioMap: Map<String, String>? = null,
    @SerializedName("credits_map") val creditsMap: Map<String, String>? = null,
    @SerializedName("copyright_map") val copyrightMap: Map<String, String>? = null
) {
    fun toDomain(): DeveloperInfo = DeveloperInfo(
        name = name ?: FirebaseAppConfigRepository.DEFAULT_DEVELOPER_INFO.name,
        avatarUrl = avatarUrl ?: FirebaseAppConfigRepository.DEFAULT_DEVELOPER_INFO.avatarUrl,
        githubUrl = githubUrl ?: FirebaseAppConfigRepository.DEFAULT_DEVELOPER_INFO.githubUrl,
        splitTripRepoUrl = splitTripRepoUrl ?: FirebaseAppConfigRepository.DEFAULT_DEVELOPER_INFO.splitTripRepoUrl,
        linkedinUrl = linkedinUrl ?: FirebaseAppConfigRepository.DEFAULT_DEVELOPER_INFO.linkedinUrl,
        portfolioUrl = portfolioUrl ?: FirebaseAppConfigRepository.DEFAULT_DEVELOPER_INFO.portfolioUrl,
        roleMap = roleMap ?: FirebaseAppConfigRepository.DEFAULT_DEVELOPER_INFO.roleMap,
        bioMap = bioMap ?: FirebaseAppConfigRepository.DEFAULT_DEVELOPER_INFO.bioMap,
        creditsMap = creditsMap ?: FirebaseAppConfigRepository.DEFAULT_DEVELOPER_INFO.creditsMap,
        copyrightMap = copyrightMap ?: FirebaseAppConfigRepository.DEFAULT_DEVELOPER_INFO.copyrightMap
    )
}
