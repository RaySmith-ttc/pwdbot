package ru.raysmith.pwdbot

import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import ru.raysmith.pwdbot.database.Database
import ru.raysmith.pwdbot.database.Location
import ru.raysmith.pwdbot.database.User
import ru.raysmith.pwdbot.utils.*
import ru.raysmith.tgbot.core.Bot
import ru.raysmith.tgbot.core.send
import ru.raysmith.tgbot.model.bot.BotCommand
import ru.raysmith.tgbot.model.network.updates.Update
import ru.raysmith.tgbot.utils.locations.LocationConfig
import ru.raysmith.tgbot.utils.messageDocument
import ru.raysmith.tgbot.utils.messagePhoto
import ru.raysmith.tgbot.utils.messageText

class LocationConfigImpl(override val update: Update) : LocationConfig {
    val user by lazy {
        transaction {
            val userId = update.findFrom()!!.id.value
            User.findById(userId) ?: User.new(userId) { }
        }
    }
}

val logger = LoggerFactory.getLogger("pwdbot")

fun main() = runBlocking {
    Database.init()
    
    Bot()
        .onError { e -> logger.error(e.message, e) }
        .enableBlocking { u -> u.findChatId() }
        .locations<LocationConfigImpl> {
            config { LocationConfigImpl(it) }
            getLocation { user.location.name }
            updateLocation { transaction { user.location = Location.valueOf(it.name) } }
            
            global {
                handleCommand {
                    isCommand(BotCommand.START) {
                        toLocation(Location.MENU)
                        send("TODO info")
                    }
                    isCommand(BotCommand.ENCODE) {
                        toLocation(Location.ENCODE)
                    }
                    isCommand(BotCommand.DECODE) {
                        toLocation(Location.DECODE)
                    }
                }
            }
            
            location(Location.MENU) {
            
            }
            
            location(Location.ENCODE) {
                onEnter {
                    send("Send text or file to encode")
                }
                
                handle {
                    handleMessage { config ->
                        messageText {
                            transaction { config.user.tempText = it }
                            toLocation(Location.ENCODE_KEY)
                        } ?: messagePhoto {
                        
                        } ?: messageDocument {
                        
                        }
                    }
                }
            }
            
            location(Location.ENCODE_KEY) {
                onEnter {
                    send("Send encoding key")
                }
                
                handle {
                    handleMessage { config ->
                        messageText {
                            send(Blowfish.encrypt(config.user.tempText!!, it).toHex())
                            transaction { config.user.tempText = null }
                        }
                    }
                }
            }
            
            location(Location.DECODE) {
                onEnter {
                    send("Send hash to decode")
                }
                
                handle { 
                    handleMessage {  config ->
                        messageText { 
                            transaction { config.user.tempText = it }
                            toLocation(Location.DECODE_KEY)
                        } ?: onEnter()
                    }
                }
            }
            
            location(Location.DECODE_KEY) {
                onEnter {
                    send("Send key")
                }
                
                handle {
                    handleMessage { config ->
                        messageText {
                            send(Blowfish.decrypt(config.user.tempText!!.decodeHex(), it))
                            transaction { config.user.tempText = null }
                        }
                    }
                }
            }
        }
}