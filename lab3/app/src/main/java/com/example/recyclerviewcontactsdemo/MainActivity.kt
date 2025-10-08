package com.example.recyclerviewcontactsdemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.app.Activity
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : AppCompatActivity() {

    private lateinit var contactsRv: RecyclerView
    private lateinit var adapter: ContactAdapter
    private val contactList = mutableListOf<Contact>()

    private val addContactLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val name = data?.getStringExtra("name") ?: ""
            val address = data?.getStringExtra("address") ?: ""
            val phone = data?.getStringExtra("phone") ?: ""
            val email = data?.getStringExtra("email") ?: ""

            // Create a new contact and add it to the list
            val newId = (contactList.maxOfOrNull { it.id } ?: 0) + 1
            val newContact = Contact(newId, name, address, phone, email)
            contactList.add(newContact)

            // Notify the adapter that a new item has been inserted
            adapter.notifyItemInserted(contactList.size - 1)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        contactsRv = findViewById(R.id.contacts_rv)
        val addContactFab: FloatingActionButton = findViewById(R.id.add_contact_fab)

        // Generate initial dummy data
        generateDummyContacts()

        // Setup RecyclerView
        adapter = ContactAdapter(contactList)
        contactsRv.adapter = adapter
        contactsRv.layoutManager = LinearLayoutManager(this) // Displays items in a vertical list
        addContactFab.setOnClickListener {
            val intent = Intent(this, AddContactActivity::class.java)
            addContactLauncher.launch(intent)
        }
    }

    private fun generateDummyContacts() {
        contactList.add(Contact(1, "Lucas Durand", "60 avenue de la République, Marseille", "0564390626", "l.durand@hotmail.com"))
        contactList.add(Contact(2, "Jean Richard", "97 rue des Ecoles, Marseille", "0543028644", "j.richard@unistra.fr"))
        contactList.add(Contact(3, "Lucas Martin", "75 rue des Ecoles, Nice", "0589700101", "l.martin@unistra.fr"))
        contactList.add(Contact(4, "Jean Martin", "94 boulevard Saint-Germain, Nice", "0516821262", "j.martin@outlook.fr"))
        contactList.add(Contact(5, "Antoine Richard", "29 place de la Concorde, Montpellier", "0516663676", "a.richard@yahoo.fr"))
        contactList.add(Contact(6, "Jean Robert", "93 rue Pasteur, Lyon", "0519457508", "j.robert@laposte.net"))
        contactList.add(Contact(7, "Benoit Durand", "12 rue de la Paix, Paris", "0512345678", "b.durand@gmail.com"))
        contactList.add(Contact(8, "Marie Curie", "5 rue Pierre et Marie Curie, Paris", "0587654321", "m.curie@science.org"))
        contactList.add(Contact(9, "Victor Hugo", "Place des Vosges, Paris", "0555555555", "v.hugo@litterature.fr"))
        contactList.add(Contact(10, "Albert Camus", "1 rue de l'Université, Alger", "0544444444", "a.camus@philosophy.com"))
    }
}