package ru.raysmith.pwdbot.database

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object Users : LongIdTable() {
    val location = enumerationByName("location", 255, Location::class).default(Location.MENU)
    val dateCreate = datetime("date_create").clientDefault { LocalDateTime.now() }
    val tempText = varchar("temp_text", 4096, COLLATE_UTF8MB4_UNICODE_CI).nullable()
}

class User(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<User>(Users)
    
    var location by Users.location
    val dateCreate by Users.dateCreate
    var tempText by Users.tempText
    
    var messagesToDelete by MessageToDelete via UsersMessagesToDelete
    
    fun addMessageToDelete(id: Int) {
        messagesToDelete = SizedCollection(messagesToDelete + MessageToDelete.new(id) {})
    }
    fun clearMessagesToDelete() {
        messagesToDelete = SizedCollection()
    }
}

object UsersMessagesToDelete : IntIdTable() {
    val userId = reference("user_id", Users)
    val messageToDeleteId = reference("message_to_delete_id", MessagesToDelete)
}

object MessagesToDelete : IntIdTable()

class MessageToDelete(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<MessageToDelete>(MessagesToDelete)
}