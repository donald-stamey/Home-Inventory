package com.example.homeinventory

import androidx.room.*

class RoomDB {
    interface InvObject{
        val id: Int
        val name: String
    }
    @Entity(tableName = "floors")
    data class Floor(
        @PrimaryKey(autoGenerate = true) override val id: Int,
        @ColumnInfo(name = "name") override val name: String,
    ): InvObject { override fun toString(): String = name}
    @Entity(tableName = "rooms")
    data class Room(
        @PrimaryKey override val id: Int,
        @ColumnInfo(name = "name") override val name: String,
        @ColumnInfo(name = "floor_id") val floor_id: Int
    ): InvObject{override fun toString(): String = name}
    @Entity(tableName = "surfaces")
    data class Surface(
        @PrimaryKey override val id: Int,
        @ColumnInfo(name = "name") override val name: String,
        @ColumnInfo(name = "floor_id") val floor_id: Int,
        @ColumnInfo(name = "room_id") val room_id: Int
    ): InvObject{override fun toString(): String = name}
    @Entity(tableName = "containers")
    data class Container(
        @PrimaryKey override val id: Int,
        @ColumnInfo(name = "name") override val name: String,
        @ColumnInfo(name = "floor_id") val floor_id: Int,
        @ColumnInfo(name = "room_id") val room_id: Int,
        @ColumnInfo(name = "surface_id") val surface_id: Int
    ): InvObject{override fun toString(): String = name}
    @Entity(tableName = "items")
    data class Item(
        @PrimaryKey override val id: Int,
        @ColumnInfo(name = "name") override val name: String,
        @ColumnInfo(name = "floor_id") val floor_id: Int,
        @ColumnInfo(name = "room_id") val room_id: Int,
        @ColumnInfo(name = "surface_id") val surface_id: Int,
        @ColumnInfo(name = "container_id") val container_id: Int,
        @ColumnInfo val image: String?,
        @ColumnInfo val category: String?
    ): InvObject{override fun toString(): String = name}

    interface InvDao{
        fun getAll(): List<InvObject>
        fun up(id: Int): InvObject
        fun downList(id: Int): List<InvObject>
    }

    @Dao
    interface FloorDao: InvDao{
        @Insert
        fun insertFloor(floor: Floor)

        @Query("SELECT * FROM floors")
        override fun getAll(): List<Floor>

        @Query("SELECT * FROM rooms WHERE floor_id=:id")
        override fun downList(id: Int): List<InvObject>
    }

    @Dao
    interface RoomDao: InvDao{
        @Insert
        fun insertRoom(room: Room)

        @Query("SELECT * FROM rooms")
        override fun getAll(): List<Room>

        @Query("SELECT * FROM rooms WHERE floor_id=:floor_id")
        fun getRoomsOnFloor(floor_id: Int): List<Room>

        @Query("SELECT * FROM floors WHERE id=:id")
        override fun up(id: Int): Floor

        @Query("SELECT * FROM surfaces WHERE room_id=:id")
        override fun downList(id: Int): List<InvObject>
    }

    @Dao
    interface SurfaceDao: InvDao{
        @Insert
        fun insertSurface(surface: Surface)

        @Query("SELECT * FROM surfaces")
        override fun getAll(): List<Surface>

        @Query("SELECT * FROM surfaces WHERE floor_id=:floor_id")
        fun getSurfacesOnFloor(floor_id: Int): List<Surface>

        @Query("SELECT * FROM surfaces WHERE room_id=:room_id")
        fun getSurfacesInRoom(room_id: Int): List<Surface>

        @Query("SELECT * FROM rooms WHERE id=:id")
        override fun up(id: Int): Room

        @Query("SELECT * FROM containers WHERE surface_id=:id")
        override fun downList(id: Int): List<InvObject>
    }

    @Dao
    interface ContainerDao: InvDao{
        @Insert
        fun insertContainer(container: Container)

        @Query("SELECT * FROM containers")
        override fun getAll(): List<Container>

        @Query("SELECT * FROM containers WHERE floor_id=:floor_id")
        fun getContainersOnFloor(floor_id: Int): List<Container>

        @Query("SELECT * FROM containers WHERE room_id=:room_id")
        fun getContainersInRoom(room_id: Int): List<Container>

        @Query("SELECT * FROM containers WHERE surface_id=:surface_id")
        fun getContainersOnSurface(surface_id: Int): List<Container>

        @Query("SELECT * FROM surfaces WHERE id=:id")
        override fun up(id: Int): Surface

        @Query("SELECT * FROM items WHERE container_id=:id")
        override fun downList(id: Int): List<InvObject>
    }

    @Dao
    interface ItemDao: InvDao{
        @Insert
        fun insertItem(item: Item)

        @Query("SELECT * FROM items")
        override fun getAll(): List<Item>

        @Query("SELECT * FROM items WHERE floor_id=:floor_id")
        fun getItemsOnFloor(floor_id: Int): List<Item>

        @Query("SELECT * FROM items WHERE room_id=:room_id")
        fun getItemsInRoom(room_id: Int): List<Item>

        @Query("SELECT * FROM items WHERE surface_id=:surface_id")
        fun getItemsOnSurface(surface_id: Int): List<Item>

        @Query("SELECT * FROM items WHERE container_id=:container_id AND name LIKE :search")
        fun getItemsInContainer(container_id: Int, search: String): List<Item>

        @Query("SELECT * FROM containers WHERE id=:id")
        override fun up(id: Int): Container
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