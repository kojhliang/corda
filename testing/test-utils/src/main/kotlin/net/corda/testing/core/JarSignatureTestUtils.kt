package net.corda.testing.core

import net.corda.core.internal.JarSignatureCollector
import net.corda.core.internal.div
import net.corda.nodeapi.internal.crypto.loadKeyStore
import java.io.FileInputStream
import java.nio.file.Path
import java.nio.file.Paths
import java.security.PublicKey
import java.util.jar.JarInputStream
import kotlin.test.assertEquals

object JarSignatureTestUtils {
    val bin = Paths.get(System.getProperty("java.home")).let { if (it.endsWith("jre")) it.parent else it } / "bin"

    fun Path.executeProcess(vararg command: String) {
        val shredder = (this / "_shredder").toFile() // No need to delete after each test.
        assertEquals(0, ProcessBuilder()
                .inheritIO()
                .redirectOutput(shredder)
                .redirectError(shredder)
                .directory(this.toFile())
                .command((bin / command[0]).toString(), *command.sliceArray(1 until command.size))
                .start()
                .waitFor())
    }

    fun Path.generateKey(alias: String, storePassword: String, name: String, keyalg: String = "RSA", keyPassword: String = storePassword, storeName: String = "_teststore") =
            executeProcess("keytool", "-genkeypair", "-keystore" ,storeName, "-storepass", storePassword, "-keyalg", keyalg, "-alias", alias, "-keypass", keyPassword, "-dname", name)

    fun Path.createJar(fileName: String, vararg contents: String) =
            executeProcess(*(arrayOf("jar", "cvf", fileName) + contents))

    fun Path.addIndexList(fileName: String) {
        executeProcess(*(arrayOf("jar", "i", fileName)))
    }

    fun Path.updateJar(fileName: String, vararg contents: String) =
            executeProcess(*(arrayOf("jar", "uvf", fileName) + contents))

    fun Path.signJar(fileName: String, alias: String, storePassword: String, keyPassword: String = storePassword): PublicKey {
        executeProcess("jarsigner", "-keystore", "_teststore", "-storepass", storePassword, "-keypass", keyPassword, fileName, alias)
        val ks = loadKeyStore(this.resolve("_teststore"), storePassword)
        return ks.getCertificate(alias).publicKey
    }

    fun Path.getJarSigners(fileName: String) =
            JarInputStream(FileInputStream((this / fileName).toFile())).use(JarSignatureCollector::collectSigners)
}
