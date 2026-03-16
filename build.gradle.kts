plugins {
  kotlin("jvm") version "2.2.21"
  id("com.diffplug.spotless") version "8.1.0"
}

group = "com.fResult"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
}

dependencies {
  testImplementation(kotlin("test"))
}

kotlin {
  jvmToolchain(24)
}

tasks.test {
  useJUnitPlatform()
}

spotless {
  kotlin {
    ktlint()

    target("**/*.kt")
    targetExclude("**/build/**")

    trimTrailingWhitespace()
    leadingTabsToSpaces()
    endWithNewline()
  }

  kotlinGradle {
    ktlint()
    target("*.gradle.kts")
  }
}

tasks.register("installGitHooks") {
  description = "Installs git hooks to run spotless check before commit"
  group = "help"

  doLast {
    val preCommitFile = file(".git/hooks/pre-commit")
    val script =
      """
      #!/bin/sh

      # 1. Identify staged Kotlin files
      STAGED_FILES=${'$'}(git diff --name-only --cached --diff-filter=ACMR | grep -E "\.kt${'$'}|\.kts${'$'}")

      if [ -z "${'$'}STAGED_FILES" ]; then
          exit 0
      fi

      echo "🧹 Running Spotless Apply on staged files..."

      ./gradlew spotlessApply

      RESULT=${'$'}?

      if [ ${'$'}RESULT -ne 0 ]; then
          echo "❌ Spotless check failed!"
          exit 1
      fi

      # 2. Re-stage formatted files
      echo "${'$'}STAGED_FILES" | xargs git add

      echo "✅ Code formatted successfully."
      exit 0
      """.trimIndent()

    preCommitFile.writeText(script)
    preCommitFile.setExecutable(true)
    println("Git hooks installed successfully!")
  }

  tasks.getByPath("build").dependsOn("installGitHooks")
}
