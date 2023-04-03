package contacts

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File
import java.io.FileNotFoundException

class PhoneBook(private val args: Array<String>) {

    init {
        load()
    }

    companion object{
        var file: File? = null
        val phoneBook = mutableListOf<Contact>()
        val moshi = Moshi.Builder()
            .add(
                PolymorphicJsonAdapterFactory.of(Contact::class.java, "type")
                    .withSubtype(
                        Person::class.java, "BaseType.Person.name")
                    .withSubtype(
                        Organization::class.java, "BaseType.Organization.name"))
            .add(KotlinJsonAdapterFactory())
            .build()
        private val type = Types.newParameterizedType(MutableList::class.java, Contact::class.java )
        private val contactAdapter = moshi.adapter<MutableList<Contact>>(type)
    }

    private fun search() {
        val searchItem = queryAndSave("Enter search query:")
        val searchResults = mutableListOf<String>()
        phoneBook.forEach {contact -> contact.listOfProperties()
            .forEach{ property -> if(property.lowercase().contains(searchItem.lowercase()))
                searchResults.add(contact.list())} }
        if(searchResults.isEmpty())
            println("Nothing found!")
        else{
            println("Found ${searchResults.size} results")
            searchResults.forEachIndexed { index, property -> println("${index + 1}. $property")  }.also { println() }
        }
        val input = queryAndSave("[search] Enter action ([number], back, again):")
        when{
            input == "again" -> search()
            input.isDigit() ->
                info(input.toInt() - 1).also { recordLevel(input.toInt() - 1) }
            input == "back" -> return
        }
    }

    private fun load(){
        val input = args
        if(input.isEmpty())
            return
        try{
            file = File(input[1])
            val jsonPhoneBook = file?.readText()
            contactAdapter.fromJson(jsonPhoneBook)?.let { phoneBook.addAll(it) }
        } catch (e: FileNotFoundException){
            File(input[1]).createNewFile()
        }
    }

    private fun save(){
        if(file == null)
            return
        if(file!!.exists())
            file?.writeText(
                contactAdapter.toJson(phoneBook))
    }

    fun displayMenu(){
        while(true){
            when(queryAndSave("[menu] Enter action (add, list, search, count, exit):")){
                "exit" -> {save(); return}
                "count" -> getCount()
                "add" -> createRecord()
                "list" -> list().also { listLevel() }
                "search" -> search()
            }
        }
    }

    private fun getCount() = println("The Phone Book has ${phoneBook.size} records.\n")

    private fun createRecord(){
        when(queryAndSave("Enter the type (person, organization):")){
            "organization" -> phoneBook.add( Organization().also { it.init() } )
            "person" -> phoneBook.add( Person().also { it.init() } )
        }
    }

    private fun list() = phoneBook.forEachIndexed { index, contact -> println("${index + 1}. ${contact.list()}")  }

    private fun info(index: Int){
        println(phoneBook[index]).also { println() }
    }

    private fun listLevel(){
        val input = queryAndSave("[list] Enter action ([number], back):")
        when {
            input.isDigit() -> info(input.toInt() - 1).also { recordLevel(input.toInt() - 1) }
            input == "back" -> return
        }
    }
    private fun recordLevel(index: Int){
        while(true){
            when(queryAndSave("[record] Enter action (edit, delete, menu):")){
                "edit" -> edit(index)
                "delete" -> remove(index)
                "menu" -> println().also { return }
            }
        }
    }

    private fun remove(index: Int) {
        if(phoneBook.isEmpty())
            println("No records to remove!").also { return }

        list().run { phoneBook.removeAt(index)  }
    }

    private fun edit(index:Int){
        phoneBook[index].edit()
        println("Saved\n")
    }
}
