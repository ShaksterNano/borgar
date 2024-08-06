val jdaVersion: String by project
val jdaKtxVersion: String by project

dependencies {
    api(project(":messaging"))

    implementation("com.github.freya022:JDA:$jdaVersion") {
        exclude(module = "opus-java")
    }
    implementation("club.minnced:jda-ktx:$jdaKtxVersion")

    testImplementation(kotlin("test"))
}
