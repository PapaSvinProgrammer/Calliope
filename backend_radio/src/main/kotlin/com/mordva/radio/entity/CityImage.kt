package com.mordva.radio.entity

import jakarta.persistence.*

@Entity
@Table(name = "city_image")
class CityImage() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null

    @Column(name = "imageurl", nullable = false, columnDefinition = "TEXT")
    var imageUrl: String = ""

    @Column(name = "downloadurl", nullable = false, columnDefinition = "TEXT")
    var downloadUrl: String = ""

    @Column(name = "source", nullable = false, columnDefinition = "TEXT")
    var source: String = ""

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CityImage) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0
}
