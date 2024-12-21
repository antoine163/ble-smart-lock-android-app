package com.antoine163.blesmartkey.data.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.antoine163.blesmartkey.DeviceListSettings
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream


/**
 * A serializer for [DeviceListSettings] that uses Protocol Buffers.
 *
 * This serializer reads and writes [DeviceListSettings] objects to and from input/output streams using the Protocol Buffer format.
 * If an error occurs during parsing, a [CorruptionException] is thrown.
 */
object DeviceListSettingsSerializer : Serializer<DeviceListSettings> {

    /**
     * The default value for this setting.
     * This value will be used if no setting is found in the persistent storage.
     *
     * In this case, it returns a default instance of [DeviceListSettings].
     */
    override val defaultValue: DeviceListSettings = DeviceListSettings.getDefaultInstance()

    /**
     * Reads and parses a [DeviceListSettings] object from the provided [InputStream].
     *
     * This function attempts to parse the input stream as a serialized [DeviceListSettings] protobuf message.
     * If the parsing is successful, it returns the parsed [DeviceListSettings] object.
     * If the input stream is corrupted or does not contain a valid protobuf message, a [CorruptionException] is thrown.
     *
     * @param input The [InputStream] to read from.
     * @return The parsed [DeviceListSettings] object.
     * @throws CorruptionException If the input stream is corrupted or does not contain a valid protobuf message.
     */
    override suspend fun readFrom(input: InputStream): DeviceListSettings {
        try {
            return DeviceListSettings.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    /**
     * Writes the [DeviceListSettings] object to the given [OutputStream].
     *
     * This function serializes the [DeviceListSettings] object and writes it to the provided output stream.
     * It delegates the actual writing process to the `writeTo` function of the [DeviceListSettings] object.
     *
     * @param t The [DeviceListSettings] object to be written.
     * @param output The [OutputStream] to write the object to.
     */
    override suspend fun writeTo(t: DeviceListSettings, output: OutputStream) = t.writeTo(output)
}
