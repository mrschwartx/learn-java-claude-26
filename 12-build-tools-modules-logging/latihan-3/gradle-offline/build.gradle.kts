// Versi TANPA dependency eksternal (sandbox tanpa akses Maven Central). Toolchain 25 diganti jdk lokal.
plugins {
    java
    application
}

repositories { mavenCentral() }

application { mainClass = "com.contoh.app.App" }
