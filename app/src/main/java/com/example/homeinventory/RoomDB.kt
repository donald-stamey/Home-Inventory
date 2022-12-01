package com.example.homeinventory

import android.net.Uri
import androidx.room.*

class RoomDB {
    interface InvObject{
        val id: Int
        val name: String
        //fun copy(i: Int? = null, n: String? = null, f: Int? = null, r: Int? = null, s: Int? = null, c: Int? = null, img: String? = null, cat: String? = null): InvObject
    }
    @Entity(tableName = "floors")
    data class Floor(
        @PrimaryKey(autoGenerate = true) override val id: Int,
        @ColumnInfo(name = "name") override val name: String,
    ): InvObject{override fun toString(): String = name}
    //fun gf(i: Int?, n: String?, f: Int?, r: Int?, s: Int?, c: Int?, img: String?, cat: String?) = Floor(i?: this.id, n?: this.name)}
    //override fun copy(i: Int?, n: String?, f: Int?, r: Int?, s: Int?, c: Int?, img: String?, cat: String?) = this.copy(id = i?: this.id, name = n?: this.name)}
    @Entity(tableName = "rooms")
    data class Room(
        @PrimaryKey(autoGenerate = true) override val id: Int,
        @ColumnInfo(name = "name") override val name: String,
        @ColumnInfo(name = "floor_id") val floor_id: Int
    ): InvObject{override fun toString(): String = name}
        //override fun copy(i: Int?, n: String?, f: Int?, r: Int?, s: Int?, c: Int?, img: String?, cat: String?) = this.copy(id = i?: this.id, name = n?: this.name, floor_id = f?: this.floor_id)}
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
        @ColumnInfo val quantity: Int = 1
    ): InvObject{override fun toString(): String = name}

    interface InvDao{
        fun insert(invObject: InvObject)
        fun getAll(): List<InvObject>
        fun getById(id: Int): InvObject
        fun delete(invObject: InvObject)
        fun up(invObject: InvObject): InvObject
        fun downList(id: Int): List<InvObject>
        fun update(invObject: InvObject)
    }

    @Dao
    interface FloorDao: InvDao{
        @Insert
        fun insertFloor(floor: Floor)
        override fun insert(invObject: InvObject) = insertFloor(invObject as Floor)

        @Query("SELECT * FROM floors")
        fun getAllHelp(): List<Floor>
        override fun getAll(): List<InvObject> = getAllHelp()

        @Query("SELECT * FROM floors WHERE id=:id")
        fun getByIdHelp(id: Int): Floor
        override fun getById(id: Int): InvObject = getByIdHelp(id)

        //Never should be called
        @Query("SELECT * FROM floors WHERE id=:id")
        fun upHelp(id: Int): Floor
        override fun up(invObject: InvObject) = upHelp((invObject as Floor).id)

        @Query("SELECT * FROM rooms WHERE floor_id=:id")
        fun downHelp(id: Int): List<Room>
        override fun downList(id: Int): List<InvObject> = downHelp(id)

        @Update
        fun updateHelp(floor: Floor)
        override fun update(invObject: InvObject) = updateHelp(invObject as Floor)

        @Delete
        fun deleteHelp(floor: Floor)
        override fun delete(invObject: InvObject) = deleteHelp(invObject as Floor)
    }

    @Dao
    interface RoomDao: InvDao{
        @Insert
        fun insertRoom(room: Room)
        override fun insert(invObject: InvObject) = insertRoom(invObject as Room)

        @Query("SELECT * FROM rooms")
        fun getAllHelp(): List<Room>
        override fun getAll(): List<InvObject> = getAllHelp()

        @Query("SELECT * FROM rooms WHERE id=:id")
        fun getByIdHelp(id: Int): Room
        override fun getById(id: Int): InvObject = getByIdHelp(id)

        @Query("SELECT * FROM floors WHERE id=:floor_id")
        fun upHelp(floor_id: Int): Floor
        override fun up(invObject: InvObject) = upHelp((invObject as Room).floor_id)

        @Query("SELECT * FROM surfaces WHERE room_id=:id")
        fun downHelp(id: Int): List<Surface>
        override fun downList(id: Int): List<InvObject> = downHelp(id)

        @Update
        fun updateHelp(room: Room)
        override fun update(invObject: InvObject) = updateHelp(invObject as Room)

        @Delete
        fun deleteHelp(room: Room)
        override fun delete(invObject: InvObject) = deleteHelp(invObject as Room)
    }

    @Dao
    interface SurfaceDao: InvDao{
        @Insert
        fun insertSurface(surface: Surface)
        override fun insert(invObject: InvObject) = insertSurface(invObject as Surface)

        @Query("SELECT * FROM surfaces")
        fun getAllHelp(): List<Surface>
        override fun getAll(): List<InvObject> = getAllHelp()

        @Query("SELECT * FROM surfaces WHERE id=:id")
        fun getByIdHelp(id: Int): Surface
        override fun getById(id: Int): InvObject = getByIdHelp(id)

        @Query("SELECT * FROM rooms WHERE id=:room_id")
        fun upHelp(room_id: Int): Room
        override fun up(invObject: InvObject) = upHelp((invObject as Surface).room_id)

        @Query("SELECT * FROM containers WHERE surface_id=:id")
        fun downHelp(id: Int): List<Container>
        override fun downList(id: Int): List<InvObject> = downHelp(id)

        @Update
        fun updateHelp(surface: Surface)
        override fun update(invObject: InvObject) = updateHelp(invObject as Surface)

        @Delete
        fun deleteHelp(surface: Surface)
        override fun delete(invObject: InvObject) = deleteHelp(invObject as Surface)
    }

    @Dao
    interface ContainerDao: InvDao{
        @Insert
        fun insertContainer(container: Container)
        override fun insert(invObject: InvObject) = insertContainer(invObject as Container)

        @Query("SELECT * FROM containers")
        fun getAllHelp(): List<Container>
        override fun getAll(): List<InvObject> = getAllHelp()

        @Query("SELECT * FROM containers WHERE id=:id")
        fun getByIdHelp(id: Int): Container
        override fun getById(id: Int): InvObject = getByIdHelp(id)

        @Query("SELECT * FROM surfaces WHERE id=:surface_id")
        fun upHelp(surface_id: Int): Surface
        override fun up(invObject: InvObject) = upHelp((invObject as Container).surface_id)

        @Query("SELECT * FROM items WHERE container_id=:id")
        fun downHelp(id: Int): List<Item>
        override fun downList(id: Int): List<InvObject> = downHelp(id)

        @Update
        fun updateHelp(container: Container)
        override fun update(invObject: InvObject) = updateHelp(invObject as Container)

        @Delete
        fun deleteHelp(container: Container)
        override fun delete(invObject: InvObject) = deleteHelp(invObject as Container)
    }

    @Dao
    interface ItemDao: InvDao{
        @Insert
        fun insertItem(item: Item)
        override fun insert(invObject: InvObject) = insertItem(invObject as Item)

        @Query("SELECT * FROM items")
        fun getAllHelp(): List<Item>
        override fun getAll(): List<InvObject> = getAllHelp()

        @Query("SELECT * FROM items WHERE id=:id")
        fun getByIdHelp(id: Int): Item
        override fun getById(id: Int): InvObject = getByIdHelp(id)

        @Query("SELECT * FROM items WHERE name LIKE :search")
        fun getSearch(search: String): List<Item>

        @Query("SELECT * FROM items WHERE floor_id=:floor_id AND name LIKE :search")
        fun getItemsOnFloor(floor_id: Int, search: String): List<Item>

        @Query("SELECT * FROM items WHERE room_id=:room_id AND name LIKE :search")
        fun getItemsInRoom(room_id: Int, search: String): List<Item>

        @Query("SELECT * FROM items WHERE surface_id=:surface_id AND name LIKE :search")
        fun getItemsOnSurface(surface_id: Int, search: String): List<Item>

        @Query("SELECT * FROM items WHERE container_id=:container_id AND name LIKE :search")
        fun getItemsInContainer(container_id: Int, search: String): List<Item>

        @Query("SELECT * FROM containers WHERE id=:container_id")
        fun upHelp(container_id: Int): Container
        override fun up(invObject: InvObject) = upHelp((invObject as Item).container_id)

        //Never should be called
        @Query("SELECT * FROM items WHERE container_id=:id")
        fun downHelp(id: Int): List<Item>
        override fun downList(id: Int): List<InvObject> = downHelp(id)

        @Update
        fun updateHelp(item: Item)
        override fun update(invObject: InvObject) = updateHelp(invObject as Item)

        @Delete
        fun deleteHelp(item: Item)
        override fun delete(invObject: InvObject) = deleteHelp(invObject as Item)
    }

    @Database(entities = [Floor::class, Room::class, Surface::class, Container::class, Item::class], version = 4)
    abstract class Inventory : RoomDatabase() {
        abstract fun floorDao(): FloorDao
        abstract fun roomDao(): RoomDao
        abstract fun surfaceDao(): SurfaceDao
        abstract fun containerDao(): ContainerDao
        abstract fun itemDao(): ItemDao
    }
}