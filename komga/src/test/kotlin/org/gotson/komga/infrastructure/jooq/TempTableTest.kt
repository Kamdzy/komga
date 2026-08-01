package org.gotson.komga.infrastructure.jooq

import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.infrastructure.jooq.TempTable.Companion.withTempTable
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.jooq.impl.DataSourceConnectionProvider
import org.jooq.impl.DefaultConfiguration
import org.jooq.impl.DefaultDSLContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy
import java.nio.file.Path

class TempTableTest {
  @TempDir
  lateinit var tempDir: Path

  private lateinit var dsl: DSLContext

  private val id = DSL.field(DSL.name("ID"), String::class.java)
  private val book = DSL.table(DSL.name("BOOK"))
  private val value = DSL.field(DSL.name("value"), String::class.java)

  @BeforeEach
  fun setup() {
    // DriverManagerDataSource hands out a new Connection on every getConnection(). That is the worst
    // case of a multi-connection pool, and makes the connection affinity failure deterministic
    // instead of racy: a Hikari pool would likely hand the same connection back to a single thread
    // and pass even without the fix. Wiring mirrors KomgaJooqConfiguration.
    val dataSource = DriverManagerDataSource("jdbc:sqlite:${tempDir.resolve("temptable.sqlite")}")
    dsl =
      DefaultDSLContext(
        DefaultConfiguration().apply {
          set(SQLDialect.SQLITE)
          set(DataSourceConnectionProvider(TransactionAwareDataSourceProxy(dataSource)))
        },
      )
    dsl.execute("CREATE TABLE BOOK (ID varchar NOT NULL PRIMARY KEY)")
    (0 until 5).forEach { dsl.execute("INSERT INTO BOOK VALUES ('BK$it')") }
  }

  @Test
  fun `given a connection per statement when using withTempTable then the sub-select resolves`() {
    dsl.withTempTable(1000, listOf("BK1", "BK3")).use { tempTable ->
      assertThat(
        dsl
          .select(id)
          .from(book)
          .where(id.`in`(tempTable.selectTempStrings()))
          .fetch(id),
      ).containsExactlyInAnyOrder("BK1", "BK3")
    }
  }

  @Test
  fun `given deferred creation when inserting after construction then the sub-select resolves`() {
    // mirrors BookDtoDao.findAll, which constructs TempTable directly "to control optional creation"
    TempTable(dsl).use { tempTable ->
      tempTable.insertTempStrings(1000, listOf("BK0"))
      assertThat(
        dsl
          .select(id)
          .from(book)
          .where(id.`in`(tempTable.selectTempStrings()))
          .fetch(id),
      ).containsExactly("BK0")
    }
  }

  @Test
  fun `given an empty collection then in matches nothing and notIn matches everything`() {
    dsl.withTempTable(1000, emptyList()).use { tempTable ->
      assertThat(
        dsl
          .select(id)
          .from(book)
          .where(id.`in`(tempTable.selectTempStrings()))
          .fetch(id),
      ).isEmpty()
      assertThat(
        dsl
          .select(id)
          .from(book)
          .where(id.notIn(tempTable.selectTempStrings()))
          .fetch(id),
      ).hasSize(5)
    }
  }

  @Test
  fun `given a temp table reused for several queries then every query resolves`() {
    // mirrors BookDtoDao.fetchAndMap and SeriesDtoDao.fetchAndMap, which call selectTempStrings()
    // 3 and 7 times respectively on a single TempTable
    dsl.withTempTable(1000, listOf("BK2", "BK4")).use { tempTable ->
      repeat(7) {
        assertThat(
          dsl
            .select(id)
            .from(book)
            .where(id.`in`(tempTable.selectTempStrings()))
            .fetch(id),
        ).containsExactlyInAnyOrder("BK2", "BK4")
      }
    }
  }

  @Test
  fun `given values needing escaping then they round trip exactly`() {
    // BookDao, SeriesDao and SidecarDao push filesystem URLs through TempTable
    val tricky =
      listOf(
        """a"b""",
        """C:\path\to\x.cbz""",
        "naïve—ダッシュ",
        "\uD83D\uDE00",
        "it's",
        "null",
        "",
        " sp ",
        "%_w%",
      )
    dsl.withTempTable(1000, tricky).use { tempTable ->
      assertThat(tempTable.selectTempStrings().fetch(value)).containsExactlyInAnyOrderElementsOf(tricky)
    }
  }
}
