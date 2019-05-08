package com.taganhorn.repositories

import com.taganhorn.entities.User
import com.taganhorn.security.Role
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime

object UserRepository {
    object Users : Table() {
        val id = integer("id").primaryKey().autoIncrement()
        val name = varchar("name", length = 100).uniqueIndex()
        val password = varchar("password", length = 100)
    }

    object UsersRole : Table() {
        val userId = (integer("userId") references Users.id).index()
        val role = varchar("role", 50).index()
    }

    object TokenUUID : Table() {
        val userId = (integer("userId") references Users.id).index()
        val uuid = varchar("uuid", length = 36)
        val date = datetime("datetime")
        val userAgent = varchar("userAgent", 300).nullable()
    }

    init {
        transaction {
            SchemaUtils.apply {
                create(Users)
                create(UsersRole)
                create(TokenUUID)
            }
        }
        /*runBlocking {
            (0..1000).forEach {
                addTokenUUID(UUID.randomUUID().toString())
            }
        }*/
    }

    suspend fun usersCount(): Int = withContext(Dispatchers.IO) {
        transaction {
            Users.selectAll().count()
        }
    }

    suspend fun checkTokenUUID(uuid: String): Boolean = withContext(Dispatchers.IO) {
        transaction {
            TokenUUID.select { TokenUUID.uuid eq uuid }.count() != 0
        }
    }

    suspend fun addTokenUUID(userId: Int, uuid: String, userAgent: String? = null) = withContext(Dispatchers.IO) {
        transaction {
            TokenUUID.insert {
                it[TokenUUID.userId] = userId
                it[TokenUUID.uuid] = uuid
                it[TokenUUID.date] = DateTime.now()
                it[TokenUUID.userAgent] = userAgent
            }
        }
    }

    suspend fun deleteTokenUUID(uuid: String) = withContext(Dispatchers.IO) {
        transaction {
            TokenUUID.deleteWhere { TokenUUID.uuid eq uuid }
        }
    }

    suspend fun addUser(user: User) = withContext(Dispatchers.IO) {
        transaction {
            val userId = UserRepository.Users.insert {
                it[name] = user.name
                it[password] = user.password
            }[UserRepository.Users.id]
            user.roles.forEach { role ->
                UserRepository.UsersRole.insert table@{
                    it[this@table.userId] = userId
                    it[this@table.role] = role.name
                }
            }
            return@transaction user.copy(id = userId)
        }
    }

    suspend fun findUserByName(name: String): User? = withContext(Dispatchers.IO) {
        transaction {
            runCatching {
                val roles = UsersRole.role.pg_array_agg().alias("roles")
                return@transaction (Users leftJoin UsersRole).slice(Users.id, Users.name, Users.password, roles)
                    .select { (Users.id eq UsersRole.userId) and (Users.name eq name) }.groupBy(Users.id).first().let {
                        User(
                            id = it[Users.id],
                            name = it[Users.name],
                            password = it[Users.password],
                            roles = (it[roles].array as Array<String>).map { Role.valueOf(it) }
                        )
                    }
            }.getOrNull()
        }
    }

    suspend fun getUser(userId: Int): User? = withContext(Dispatchers.IO) {
        transaction {
            val roles = UsersRole.role.pg_array_agg().alias("roles")
            return@transaction (Users leftJoin UsersRole).slice(Users.id, Users.name, Users.password, roles)
                .select { (Users.id eq UsersRole.userId) and (Users.id eq userId) }.groupBy(Users.id).first().let {
                    User(
                        id = it[Users.id],
                        name = it[Users.name],
                        password = it[Users.password],
                        roles = (it[roles].array as Array<String>).map { Role.valueOf(it) }
                    )
                }
        }
    }

    suspend fun getUsers(offset: Int = 0, limit: Int = 10): List<User> = withContext(Dispatchers.IO) {
        transaction {
            val roles = UsersRole.role.pg_array_agg().alias("roles")
            return@transaction (Users leftJoin UsersRole).slice(Users.id, Users.name, Users.password, roles)
                .select { Users.id eq UsersRole.userId }
                .groupBy(Users.id)
                .limit(limit, offset * limit)
                .map {
                    User(
                        id = it[Users.id],
                        name = it[Users.name],
                        password = it[Users.password],
                        roles = (it[roles].array as Array<String>).map { Role.valueOf(it) }
                    )
                }
        }
    }
}