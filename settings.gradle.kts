rootProject.name = "dp-arena-trakt"
include("app")
include("modell")

dependencyResolutionManagement {
    repositories {
        maven("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
    }
    versionCatalogs {
        create("libs") {
            from("no.nav.dagpenger:dp-version-catalog:20250303.140.82ddc5")
        }
    }
}
