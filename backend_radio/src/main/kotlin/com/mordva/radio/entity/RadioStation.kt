package com.mordva.radio.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.hibernate.annotations.UpdateTimestamp
import java.time.OffsetDateTime

@Entity
@Table(name = "radio_station")
class RadioStation() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null

    @Column(nullable = false, length = 500)
    var name: String = ""

    @Column(nullable = false, columnDefinition = "TEXT")
    var description: String = ""

    @Column(name = "imageurl", nullable = false, columnDefinition = "TEXT")
    var imageUrl: String = ""

    @Column(name = "streamurl", nullable = false, columnDefinition = "TEXT")
    var streamUrl: String = ""

    @Column(name = "createdat", nullable = false, updatable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now()

    @Column(name = "updatedat", nullable = false)
    @UpdateTimestamp
    var updatedAt: OffsetDateTime = OffsetDateTime.now()

    @ManyToMany
    @JoinTable(
        name = "radio_station_city",
        joinColumns = [JoinColumn(name = "radio_station_id")],
        inverseJoinColumns = [JoinColumn(name = "city_id")]
    )
    @JsonIgnore
    var cities: MutableList<City> = mutableListOf()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RadioStation) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0
}
