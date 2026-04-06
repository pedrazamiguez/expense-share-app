package es.pedrazamiguez.splittrip.domain.exception

import es.pedrazamiguez.splittrip.domain.enums.SplitType

class InvalidSplitException(val splitType: SplitType, message: String) : Exception(message)
