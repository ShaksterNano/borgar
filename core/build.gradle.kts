val kotlinCoroutinesVersion: String by project
val ktorVersion: String by project
val log4j2Version: String by project
val disruptorVersion: String by project
val guavaVersion: String by project
val gsonVersion: String by project
val commonsIoVersion: String by project
val javacvVersion: String by project
val scrimageVersion: String by project
val twelveMonkeysVersion: String by project
val image4jVersion: String by project
val reflectionsVersion: String by project
val jdaVersion: String by project
val discordWebhooksVersion: String by project
val exposedVersion: String by project
val postgreSqlVersion: String by project
val junitVersion: String by project

plugins {
    kotlin("plugin.serialization") version "1.9.10"
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")

    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-apache5:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

    implementation("org.apache.logging.log4j:log4j-slf4j-impl:$log4j2Version")
    runtimeOnly("com.lmax:disruptor:$disruptorVersion")

    implementation("com.google.guava:guava:$guavaVersion-jre")
    implementation("com.google.code.gson:gson:$gsonVersion")
    implementation("commons-io:commons-io:$commonsIoVersion")
    implementation("org.reflections:reflections:$reflectionsVersion")
    // For utility classes such as SplitUtil
    implementation("net.dv8tion:JDA:$jdaVersion") {
        exclude(module = "opus-java")
    }
    @Suppress
    implementation("club.minnced:discord-webhooks:$discordWebhooksVersion")

    implementation("org.bytedeco:javacv-platform:$javacvVersion") {
        excludeJavaCpp(
            "artoolkitplus",
            "flandmark",
            "flycapture",
            "leptonica",
            "libdc1394",
            "libfreenect",
            "libfreenect2",
            "librealsense",
            "librealsense2",
            "openblas",
            "opencv",
            "tesseract",
            "videoinput",
        )
    }
    implementation("com.sksamuel.scrimage:scrimage-core:$scrimageVersion") {
        exclude(group = "ch.qos.logback", module = "logback-classic")
        exclude(group = "ch.qos.logback", module = "logback-core")
    }
    implementation("com.twelvemonkeys.imageio:imageio-webp:$twelveMonkeysVersion")
    implementation("net.ifok.image:image4j:$image4jVersion")

    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    runtimeOnly("org.postgresql:postgresql:$postgreSqlVersion")

    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$kotlinCoroutinesVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

/**
 * Exclude unused native libraries in order to reduce the JAR size.
 */
fun ModuleDependency.excludeJavaCpp(vararg modules: String) = modules.forEach {
    exclude(group = "org.bytedeco", module = it)
    exclude(group = "org.bytedeco", module = "$it-platform")
}
