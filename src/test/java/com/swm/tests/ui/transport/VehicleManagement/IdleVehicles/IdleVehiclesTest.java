package com.swm.tests.ui.transport.VehicleManagement.IdleVehicles;

import org.testng.annotations.Test;
import com.swm.core.base.BaseTest;
import com.swm.ui.pages.transport.VehicleManagement.IdleVehiclesPage;

public class IdleVehiclesTest extends BaseTest {
    
    @Test
    public void testViewIdleVehicles() {
        IdleVehiclesPage page = new IdleVehiclesPage();
        page.searchVehicle("TEST123");
    }
}
