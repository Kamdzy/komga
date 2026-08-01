package org.gotson.komga.infrastructure.jooq

import com.fasterxml.jackson.databind.ObjectMapper
import org.jooq.DSLContext
import org.jooq.impl.DSL
import java.io.Closeable

/**
 * Holds a collection of values that are too long to be specified in a query condition,
 * and exposes them as a sub-select.
 *
 * Backed by SQLite's json_each(): the whole collection travels as a single bind variable, so the
 * sub-select is valid on any connection.
 *
 * This used to be backed by CREATE TEMPORARY TABLE. SQLite scopes TEMP tables to the connection that
 * created them, while jOOQ acquires and releases a pooled connection per statement. With
 * komga.database.max-pool-size > 1 (which only widens the read pool, the write pool is pinned to 1)
 * the CREATE and the statements that followed could land on different connections, failing with
 * "[SQLITE_ERROR] no such table: temp_XXXX" from any DAO method not running inside a transaction,
 * such as BookDtoDao.findAll or findAllOnDeck.
 *
 * The Closeable shape is kept so that call sites do not need to change; close() is a no-op.
 */
class TempTable(
  private val dslContext: DSLContext,
) : Closeable {
  private val values = mutableListOf<String>()

  // batchSize is unused, there is nothing to batch: kept so existing call sites are unchanged
  fun insertTempStrings(
    batchSize: Int,
    collection: Collection<String>,
  ) {
    values.addAll(collection)
  }

  fun selectTempStrings() =
    dslContext
      .select(DSL.field(DSL.name("value"), String::class.java))
      .from(DSL.table("json_each({0})", DSL.`val`(objectMapper.writeValueAsString(values))))

  override fun close() {
    // nothing to clean up
  }

  companion object {
    private val objectMapper = ObjectMapper()

    fun DSLContext.withTempTable(
      batchSize: Int,
      collection: Collection<String>,
    ) = TempTable(this)
      .also {
        it.insertTempStrings(batchSize, collection)
      }
  }
}
