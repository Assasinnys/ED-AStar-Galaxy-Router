package elite.algorithm

import elite.pojo.Coordinates
import elite.utils.calcDistance

@Suppress("MemberVisibilityCanBePrivate", "unused", "RedundantVisibilityModifier")
class StarPoint(
    var previousStarPoint: StarPoint?,
    val systemId64: Long,
    val coords: Coordinates,
    val isNeutronStar: Boolean = false,
    val distance: Double = 0.0,
    val systemName: String?,
    val jumpCounter: Int = 0,
    finishCoords: Coordinates
) {

    val costG: Double = calculateCostG(distance)
    val costF: Double = calculateCostF(finishCoords)

    private fun calculateCostG(distance: Double): Double {
        if (distance == 0.0) return 0.0

        val previousCostG = previousStarPoint?.costG ?: 0.0
        val costGForStarPoint = if (isNeutronStar)
            DISTANCE_MODIFIER.minus(distance).minus(NEUTRON_COF)
//            DISTANCE_MODIFIER.div(distance).minus(NEUTRON_COF)
        else
            DISTANCE_MODIFIER.minus(distance)
//            DISTANCE_MODIFIER.div(distance)

        return previousCostG.plus(costGForStarPoint)
    }

    private fun calculateCostF(finishCoords: Coordinates): Double {
        return if (previousStarPoint != null) {
            costG.plus(calculateCostH(finishCoords))
        } else {
            0.0
        }
    }

    override fun equals(other: Any?) =
        if (other != null && other is StarPoint) this.systemId64 == other.systemId64 else false

    private fun calculateCostH(finishCoords: Coordinates) =
        calcDistance(coords.x, finishCoords.x, coords.y, finishCoords.y, coords.z, finishCoords.z).toInt()

    override fun hashCode(): Int {
        var result = previousStarPoint?.hashCode() ?: 0
        result = 31 * result + systemId64.hashCode()
        result = 31 * result + coords.hashCode()
        result = 31 * result + isNeutronStar.hashCode()
        result = 31 * result + costG.toInt()
        result = 31 * result + costF.toInt()
        return result
    }

    companion object {
        const val DISTANCE_MODIFIER = 100
        const val NEUTRON_COF = 340       // Default
    }

    // division
    // DM = 10, COF = 340 ---> JC = 55, distance = 3311.4 ly, replaces = 55, cof = 340, time: 81.847
    // DM = 30, COF = 340 ---> JC = 55, distance = 3311.4 ly, replaces = 55, cof = 340, time: 80.506
    // DM = 50, COF = 340 ---> JC = 55, distance = 3311.4 ly, replaces = 55, cof = 340, time: 78.294
    // DM = 60, COF = 340 ---> JC = 55, distance = 3311.4 ly, replaces = 55, cof = 340, time: 78.388
    // DM = 70, COF = 340 ---> JC = 55, distance = 3311.4 ly, replaces = 55, cof = 340, time: 79.375
    // DM = 90, COF = 340 ---> JC = 55, distance = 3311.4 ly, replaces = 55, cof = 340, time: 81.595
    // DM = 100, COF = 340 ---> JC = 55, distance = 3311.4 ly, replaces = 55, cof = 340, time: 80.53
    // DM = 110, COF = 340 ---> JC = 55, distance = 3311.4 ly, replaces = 55, cof = 340, time: 78.799
    // DM = 120, COF = 340 ---> JC = 55, distance = 3311.4 ly, replaces = 55, cof = 340, time: 79.495
    // DM = 200, COF = 340 ---> JC = 56, distance = 3420.7 ly, replaces = 55, cof = 340, time: 79.303

    // DM = 60, COF = 400 ---> JC = 55, distance = 3311.4145200710614 ly, replaces = 55, cof = 400, time: 79.947
    // DM = 60, COF = 450 ---> JC = --//--
    // DM = 60, COF = 480 ---> JC =
    // DM = 60, COF = 500 ---> JC =
    // DM = 60, COF = 200 ---> JC =
    // DM = 60, COF = 250 ---> JC =
    // DM = 60, COF = 300 ---> JC =

}