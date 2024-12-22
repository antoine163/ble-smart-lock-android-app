package com.antoine163.blesmartkey.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothStatusCodes
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.UUID

/**
 * Represents a Bluetooth Low Energy (BLE) device.
 *
 * This class handles the connection, communication, and data exchange with a BLE device.
 * It provides methods for connecting, disconnecting, reading, and writing to characteristics
 * of the device. It also manages the callback interface for notifying the application
 * about device events like connection state changes, data updates, and errors.
 *
 * @param context The application context.
 * @param address The MAC address of the BLE device.
 * @param callback The callback interface for receiving device events.
 */
@SuppressLint("MissingPermission")
class BleDevice(
    private val context: Context,
    private val address: String,
    callback: BleDeviceCallback
) {
    private val bluetoothManager: BluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

    private var gattDevice: BluetoothGatt? = null
    private var gattCharDeviceName: BluetoothGattCharacteristic? = null
    private var gattCharLockState: BluetoothGattCharacteristic? = null
    private var gattCharDoorState: BluetoothGattCharacteristic? = null
    private var gattCharOpenDoor: BluetoothGattCharacteristic? = null
    private var gattCharBrightness: BluetoothGattCharacteristic? = null
    private var gattCharBrightnessTh: BluetoothGattCharacteristic? = null

    // Map of UUID to BluetoothGattCharacteristic for reading multiple characteristics
    private var pendingRead = mutableMapOf<UUID, BluetoothGattCharacteristic>()

    // Map of UUID to BluetoothGattCharacteristic for writing multiple characteristics
    private var pendingWrite = mutableMapOf<UUID, WriteCharData>()


    private var pendingReadRssi = false
    private var isAutoUnlockEnabled = false
    private var autoUnlockRssi: Int = 0
    private var autoUnlockJob: Job? = null

    /**
     * Returns the address.
     *
     * @return The address as a String.
     */
    fun getAddress(): String {
        return address
    }

    /**
     * Data class representing data to be written to a Bluetooth GATT characteristic.
     *
     * @property char The Bluetooth GATT characteristic to write to.
     * @property value The byte array value to write.
     * @property writeType The write type to use (e.g., WRITE_TYPE_DEFAULT, WRITE_TYPE_NO_RESPONSE,
     * WRITE_TYPE_SIGNED).
     */
    data class WriteCharData(
        val char: BluetoothGattCharacteristic,
        val value: ByteArray,
        val writeType: Int
    )


    /**
     * GATT callback instance that handles various Bluetooth events.
     *
     * This callback is responsible for handling connection state changes, service discovery,
     * characteristic reads and writes, and notifications. It interacts with the `callback`
     * instance to notify the application about these events.
     *
     * Key functionalities handled by this callback:
     * - Connection state changes: Informs the application when the device connects or disconnects.
     * - Service discovery: Discovers and saves references to essential services and characteristics.
     * - Characteristic reads: Reads values from characteristics like device name, door state, etc.
     * - Characteristic writes: Writes values to characteristics like lock state, brightness, etc.
     * - Notifications: Handles notifications for characteristic changes, such as door state updates.
     * - RSSI reads: Reads and reports the Received Signal Strength Indicator (RSSI) value.
     */
    private val gattCallback = object : BluetoothGattCallback() {
        /**
         * Called when the connection state changes.
         *
         * @param gatt The GATT client.
         * @param status The status of the connection change.
         * @param newState The new connection state.
         */
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    // Attempts to discover services after successful connection.
                    gatt?.discoverServices()

                    Log.i("BSK", "$address -> Connected")
                } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                    // Disconnected from the GATT Server
                    callback.onConnectionStateChanged(false)

                    Log.i("BSK", "$address -> Disconnected")
                }
            } else {
                Log.e("BSK", "$address -> Connection state change failed! Status: $status")
            }
        }

        /**
         * Callback triggered when services are discovered on the remote device.
         *
         * This method handles the following:
         * - Checks if the service discovery was successful.
         * - Saves the GATT device instance.
         * - Retrieves necessary services and characteristics (Generic Access, Application).
         * - Enables notifications for the door state characteristic.
         * - Notifies the callback about the initial lock state (assumed to be locked).
         * - Initiates reads for device name, door state, and brightness threshold characteristics.
         * - Logs an error if service discovery fails.
         *
         * @param gatt The BluetoothGatt instance.
         * @param status The status of the service discovery operation.
         */
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)

            if (status == BluetoothGatt.GATT_SUCCESS) {
                gatt?.let {
                    Log.i("BSK", "$address -> Services discovered with success!")

                    // Get the generic access service and characteristic
                    val gattServiceGenericAccess = gatt.getService(SERV_UUID_GENERIC_ACCESS)
                    gattCharDeviceName =
                        gattServiceGenericAccess?.getCharacteristic(CHAR_UUID_DEVICE_NAME)

                    // Get the application service and characteristics
                    val gattServiceApp = gatt.getService(SERV_UUID_APP)
                    gattServiceApp?.let { service ->
                        gattCharLockState = service.getCharacteristic(CHAR_UUID_LOCK_STATE)
                        gattCharDoorState = service.getCharacteristic(CHAR_UUID_DOOR_STATE)
                        gattCharOpenDoor = service.getCharacteristic(CHAR_UUID_OPEN_DOOR)
                        gattCharBrightness = service.getCharacteristic(CHAR_UUID_BRIGHTNESS)
                        gattCharBrightnessTh = service.getCharacteristic(CHAR_UUID_BRIGHTNESS_TH)
                    }

                    // Error if service and characteristic not found
                    if (gattCharLockState == null || gattCharDoorState == null || gattCharOpenDoor == null ||
                        gattCharBrightness == null || gattCharBrightnessTh == null
                    ) {
                        Log.e("BSK", "$address -> Service not found!")

                        callback.onConnectionFailed()
                        disconnect()
                        return
                    }

                    // Enable notifications for the door state characteristic
                    gatt.setCharacteristicNotification(gattCharDoorState, true)

                    // Configure the Client Characteristic Configuration Descriptor (CCCD)
                    // to enable notifications for the door state characteristic
                    gattCharDoorState?.getDescriptor(DESC_UUID_CCCD)?.let { descriptor ->
                        val state = gatt.writeDescriptor(
                            descriptor,
                            BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        )
                        logWriteError(state, descriptor.uuid)
                    }

                    // Successfully connected to the GATT Server and discovered services
                    callback.onConnectionStateChanged(true)

                    // Handle the door state change, before a connection the lock is locked
                    callback.onLockStateChanged(true)
                }
            } else {
                Log.e("BSK", "$address -> Service discovery failed! Status: $status")
                callback.onConnectionFailed()
                disconnect()
            }
        }

        /**
         * Callback triggered as a result of a descriptor write operation.
         *
         * @param gatt GATT client invoked {@link BluetoothGatt#writeDescriptor}
         * @param descriptor Descriptor that was written.  Will be null if the
         *            write failed.
         * @param status The result of the write operation
         *               {@link BluetoothGatt#GATT_SUCCESS} if the operation succeeds.
         */
        override fun onDescriptorWrite(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            super.onDescriptorWrite(gatt, descriptor, status)

            // Process the next pending write or read operation
            processNextOperation()
        }

        /**
         * Callback triggered when a write operation on a characteristic has completed.
         *
         * @param gatt The GATT client.
         * @param characteristic The characteristic that was written to.
         * @param status The result of the write operation. [BluetoothGatt.GATT_SUCCESS] indicates success.
         */
        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)

            when (characteristic?.uuid) {

                // Handle the lock state write response
                CHAR_UUID_LOCK_STATE -> {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        callback.onLockStateChanged(false)
                    }
                }

                // Read the device name characteristic after writing to it successfully
                CHAR_UUID_DEVICE_NAME -> {
                    readCharacteristics(gattCharDeviceName)
                }

                // Read the brightness threshold characteristic after writing to it successfully
                CHAR_UUID_BRIGHTNESS_TH -> {
                    readCharacteristics(gattCharBrightnessTh)
                }
            }

            // Remove the characteristic from the pending write map
            characteristic?.let { char ->
                pendingWrite.remove(char.uuid)
            }

            // Process the next pending write or read operation
            processNextOperation()
        }

        /**
         * Callback triggered when a characteristic has been read from the remote device.
         *
         * This method handles the read characteristic based on its UUID and notifies the callback
         * with the parsed value.
         *
         * @param gatt The GATT client.
         * @param characteristic The characteristic that was read.
         * @param value The value read from the characteristic.
         * @param status The result of the read operation.
         */
        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, value, status)

            when (characteristic.uuid) {

                // Handle the device name read response
                CHAR_UUID_DEVICE_NAME -> {
                    val deviceName = String(value)
                    callback.onDeviceNameChanged(deviceName)
                }

                // Handle the door state read response
                CHAR_UUID_DOOR_STATE -> {
                    val isOpened = value[0] == 0x01.toByte()
                    callback.onDoorStateChanged(isOpened)
                }

                // Handle the brightness read response
                CHAR_UUID_BRIGHTNESS -> {
                    val brightness =
                        ByteBuffer.wrap(value).order(ByteOrder.LITTLE_ENDIAN).getFloat()
                    callback.onBrightnessRead(brightness)
                }

                // Handle the brightness threshold read response
                CHAR_UUID_BRIGHTNESS_TH -> {
                    val brightness =
                        ByteBuffer.wrap(value).order(ByteOrder.LITTLE_ENDIAN).getFloat()
                    callback.onBrightnessThChanged(brightness)
                }
            }

            // Remove the characteristic from the pending read map
            pendingRead.remove(characteristic.uuid)

            // Process the next pending write or read operation
            processNextOperation()
        }

        /**
         * Called when a characteristic changes.
         *
         * @param gatt The GATT client.
         * @param characteristic The characteristic that changed.
         * @param value The new value of the characteristic.
         */
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            super.onCharacteristicChanged(gatt, characteristic, value)

            // Handle the door state change
            when (characteristic.uuid) {
                CHAR_UUID_DOOR_STATE -> {
                    val isOpened = value[0] == 0x01.toByte()
                    callback.onDoorStateChanged(isOpened)
                }
            }
        }

        /**
         * Callback triggered when a remote RSSI value is read.
         *
         * This function handles the received RSSI value, updating the UI or triggering actions
         * based on the RSSI level.
         *
         * It performs the following tasks:
         * - Checks if the RSSI read was successful.
         * - If successful and initiated by a user request, delivers the RSSI value to the callback.
         * - If successful and auto-unlock is enabled, checks if the RSSI is above the threshold
         *   to trigger an unlock action.
         * - If the RSSI read failed, logs an error and disables auto-unlock.
         *
         * @param gatt The Bluetooth GATT object associated with the remote device.
         * @param rssi The received RSSI value in dBm.
         * @param status The status of the operation, indicating success or failure.
         */
        override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
            super.onReadRemoteRssi(gatt, rssi, status)

            if (status == BluetoothGatt.GATT_SUCCESS) {
                // The user requested to read the RSSI.
                if (pendingReadRssi == true) {
                    pendingReadRssi = false

                    // Handle the RSSI value
                    callback.onRssiRead(rssi)
                }

                // The auto unlock is enable ?
                if ((isAutoUnlockEnabled == true) &&
                    (rssi >= autoUnlockRssi)) {
                    autoUnlockDisable()
                    unlock()
                }
            } else {
                Log.e("BSK", "$address -> Read remote RSSI failed! Status: $status")

                pendingReadRssi = false
                autoUnlockDisable()
            }
        }
    }


    /**
     * Writes the given value to the specified Bluetooth GATT characteristic.
     *
     * This function manages a queue of characteristics to be written. If the queue is empty,
     * it immediately writes the characteristic. Otherwise, it adds the characteristic to the queue.
     *
     * @param char The characteristic to write to.
     * @param value The value to write.
     * @param writeType The write type to use (e.g., WRITE_TYPE_DEFAULT, WRITE_TYPE_NO_RESPONSE,
     * WRITE_TYPE_SIGNED).
     */
    private fun writeCharacteristics(
        char: BluetoothGattCharacteristic?,
        value: ByteArray,
        writeType: Int
    ) {
        char?.let {
            val wasEmpty = pendingWrite.isEmpty()
            pendingWrite[char.uuid] = WriteCharData(char, value, writeType)
            if (wasEmpty) {
                writeNextCharacteristic()
            }
        }
    }

    /**
     * Writes the next characteristic in the `writeCharMap`.
     *
     * If the `writeCharMap` is not empty, this function retrieves the first write data
     * entry and initiates a write operation on the associated GATT characteristic.
     */
    private fun writeNextCharacteristic() {
        if (pendingWrite.isNotEmpty()) {
            val writeData = pendingWrite.values.first()
            gattDevice?.let { device ->
                val state = device.writeCharacteristic(
                    writeData.char, writeData.value, writeData.writeType
                )
                logWriteError(state, writeData.char.uuid)
            }
        }
    }

    /**
     * Adds a characteristic to the map of characteristics to be read.
     * If the map is empty, it starts reading the characteristic immediately.
     *
     * @param char The characteristic to read.
     */
    private fun readCharacteristics(char: BluetoothGattCharacteristic?) {
        if (char != null) {
            if (pendingRead.isEmpty()) {
                pendingRead[char.uuid] = char
                readNextCharacteristic()
            } else {
                pendingRead.putIfAbsent(char.uuid, char)
            }
        }
    }

    /**
     * Reads the next characteristic from the `readCharMap`.
     *
     * If the `readCharMap` is not empty, it retrieves the first characteristic
     * and initiates a read operation on the GATT device.
     */
    private fun readNextCharacteristic() {
        if (pendingRead.isNotEmpty()) {
            val char = pendingRead.values.first()
            if (gattDevice?.readCharacteristic(char) == false) {
                Log.w("BSK", "$address -> ${uuidName(char.uuid)}: Read failed!")
            }
        }
    }

    /**
     * Processes the next pending read or write operation.
     *
     * This function checks if there are any pending write operations in the `pendingWrite` map.
     * If there are, it calls `writeNextCharacteristic()` to process the next write.
     * If there are no pending writes, it checks for pending read operations in the `pendingRead` map
     * and calls `readNextCharacteristic()` to process the next read.
     */
    private fun processNextOperation() {
        if (pendingWrite.isNotEmpty()) {
            writeNextCharacteristic()
        } else {
            readNextCharacteristic()
        }
    }

    /**
     * Sets the device name on the Bluetooth device.
     *
     * This function writes the provided device name to the device's characteristic
     * responsible for the device name.
     *
     * @param deviceName The new device name to be set.
     */
    fun setDeviceName(deviceName: String) {
        writeCharacteristics(
            gattCharDeviceName,
            deviceName.toByteArray(), BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        )
    }

    /**
     * Reads the device name from the connected Bluetooth device.
     *
     * This function initiates the reading of the device name characteristic
     * (gattCharDeviceName) using the `readCharacteristics` function.
     * The result of the read operation will be handled by the corresponding callback
     * mechanism (e.g., onCharacteristicRead).
     */
    fun readDeviceName()
    {
        readCharacteristics(gattCharDeviceName)
    }

    /**
     * Sets the brightness threshold for the device.
     *
     * This function converts the given [brightnessTh] value to a byte array and writes it to the
     * gatt characteristic responsible for the brightness threshold.
     *
     * @param brightnessTh The brightness threshold value to set (as a float).
     */
    fun setBrightnessTh(brightnessTh: Float) {
        val byteArray = ByteBuffer.allocate(4)
            .order(ByteOrder.LITTLE_ENDIAN)
            .putFloat(brightnessTh)
            .array()

        writeCharacteristics(
            gattCharBrightnessTh,
            byteArray, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        )
    }

    /**
     * Reads the brightness threshold characteristic from the connected device.
     *
     * This function initiates a read operation for the characteristic
     * represented by `gattCharBrightnessTh`. The result of the read operation
     * is typically handled by a callback function that is registered when
     * setting up Bluetooth communication.
     */
    fun readBrightnessTh()
    {
        readCharacteristics(gattCharBrightnessTh)
    }

    /**
     * Reads the current state of the door.
     *
     * This function initiates a read operation for the characteristic
     * representing the door state. The result of the read operation
     * will be handled by the callback registered for characteristic
     * value changes.
     *
     * @see readCharacteristics
     */
    fun readDoorState()
    {
        readCharacteristics(gattCharDoorState)
    }

    /**
     * Unlocks the device.
     *
     * This function writes the unlock command (0x01) to the lock state characteristic
     * using a default write type.
     */
    fun unlock() {
        writeCharacteristics(
            gattCharLockState,
            byteArrayOf(0x01), BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        )
    }

    /**
     * Opens the door by writing the "open" command to the corresponding Bluetooth characteristic.
     *
     * This function sends a byte array containing the value `0x01` to the characteristic
     * responsible for controlling the door's open state.
     */
    fun openDoor() {
        writeCharacteristics(
            gattCharOpenDoor,
            byteArrayOf(0x01), BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        )
    }

    /**
     * Reads the RSSI for the currently connected GATT device.
     *
     * This function initiates a request to read the Received Signal Strength Indication (RSSI) value
     * from the connected GATT device. The result will be delivered asynchronously through the
     * `BleDeviceCallback.onRssiRead()` callback.
     */
    fun readRssi() {
        pendingReadRssi = true
        gattDevice?.readRemoteRssi()
    }

    /**
     * Reads the brightness characteristic from the connected device.
     * This function initiates a read operation for the gattCharBrightness characteristic,
     * triggering a callback when the value is received.
     */
    fun readBrightness() {
        readCharacteristics(gattCharBrightness)
    }

    /**
     * Enables and configures the auto-unlock feature.
     *
     * This function starts a job that periodically checks the RSSI value of the connected
     * Bluetooth device. If the RSSI value is greater than or equal to the specified
     * `rssi` threshold, it triggers the unlock action.
     *
     * @param rssi The RSSI threshold value. If the device's RSSI is greater than or equal
     * to this value, the auto-unlock mechanism will be triggered.
     */
    fun autoUnlock(rssi: Int) {
        stopAutoUnlockJob()

        gattDevice?.let { device ->
            autoUnlockRssi = rssi
            isAutoUnlockEnabled = true

            startAutoUnlockJob()
        }
    }

    /**
     * Disables the auto-unlock feature.
     *
     * This function sets the `isAutoUnlockEnabled` flag to false, indicating that auto-unlock is disabled.
     * It also calls `stopAutoUnlockJob()` to stop any ongoing auto-unlock job.
     */
    fun autoUnlockDisable() {
        isAutoUnlockEnabled = false
        stopAutoUnlockJob()
    }

    /**
     * Starts a background job that periodically checks the RSSI of the connected GATT device.
     *
     * This job runs on the IO dispatcher and continuously performs the following actions:
     * 1. Reads the RSSI of the connected GATT device.
     * 2. Waits for a specified interval (AUTO_UNLOCK_CHECK_INTERVAL).
     * 3. Repeats the process.
     *
     * If the GATT device becomes disconnected (gattDevice is null), the job will:
     * 1. Call autoUnlockDisable() to disable auto-unlock functionality.
     * 2. Terminate the job.
     */
    private fun startAutoUnlockJob() {
        autoUnlockJob = CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                gattDevice?.let { device ->
                    device.readRemoteRssi()
                    delay(AUTO_UNLOCK_CHECK_INTERVAL)
                } ?: run {
                    autoUnlockDisable()
                    return@launch
                }
            }
        }
    }

    /**
     * Cancels the currently running auto-unlock job, if any, and sets it to null.
     * This effectively stops the scheduled auto-unlock operation.
     */
    private fun stopAutoUnlockJob() {
        autoUnlockJob?.cancel()
        autoUnlockJob = null
    }

    /**
     * Dissociates (unpairs) the current Bluetooth device.
     *
     * This function first disconnects from the device if it's connected,
     * then removes the bond (pairing) information using reflection.
     */
    fun dissociate() {
        disconnect()

        val bluetoothDevice = bluetoothManager.adapter.getRemoteDevice(address)
        val method = bluetoothDevice.javaClass.getMethod("removeBond")
        method.invoke(bluetoothDevice)

        Log.i("BSK", "$address -> Dissociated")
    }

    /**
     * Connects to the GATT server of the Bluetooth device.
     * If already connected, this function does nothing.
     */
    fun connect() {
        disconnect()

        val bluetoothDevice = bluetoothManager.adapter.getRemoteDevice(address)
        gattDevice = bluetoothDevice.connectGatt(context, true, gattCallback)

        Log.i("BSK", "$address -> Connecting")
    }

    /**
     * Disconnects from the GATT server and resets all associated GATT characteristics and pending operations.
     */
    fun disconnect() {
        autoUnlockDisable()

        gattDevice?.let { bleDev ->
            bleDev.disconnect()
            bleDev.close()

            Log.i("BSK", "$address -> Disconnecting")
        }

        gattDevice = null
        gattCharDeviceName = null
        gattCharLockState = null
        gattCharDoorState = null
        gattCharOpenDoor = null
        gattCharBrightness = null
        gattCharBrightnessTh = null

        pendingRead.clear()
        pendingWrite.clear()
    }

    /**
     * Called by the garbage collector on an object when garbage collection
     * determines that there are no more references to the object.
     *
     * This implementation calls `disconnect()` to ensure any resources held
     * by the object are released before it is garbage collected.
     */
    protected fun finalize() {
        disconnect()
    }

    companion object {
        private const val AUTO_UNLOCK_CHECK_INTERVAL = 800L

        private val SERV_UUID_GENERIC_ACCESS =
            UUID.fromString("00001800-0000-1000-8000-00805f9b34fb")
        private val CHAR_UUID_DEVICE_NAME = UUID.fromString("00002a00-0000-1000-8000-00805f9b34fb")

        private val SERV_UUID_APP = UUID.fromString("44707b20-3459-11ee-aea4-0800200c9a66")
        private val CHAR_UUID_LOCK_STATE = UUID.fromString("44707b21-3459-11ee-aea4-0800200c9a66")
        private val CHAR_UUID_DOOR_STATE = UUID.fromString("44707b22-3459-11ee-aea4-0800200c9a66")
        private val CHAR_UUID_OPEN_DOOR = UUID.fromString("44707b23-3459-11ee-aea4-0800200c9a66")
        private val CHAR_UUID_BRIGHTNESS = UUID.fromString("44707b24-3459-11ee-aea4-0800200c9a66")
        private val CHAR_UUID_BRIGHTNESS_TH =
            UUID.fromString("44707b25-3459-11ee-aea4-0800200c9a66")

        // UUID of Client Characteristic Configuration Descriptor
        private val DESC_UUID_CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

        /**
         * Returns the name of a characteristic based on its UUID.
         *
         * @param uuid The UUID of the characteristic.
         * @return The name of the characteristic or "unknown" if the UUID is not recognized.
         */
        private fun uuidName(uuid: UUID): String {
            return when (uuid) {
                CHAR_UUID_DEVICE_NAME -> "UUID_DEVICE_NAME"
                CHAR_UUID_DOOR_STATE -> "UUID_DOOR_STATE"
                CHAR_UUID_BRIGHTNESS -> "UUID_BRIGHTNESS"
                CHAR_UUID_BRIGHTNESS_TH -> "UUID_BRIGHTNESS_TH"
                CHAR_UUID_LOCK_STATE -> "UUID_LOCK_STATE"
                CHAR_UUID_OPEN_DOOR -> "UUID_OPEN_DOOR"
                DESC_UUID_CCCD -> "UUID_CCCD"
                else -> uuid.toString()
            }
        }
    }

    /**
     * Converts Bluetooth status codes to human-readable strings.
     *
     * @param statusCode The integer representation of the Bluetooth status code.
     * @return A string describing the status code.
     */
    fun stateToString(statusCode: Int): String {
        return when (statusCode) {
            BluetoothStatusCodes.SUCCESS -> "Success"
            BluetoothStatusCodes.ERROR_BLUETOOTH_NOT_ALLOWED -> "Bluetooth not allowed"
            BluetoothStatusCodes.ERROR_BLUETOOTH_NOT_ENABLED -> "Bluetooth not enabled"
            BluetoothStatusCodes.ERROR_DEVICE_NOT_BONDED -> "Device not bonded"
            BluetoothStatusCodes.ERROR_GATT_WRITE_NOT_ALLOWED -> "GATT write not allowed"
            BluetoothStatusCodes.ERROR_GATT_WRITE_REQUEST_BUSY -> "GATT write request busy"
            BluetoothStatusCodes.ERROR_MISSING_BLUETOOTH_CONNECT_PERMISSION -> "Missing Bluetooth connect permission"
            BluetoothStatusCodes.ERROR_PROFILE_SERVICE_NOT_BOUND -> "Profile service not bound"
            BluetoothStatusCodes.ERROR_UNKNOWN -> "Unknown error"
            BluetoothStatusCodes.FEATURE_NOT_CONFIGURED -> "Feature not configured"
            BluetoothStatusCodes.FEATURE_NOT_SUPPORTED -> "Feature not supported"
            BluetoothStatusCodes.FEATURE_SUPPORTED -> "Feature supported"
            else -> "Unknown status code: $statusCode"
        }
    }

    /**
     * Logs an error message if a write operation to a Bluetooth GATT characteristic or descriptor
     * was unsuccessful.
     *
     * @param state The status code returned by the write operation.
     * @param uuid The UUID of the characteristic that was written to.
     */
    private fun logWriteError(state: Int, uuid: UUID) {
        if (state == BluetoothGatt.GATT_SUCCESS) return
        Log.e("BSK", "$address -> ${uuidName(uuid)}: Write failed: ${stateToString(state)}")
    }
}