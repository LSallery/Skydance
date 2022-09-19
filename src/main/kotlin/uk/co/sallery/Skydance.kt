package uk.co.sallery

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

fun main (args: Array<String>) {
    val skydance = Skydance()
    skydance.zonePower(args[0].toInt(), args[1].toInt())
}

class Skydance {
    fun zonePower(zone: Int, onOff: Int) {
        val seq = (System.currentTimeMillis() % 255).toInt()
        val command = byteArrayOfInts(
            0x55, 0xAA, 0x5A, 0xA5, 0x7E,
            seq,
            0x80, 0x11, 0x80, 0x30, 0x71, 0x53, 0x7B,
            zone,
            0x00, 0x0A, 0x01, 0x00,
            onOff, 0x00, 0x7E)

        val selectorManager = SelectorManager(Dispatchers.IO)
        runBlocking {
            selectorManager.use {
                val socket = aSocket(selectorManager).tcp().connect("192.168.1.159", 8899)
                val sendChannel = socket.openWriteChannel()
                sendChannel.writeFully(command)
                sendChannel.flush()
            }
        }

    }

    private fun byteArrayOfInts (vararg ints: Int) = ByteArray(ints.size) { pos -> ints[pos].toByte()}
}