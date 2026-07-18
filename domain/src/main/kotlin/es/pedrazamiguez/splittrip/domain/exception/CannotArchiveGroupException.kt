package es.pedrazamiguez.splittrip.domain.exception

class CannotArchiveGroupException : Exception("Only the group creator can archive the group")
