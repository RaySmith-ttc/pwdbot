package ru.raysmith.pwdbot

object Environment {
    val databaseHost = System.getenv("DB_HOST") ?: "localhost"
    val databasePort = System.getenv("DB_PORT")?.toInt()
    val databaseUser = System.getenv("DB_USER")
    val databasePass = System.getenv("DB_PASS")
    val databaseName = System.getenv("DB_NAME") ?: "pwdbot"
}