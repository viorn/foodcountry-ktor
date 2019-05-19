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
import tables.TokenUUIDTable
import tables.UsersTable
import tables.UsersRoleTable

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
    override suspend fun totalUsers(): Int = withContext(Dispatchers.IO) {
        transaction {
            UsersTable.selectAll().count()
        }
    }

    override suspend fun checkTokenUUID(uuid: String): Boolean = withContext(Dispatchers.IO) {
        transaction {
            TokenUUIDTable.select { TokenUUIDTable.uuid eq uuid }.count() != 0
        }
    }

    override suspend fun addTokenUUID(userId: Int, uuid: String, userAgent: String?) = withContext(Dispatchers.IO) {
        transaction {
            TokenUUIDTable.insert {
                it[TokenUUIDTable.userId] = userId
                it[TokenUUIDTable.uuid] = uuid
                it[TokenUUIDTable.date] = DateTime.now()
                it[TokenUUIDTable.userAgent] = userAgent
            }
        }
    }

    override suspend fun deleteTokenUUID(uuid: String) = withContext(Dispatchers.IO) {
        transaction {
            TokenUUIDTable.deleteWhere { TokenUUIDTable.uuid eq uuid }
        }
    }

    override suspend fun addUser(user: User) = withContext(Dispatchers.IO) {
        transaction {
            val userId = UsersTable.insert {
                it[name] = user.name
                it[password] = user.password
            }[UsersTable.id]
            user.roles.forEach { role ->
                UsersRoleTable.insert table@{
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
                val roles = UsersRoleTable.role.pg_array_agg().alias("roles")
                return@transaction (UsersTable leftJoin UsersRoleTable).slice(UsersTable.id, UsersTable.name, UsersTable.password, roles)
                    .select { (UsersTable.id eq UsersRoleTable.userId) and (UsersTable.name eq name) }.groupBy(
                        UsersTable.id).first().let {
                        User(
                            id = it[UsersTable.id],
                            name = it[UsersTable.name],
                            password = it[UsersTable.password],
                            roles = (it[roles].array as Array<String>).map { Role.valueOf(it) }
                        )
                    }
            }.getOrNull()
        }
    }

    override suspend fun getUser(userId: Int): User? = withContext(Dispatchers.IO) {
        transaction {
            val roles = UsersRoleTable.role.pg_array_agg().alias("roles")
            return@transaction (UsersTable leftJoin UsersRoleTable).slice(UsersTable.id, UsersTable.name, UsersTable.password, roles)
                .select { (UsersTable.id eq UsersRoleTable.userId) and (UsersTable.id eq userId) }.groupBy(
                    UsersTable.id).first().let {
                    User(
                        id = it[UsersTable.id],
                        name = it[UsersTable.name],
                        password = it[UsersTable.password],
                        roles = (it[roles].array as Array<String>).map { Role.valueOf(it) }
                    )
                }
        }
    }

    override suspend fun lockUser(userId: Int): User? = withContext(Dispatchers.IO) {
        transaction {
            UsersTable.update(where = { UsersTable.id eq userId }) { it[locked] = true }
            val roles = UsersRoleTable.role.pg_array_agg().alias("roles")
            return@transaction (UsersTable leftJoin UsersRoleTable).slice(UsersTable.id, UsersTable.name, UsersTable.password, roles)
                .select { (UsersTable.id eq UsersRoleTable.userId) and (UsersTable.id eq userId) }.groupBy(
                    UsersTable.id).first().let {
                    User(
                        id = it[UsersTable.id],
                        name = it[UsersTable.name],
                        password = it[UsersTable.password],
                        roles = (it[roles].array as Array<String>).map { Role.valueOf(it) }
                    )
                }
        }
    }

    override suspend fun unlockUser(userId: Int): User? = withContext(Dispatchers.IO) {
        transaction {
            UsersTable.update(where = { UsersTable.id eq userId }) { it[locked] = false }
            val roles = UsersRoleTable.role.pg_array_agg().alias("roles")
            return@transaction (UsersTable leftJoin UsersRoleTable).slice(UsersTable.id, UsersTable.name, UsersTable.password, roles)
                .select { (UsersTable.id eq UsersRoleTable.userId) and (UsersTable.id eq userId) }.groupBy(
                    UsersTable.id).first().let {
                    User(
                        id = it[UsersTable.id],
                        name = it[UsersTable.name],
                        password = it[UsersTable.password],
                        roles = (it[roles].array as Array<String>).map { Role.valueOf(it) }
                    )
                }
        }
    }

    override suspend fun getUsers(offset: Int, limit: Int): List<User> = withContext(Dispatchers.IO) {
        transaction {
            val roles = UsersRoleTable.role.pg_array_agg().alias("roles")
            return@transaction (UsersTable leftJoin UsersRoleTable).slice(UsersTable.id, UsersTable.name, UsersTable.password, roles)
                .select { UsersTable.id eq UsersRoleTable.userId }
                .groupBy(UsersTable.id)
                .limit(limit, offset)
                .map {
                    User(
                        id = it[UsersTable.id],
                        name = it[UsersTable.name],
                        password = it[UsersTable.password],
                        roles = (it[roles].array as Array<String>).map { Role.valueOf(it) }
                    )
                }
        }
    }
}