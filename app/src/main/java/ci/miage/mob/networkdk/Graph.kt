package ci.miage.mob.networkdk

import android.graphics.Color
import android.graphics.PointF
import android.util.Log
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Graph {

    private val nodes = mutableListOf<Node>()
    private val connections = mutableSetOf<Connection>()

    data class Node(
        @SerializedName("position")
        var position: PointF,
        @SerializedName("label")
        var label: String,
        @SerializedName("color")
        var color: Int = Color.BLUE,
        @SerializedName("imageRes")
        var imageRes: Int? = null
    ): Serializable

    data class Connection(
        @SerializedName("start")
        val start: Node,
        @SerializedName("end")
        val end: Node,
        @SerializedName("label")
        var label: String = "",
        @SerializedName("color")
        var color: Int = Color.BLACK,
        @SerializedName("strokeWidth")
        var strokeWidth: Float = 10f
    ): Serializable{
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Connection) return false
            return (start == other.start && end == other.end) ||
                    (start == other.end && end == other.start)
        }

        override fun hashCode(): Int {
            return start.hashCode() + end.hashCode() // Doit être symétrique
        }
    }


    fun addNode(position: PointF, label: String, color: Int = Color.BLUE, imageRes: Int? = null) {
        nodes.add(Node(position, label, color, imageRes))
    }

    fun addConnection(start: Node, end: Node, label: String = ""): Boolean {
        if (start == end) return false // Empêche les boucles (connexion à soi-même)

        val newConnection = Connection(start, end, label)
        if (!connections.contains(newConnection)) {
            connections.add(newConnection)
            return true
        }
        return false // Connexion déjà existante
    }

    fun removeNode(node: Node) {
        nodes.remove(node)
        connections.removeIf { it.start == node || it.end == node }
    }

    fun removeConnection(connection: Connection) {
        connections.remove(connection)
    }

    fun clearConnections() {
        connections.clear()
    }

    fun clearNode(){
        nodes.clear()
    }

    fun getNodes(): List<Node> {
        return nodes
    }

    fun getConnections(): Set<Connection> {
        return connections
    }

    fun toJson(): String {
        return Gson().toJson(this)
    }

    companion object {
        fun fromJson(json: String): Graph {
            return Gson().fromJson(json, Graph::class.java)
        }
    }
}