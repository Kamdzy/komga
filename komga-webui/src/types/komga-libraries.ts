import {ScanIntervalDto, SeriesCoverDto} from '@/types/enum-libraries'

export interface LibraryDto {
  id: string,
  name: string,
  root: string,
  importComicInfoBook: boolean,
  importComicInfoSeries: boolean,
  importComicInfoCollection: boolean,
  importComicInfoReadList: boolean,
  importComicInfoSeriesAppendVolume: boolean,
  importEpubBook: boolean,
  importEpubSeries: boolean,
  importMylarSeries: boolean,
  importLocalArtwork: boolean,
  importBarcodeIsbn: boolean,
  scanForceModifiedTime: boolean,
  scanInterval: ScanIntervalDto,
  scanOnStartup: boolean,
  scanCbx: boolean,
  scanPdf: boolean,
  scanEpub: boolean,
  scanDirectoryExclusions: string[],
  repairExtensions: boolean,
  convertToCbz: boolean,
  emptyTrashAfterScan: boolean,
  seriesCover: SeriesCoverDto,
  hashFiles: boolean,
  hashPages: boolean,
  hashKoreader: boolean,
  analyzeDimensions: boolean,
  oneshotsDirectory: string,
  unavailable: boolean,
}

export interface LibraryCreationDto {
  name: string,
  root: string,
  importComicInfoBook: boolean,
  importComicInfoSeries: boolean,
  importComicInfoCollection: boolean,
  importComicInfoReadList: boolean,
  importComicInfoSeriesAppendVolume: boolean,
  importEpubBook: boolean,
  importEpubSeries: boolean,
  importMylarSeries: boolean,
  importLocalArtwork: boolean,
  importBarcodeIsbn: boolean,
  scanForceModifiedTime: boolean,
  scanInterval: ScanIntervalDto,
  scanOnStartup: boolean,
  scanCbx: boolean,
  scanPdf: boolean,
  scanEpub: boolean,
  scanDirectoryExclusions: string[],
  repairExtensions: boolean,
  convertToCbz: boolean,
  emptyTrashAfterScan: boolean,
  seriesCover: SeriesCoverDto,
  hashFiles: boolean,
  hashPages: boolean,
  hashKoreader: boolean,
  analyzeDimensions: boolean,
  oneshotsDirectory: string,
}

export interface LibraryUpdateDto {
  name: string,
  root: string,
  importComicInfoBook: boolean,
  importComicInfoSeries: boolean,
  importComicInfoCollection: boolean,
  importComicInfoReadList: boolean,
  importComicInfoSeriesAppendVolume: boolean,
  importEpubBook: boolean,
  importEpubSeries: boolean,
  importMylarSeries: boolean,
  importLocalArtwork: boolean,
  importBarcodeIsbn: boolean,
  scanForceModifiedTime: boolean,
  scanInterval: ScanIntervalDto,
  scanOnStartup: boolean,
  scanCbx: boolean,
  scanPdf: boolean,
  scanEpub: boolean,
  scanDirectoryExclusions: string[],
  repairExtensions: boolean,
  convertToCbz: boolean,
  emptyTrashAfterScan: boolean,
  seriesCover: SeriesCoverDto,
  hashFiles: boolean,
  hashPages: boolean,
  hashKoreader: boolean,
  analyzeDimensions: boolean,
  oneshotsDirectory: string,
}
