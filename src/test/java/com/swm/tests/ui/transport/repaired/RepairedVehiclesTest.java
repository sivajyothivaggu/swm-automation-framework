package com.swm.tests.ui.transport.repaired;

import org.testng.annotations.Test;
import com.swm.core.base.BaseTest;
import com.swm.ui.pages.transport.repaired.RepairedVehiclesPage;
import static org.testng.Assert.*;

public class RepairedVehiclesTest extends BaseTest {
    
    @Test
    public void testViewRepairedVehicles() {
        RepairedVehiclesPage page = new RepairedVehiclesPage();
        assertTrue(page.isTableDisplayed());
    }
}
