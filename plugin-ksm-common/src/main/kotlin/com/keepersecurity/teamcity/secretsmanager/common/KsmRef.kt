package com.keepersecurity.teamcity.secretsmanager.common

import com.keepersecurity.secretsManager.core.*

data class KsmRef(val ref: String) {
    // Note! TC uses keeper:URI while KSM uses keeper://URI
    // TC URI format is the same as Notation URI (without //)
    // keeper:<TITLE|UID>/<type|title|notes>
    // keeper:<TITLE|UID>/file/<filename|title|fileUID>
    // keeper:<TITLE|UID>/<field|custom_field>/<label|type>[[predicate][predicate]]
    val keeperNotation = if (ref.lowercase().startsWith("keeper://")) ref.substring(9)
    else if (ref.lowercase().startsWith("keeper:")) ref.substring(7)
    else ref
    val notation = tryParseNotation(keeperNotation).first
    val recordId: String = if (notation.size > 1) notation[1].text?.first ?: "" else ""
    fun valid(): Boolean {
        return validateNotation(ref)
    }
}
