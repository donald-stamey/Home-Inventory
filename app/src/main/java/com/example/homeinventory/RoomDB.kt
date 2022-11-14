package com.example.homeinventory

import androidx.room.*

class RoomDB {
    @Entity(tableName = "floors")
    data class Floor(
        @PrimaryKey(autoGenerate = true) val id: Int,
        @ColumnInfo(name = "name") val name: String,
    ){override fun toString(): String = name}
    @Entity(tableName = "rooms")
    data class Room(
        @PrimaryKey val id: Int,
        @ColumnInfo(name = "name") val name: String,
        @ColumnInfo(name = "floor_id") val floor_id: Int
    ){override fun toString(): String = name}
    @Entity(tableName = "surfaces")
    data class Surface(
        @PrimaryKey val id: Int,
        @ColumnInfo(name = "name") val name: String,
        @ColumnInfo(name = "floor_id") val floor_id: Int,
        @ColumnInfo(name = "room_id") val room_id: Int
    ){override fun toString(): String = name}
    @Entity(tableName = "containers")
    data class Container(
        @PrimaryKey val id: Int,
        @ColumnInfo(name = "name") val name: String,
        @ColumnInfo(name = "floor_id") val floor_id: Int,
        @ColumnInfo(name = "room_id") val room_id: Int,
        @ColumnInfo(name = "surface_id") val surface_id: Int
    ){override fun toString(): String = name}
    @Entity(tableName = "items")
    data class Item(
        @PrimaryKey val id: Int,
        @ColumnInfo(name = "name") val name: String,
        @ColumnInfo(name = "floor_id") val floor_id: Int,
        @ColumnInfo(name = "room_id") val room_id: Int,
        @ColumnInfo(name = "surface_id") val surface_id: Int,
        @ColumnInfo(name = "container_id") val container_id: Int,
        @ColumnInfo val image: String?,
        @ColumnInfo val category: String?
    ){override fun toString(): String = name}

    data class FloorWithRooms(
        @Embedded val floor: Floor,
        @Relation(
            parentColumn = "id",
            entityColumn = "floor_id"
        )
        val rooms: List<Room>
    )
    data class FloorWithSurfaces(
        @Embedded val floor: Floor,
        @Relation(
            parentColumn = "id",
            entityColumn = "floor_id"
        )
        val surfaces: List<Surface>
    )
    data class FloorWithContainers(
        @Embedded val floor: Floor,
        @Relation(
            parentColumn = "id",
            entityColumn = "floor_id"
        )
        val containers: List<Container>
    )
    data class FloorWithItems(
        @Embedded val floor: Floor,
        @Relation(
            parentColumn = "id",
            entityColumn = "floor_id"
        )
        val items: List<Item>
    )
    data class RoomWithSurfaces(
        @Embedded val room: Room,
        @Relation(
            parentColumn = "id",
            entityColumn = "room_id"
        )
        val surfaces: List<Surface>
    )
    data class RoomWithContainers(
        @Embedded val room: Room,
        @Relation(
            parentColumn = "id",
            entityColumn = "room_id"
        )
        val containers: List<Container>
    )
    data class RoomWithItems(
        @Embedded val room: Room,
        @Relation(
            parentColumn = "id",
            entityColumn = "room_id"
        )
        val items: List<Item>
    )
    data class SurfaceWithContainers(
        @Embedded val surface: Surface,
        @Relation(
            parentColumn = "id",
            entityColumn = "surface_id"
        )
        val containers: List<Container>
    )
    data class SurfaceWithItems(
        @Embedded val surface: Surface,
        @Relation(
            parentColumn = "id",
            entityColumn = "surface_id"
        )
        val items: List<Item>
    )
    data class ContainerWithItems(
        @Embedded val container: Container,
        @Relation(
            parentColumn = "id",
            entityColumn = "container_id"
        )
        val items: List<Item>
    )

    @Dao
    interface FloorDao{
        @Insert
        fun insertFloor(floor: Floor)

        @Query("SELECT * FROM floors")
        fun getFloors(): List<Floor>
    }

    @Dao
    interface RoomDao{
        @Insert
        fun insertRoom(room: Room)

        @Query("SELECT * FROM rooms")
        fun getRooms(): List<Room>

        @Query("SELECT * FROM rooms WHERE floor_id=:floor_id")
        fun getRoomsOnFloor(floor_id: Int): List<Room>
    }

    @Dao
    interface SurfaceDao{
        @Insert
        fun insertSurface(surface: Surface)

        @Query("SELECT * FROM surfaces")
        fun getSurfaces(): List<Surface>

        @Query("SELECT * FROM surfaces WHERE floor_id=:floor_id")
        fun getSurfacesOnFloor(floor_id: Int): List<Surface>

        @Query("SELECT * FROM surfaces WHERE room_id=:room_id")
        fun getSurfacesInRoom(room_id: Int): List<Surface>
    }

    @Dao
    interface ContainerDao{
        @Insert
        fun insertContainer(container: Container)

        @Query("SELECT * FROM containers")
        fun getContainers(): List<Container>

        @Query("SELECT * FROM containers WHERE floor_id=:floor_id")
        fun getContainersOnFloor(floor_id: Int): List<Container>

        @Query("SELECT * FROM containers WHERE room_id=:room_id")
        fun getContainersInRoom(room_id: Int): List<Container>

        @Query("SELECT * FROM containers WHERE surface_id=:surface_id")
        fun getContainersOnSurface(surface_id: Int): List<Container>
    }

    @Dao
    interface ItemDao{
        @Insert
        fun insertItem(item: Item)

        @Query("SELECT * FROM items")
        fun getItems(): List<Item>

        @Query("SELECT * FROM items WHERE floor_id=:floor_id")
        fun getItemsOnFloor(floor_id: Int): List<Item>

        @Query("SELECT * FROM items WHERE room_id=:room_id")
        fun getItemsInRoom(room_id: Int): List<Item>

        @Query("SELECT * FROM items WHERE surface_id=:surface_id")
        fun getItemsOnSurface(surface_id: Int): List<Item>

        @Query("SELECT * FROM items WHERE container_id=:container_id LIKE :search")
        fun getItemsInContainer(container_id: Int, search: String?): List<Item>
    }

    @Database(entities = [Floor::class, Room::class, Surface::class, Container::class, Item::class], version = 1)
    abstract class Inventory : RoomDatabase() {
        abstract fun floorDao(): FloorDao
        abstract fun roomDao(): RoomDao
        abstract fun surfaceDao(): SurfaceDao
        abstract fun containerDao(): ContainerDao
        abstract fun itemDao(): ItemDao
    }
}