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

    /**
     * The default value for this setting.
     * This value will be used if no setting is found in the persistent storage.
     *
     * In this case, it returns a default instance of [DevicesBleSettings].
     */
    override val defaultValue: DevicesBleSettings = DevicesBleSettings.getDefaultInstance()

    /**
     * Reads and parses a [DevicesBleSettings] object from the provided [InputStream].
     *
     * This function attempts to parse the input stream as a serialized [DevicesBleSettings] protobuf message.
     * If the parsing is successful, it returns the parsed [DevicesBleSettings] object.
     * If the input stream is corrupted or does not contain a valid protobuf message, a [CorruptionException] is thrown.
     *
     * @param input The [InputStream] to read from.
     * @return The parsed [DevicesBleSettings] object.
     * @throws CorruptionException If the input stream is corrupted or does not contain a valid protobuf message.
     */
    override suspend fun readFrom(input: InputStream): DevicesBleSettings {
        try {
            return DevicesBleSettings.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    /**
     * Writes the [DevicesBleSettings] object to the given [OutputStream].
     *
     * This function serializes the [DevicesBleSettings] object and writes it to the provided output stream.
     * It delegates the actual writing process to the `writeTo` function of the [DevicesBleSettings] object.
     *
     * @param t The [DevicesBleSettings] object to be written.
     * @param output The [OutputStream] to write the object to.
     */
    override suspend fun writeTo(t: DevicesBleSettings, output: OutputStream) = t.writeTo(output)
}
