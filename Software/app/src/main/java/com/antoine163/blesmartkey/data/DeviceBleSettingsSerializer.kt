package com.antoine163.blesmartkey.data

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.antoine163.blesmartkey.DeviceBleSettings
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream


/**
 * A serializer for [DeviceBleSettings] that uses Protocol Buffers.
 *
 * This serializer reads and writes [DeviceBleSettings] objects to and from input/output streams using the Protocol Buffer format.
 * If an error occurs during parsing, a [CorruptionException] is thrown.
 */
object DeviceBleSettingsSerializer : Serializer<DeviceBleSettings> {
    override val defaultValue: DeviceBleSettings = DeviceBleSettings.getDefaultInstance()
    override suspend fun readFrom(input: InputStream): DeviceBleSettings {
        try {
            return DeviceBleSettings.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: DeviceBleSettings, output: OutputStream) = t.writeTo(output)
}
