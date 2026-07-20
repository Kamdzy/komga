package org.gotson.komga.infrastructure.jooq.main

import org.assertj.core.api.Assertions.assertThat
import org.gotson.komga.domain.model.BookMetadata
import org.gotson.komga.domain.model.SeriesMetadata
import org.gotson.komga.domain.model.makeBook
import org.gotson.komga.domain.model.makeLibrary
import org.gotson.komga.domain.model.makeSeries
import org.gotson.komga.domain.persistence.BookRepository
import org.gotson.komga.domain.persistence.LibraryRepository
import org.gotson.komga.domain.persistence.SeriesRepository
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ReferentialDaoTest(
  @Autowired private val referentialDao: ReferentialDao,
  @Autowired private val bookMetadataDao: BookMetadataDao,
  @Autowired private val seriesMetadataDao: SeriesMetadataDao,
  @Autowired private val bookRepository: BookRepository,
  @Autowired private val seriesRepository: SeriesRepository,
  @Autowired private val libraryRepository: LibraryRepository,
) {
  private val library1 = makeLibrary("library1")
  private val library2 = makeLibrary("library2")

  @BeforeAll
  fun setup() {
    libraryRepository.insert(library1)
    libraryRepository.insert(library2)

    val series1 = makeSeries("Series1", libraryId = library1.id).also { seriesRepository.insert(it) }
    val series2 = makeSeries("Series2", libraryId = library2.id).also { seriesRepository.insert(it) }

    seriesMetadataDao.insert(SeriesMetadata(title = "Series1", tags = setOf("shounen", "épique"), seriesId = series1.id))
    seriesMetadataDao.insert(SeriesMetadata(title = "Series2", tags = setOf("shounen"), seriesId = series2.id))

    // "action" is deliberately repeated across books, "éclair" sorts after "zebra" under a binary collation
    listOf(
      Triple("Book1", series1 to library1, setOf("action", "éclair")),
      Triple("Book2", series1 to library1, setOf("action", "zebra")),
      Triple("Book3", series2 to library2, setOf("manga", "eagle")),
    ).forEach { (name, location, tags) ->
      val (series, library) = location
      val book = makeBook(name, libraryId = library.id, seriesId = series.id).also { bookRepository.insert(it) }
      bookMetadataDao.insert(BookMetadata(title = name, number = "1", numberSort = 1F, tags = tags, bookId = book.id))
    }
  }

  @AfterAll
  fun tearDown() {
    bookRepository.findAll().forEach { bookMetadataDao.delete(it.id) }
    seriesRepository.findAll().forEach { seriesMetadataDao.delete(it.id) }
    bookRepository.deleteAll()
    seriesRepository.deleteAll()
    libraryRepository.deleteAll()
  }

  @Test
  fun `given books with duplicate tags when finding all book tags then tags are deduplicated`() {
    val tags = referentialDao.findAllBookTags(null)

    // "action" is on two books but must appear once
    assertThat(tags).containsExactly("action", "eagle", "éclair", "manga", "zebra")
  }

  @Test
  fun `given books in multiple libraries when finding all book tags filtered then only that library's tags are returned`() {
    val tags = referentialDao.findAllBookTags(listOf(library1.id))

    assertThat(tags).containsExactly("action", "éclair", "zebra")
  }

  @Test
  fun `given accented tags when finding all book tags then they are sorted ignoring accents`() {
    val tags = referentialDao.findAllBookTags(null).toList()

    // under a plain binary collation "éclair" (U+00E9) would sort after "zebra"
    assertThat(tags.indexOf("éclair")).isLessThan(tags.indexOf("manga"))
    assertThat(tags.indexOf("eagle")).isLessThan(tags.indexOf("éclair"))
  }

  @Test
  fun `given series with duplicate tags when finding all series tags then tags are deduplicated and sorted`() {
    val tags = referentialDao.findAllSeriesTags(null)

    assertThat(tags).containsExactly("épique", "shounen")
  }

  @Test
  fun `given a series when finding book tags by series then only that series' tags are returned`() {
    val series1 = seriesRepository.findAll().first { it.name == "Series1" }

    val tags = referentialDao.findAllBookTagsBySeries(series1.id, null)

    assertThat(tags).containsExactly("action", "éclair", "zebra")
  }
}
