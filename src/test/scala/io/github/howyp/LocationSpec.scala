package io.github.howyp

import org.scalatest.{FreeSpec, Matchers}

class LocationSpec extends FreeSpec with Matchers {
  "Locations" - {
    "have the distance between them determined by the haversine formula" - {
      "two identical locations should have a distance of zero" in {
        val l = Location(1.2345,5.6789)
        l.distanceInMeters(l) should be (0)
      }
      "two sample locations should have the same distance as specified in rosetta code" in {
        val l1 = Location(36.12, -86.67)
        val l2 = Location(33.94, -118.40)
        l1.distanceInMeters(l2) should be (2887259.9506071107)
      }
    }
  }
}
