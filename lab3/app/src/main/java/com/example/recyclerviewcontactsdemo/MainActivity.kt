package com.example.recyclerviewcontactsdemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.app.Activity
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.ItemTouchHelper
import java.util.Collections

class MainActivity : AppCompatActivity() {

    private lateinit var contactsRv: RecyclerView
    private lateinit var adapter: ContactAdapter
    private lateinit var itemTouchHelper: ItemTouchHelper
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
        itemTouchHelper = ItemTouchHelper(simpleCallback)
        itemTouchHelper.attachToRecyclerView(contactsRv)
        adapter = ContactAdapter(contactList, itemTouchHelper)
        contactsRv.adapter = adapter
        contactsRv.layoutManager = LinearLayoutManager(this) // Displays items in a vertical list
        addContactFab.setOnClickListener {
            val intent = Intent(this, AddContactActivity::class.java)
            addContactLauncher.launch(intent)
        }
        val itemTouchHelper = ItemTouchHelper(simpleCallback)
    }

    private val simpleCallback = object : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN, // Directions for dragging
        0 // Directions for swiping (0 means disabled)
    ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val fromPosition = viewHolder.adapterPosition
            val toPosition = target.adapterPosition

            // Swap the items in the list
            Collections.swap(contactList, fromPosition, toPosition)

            adapter.notifyItemMoved(fromPosition, toPosition)

            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        }
    }
    private fun generateDummyContacts() {
        contactList.add(Contact(1, "Anar Məmmədov", "15 Nizami küçəsi, Bakı", "050 212 34 56", "anar.m@gmail.com"))
        contactList.add(Contact(2, "Leyla Əliyeva", "44 Neftçilər prospekti, Bakı", "055 789 12 34", "leyla.aliyeva@yahoo.com"))
        contactList.add(Contact(3, "Fərid Həsənov", "8 Atatürk prospekti, Gəncə", "070 555 67 89", "f.hasanov@mail.ru"))
        contactList.add(Contact(4, "Nigar Hüseynova", "102 Füzuli küçəsi, Sumqayıt", "051 321 98 76", "nigar.h@box.az"))
        contactList.add(Contact(5, "Rəşad Quliyev", "23 Azadlıq prospekti, Bakı", "077 411 22 33", "r.guliyev@hotmail.com"))
        contactList.add(Contact(6, "Aysel İsmayılova", "5 Heydər Əliyev prospekti, Lənkəran", "050 888 77 66", "aysel.ismayilova@gmail.com"))
        contactList.add(Contact(7, "Orxan Abbasov", "78 İstiqlaliyyət küçəsi, Bakı", "055 123 45 67", "orkhan.a@yahoo.com"))
        contactList.add(Contact(8, "Günay Mustafayeva", "19 Cavadxan küçəsi, Gəncə", "070 999 88 77", "gunay.m@mail.ru"))
        contactList.add(Contact(9, "Tural Süleymanov", "33 Səməd Vurğun küçəsi, Bakı", "051 456 78 90", "tural.s@hotmail.com"))
        contactList.add(Contact(10, "Sevinc Kərimova", "12 Sülh küçəsi, Sumqayıt", "077 345 67 89", "sevinc.k@gmail.com"))
        contactList.add(Contact(11, "Emin Bağırov", "41 Xətai prospekti, Bakı", "050 567 11 22", "emin.bagirov@box.az"))
        contactList.add(Contact(12, "Fidan Vəliyeva", "22 Şah İsmayıl Xətai, Gəncə", "055 234 56 78", "fidan.v@yahoo.com"))
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