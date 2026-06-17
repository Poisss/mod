plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.example"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    compileOnly("com.google.code.gson:gson:2.10.1")
    compileOnly("org.slf4j:slf4j-api:2.0.12")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.shadowJar {
    archiveBaseName.set("CustomMapMaker")
    archiveClassifier.set("")
    archiveVersion.set("")

    manifest {
        attributes(
            "Main-Class" to "com.example.custommapmaker.CustomMapMakerPlugin",
            "Plugin-Class" to "com.example.custommapmaker.CustomMapMakerPlugin",
            "Plugin-Name" to "CustomMapMaker",
            "Plugin-Version" to "1.0.0"
        )
    }

    configurations = listOf(project.configurations.runtimeClasspath.get())
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
    exclude("com/hypixel/**", "com/google/**", "org/slf4j/**")
}

tasks.jar {
    enabled = false
}
