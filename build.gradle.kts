plugins {
    java
    application
}

group = "mx.edu.uttt"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    // Sin dependencias externas: el servidor HTTP es el de la JDK
    // (com.sun.net.httpserver) y el JSON es una utilidad propia
    // (mx.edu.uttt.hydrosafe.web.Json). Esto significa que el proyecto
    // compila y corre con SOLO el JDK, sin que Gradle tenga que bajar nada.
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

application {
    mainClass.set("mx.edu.uttt.hydrosafe.Main")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.test {
    useJUnitPlatform()
}
