package com.swm.tests.ui.transport.VehicleManagement.ActiveVehicles;

import org.testng.annotations.Test;
import com.swm.core.base.BaseTest;
import com.swm.ui.pages.transport.VehicleManagement.ActiveVehiclesPage;

public class ActiveVehiclesTest extends BaseTest {
    
    @Test
    public void testViewActiveVehicles() {
        ActiveVehiclesPage page = new ActiveVehiclesPage();
        page.clickExport();
    }
}
