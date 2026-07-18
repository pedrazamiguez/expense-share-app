package es.pedrazamiguez.splittrip.data.firebase.firestore.mapper

import es.pedrazamiguez.splittrip.data.firebase.firestore.document.MembershipRemovalEventDocument
import es.pedrazamiguez.splittrip.domain.model.MembershipRemovalEvent

fun MembershipRemovalEvent.toDocument(eventId: String, groupId: String) = MembershipRemovalEventDocument(
    eventId = eventId,
    groupId = groupId,
    userId = userId,
    createdAt = createdAt.toTimestampUtc()
)
