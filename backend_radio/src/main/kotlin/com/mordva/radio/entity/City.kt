package com.mordva.radio.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*

@Entity
@Table(name = "city")
class City() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null

    @Column(nullable = false, length = 500)
    var name: String = ""

    @Column(length = 500)
    var region: String? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_image_id")
    var cityImage: CityImage? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_image_id")
    var regionImage: RegionImage? = null

    @ManyToMany(mappedBy = "cities")
    @JsonIgnore
    var radioStations: MutableList<RadioStation> = mutableListOf()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is City) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0
}
