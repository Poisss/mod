plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.24"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.example"
version = "1.0.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
    mavenLocal()
    maven {
        url = uri("https://repo.hypixel.net/releases")
    }
    maven {
        url = uri("https://repo.hypixel.net/snapshots")
    }
}

dependencies {
    // Hytale Server API (provided at runtime)
    compileOnly("com.hypixel.hytale:hytale-server-api:0.5.4")
    
    // JSON serialization
    implementation("com.google.code.gson:gson:2.10.1")
    
    // Logging
    compileOnly("org.slf4j:slf4j-api:2.0.12")
    
    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("org.mockito:mockito-core:5.11.0")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs << "-Xlint:unchecked" << "-Xlint:deprecation"
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

shadowJar {
    archiveBaseName.set("CustomMapMaker")
    archiveClassifier.set("")
    archiveVersion.set("")
    
    manifest {
        attributes(
            "Main-Class" to "com.example.custommapmaker.CustomMapMakerPlugin",
            "Plugin-Class" to "com.example.custommapmaker.CustomMapMakerPlugin",
            "Plugin-Name" to "CustomMapMaker",
            "Plugin-Version" to "1.0.0",
            "Plugin-Author" to "Example Author",
            "Plugin-Description" to "Create, manage, and share custom game maps"
        )
    }
    
    // Include dependencies but exclude Hytale API (provided at runtime)
    configurations = [project.configurations.runtimeClasspath]
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
    mergeServiceFiles()
}

tasks.named<Jar>("jar") {
    enabled = false
}