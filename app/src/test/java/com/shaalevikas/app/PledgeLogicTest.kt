package com.shaalevikas.app

import com.shaalevikas.app.data.NeedCategory
import com.shaalevikas.app.data.NeedStatus
import com.shaalevikas.app.data.SchoolNeed
import org.junit.Assert.assertEquals
import org.junit.Test

class PledgeLogicTest {

    @Test
    fun testPledgeProgressCalculation() {
        val need = SchoolNeed(
            id = "test-need",
            estimatedCost = 10000,
            collectedAmount = 5000,
            category = NeedCategory.Roof,
            status = NeedStatus.Open
        )
        
        // Progress should be 0.5 (50%)
        assertEquals(0.5f, need.progress, 0.01f)
    }

    @Test
    fun testZeroCostNeedProgress() {
        val need = SchoolNeed(
            id = "zero-cost",
            estimatedCost = 0,
            collectedAmount = 100,
            status = NeedStatus.Open
        )
        
        // Should not crash, should return 0f
        assertEquals(0f, need.progress)
    }

    @Test
    fun testCompletedNeedProgress() {
        val need = SchoolNeed(
            id = "completed",
            estimatedCost = 5000,
            collectedAmount = 5000,
            status = NeedStatus.Completed
        )
        
        assertEquals(1.0f, need.progress)
    }
}
