package pe.edu.upc.viora.features.iam.domain.repository

import pe.edu.upc.viora.features.iam.domain.valueobject.OnboardingAnswers

/**
 * The onboarding answers kept on the phone until the email is verified, so closing the app in the middle does not
 * lose them (guide 02, section 3.1).
 */
interface OnboardingAnswersRepository {
    suspend fun answers(): OnboardingAnswers

    suspend fun save(answers: OnboardingAnswers)

    suspend fun clear()
}
