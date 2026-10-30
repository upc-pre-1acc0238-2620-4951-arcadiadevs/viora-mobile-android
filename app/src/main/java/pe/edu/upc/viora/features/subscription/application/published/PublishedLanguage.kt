package pe.edu.upc.viora.features.subscription.application.published

/*
 * The published language of Subscription: the types its use cases hand to other contexts. Other contexts import them
 * from here, never from this context's domain, so the domain can change behind this list without them noticing.
 */

typealias InvitationCode = pe.edu.upc.viora.features.subscription.domain.valueobject.InvitationCode
