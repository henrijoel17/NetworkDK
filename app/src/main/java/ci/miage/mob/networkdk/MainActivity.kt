package ci.miage.mob.networkdk

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.PointF
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(), GraphView.GraphViewListener {
    private lateinit var graphView: GraphView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.toolbar))

        graphView = findViewById(R.id.graphView)
        graphView.listener = this
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_graph, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.addNodeMenuItem -> {
                //graphView.addNode()
                showCreatNodeDialog()
                true
            }
            R.id.addConnectionMenuItem -> {
                graphView.startConnectionMode()
                true
            }
            R.id.resetMenuItem -> {
                graphView.resetConnections()
                true
            }
            R.id.action_edit -> {
                graphView.activeEdit()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNodeDoubleClick(node: Graph.Node){
        showNodeOptionsDialog(node)
    }

    override fun onNodeLongClick(node: Graph.Node) {
        showNodeOptionsDialog(node)
    }

    override fun onConnectionLongClick(connection: Graph.Connection) {
        showConnectionOptionsDialog(connection)
    }

    override fun showConnectionLabelDialog(startNode: Graph.Node, endNode: Graph.Node) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Étiquette de la connexion")

        val input = EditText(this)
        builder.setView(input)

        builder.setPositiveButton("Créer") { _, _ ->
            val label = input.text.toString()
            // Essayer d'ajouter la connexion
            if (graphView.addConnection(startNode, endNode, label)) {
                graphView.completeConnection(label)
                graphView.invalidate()
            } else {
                // Afficher un message d'erreur si la connexion existe déjà
                Toast.makeText(this, "Connexion déjà existante.", Toast.LENGTH_SHORT).show()
                graphView.cancelConnection()
            }
        }

        builder.setNegativeButton("Annuler") { _, _ ->
            graphView.cancelConnection()
        }
        builder.show()
    }

    //Modal pour les options des noeuds
    private fun showNodeOptionsDialog(node: Graph.Node) {
        val options = arrayOf("Modifier", "Supprimer")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Options du nœud")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> showEditNodeDialog(node)
                1 -> graphView.removeSelectedNode()
            }
        }
        builder.show()
    }

    //Modal pour modifier les noeuds
    private fun showEditNodeDialog(node: Graph.Node) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Modifier le nœud")

        val view = layoutInflater.inflate(R.layout.dialog_edit_node, null)
        builder.setView(view)

        val labelEdit = view.findViewById<EditText>(R.id.nodeLabelEdit)
        val colorSpinner = view.findViewById<Spinner>(R.id.nodeColorSpinner)
        val imageSpinner = view.findViewById<Spinner>(R.id.nodeImageSpinner)
        val imagePreview = view.findViewById<ImageView>(R.id.nodeImagePreview)

        labelEdit.setText(node.label)

        // Couleurs
        val colors = arrayOf(
            "Bleu" to Color.BLUE,
            "Rouge" to Color.RED,
            "Vert" to Color.GREEN,
            "Orange" to Color.parseColor("#FFA500"),
            "Cyan" to Color.CYAN,
            "Magenta" to Color.MAGENTA,
            "Noir" to Color.BLACK
        )

        val colorAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            colors.map { it.first }
        )
        colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        colorSpinner.adapter = colorAdapter

        // Images
        val images = listOf(
            "Aucune" to null,
            "Imprimante" to R.drawable.ic_printer,
            "Télévision" to R.drawable.ic_tv,
            "Ordinateur" to R.drawable.ic_computer,
            "Téléphone" to R.drawable.ic_phone
        )

        val imageAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            images.map { it.first }
        )
        imageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        imageSpinner.adapter = imageAdapter

        // Préremplir les champs avec les données actuelles du nœud
        val currentColorIndex = colors.indexOfFirst { it.second == node.color }
        if (currentColorIndex != -1) {
            colorSpinner.setSelection(currentColorIndex)
        }

        val currentImageIndex = images.indexOfFirst { it.second == node.imageRes }
        if (currentImageIndex != -1) {
            imageSpinner.setSelection(currentImageIndex)
            node.imageRes?.let { imagePreview.setImageResource(it) }
        }

        // Mettre à jour la prévisualisation lorsque l'image est sélectionnée
        imageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedImageRes = images[position].second
                if (selectedImageRes != null) {
                    imagePreview.setImageResource(selectedImageRes)
                } else {
                    imagePreview.setImageDrawable(null) // Pas d'image sélectionnée
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        builder.setPositiveButton("Enregistrer") { _, _ ->
            val newLabel = labelEdit.text.toString()
            val selectedColor = colors[colorSpinner.selectedItemPosition].second
            val selectedImageRes = images[imageSpinner.selectedItemPosition].second

            graphView.updateNode(node, newLabel, selectedColor, selectedImageRes)
        }

        builder.setNegativeButton("Annuler", null)
        builder.show()
    }

    //Les options de Connexion
    private fun showConnectionOptionsDialog(connection: Graph.Connection) {
        val options = arrayOf("Modifier", "Supprimer")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Options de la connexion")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> showEditConnectionDialog(connection)
                1 -> graphView.removeSelectedConnection()
            }
        }
        builder.show()
    }

    //Modifier la connexion
    private fun showEditConnectionDialog(connection: Graph.Connection) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Modifier la connexion")

        val view = layoutInflater.inflate(R.layout.dialog_edit_connection, null)
        builder.setView(view)

        val labelEdit = view.findViewById<EditText>(R.id.connectionLabelEdit)
        val colorSpinner = view.findViewById<Spinner>(R.id.connectionColorSpinner)
        val widthEdit = view.findViewById<EditText>(R.id.connectionWidthEdit)

        labelEdit.setText(connection.label)

        // Couleurs
        val colors = arrayOf(
            "Noir" to Color.BLACK,
            "Bleu" to Color.BLUE,
            "Rouge" to Color.RED,
            "Vert" to Color.GREEN,
            "Orange" to Color.parseColor("#FFA500"),
            "Cyan" to Color.CYAN,
            "Magenta" to Color.MAGENTA
        )

        val colorAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            colors.map { it.first }
        )
        colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        colorSpinner.adapter = colorAdapter

        // Sélectionner la couleur actuelle
        val currentColorIndex = colors.indexOfFirst { it.second == connection.color }
        if (currentColorIndex != -1) {
            colorSpinner.setSelection(currentColorIndex)
        }

        widthEdit.setText(connection.strokeWidth.toString())

        builder.setPositiveButton("Enregistrer") { _, _ ->
            val newLabel = labelEdit.text.toString()
            val selectedColor = colors[colorSpinner.selectedItemPosition].second
            val newWidth = widthEdit.text.toString().toFloatOrNull() ?: 10f

            graphView.updateConnection(connection, newLabel, selectedColor, newWidth)
        }

        builder.setNegativeButton("Annuler", null)
        builder.show()
    }

    private fun showCreatNodeDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Création du nœud")

        val view = layoutInflater.inflate(R.layout.dialog_create_node, null)
        builder.setView(view)

        val labelEdit = view.findViewById<EditText>(R.id.createNodeLabel)
        val colorSpinner = view.findViewById<Spinner>(R.id.createNodeColorSpinner)
        val imageSpinner = view.findViewById<Spinner>(R.id.createNodeImageSpinner)
        val imagePreview = view.findViewById<ImageView>(R.id.createNodeImagePreview)

        // Couleurs
        val colors = arrayOf(
            "Bleu" to Color.BLUE,
            "Rouge" to Color.RED,
            "Vert" to Color.GREEN,
            "Orange" to Color.parseColor("#FFA500"),
            "Cyan" to Color.CYAN,
            "Magenta" to Color.MAGENTA,
            "Noir" to Color.BLACK
        )

        val colorAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            colors.map { it.first }
        )
        colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        colorSpinner.adapter = colorAdapter

        // Images
        val images = listOf(
            "Aucune" to null,
            "Imprimante" to R.drawable.ic_printer,
            "Télévision" to R.drawable.ic_tv,
            "Ordinateur" to R.drawable.ic_computer,
            "Téléphone" to R.drawable.ic_phone
        )

        val imageAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            images.map { it.first }
        )
        imageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        imageSpinner.adapter = imageAdapter

        // Mettre à jour la prévisualisation lorsque l'image est sélectionnée
        imageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedImageRes = images[position].second
                if (selectedImageRes != null) {
                    imagePreview.setImageResource(selectedImageRes)
                } else {
                    imagePreview.setImageDrawable(null) // Pas d'image sélectionnée
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        builder.setPositiveButton("Créer") { _, _ ->
            val newLabel = labelEdit.text.toString()
            val selectedColor = colors[colorSpinner.selectedItemPosition].second
            val selectedImageRes = images[imageSpinner.selectedItemPosition].second

            graphView.addNodeWithLabel(PointF(0f,0f), newLabel, selectedColor, selectedImageRes)

        }

        builder.setNegativeButton("Annuler", null)
        builder.show()
    }

    override fun CreateNodeLongPress(position: PointF) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Création du nœud")

        val view = layoutInflater.inflate(R.layout.dialog_create_node, null)
        builder.setView(view)

        val labelEdit = view.findViewById<EditText>(R.id.createNodeLabel)
        val colorSpinner = view.findViewById<Spinner>(R.id.createNodeColorSpinner)
        val imageSpinner = view.findViewById<Spinner>(R.id.createNodeImageSpinner)
        val imagePreview = view.findViewById<ImageView>(R.id.createNodeImagePreview)

        // Couleurs
        val colors = arrayOf(
            "Bleu" to Color.BLUE,
            "Rouge" to Color.RED,
            "Vert" to Color.GREEN,
            "Orange" to Color.parseColor("#FFA500"),
            "Cyan" to Color.CYAN,
            "Magenta" to Color.MAGENTA,
            "Noir" to Color.BLACK
        )

        val colorAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            colors.map { it.first }
        )
        colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        colorSpinner.adapter = colorAdapter

        // Images
        val images = listOf(
            "Aucune" to null,
            "Imprimante" to R.drawable.ic_printer,
            "Télévision" to R.drawable.ic_tv,
            "Ordinateur" to R.drawable.ic_computer,
            "Téléphone" to R.drawable.ic_phone
        )

        val imageAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            images.map { it.first }
        )
        imageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        imageSpinner.adapter = imageAdapter

        // Mettre à jour la prévisualisation lorsque l'image est sélectionnée
        imageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedImageRes = images[position].second
                if (selectedImageRes != null) {
                    imagePreview.setImageResource(selectedImageRes)
                } else {
                    imagePreview.setImageDrawable(null)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        builder.setPositiveButton("Créer") { _, _ ->
            val newLabel = labelEdit.text.toString()
            val selectedColor = colors[colorSpinner.selectedItemPosition].second
            val selectedImageRes = images[imageSpinner.selectedItemPosition].second
            graphView.addNodeAtPosition(position, newLabel, selectedColor, selectedImageRes)

        }

        builder.setNegativeButton("Annuler", null)
        builder.show()
    }


}