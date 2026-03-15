package es.pedrazamiguez.expenseshareapp.domain.exception

import es.pedrazamiguez.expenseshareapp.domain.enums.SplitType

class InvalidSplitException(val splitType: SplitType, message: String) : Exception(message)
