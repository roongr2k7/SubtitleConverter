// stl reference -> http://geocities.com/McPoodle43/DVDMaestro/stl_format.html
// srt reference -> http://www.matroska.org/technical/specs/subtitles/srt.html

class SubtitleConverter {

  protected static final STL_PATTERN = /^((\d{2}:){3}\d{2}\s,\s){2}(.+)/
  protected static final STL_DELIMITER_PATTERN = /\s,\s/

  private convertSTL2SRT(srcFilename, File = File) {
    def stlFile = File.newInstance(srcFilename)
    def dscFilename = srcFilename.replace('.stl', '.srt')
    def dstFile = File.newInstance(dscFilename)
    def dstWriter = dstFile.newWriter('UTF-8')
    def seq = 1

    stlFile.eachLine { line ->
      if (line ==~ STL_PATTERN) {
        dstWriter << constructSRT(parseSTL(line), seq++)
      }
    }
    dstWriter.close()
  }

  private constructSRT(data, sequence) {
    return "${sequence}\r${data.start} --> ${data.end}\r${data.text}\r\r"
  }

  private parseSTL(stlLine) {
    def (start, end, text) = stlLine.split(STL_DELIMITER_PATTERN)
    return [
      start: convertTimeCodeToTime(start),
        end: convertTimeCodeToTime(end),
       text: text.replace(' | ', '\r')
    ]
  }

  private convertTimeCodeToTime(timecode) {
    return timecode.replaceAll(/^(..:..:..):(..)$/){ all, hms, f -> "${hms},${frameToMsec(f)}" }
  }

  private frameToMsec(frameNo) {
    def msec = 33 * (frameNo as Integer)
    return sprintf("%03d", msec)
  }

}
