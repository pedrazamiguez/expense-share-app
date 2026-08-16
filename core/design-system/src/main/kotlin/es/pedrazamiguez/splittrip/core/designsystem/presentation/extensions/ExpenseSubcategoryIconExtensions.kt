package es.pedrazamiguez.splittrip.core.designsystem.presentation.extensions

import androidx.compose.ui.graphics.vector.ImageVector
import es.pedrazamiguez.splittrip.core.designsystem.icon.TablerIcons
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.AirBalloon
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.ArrowsTransferUp
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.Backpack
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.Beer
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.Brush
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.BuildingCarousel
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.BuildingCommunity
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.BuildingCottage
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.BuildingEstate
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.BuildingSkyscraper
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.Bus
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.Car
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.CarSuv
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.ChefHat
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.CircleDotted
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.Coffee
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.Cpu
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.DeviceGamepad2
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.DeviceSim
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.GasStation
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.Gift
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.GlassCocktail
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.Home2
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.MasksTheater
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.MedicalCross
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.Microphone2
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.Motorbike
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.Mountain
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.PaperBag
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.ParkingCircle
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.Physiotherapist
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.PicnicTable
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.Plane
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.PlaneTilt
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.Pool
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.ReceiptEuro
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.ShieldBolt
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.ShieldHeart
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.ShieldLock
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.ShieldPin
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.Shirt
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.ShoppingBag
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.SoccerField
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.Speedboat
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.Tent
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.TipJarEuro
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.Train
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.TruckDelivery
import es.pedrazamiguez.splittrip.core.designsystem.icon.outline.WashMachine
import es.pedrazamiguez.splittrip.domain.enums.ExpenseSubcategory

@Suppress("CyclomaticComplexMethod")
fun ExpenseSubcategory.toIconVector(): ImageVector = when (this) {
    ExpenseSubcategory.INTERNATIONAL_FLIGHT -> TablerIcons.Outline.PlaneTilt
    ExpenseSubcategory.DOMESTIC_FLIGHT -> TablerIcons.Outline.Plane
    ExpenseSubcategory.TRAIN -> TablerIcons.Outline.Train
    ExpenseSubcategory.BUS -> TablerIcons.Outline.Bus
    ExpenseSubcategory.TAXI_RIDESHARE -> TablerIcons.Outline.Car
    ExpenseSubcategory.CAR_RENTAL -> TablerIcons.Outline.CarSuv
    ExpenseSubcategory.MOTORBIKE_RENTAL -> TablerIcons.Outline.Motorbike
    ExpenseSubcategory.FUEL -> TablerIcons.Outline.GasStation
    ExpenseSubcategory.TOLLS_PARKING -> TablerIcons.Outline.ParkingCircle
    ExpenseSubcategory.FERRY_BOAT -> TablerIcons.Outline.Speedboat
    ExpenseSubcategory.PUBLIC_TRANSIT -> TablerIcons.Outline.ArrowsTransferUp
    ExpenseSubcategory.RESTAURANT -> TablerIcons.Outline.ChefHat
    ExpenseSubcategory.STREET_FOOD -> TablerIcons.Outline.PicnicTable
    ExpenseSubcategory.GROCERIES_SUPERMARKET -> TablerIcons.Outline.ShoppingBag
    ExpenseSubcategory.CAFE_BREAKFAST -> TablerIcons.Outline.Coffee
    ExpenseSubcategory.BAR_DRINKS -> TablerIcons.Outline.Beer
    ExpenseSubcategory.DELIVERY -> TablerIcons.Outline.TruckDelivery
    ExpenseSubcategory.HOTEL -> TablerIcons.Outline.BuildingCommunity
    ExpenseSubcategory.HOSTEL -> TablerIcons.Outline.Home2
    ExpenseSubcategory.VACATION_RENTAL -> TablerIcons.Outline.BuildingEstate
    ExpenseSubcategory.RESORT -> TablerIcons.Outline.BuildingSkyscraper
    ExpenseSubcategory.CAMPING -> TablerIcons.Outline.Tent
    ExpenseSubcategory.HOMESTAY -> TablerIcons.Outline.BuildingCottage
    ExpenseSubcategory.MUSEUM_CULTURE -> TablerIcons.Outline.Brush
    ExpenseSubcategory.TOUR_EXCURSION -> TablerIcons.Outline.Backpack
    ExpenseSubcategory.NATURE_PARK -> TablerIcons.Outline.Mountain
    ExpenseSubcategory.WATER_SPORTS -> TablerIcons.Outline.Pool
    ExpenseSubcategory.ADVENTURE_SPORTS -> TablerIcons.Outline.AirBalloon
    ExpenseSubcategory.SPA_WELLNESS -> TablerIcons.Outline.Physiotherapist
    ExpenseSubcategory.TICKETS_ATTRACTIONS -> TablerIcons.Outline.BuildingCarousel
    ExpenseSubcategory.CONCERT_FESTIVAL -> TablerIcons.Outline.Microphone2
    ExpenseSubcategory.NIGHTLIFE_CLUB -> TablerIcons.Outline.GlassCocktail
    ExpenseSubcategory.CINEMA_THEATER -> TablerIcons.Outline.MasksTheater
    ExpenseSubcategory.GAMES_ARCADE -> TablerIcons.Outline.DeviceGamepad2
    ExpenseSubcategory.SPORTS_EVENT -> TablerIcons.Outline.SoccerField
    ExpenseSubcategory.CLOTHING -> TablerIcons.Outline.Shirt
    ExpenseSubcategory.SOUVENIRS_GIFTS -> TablerIcons.Outline.Gift
    ExpenseSubcategory.ELECTRONICS -> TablerIcons.Outline.Cpu
    ExpenseSubcategory.PHARMACY_HEALTH -> TablerIcons.Outline.MedicalCross
    ExpenseSubcategory.CONVENIENCE_STORE -> TablerIcons.Outline.PaperBag
    ExpenseSubcategory.TRAVEL_INSURANCE -> TablerIcons.Outline.ShieldPin
    ExpenseSubcategory.MEDICAL_HEALTH -> TablerIcons.Outline.ShieldHeart
    ExpenseSubcategory.VEHICLE_INSURANCE -> TablerIcons.Outline.ShieldBolt
    ExpenseSubcategory.EQUIPMENT_INSURANCE -> TablerIcons.Outline.ShieldLock
    ExpenseSubcategory.TIPS_GRATUITIES -> TablerIcons.Outline.TipJarEuro
    ExpenseSubcategory.SIM_COMMUNICATIONS -> TablerIcons.Outline.DeviceSim
    ExpenseSubcategory.LAUNDRY -> TablerIcons.Outline.WashMachine
    ExpenseSubcategory.FEES_SURCHARGES -> TablerIcons.Outline.ReceiptEuro
    ExpenseSubcategory.UNSPECIFIED -> TablerIcons.Outline.CircleDotted
}
