package es.pedrazamiguez.splittrip.core.designsystem.presentation.extensions

import es.pedrazamiguez.splittrip.core.common.R
import es.pedrazamiguez.splittrip.domain.enums.ExpenseSubcategory

@Suppress("CyclomaticComplexMethod")
fun ExpenseSubcategory.toStringRes(): Int = when (this) {
    ExpenseSubcategory.INTERNATIONAL_FLIGHT -> R.string.expense_subcategory_international_flight
    ExpenseSubcategory.DOMESTIC_FLIGHT -> R.string.expense_subcategory_domestic_flight
    ExpenseSubcategory.TRAIN -> R.string.expense_subcategory_train
    ExpenseSubcategory.BUS -> R.string.expense_subcategory_bus
    ExpenseSubcategory.TAXI_RIDESHARE -> R.string.expense_subcategory_taxi_rideshare
    ExpenseSubcategory.CAR_RENTAL -> R.string.expense_subcategory_car_rental
    ExpenseSubcategory.MOTORBIKE_RENTAL -> R.string.expense_subcategory_motorbike_rental
    ExpenseSubcategory.FUEL -> R.string.expense_subcategory_fuel
    ExpenseSubcategory.TOLLS_PARKING -> R.string.expense_subcategory_tolls_parking
    ExpenseSubcategory.FERRY_BOAT -> R.string.expense_subcategory_ferry_boat
    ExpenseSubcategory.PUBLIC_TRANSIT -> R.string.expense_subcategory_public_transit
    ExpenseSubcategory.RESTAURANT -> R.string.expense_subcategory_restaurant
    ExpenseSubcategory.STREET_FOOD -> R.string.expense_subcategory_street_food
    ExpenseSubcategory.GROCERIES_SUPERMARKET -> R.string.expense_subcategory_groceries_supermarket
    ExpenseSubcategory.CAFE_BREAKFAST -> R.string.expense_subcategory_cafe_breakfast
    ExpenseSubcategory.BAR_DRINKS -> R.string.expense_subcategory_bar_drinks
    ExpenseSubcategory.DELIVERY -> R.string.expense_subcategory_delivery
    ExpenseSubcategory.HOTEL -> R.string.expense_subcategory_hotel
    ExpenseSubcategory.HOSTEL -> R.string.expense_subcategory_hostel
    ExpenseSubcategory.VACATION_RENTAL -> R.string.expense_subcategory_vacation_rental
    ExpenseSubcategory.RESORT -> R.string.expense_subcategory_resort
    ExpenseSubcategory.CAMPING -> R.string.expense_subcategory_camping
    ExpenseSubcategory.HOMESTAY -> R.string.expense_subcategory_homestay
    ExpenseSubcategory.MUSEUM_CULTURE -> R.string.expense_subcategory_museum_culture
    ExpenseSubcategory.TOUR_EXCURSION -> R.string.expense_subcategory_tour_excursion
    ExpenseSubcategory.NATURE_PARK -> R.string.expense_subcategory_nature_park
    ExpenseSubcategory.WATER_SPORTS -> R.string.expense_subcategory_water_sports
    ExpenseSubcategory.ADVENTURE_SPORTS -> R.string.expense_subcategory_adventure_sports
    ExpenseSubcategory.SPA_WELLNESS -> R.string.expense_subcategory_spa_wellness
    ExpenseSubcategory.TICKETS_ATTRACTIONS -> R.string.expense_subcategory_tickets_attractions
    ExpenseSubcategory.CONCERT_FESTIVAL -> R.string.expense_subcategory_concert_festival
    ExpenseSubcategory.NIGHTLIFE_CLUB -> R.string.expense_subcategory_nightlife_club
    ExpenseSubcategory.CINEMA_THEATER -> R.string.expense_subcategory_cinema_theater
    ExpenseSubcategory.GAMES_ARCADE -> R.string.expense_subcategory_games_arcade
    ExpenseSubcategory.SPORTS_EVENT -> R.string.expense_subcategory_sports_event
    ExpenseSubcategory.CLOTHING -> R.string.expense_subcategory_clothing
    ExpenseSubcategory.SOUVENIRS_GIFTS -> R.string.expense_subcategory_souvenirs_gifts
    ExpenseSubcategory.ELECTRONICS -> R.string.expense_subcategory_electronics
    ExpenseSubcategory.PHARMACY_HEALTH -> R.string.expense_subcategory_pharmacy_health
    ExpenseSubcategory.CONVENIENCE_STORE -> R.string.expense_subcategory_convenience_store
    ExpenseSubcategory.TRAVEL_INSURANCE -> R.string.expense_subcategory_travel_insurance
    ExpenseSubcategory.MEDICAL_HEALTH -> R.string.expense_subcategory_medical_health
    ExpenseSubcategory.VEHICLE_INSURANCE -> R.string.expense_subcategory_vehicle_insurance
    ExpenseSubcategory.EQUIPMENT_INSURANCE -> R.string.expense_subcategory_equipment_insurance
    ExpenseSubcategory.TIPS_GRATUITIES -> R.string.expense_subcategory_tips_gratuities
    ExpenseSubcategory.SIM_COMMUNICATIONS -> R.string.expense_subcategory_sim_communications
    ExpenseSubcategory.LAUNDRY -> R.string.expense_subcategory_laundry
    ExpenseSubcategory.FEES_SURCHARGES -> R.string.expense_subcategory_fees_surcharges
    ExpenseSubcategory.UNSPECIFIED -> R.string.expense_subcategory_unspecified
}
