package contacts

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.datetime.*
import java.io.File
import java.io.FileNotFoundException
import java.lang.NumberFormatException
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.system.exitProcess

const val default = "[no data]"

fun main(args: Array<String>) = PhoneBook(args).displayMenu()

enum class BaseType { Person, Organization }

sealed class Contact(
    var number: String = default,
    var timeCreated: String = default,
    var timeEdited: String = default
) {
    abstract val type: BaseType
    init {
        this.timeCreated = Clock.System.now().toLocalDateTime(TimeZone.of("Asia/Almaty")).toString()
        this.timeEdited = timeCreated
    }

    abstract fun setName()

    abstract fun list(): String

    fun listOfProperties(): List<String> {
        val listOfPropertyValues = mutableListOf<String>()
        this::class.memberProperties
            .filterIsInstance<KMutableProperty<*>>()
            .forEach { if(it.getter.call(this) != default )
                listOfPropertyValues.add(it.getter.call(this) as String) }
        return listOfPropertyValues
    }

    abstract fun edit()

    abstract fun init()

    fun updateTimeEdited() {
        this.timeEdited = Clock.System.now().toLocalDateTime(TimeZone.of("Asia/Almaty")).toString()
    }

    fun setNumber() {
         val regex1 =
             Regex("^\\+?\\d?\\s?\\([a-zA-z0-9]+\\)([ -][a-zA-Z0-9]{2,})*$")
         val regex2 =
             Regex("^\\+?\\d?\\s?[a-zA-z0-9]+\\(([ -][a-zA-Z0-9]{2,})*\\)$")
         val regex3 =
             Regex("^\\+?\\d?\\s?[a-zA-z0-9]+([ -][a-zA-Z0-9]{2,})*$")
         val regex4 =
             Regex("^\\+?\\d?\\s?[a-zA-z0-9]+[ -]\\([a-zA-Z0-9]+\\)([ -][a-zA-Z0-9]*)*$")
         val number = queryAndSave("Enter the number:")
         this.number =  when(regex1.matches(number) ||
                 regex2.matches(number) ||
                 regex3.matches(number) ||
                 regex4.matches(number)){
                        true -> number
                        else -> {
                             println("Wrong number format!")
                             "[no number]" }
         }
     }
}

data class Person(
    var name: String = default,
    var lastName: String = default,
    var birthDate: String = default,
    var gender: String = default
): Contact()
{
    override fun init() {
        setName()
        setLastName()
        setBirthDate()
        setGender()
        setNumber()
        println("The record added.\n")
    }

    override val type: BaseType = BaseType.Person

    override fun toString(): String =
        "Name: $name\nSurname: $lastName\nBirth date: $birthDate\nGender: $gender\n" +
                "Number: $number\nTime created: $timeCreated\nTime last edit: $timeEdited"

    override fun setName() {
        queryAndSave("Enter the name:").run {
            if(this.isEmpty())
                println("Bad address!")
            else
                this@Person.name = this
        }
    }

    override fun edit() {
        val propertyToChange = queryAndSave("Select a field (name, surname, birth, gender, number):")
        this::class.memberProperties.filterIsInstance<KMutableProperty<*>>().find { it.name.contains(propertyToChange) }
            ?.setter?.call(this, queryAndSave("Enter $propertyToChange"))
        updateTimeEdited()
    }

    override fun list() = "${this.name} ${this.lastName}"

    private fun setLastName() {
        queryAndSave("Enter the surname:").run {
            if(this.isEmpty())
                println("Bad address!")
            else
                this@Person.lastName = this
        }
    }

    private fun setBirthDate() {
        try {
            this.birthDate = queryAndSave("Enter the birth date:").toLocalDate().toString()
        } catch (e: Exception) {
            println("Bad birth date!")
        }
    }

    private fun setGender() {
        queryAndSave("Enter the gender (M, F):").uppercase().run {
            if(this.isEmpty()){
                println("Bad gender!")
                return
            } else if(this in "MF"){
                this@Person.gender = this
                return
            }
            println("Bad gender!")
        }
    }
}

data class Organization(
    var name: String = default,
    var address: String = default
): Contact(){

    override fun init() {
        setName()
        setAddress()
        setNumber()
        println("The record added.\n")
    }

    override val type: BaseType = BaseType.Organization

    override fun toString(): String =
        "Organization name: $name\nAddress: $address\nNumber: $number\nTime created: $timeCreated\nTime last edit: $timeEdited "

    override fun setName() {
        queryAndSave("Enter the name of the organization:").run {
            if(this.isEmpty())
                println("Bad address!")
            else
                this@Organization.name = this
        }
    }

    override fun edit() {
        val propertyToChange = queryAndSave("Select a field (name, address, number):")
        this::class.memberProperties.filterIsInstance<KMutableProperty<*>>().find { it.name.contains(propertyToChange) }
            ?.setter?.call(this, queryAndSave("Enter $propertyToChange"))
        updateTimeEdited()
    }

    override fun list(): String = this.name

    private fun setAddress() {
        queryAndSave("Enter the address:").run {
            if(this.isEmpty())
                println("Bad address!")
            else
                this@Organization.address = this
        }
    }

}


fun queryAndSave(msg: String) = println(msg).run { readln() }

fun String.isDigit(): Boolean{
    return try{
        this.toInt()
        true
    } catch (e: NumberFormatException){
        false
    }
}