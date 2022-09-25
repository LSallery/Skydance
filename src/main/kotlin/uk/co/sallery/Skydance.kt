package uk.co.sallery

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.lang.IllegalArgumentException
import java.nio.ByteBuffer

fun main(args: Array<String>) {
    val ip = if (args.size > 2) args[2] else "192.168.1.159" // Local IP for testing
    val skydance = Skydance(ip)
//    skydance.zonePower(args[0].toInt(), args[1].toInt())
//    skydance.brightness(args[0].toInt(), args[1].toInt())
    skydance.zonePower(1, 1)
//    skydance.colour(2, 0x00, 0xff, 0x00)
//    skydance.temperature(1, 0x00, 0x5f)
//    skydance.brightness(4, 17, 40)
//    println(skydance.getNumberOfZones())
//    println(skydance.getZoneInfo(1))
//    println(skydance.getZoneInfo(2))
//    println(skydance.getZoneInfo(4))
    /*
        (33, Shed)
        (65, Patio)
        (17, Hornbeams)
     */
}


@Suppress("unused")
class Skydance(private val ip: String) {
    private val header = byteArrayOfInts(0x55, 0xAA, 0x5A, 0xA5, 0x7E)
    private val trailer = byteArrayOfInts(0x00, 0x7E)

    fun zonePower(zone: Int, onOff: Int) {
        sendCommand(
            byteArrayOfInts(
                0x80, 0x11, 0x80, 0x30, 0x71, 0x53, 0x7B,
                zone,
                0x00, 0x0A, 0x01, 0x00,
                onOff
            )
        )
    }

    fun temperature(zone: Int, warmth: Int, brightness: Int) {
        sendCommand(
            byteArrayOfInts(
                0x80, 0x21, 0x00, 0xcc, 0x04, 0x00, 0x00,
                zone,
                0x00, 0x01, 0x07, 0x00,
                warmth, brightness,
                0x00, 0x00, 0x00, 0xff, 0x00
            )
        )
    }

    fun colour(zone: Int, red: Int, green: Int, blue: Int) {
        sendCommand(
            byteArrayOfInts(
                0x80, 0x41, 0x00, 0xcc, 0x04, 0x00, 0x00,
                zone,
                0x00, 0x01, 0x07, 0x00,
                red, green, blue, 0x01, 0x00, 0x32, 0x00
            )
        )
    }

    fun brightness(zone: Int, zoneType: Int, brightness: Int) {
        //brightness: A brightness level between 1-255 (higher = more bright)
        if ((brightness < 1) or (brightness > 255)) {
            throw IllegalArgumentException("Brightness level must fit into one byte and be >= 1.")
        }
        when (zoneType) {
            33, 65 -> sendCommand(
                byteArrayOfInts(
                    0x80, 0x21, 0x00, 0x34, 0xB2, 0x00, 0x00,
                    zone, // zone
                    0x00, 0x07, 0x02, 0x00, 0x00, brightness
                )
            )

            17 -> sendCommand(
                byteArrayOfInts(
                    0x80,
                    0x11,
                    0x00,
                    0x62,
                    0x90,
                    0x00,
                    0x00, // Yes, this type of light has a different "magic" sequence. If it gets a different one, the light turns off instead of dimming.
                    zone, // zone
                    0x00,
                    0x07,
                    0x02,
                    0x00,
                    0x00,
                    brightness
                )
            )

            else -> throw IllegalArgumentException("Zone type $zoneType is not supported")
        }
    }

    fun getNumberOfZones(): Int {
        val command = byteArrayOfInts(
            0x80, 0x00, 0x00, 0x62, 0x90, 0x00, 0x00,
            0x01, 0x00, 0x79, 0x00, 0x00
        )
        val response = ByteBuffer.allocate(34)
        sendCommandAndAwaitResponse(command, response)
        val body = response.array().copyOfRange(18, 34)
        return body.count { it != 0x0.toByte() }
    }

    fun getZoneInfo(zone: Int): Pair<Byte, String> {
        val command = byteArrayOfInts(
            0x80, 0x00, 0x00, 0x62, 0x90, 0x00, 0x00,
            zone, 0x00, 0x78, 0x00, 0x00
        )
        val response = ByteBuffer.allocate(36)
        sendCommandAndAwaitResponse(command, response)
        val zoneType = response.array()[18]
        val name = String(response.array().copyOfRange(20, 34)).trim()
        return Pair(zoneType, name)
    }

    private fun sendCommand(command: ByteArray) {
        val selectorManager = SelectorManager(Dispatchers.IO)
        runBlocking {
            selectorManager.use {
                val socket = aSocket(selectorManager).tcp().connect(ip, 8899)
                val sendChannel = socket.openWriteChannel()
                sendChannel.writeFully(header)
                sendChannel.writeFully(byteArrayOfInts((System.currentTimeMillis() % 255).toInt()))
                sendChannel.writeFully(command)
                sendChannel.writeFully(trailer)
                sendChannel.flush()
                socket.close()
            }
        }
    }

    private fun sendCommandAndAwaitResponse(command: ByteArray, response: ByteBuffer) {
        val selectorManager = SelectorManager(Dispatchers.IO)
        runBlocking {
            selectorManager.use {
                val socket = aSocket(selectorManager).tcp().connect(ip, 8899)
                val receiveChannel = socket.openReadChannel()
                val sendChannel = socket.openWriteChannel()
                sendChannel.writeFully(header)
                sendChannel.writeFully(byteArrayOfInts((System.currentTimeMillis() % 255).toInt()))
                sendChannel.writeFully(command)
                sendChannel.writeFully(trailer)
                sendChannel.flush()
                receiveChannel.readFully(response)
                socket.close()
            }
        }
    }

    private fun ByteArray.toHex(): String = joinToString(separator = " ") { eachByte -> "%02x".format(eachByte) }
    private fun byteArrayOfInts(vararg ints: Int) = ByteArray(ints.size) { pos -> ints[pos].toByte() }
}