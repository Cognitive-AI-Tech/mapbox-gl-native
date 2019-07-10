package com.mapbox.mapboxsdk.maps

import android.graphics.PointF
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.matcher.ViewMatchers.withId
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.constants.MapboxConstants
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.GesturesUiTestUtils.move
import com.mapbox.mapboxsdk.maps.GesturesUiTestUtils.quickScale
import com.mapbox.mapboxsdk.testapp.R
import com.mapbox.mapboxsdk.testapp.activity.BaseTest
import com.mapbox.mapboxsdk.testapp.activity.espresso.DeviceIndependentTestActivity
import junit.framework.Assert
import org.junit.Test

class MapGestureDetectorTest : BaseTest() {
  override fun getActivityClass() = DeviceIndependentTestActivity::class.java

  @Test
  fun sanity_quickZoom() {
    validateTestSetup()
    var initialZoom: Double? = null
    rule.runOnUiThread {
      initialZoom = mapboxMap.cameraPosition.zoom
    }
    onView(withId(R.id.mapView)).perform(quickScale(350f, withVelocity = false))
    rule.runOnUiThread {
      Assert.assertTrue(mapboxMap.cameraPosition.zoom > initialZoom!!)
      Assert.assertEquals(2.77, mapboxMap.cameraPosition.zoom, 0.5)
    }
  }

  @Test
  fun quickZoomDisabled_phantomQuickZoom_moveStillEnabled_15091() {
    // regression test for https://github.com/mapbox/mapbox-gl-native/issues/15091
    validateTestSetup()
    var initialCameraPosition: CameraPosition? = null
    rule.runOnUiThread {
      // zoom in so we can move vertically
      mapboxMap.moveCamera(CameraUpdateFactory.zoomTo(4.0))
      initialCameraPosition = mapboxMap.cameraPosition
      mapboxMap.uiSettings.isQuickZoomGesturesEnabled = false
    }

    onView(withId(R.id.mapView)).perform(quickScale(300f))
    rule.runOnUiThread {
      // camera did not move
      Assert.assertEquals(initialCameraPosition!!, mapboxMap.cameraPosition)
    }

    var expectedTarget: LatLng? = null
    rule.runOnUiThread {
      val currentPoint = mapboxMap.projection.toScreenLocation(initialCameraPosition!!.target)
      val resultingPoint = PointF(currentPoint.x + 300f, currentPoint.y + 300f)
      expectedTarget = mapboxMap.projection.fromScreenLocation(resultingPoint)
    }

    // move to expected target
    onView(withId(R.id.mapView)).perform(move(-300f, -300f, withVelocity = false))
    rule.runOnUiThread {
      Assert.assertEquals(expectedTarget!!.latitude, mapboxMap.cameraPosition.target.latitude, 1.0)
      Assert.assertEquals(expectedTarget!!.longitude, mapboxMap.cameraPosition.target.longitude, 1.0)
    }
  }

  @Test
  fun quickZoom_doNotMove_14227() {
    // test for https://github.com/mapbox/mapbox-gl-native/issues/14227
    validateTestSetup()
    var initialTarget: LatLng? = null
    rule.runOnUiThread {
      initialTarget = mapboxMap.cameraPosition.target
    }

    onView(withId(R.id.mapView)).perform(quickScale(300f))
    rule.runOnUiThread {
      // camera did not move
      Assert.assertEquals(initialTarget!!.latitude, mapboxMap.cameraPosition.target.latitude, 0.5)
      Assert.assertEquals(initialTarget!!.longitude, mapboxMap.cameraPosition.target.longitude, 0.5)
    }
  }

  @Test
  fun quickZoom_interrupted_moveStillEnabled_14598() {
    // test for https://github.com/mapbox/mapbox-gl-native/issues/14598
    validateTestSetup()
    onView(withId(R.id.mapView)).perform(quickScale(300f, interrupt = true))

    var expectedTarget: LatLng? = null
    rule.runOnUiThread {
      val currentPoint = mapboxMap.projection.toScreenLocation(mapboxMap.cameraPosition.target)
      val resultingPoint = PointF(currentPoint.x + 300f, currentPoint.y + 300f)
      expectedTarget = mapboxMap.projection.fromScreenLocation(resultingPoint)
    }

    // move to expected target
    onView(withId(R.id.mapView)).perform(move(-300f, -300f, withVelocity = false))
    rule.runOnUiThread {
      Assert.assertEquals(expectedTarget!!.latitude, mapboxMap.cameraPosition.target.latitude, 1.0)
      Assert.assertEquals(expectedTarget!!.longitude, mapboxMap.cameraPosition.target.longitude, 1.0)
    }
  }

  @Test
  fun doubleTap_minimalMovement() {
    validateTestSetup()
    var initialZoom: Double? = null
    rule.runOnUiThread {
      initialZoom = mapboxMap.cameraPosition.zoom
    }
    onView(withId(R.id.mapView)).perform(quickScale(mapboxMap.gesturesManager.standardScaleGestureDetector.spanSinceStartThreshold / 2, withVelocity = false, duration = 50L))
    R.id.mapView.loopFor(MapboxConstants.ANIMATION_DURATION.toLong())
    rule.runOnUiThread {
      Assert.assertEquals(initialZoom!! + 1, mapboxMap.cameraPosition.zoom, 0.1)
    }
  }

  @Test
  fun doubleTap_overMaxThreshold_ignore_14013() {
    // test for https://github.com/mapbox/mapbox-gl-native/issues/14013
    validateTestSetup()
    var initialZoom: Double? = null
    rule.runOnUiThread {
      initialZoom = mapboxMap.cameraPosition.zoom
    }
    onView(withId(R.id.mapView)).perform(quickScale(mapboxMap.gesturesManager.standardScaleGestureDetector.spanSinceStartThreshold, withVelocity = false, duration = 50L))
    R.id.mapView.loopFor(MapboxConstants.ANIMATION_DURATION.toLong())
    rule.runOnUiThread {
      Assert.assertEquals(initialZoom!!, mapboxMap.cameraPosition.zoom, 0.01)
    }
  }
}