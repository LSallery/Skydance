package uk.co.sallery

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.lang.IllegalArgumentException

fun main(args: Array<String>) {
    val ip = if (args.size > 2) args[2] else "192.168.1.159" // Local IP for testing
    println(ip)
    val skydance = Skydance(ip)
    //skydance.zonePower(args[0].toInt(), args[1].toInt())
    skydance.brightness(args[0].toInt(), args[1].toInt())
//    skydance.brightness(1, 30)
//    skydance.zonePower(1, 0)
}

class Skydance(val ip: String) {
    fun zonePower(zone: Int, onOff: Int) {
        val seq = (System.currentTimeMillis() % 255).toInt()
        val command = byteArrayOfInts(
            0x55, 0xAA, 0x5A, 0xA5, 0x7E,
            seq,
            0x80, 0x11, 0x80, 0x30, 0x71, 0x53, 0x7B,
            zone,
            0x00, 0x0A, 0x01, 0x00,
            onOff,
            0x00, 0x7E
        )

        sendCommand(command)

    }

    fun brightness(zone: Int, brightness: Int) {
        //brightness: A brightness level between 1-255 (higher = more bright)
        if ((brightness < 1) or (brightness > 255)) {
            throw IllegalArgumentException("Brightness level must fit into one byte and be >= 1.")
        }

        val seq = (System.currentTimeMillis() % 255).toInt()
        val command = byteArrayOfInts(
            0x55, 0xAA, 0x5A, 0xA5, 0x7E, //Head
            seq,
            0x80, 0x21, 0x00, 0x34, 0xB2, 0x00, 0x00,
            0x01, // zone
            0x00, 0x07, 0x02, 0x00, 0x00, brightness,
            0x00, 0x7E
        )
        sendCommand(command)
    }

    private fun sendCommand(command: ByteArray) {
        val selectorManager = SelectorManager(Dispatchers.IO)
        runBlocking {
            selectorManager.use {
                val socket = aSocket(selectorManager).tcp().connect(ip, 8899)
                val sendChannel = socket.openWriteChannel()
                sendChannel.writeFully(command)
                sendChannel.flush()
            }
        }
    }

    private fun byteArrayOfInts(vararg ints: Int) = ByteArray(ints.size) { pos -> ints[pos].toByte() }
}