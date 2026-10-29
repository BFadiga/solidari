// O plugin Android (carregado aqui na raiz) traz uma commons-compress antiga para o
// classpath dos plugins. O Spring Boot (modulo backend) precisa de uma versao nova
// para gerar o jar executavel (bootJar). Fixar a versao aqui resolve o conflito.
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.apache.commons:commons-compress:1.27.1")
    }
}

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}