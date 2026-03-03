package com.swm.tests.ui.transport.route;

import org.testng.annotations.Test;
import com.swm.core.base.BaseTest;
import com.swm.ui.pages.transport.route.RouteManagementPage;

public class RouteManagementTest extends BaseTest {
    
    @Test
    public void testCreateRoute() {
        RouteManagementPage page = new RouteManagementPage();
        page.clickCreateRoute();
        page.enterRouteName("Route 1");
    }
}
