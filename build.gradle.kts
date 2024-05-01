plugins {
    id("java-library")
    id("application")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("com.github.gmazzo.buildconfig") version "5.3.5"
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    api(libs.net.lingala.zip4j.zip4j)
    api(libs.commons.codec.commons.codec)
    api(libs.net.java.dev.jna.jna.platform)
    api(libs.com.squareup.okhttp3.okhttp)
    api(libs.com.beust.jcommander)
    api(libs.com.google.code.gson.gson)
    api(libs.com.formdev.flatlaf)
    api(libs.org.apache.logging.log4j.log4j.core)
    api(libs.org.apache.logging.log4j.log4j.api)
}

tasks.shadowJar {
    archiveBaseName = project.name
    archiveVersion = project.version.toString()
    archiveClassifier = ""

    manifest {
        attributes["Main-Class"] = application.mainClass
        attributes["Implementation-Version"] = project.version.toString()
    }
}

buildConfig {
    packageName = "me.theentropyshard.crlauncher"
    className = "BuildConfig"
    useJavaOutput()

    buildConfigField("APP_NAME", provider { project.name })
    buildConfigField("APP_VERSION", provider { project.version.toString() })
}

group = "me.theentropyshard"
version = "0.2.1"
description = "CRLauncher"

application.mainClass = "me.theentropyshard.crlauncher.Main"

java.sourceCompatibility = JavaVersion.VERSION_17
java.sourceCompatibility = JavaVersion.VERSION_17

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
}
