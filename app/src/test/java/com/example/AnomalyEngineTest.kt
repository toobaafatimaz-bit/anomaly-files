package com.example

import com.example.data.cases.CaseCatalogPart2
import com.example.engine.FirstPersonEngine
import com.example.engine.Player3DState
import org.junit.Assert.*
import org.junit.Test

class AnomalyEngineTest {

    @Test
    fun allEightCasesHaveValidWalkableSpawnsObjectsAndEntities() {
        assertEquals(8, CaseCatalogPart2.allStoryCases.size)
        for (c in CaseCatalogPart2.allStoryCases) {
            val (sx, sy) = FirstPersonEngine.findSpawnPosition(c.mapGrid)
            assertTrue("Spawn not walkable in ${c.id}", FirstPersonEngine.isWalkable(sx, sy, c.mapGrid, emptySet(), margin = 0.1f))

            for (obj in c.objects) {
                val gx = obj.gridX.toInt()
                val gy = obj.gridY.toInt()
                assertTrue("Object ${obj.id} out of bounds in ${c.id}", gy in c.mapGrid.indices && gx in c.mapGrid[gy].indices)
                val tile = c.mapGrid[gy][gx]
                if (obj.kind == com.example.model.ObjectKind.MIRROR) {
                    assertTrue("Mirror ${obj.id} inside solid wall # in ${c.id} (tile=$tile)", tile != '#')
                } else {
                    assertTrue("Object ${obj.id} inside wall in ${c.id} (tile=$tile)", tile != '#' && tile != 'M')
                }
            }

            for (ent in c.entities) {
                assertTrue(
                    "Entity ${ent.id} start not walkable in ${c.id}",
                    FirstPersonEngine.isWalkable(ent.startX, ent.startY, c.mapGrid, emptySet(), margin = 0.1f)
                )
            }
            assertTrue("Case ${c.id} missing valid theories", c.validTheories.any { it.isValidDeduction })
        }
    }

    @Test
    fun playerLookTargetDetectsNearbyObjectInFront() {
        val case1 = CaseCatalogPart2.getCaseById("case_001")!!
        val player = Player3DState(x = 2.0f, y = 4.5f, yaw = 0f) // looking +X toward photo at (2.5, 4.5)
        val target = FirstPersonEngine.findLookTarget(player, case1.objects, emptySet())
        assertNotNull(target)
        assertEquals("c1_family_photo", target?.id)
    }

    @Test
    fun endlessCaseGeneratorProducesPlayableCase() {
        val endless = CaseCatalogPart2.generateEndlessCase(4242)
        assertTrue(endless.caseNumber.contains("4242"))
        assertFalse(endless.isStoryCase)
        assertTrue(endless.objects.isNotEmpty())
    }
}
