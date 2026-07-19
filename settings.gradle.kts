pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google {
            content {
                excludeGroupByRegex("com\\.airwatch(\\..*)?")
                excludeGroupByRegex("com\\.ws1(\\..*)?")
            }
        }
        mavenCentral {
            content {
                excludeGroupByRegex("com\\.airwatch(\\..*)?")
                excludeGroupByRegex("com\\.ws1(\\..*)?")
            }
        }
        maven {
            url = uri("https://maven.pkg.github.com/euc-releases/Android-WorkspaceONE-SDK")
            content {
                includeGroupByRegex("com\\.airwatch(\\..*)?")
                includeGroupByRegex("com\\.ws1(\\..*)?")
            }

            credentials {
                username = providers.gradleProperty("gpr.user")
                    .orElse(providers.environmentVariable("GPR_USER"))
                    .get()
                password = providers.gradleProperty("gpr.key")
                    .orElse(providers.environmentVariable("GPR_KEY"))
                    .get()
            }
        }
    }
}

rootProject.name = "AirWatchPoC"
include(":app")
 
