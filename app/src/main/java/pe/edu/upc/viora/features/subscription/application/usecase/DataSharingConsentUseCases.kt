package pe.edu.upc.viora.features.subscription.application.usecase

import javax.inject.Inject
import pe.edu.upc.viora.features.subscription.domain.repository.DataSharingConsentRepository

/** P99 · Compartes datos: the newest consent of the producer. */
class GetLatestDataSharingConsentUseCase @Inject constructor(private val repository: DataSharingConsentRepository) {
    suspend operator fun invoke() = repository.latest()
}

/** P99 · Dejar de compartir: the manager stops reading at once; the plan goes on. */
class WithdrawDataSharingConsentUseCase @Inject constructor(private val repository: DataSharingConsentRepository) {
    suspend operator fun invoke(consentId: String) = repository.withdraw(consentId)
}
