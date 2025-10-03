package es.pedrazamiguez.expenseshareapp.data.firebase.firestore.mapper

import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.document.GroupDocument
import es.pedrazamiguez.expenseshareapp.domain.model.Group

fun Group.toDocument(userId: String) = GroupDocument(
    groupId = id,
    name = name,
    description = description,
    currency = currency,
    createdBy = userId
)
