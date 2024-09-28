package com.antoine163.blesmartkey.data

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.antoine163.blesmartkey.DevicesBleSettings
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream


/**
 * A serializer for [DevicesBleSettings] that uses Protocol Buffers.
 *
 * This serializer reads and writes [DevicesBleSettings] objects to and from input/output streams using the Protocol Buffer format.
 * If an error occurs during parsing, a [CorruptionException] is thrown.
 */
object DevicesBleSettingsSerializer : Serializer<DevicesBleSettings> {
    override val defaultValue: DevicesBleSettings = DevicesBleSettings.getDefaultInstance()
    override suspend fun readFrom(input: InputStream): DevicesBleSettings {
        try {
            return DevicesBleSettings.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: DevicesBleSettings, output: OutputStream) = t.writeTo(output)
}
