package com.taganhorn.repositories

import com.taganhorn.entities.User
import com.taganhorn.kodein
import com.taganhorn.security.Role
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.kodein.di.generic.instance
import tables.TokenUUID
import tables.Users
import tables.UsersRole

val UserRepository by kodein.instance<IUserRepository>()

interface IUserRepository {
    suspend fun totalUsers(): Int

    suspend fun checkTokenUUID(uuid: String): Boolean

    suspend fun addTokenUUID(userId: Int, uuid: String, userAgent: String? = null): InsertStatement<Number>

    suspend fun deleteTokenUUID(uuid: String): Int

    suspend fun addUser(user: User): User

    suspend fun findUserByName(name: String): User?

    suspend fun getUser(userId: Int): User?

    suspend fun lockUser(userId: Int): User?

    suspend fun unlockUser(userId: Int): User?

    suspend fun getUsers(offset: Int = 0, limit: Int = 10): List<User>
}

class UserRepositoryImpl : IUserRepository {
    init {
        transaction {
            SchemaUtils.apply {
                create(Users)
                create(UsersRole)
                create(TokenUUID)
            }
        }
    }

    override suspend fun totalUsers(): Int = withContext(Dispatchers.IO) {
        transaction {
            Users.selectAll().count()
        }
    }

    override suspend fun checkTokenUUID(uuid: String): Boolean = withContext(Dispatchers.IO) {
        transaction {
            TokenUUID.select { TokenUUID.uuid eq uuid }.count() != 0
        }
    }

    override suspend fun addTokenUUID(userId: Int, uuid: String, userAgent: String?) = withContext(Dispatchers.IO) {
        transaction {
            TokenUUID.insert {
                it[TokenUUID.userId] = userId
                it[TokenUUID.uuid] = uuid
                it[TokenUUID.date] = DateTime.now()
                it[TokenUUID.userAgent] = userAgent
            }
        }
    }

    override suspend fun deleteTokenUUID(uuid: String) = withContext(Dispatchers.IO) {
        transaction {
            TokenUUID.deleteWhere { TokenUUID.uuid eq uuid }
        }
    }

    override suspend fun addUser(user: User) = withContext(Dispatchers.IO) {
        transaction {
            val userId = Users.insert {
                it[name] = user.name
                it[password] = user.password
            }[Users.id]
            user.roles.forEach { role ->
                UsersRole.insert table@{
                    it[this@table.userId] = userId
                    it[this@table.role] = role.name
                }
            }
            return@transaction user.copy(id = userId)
        }
    }

    override suspend fun findUserByName(name: String): User? = withContext(Dispatchers.IO) {
        transaction {
            runCatching {
                val roles = UsersRole.role.pg_array_agg().alias("roles")
                return@transaction (Users leftJoin UsersRole).slice(Users.id, Users.name, Users.password, roles)
                    .select { (Users.id eq UsersRole.userId) and (Users.name eq name) }.groupBy(
                        Users.id).first().let {
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

    override suspend fun getUser(userId: Int): User? = withContext(Dispatchers.IO) {
        transaction {
            val roles = UsersRole.role.pg_array_agg().alias("roles")
            return@transaction (Users leftJoin UsersRole).slice(Users.id, Users.name, Users.password, roles)
                .select { (Users.id eq UsersRole.userId) and (Users.id eq userId) }.groupBy(
                    Users.id).first().let {
                    User(
                        id = it[Users.id],
                        name = it[Users.name],
                        password = it[Users.password],
                        roles = (it[roles].array as Array<String>).map { Role.valueOf(it) }
                    )
                }
        }
    }

    override suspend fun lockUser(userId: Int): User? = withContext(Dispatchers.IO) {
        transaction {
            Users.update(where = { Users.id eq userId }) { it[locked] = true }
            val roles = UsersRole.role.pg_array_agg().alias("roles")
            return@transaction (Users leftJoin UsersRole).slice(Users.id, Users.name, Users.password, roles)
                .select { (Users.id eq UsersRole.userId) and (Users.id eq userId) }.groupBy(
                    Users.id).first().let {
                    User(
                        id = it[Users.id],
                        name = it[Users.name],
                        password = it[Users.password],
                        roles = (it[roles].array as Array<String>).map { Role.valueOf(it) }
                    )
                }
        }
    }

    override suspend fun unlockUser(userId: Int): User? = withContext(Dispatchers.IO) {
        transaction {
            Users.update(where = { Users.id eq userId }) { it[locked] = false }
            val roles = UsersRole.role.pg_array_agg().alias("roles")
            return@transaction (Users leftJoin UsersRole).slice(Users.id, Users.name, Users.password, roles)
                .select { (Users.id eq UsersRole.userId) and (Users.id eq userId) }.groupBy(
                    Users.id).first().let {
                    User(
                        id = it[Users.id],
                        name = it[Users.name],
                        password = it[Users.password],
                        roles = (it[roles].array as Array<String>).map { Role.valueOf(it) }
                    )
                }
        }
    }

    override suspend fun getUsers(offset: Int, limit: Int): List<User> = withContext(Dispatchers.IO) {
        transaction {
            val roles = UsersRole.role.pg_array_agg().alias("roles")
            return@transaction (Users leftJoin UsersRole).slice(Users.id, Users.name, Users.password, roles)
                .select { Users.id eq UsersRole.userId }
                .groupBy(Users.id)
                .limit(limit, offset)
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