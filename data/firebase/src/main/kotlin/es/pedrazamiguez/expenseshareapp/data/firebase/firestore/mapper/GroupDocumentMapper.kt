package es.pedrazamiguez.expenseshareapp.data.firebase.firestore.mapper

import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.document.GroupDocument
import es.pedrazamiguez.expenseshareapp.domain.model.Group

fun Group.toDocument(groupId: String, userId: String) = GroupDocument(
    groupId = groupId,
    name = name,
    description = description,
    currency = currency,
    createdBy = userId
)
