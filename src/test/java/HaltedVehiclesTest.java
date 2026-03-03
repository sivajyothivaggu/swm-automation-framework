package com.swm.tests.ui.transport.VehicleManagement.HaltedVehicles;

import org.testng.annotations.Test;
import com.swm.core.base.BaseTest;
import com.swm.ui.pages.transport.VehicleManagement.HaltedVehiclesPage;

public class HaltedVehiclesTest extends BaseTest {
    
    @Test
    public void testViewHaltedVehicles() {
        HaltedVehiclesPage page = new HaltedVehiclesPage();
        page.clickFilter();
    }
}
