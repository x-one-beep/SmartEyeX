plugins {
 kotlin("jvm") version "1.9.10" apply false
 id("com.android.application") version "8.1.0" apply false
}
dependencyResolutionManagement {
 repositoriesMode.set(org.gradle.api.initialization.dsl.RepositoriesMode.FAIL_ON_PROJECT_REPOS)
 repositories {
 google()
 mavenCentral()
 }
}
