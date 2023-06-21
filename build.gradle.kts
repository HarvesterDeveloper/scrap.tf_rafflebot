plugins {
    id("java")
    `java-library`
    `maven-publish`
}

repositories {
    mavenCentral()
}

dependencies {
    api("org.seleniumhq.selenium:selenium-java:4.8.0")
    api("org.jsoup:jsoup:1.15.4")
}

group = "org.harvestdev"
version = "1.0-SNAPSHOT"
description = "scrap.tf_rafflebot"
java.sourceCompatibility = JavaVersion.VERSION_1_8

//publishing {
//    publications.create<MavenPublication>("maven") {
//        from(components["java"])
//    }
//}



//plugins {
//    id("java")
//}
//
//group = "org.harvestdev"
//version = "1.0-SNAPSHOT"
//
//repositories {
//    mavenCentral()
//}
//
//dependencies {
//    testImplementation(platform("org.junit:junit-bom:5.9.1"))
//    testImplementation("org.junit.jupiter:junit-jupiter")
//}
//
//tasks.test {
//    useJUnitPlatform()
//}