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
    ): InvObject{override fun toString(): String = name}
    @Entity(tableName = "rooms")
    data class Room(
        @PrimaryKey(autoGenerate = true) override val id: Int,
        @ColumnInfo(name = "name") override val name: String,
        @ColumnInfo(name = "floor_id") val floor_id: Int
    ): InvObject{override fun toString(): String = name}
    @Entity(tableName = "surfaces")
    data class Surface(
        @PrimaryKey(autoGenerate = true) override val id: Int,
        @ColumnInfo(name = "name") override val name: String,
        @ColumnInfo(name = "floor_id") val floor_id: Int,
        @ColumnInfo(name = "room_id") val room_id: Int
    ): InvObject{override fun toString(): String = name}
    @Entity(tableName = "containers")
    data class Container(
        @PrimaryKey(autoGenerate = true) override val id: Int,
        @ColumnInfo(name = "name") override val name: String,
        @ColumnInfo(name = "floor_id") val floor_id: Int,
        @ColumnInfo(name = "room_id") val room_id: Int,
        @ColumnInfo(name = "surface_id") val surface_id: Int
    ): InvObject{override fun toString(): String = name}
    @Entity(tableName = "items")
    data class Item(
        @PrimaryKey(autoGenerate = true) override val id: Int,
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
        fun up(invObject: InvObject): InvObject
        fun downList(id: Int): List<InvObject>
    }

    @Dao
    interface FloorDao: InvDao{
        @Insert
        fun insertFloor(floor: Floor)

        @Query("SELECT * FROM floors")
        fun getAllHelp(): List<Floor>
        override fun getAll(): List<InvObject> = getAllHelp()

        //Never should be called
        @Query("SELECT * FROM floors WHERE id=:id")
        fun upHelp(id: Int): Floor
        override fun up(invObject: InvObject) = upHelp((invObject as Floor).id)

        @Query("SELECT * FROM rooms WHERE floor_id=:id")
        fun downHelp(id: Int): List<Room>
        override fun downList(id: Int): List<InvObject> = downHelp(id)
    }

    @Dao
    interface RoomDao: InvDao{
        @Insert
        fun insertRoom(room: Room)

        @Query("SELECT * FROM rooms")
        fun getAllHelp(): List<Room>
        override fun getAll(): List<InvObject> = getAllHelp()

        @Query("SELECT * FROM rooms WHERE floor_id=:floor_id")
        fun getRoomsOnFloor(floor_id: Int): List<Room>

        @Query("SELECT * FROM floors WHERE id=:floor_id")
        fun upHelp(floor_id: Int): Floor
        override fun up(invObject: InvObject) = upHelp((invObject as Room).floor_id)

        @Query("SELECT * FROM surfaces WHERE room_id=:id")
        fun downHelp(id: Int): List<Surface>
        override fun downList(id: Int): List<InvObject> = downHelp(id)
    }

    @Dao
    interface SurfaceDao: InvDao{
        @Insert
        fun insertSurface(surface: Surface)

        @Query("SELECT * FROM surfaces")
        fun getAllHelp(): List<Surface>
        override fun getAll(): List<InvObject> = getAllHelp()

        @Query("SELECT * FROM surfaces WHERE floor_id=:floor_id")
        fun getSurfacesOnFloor(floor_id: Int): List<Surface>

        @Query("SELECT * FROM surfaces WHERE room_id=:room_id")
        fun getSurfacesInRoom(room_id: Int): List<Surface>

        @Query("SELECT * FROM rooms WHERE id=:room_id")
        fun upHelp(room_id: Int): Room
        override fun up(invObject: InvObject) = upHelp((invObject as Surface).room_id)

        @Query("SELECT * FROM containers WHERE surface_id=:id")
        fun downHelp(id: Int): List<Container>
        override fun downList(id: Int): List<InvObject> = downHelp(id)
    }

    @Dao
    interface ContainerDao: InvDao{
        @Insert
        fun insertContainer(container: Container)

        @Query("SELECT * FROM containers")
        fun getAllHelp(): List<Container>
        override fun getAll(): List<InvObject> = getAllHelp()

        @Query("SELECT * FROM containers WHERE floor_id=:floor_id")
        fun getContainersOnFloor(floor_id: Int): List<Container>

        @Query("SELECT * FROM containers WHERE room_id=:room_id")
        fun getContainersInRoom(room_id: Int): List<Container>

        @Query("SELECT * FROM containers WHERE surface_id=:surface_id")
        fun getContainersOnSurface(surface_id: Int): List<Container>

        @Query("SELECT * FROM surfaces WHERE id=:surface_id")
        fun upHelp(surface_id: Int): Surface
        override fun up(invObject: InvObject) = upHelp((invObject as Container).surface_id)

        @Query("SELECT * FROM items WHERE container_id=:id")
        fun downHelp(id: Int): List<Item>
        override fun downList(id: Int): List<InvObject> = downHelp(id)
    }

    @Dao
    interface ItemDao: InvDao{
        @Insert
        fun insertItem(item: Item)

        @Query("SELECT * FROM items")
        fun getAllHelp(): List<Item>
        override fun getAll(): List<InvObject> = getAllHelp()

        @Query("SELECT * FROM items WHERE floor_id=:floor_id")
        fun getItemsOnFloor(floor_id: Int): List<Item>

        @Query("SELECT * FROM items WHERE room_id=:room_id")
        fun getItemsInRoom(room_id: Int): List<Item>

        @Query("SELECT * FROM items WHERE surface_id=:surface_id")
        fun getItemsOnSurface(surface_id: Int): List<Item>

        @Query("SELECT * FROM items WHERE container_id=:container_id AND name LIKE :search")
        fun getItemsInContainer(container_id: Int, search: String): List<Item>

        @Query("SELECT * FROM containers WHERE id=:container_id")
        fun upHelp(container_id: Int): Container
        override fun up(invObject: InvObject) = upHelp((invObject as Item).container_id)

        //Never should be called
        @Query("SELECT * FROM items WHERE container_id=:id")
        fun downHelp(id: Int): List<Item>
        override fun downList(id: Int): List<InvObject> = downHelp(id)
    }

    @Database(entities = [Floor::class, Room::class, Surface::class, Container::class, Item::class], version = 2)
    abstract class Inventory : RoomDatabase() {
        abstract fun floorDao(): FloorDao
        abstract fun roomDao(): RoomDao
        abstract fun surfaceDao(): SurfaceDao
        abstract fun containerDao(): ContainerDao
        abstract fun itemDao(): ItemDao
    }
}