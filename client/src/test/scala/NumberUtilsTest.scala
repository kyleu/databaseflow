import utest._
import utils.NumberUtils

object NumberUtilsTest extends TestSuite {

  val tests = this{
    "Format Integers" - {
      assert(NumberUtils.withCommas(0) == "0")
      assert(NumberUtils.withCommas(1) == "1")
      assert(NumberUtils.withCommas(-1) == "-1")
      assert(NumberUtils.withCommas(100) == "100")
      assert(NumberUtils.withCommas(1000) == "1,000")
      assert(NumberUtils.withCommas(1000000) == "1,000,000")
      assert(NumberUtils.withCommas(1000000000) == "1,000,000,000")
      assert(NumberUtils.withCommas(1234567890) == "1,234,567,890")
    }
  }
}
