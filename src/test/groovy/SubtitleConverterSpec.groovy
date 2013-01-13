import spock.lang.*
import groovy.mock.interceptor.MockFor

class SubtitleConverterSpec extends Specification {
  def converter
  def data

  def setup() {
    converter = new SubtitleConverter()
  }

  def 'convert STL to SRT'() {
    setup:
    def stlData = [
      "00:00:03:15\t,\t00:00:05:26\t,\tTonight, The Deep Dive.",
      "00:00:05:27\t,\t00:00:10:01\t,\tOne company's secret weapon for innovation",
      "00:00:14:19 , 00:00:25:28 , Actually, ^IBrillstein^I said you were | unimaginably full of yourself."
    ]
    def expectData = [
      "1\r00:00:03,495 --> 00:00:05,858\rTonight, The Deep Dive.\r\r",
      "2\r00:00:05,891 --> 00:00:10,033\rOne company's secret weapon for innovation\r\r",
      "3\r00:00:14,627 --> 00:00:25,924\rActually, ^IBrillstein^I said you were\runimaginably full of yourself.\r\r"
    ]
    def resultData = []
    resultData.metaClass.close = {return true}
    def myMockFile
    myMockFile = [
      filename: '',
      newInstance: {filename -> return myMockFile},
      newWriter: {unicode -> resultData},
      eachLine: {closure -> stlData.each(closure)}
    ]

    when:
    converter.convertSTL2SRT('someFilename.stl', myMockFile)

    then:
    expectData == resultData
  }


  def 'construct SRT'() {
    expect:
      result == converter.constructSRT(data, seq)

    where:
    seq << [1, 2]
    data << [
      [
        start: "00:00:03,495",
          end: "00:00:05,858",
         text: "Tonight, The Deep Dive."
      ],
      [
        start: "00:00:05,891",
          end: "00:00:10,033",
         text: "One company's secret weapon for innovation"
      ]
    ]
    result << [
      "1\r00:00:03,495 --> 00:00:05,858\rTonight, The Deep Dive.\r\r",
      "2\r00:00:05,891 --> 00:00:10,033\rOne company's secret weapon for innovation\r\r"
    ]
  }

  def 'parse STL'() {
    expect:
      result == converter.parseSTL(raw)

    where:
    raw << [
      "00:00:03:15\t,\t00:00:05:26\t,\tTonight, The Deep Dive.",
      "00:00:05:27\t,\t00:00:10:01\t,\tOne company's secret weapon for innovation",
      "00:00:14:19 , 00:00:25:28 , Actually, ^IBrillstein^I said you were | unimaginably full of yourself."
    ]
    result << [
      [
        start: "00:00:03,495",
          end: "00:00:05,858",
         text: "Tonight, The Deep Dive."
      ],
      [
        start: "00:00:05,891",
          end: "00:00:10,033",
         text: "One company's secret weapon for innovation"
      ],
      [
        start: "00:00:14,627",
          end: "00:00:25,924",
         text: "Actually, ^IBrillstein^I said you were\runimaginably full of yourself."
      ]
    ]

  }

  def 'convert timecode(HH:MM:SS:FF) to time(HH:MM:SS,msec)'() {
    expect:
      time == converter.convertTimeCodeToTime(timecode)

    where:
    timecode      | time
    '00:00:03:15' | '00:00:03,495'
    '00:00:05:26' | '00:00:05,858'
    '00:00:05:27' | '00:00:05,891'
    '00:00:10:01' | '00:00:10,033'
  }

  def 'reformat frame number to millisec'() {
    expect:
      msec == converter.frameToMsec(frame)

    where:
    frame | msec
    '01'  | '033'
    '15'  | '495'
    '27'  | '891'
  }
}
