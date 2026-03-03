package com.swm.tests.ui.transport.geofencing;

import org.testng.annotations.Test;
import com.swm.core.base.BaseTest;
import com.swm.ui.pages.transport.geofencing.GeoFencingPage;

public class GeoFencingTest extends BaseTest {
    
    @Test
    public void testCreateGeofence() {
        GeoFencingPage page = new GeoFencingPage();
        page.clickCreateGeofence();
    }
}
