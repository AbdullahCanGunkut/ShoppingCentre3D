
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {

        mavenCentral()
        maven {
            url = uri("https://github.com/google/filament")
        }
        google()
    }
}
pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven {
            url = uri("https://github.com/google/filament")
        }

    }
}


rootProject.name = "ShoppingCentre3D"
include(":app")
