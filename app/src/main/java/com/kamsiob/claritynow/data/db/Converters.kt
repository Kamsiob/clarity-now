package com.kamsiob.claritynow.data.db

import androidx.room.TypeConverter
import com.kamsiob.claritynow.data.event.ReportSectionSnapshot
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

private val converterJson: Json = Json { encodeDefaults = true; ignoreUnknownKeys = true }
private val stringMapSerializer: KSerializer<Map<String, String>> =
    MapSerializer(String.serializer(), String.serializer())
private val sectionListSerializer: KSerializer<List<ReportSectionSnapshot>> =
    ListSerializer(ReportSectionSnapshot.serializer())

/**
 * The two composite column types. Both are stored as JSON rather than as extra
 * tables, because both are opaque snapshots that are only ever read back whole.
 *
 * Nothing but the converter functions lives in this class. Room reflects over every
 * declaration it finds here, and a helper with an inferred type is a needless way
 * to confuse it.
 */
class Converters {

    @TypeConverter
    fun mapToString(value: Map<String, String>): String =
        converterJson.encodeToString(stringMapSerializer, value)

    @TypeConverter
    fun stringToMap(value: String): Map<String, String> =
        converterJson.decodeFromString(stringMapSerializer, value)

    @TypeConverter
    fun sectionsToString(value: List<ReportSectionSnapshot>): String =
        converterJson.encodeToString(sectionListSerializer, value)

    @TypeConverter
    fun stringToSections(value: String): List<ReportSectionSnapshot> =
        converterJson.decodeFromString(sectionListSerializer, value)
}
